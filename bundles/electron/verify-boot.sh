#!/bin/bash
# Boot-verifies the packaged electron app: launches it under xvfb-run with a
# scratch $HOME, polls until the main process and the worker window are
# provably up (sqlite files created, no error markers in the log), then
# tears the process tree down. Exits 0 only on a clean boot.
#
# Usage:
#   bash verify-boot.sh                                  # tests dist/electron/prepared/
#   APP=../../dist/electron/Explorama-linux.AppImage \
#     bash verify-boot.sh                                # tests the AppImage
#                                                        # (--appimage-extract-and-run, no FUSE needed)
# Runnable from anywhere; paths resolve relative to this script.
set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PREPARED="$REPO_ROOT/dist/electron/prepared"
SCRATCH_ROOT="$REPO_ROOT/.superpowers"

APP="${APP:-}"

if [ -n "$APP" ]; then
  if [ ! -f "$APP" ]; then
    echo "verify-boot: APP=$APP does not exist" >&2
    exit 1
  fi
  APP="$(cd "$(dirname "$APP")" && pwd)/$(basename "$APP")"
  chmod +x "$APP"
  echo "verify-boot: APP mode -- testing the packaged AppImage at $APP"
else
  if [ ! -d "$PREPARED" ]; then
    echo "verify-boot: $PREPARED does not exist -- run 'make prepare-prod' first" >&2
    exit 1
  fi

  if [ ! -x "$PREPARED/node_modules/.bin/electron" ]; then
    echo "verify-boot: $PREPARED/node_modules/.bin/electron missing -- prepared/'s npm install didn't even provision the electron package; run 'make prepare-prod' first" >&2
    exit 1
  fi

  # prepared/'s npm install runs --ignore-scripts, which skips electron's
  # postinstall binary download. Provision THIS copy (not backend/'s dev
  # install): electron-builder packages prepared/node_modules/electron, so
  # that's the copy that must be runnable. Idempotent.
  ELECTRON_PKG_DIR="$PREPARED/node_modules/electron"
  ELECTRON_BIN="$ELECTRON_PKG_DIR/dist/electron"
  if [ ! -x "$ELECTRON_BIN" ]; then
    echo "verify-boot: electron binary missing at $ELECTRON_BIN -- running node install.js"
    (cd "$ELECTRON_PKG_DIR" && node install.js)
  fi
  if [ ! -x "$ELECTRON_BIN" ]; then
    echo "verify-boot: node install.js ran but $ELECTRON_BIN is still missing" >&2
    exit 1
  fi
fi

# Scratch HOME: the app derives its data root from $HOME ($HOME/.config/
# Explorama), so this keeps real profiles untouched and gives a
# deterministic path to poll for the sqlite files.
mkdir -p "$SCRATCH_ROOT"
SCRATCH_HOME="$(mktemp -d -p "$SCRATCH_ROOT" verify-boot-home.XXXXXX)"
export HOME="$SCRATCH_HOME"
export XDG_CONFIG_HOME="$SCRATCH_HOME/.config"
mkdir -p "$XDG_CONFIG_HOME"

APP_NAME="Explorama" # prod RUNTIME_MODE -> non-dev folder name
APP_DATA_DIR="$HOME/.config/$APP_NAME/app-data"
LOG="$SCRATCH_HOME/verify-boot.log"
: > "$LOG"

echo "verify-boot: scratch HOME=$SCRATCH_HOME"
echo "verify-boot: log=$LOG"
if [ -n "$APP" ]; then
  echo "verify-boot: launching AppImage (xvfb-run, --appimage-extract-and-run) from $APP"
else
  echo "verify-boot: launching electron (xvfb-run) from $PREPARED"
fi

XVFB_PID=""

cleanup() {
  # Kill everything in the process group we launched (setsid makes the
  # xvfb-run process its own group leader, so its pid IS the pgid; Xvfb and
  # electron -- and electron's own child processes -- inherit it unless they
  # setsid themselves, which none of them do here).
  if [ -n "$XVFB_PID" ]; then
    kill -TERM "-$XVFB_PID" 2>/dev/null || true
    sleep 2
    kill -KILL "-$XVFB_PID" 2>/dev/null || true
    sleep 1
    # Safety net: report (and reap) anything that escaped the group instead
    # of silently leaving it running.
    strays="$(pgrep -g "$XVFB_PID" 2>/dev/null || true)"
    if [ -n "$strays" ]; then
      echo "verify-boot: WARNING stray pids remained in group $XVFB_PID: $strays" | tee -a "$LOG"
      # shellcheck disable=SC2086
      kill -KILL $strays 2>/dev/null || true
    fi
  fi
}
trap cleanup EXIT

if [ -n "$APP" ]; then
  # --appimage-extract-and-run avoids the FUSE requirement; TMPDIR points
  # into the scratch tree so the extraction doesn't leak into /tmp.
  APPIMAGE_TMPDIR="$SCRATCH_HOME/appimage-tmp"
  mkdir -p "$APPIMAGE_TMPDIR"
  (
    cd "$(dirname "$APP")"
    export TMPDIR="$APPIMAGE_TMPDIR"
    exec setsid xvfb-run -a -f "$SCRATCH_HOME/.Xauthority" "$APP" --appimage-extract-and-run --no-sandbox --disable-gpu
  ) >> "$LOG" 2>&1 &
else
  (
    cd "$PREPARED"
    exec setsid xvfb-run -a -f "$SCRATCH_HOME/.Xauthority" "$ELECTRON_BIN" --no-sandbox --disable-gpu .
  ) >> "$LOG" 2>&1 &
fi
XVFB_PID=$!

ERROR_PATTERN='uncaught exception|javascript error|cannot find module|err_file_not_found|renderer process crashed'

DEADLINE=$((SECONDS + 90))
RESULT=""
while [ "$SECONDS" -lt "$DEADLINE" ]; do
  if [ -s "$LOG" ] && grep -qiE "$ERROR_PATTERN" "$LOG"; then
    RESULT="fail-error-marker"
    break
  fi

  SQLITE_FOUND=0
  if [ -d "$APP_DATA_DIR" ]; then
    if ls "$APP_DATA_DIR"/de.explorama.backend.expdb.*.sqlite3 >/dev/null 2>&1; then
      SQLITE_FOUND=1
    fi
  fi

  if [ "$SQLITE_FOUND" -eq 1 ] && [ -s "$LOG" ]; then
    if kill -0 "$XVFB_PID" 2>/dev/null; then
      RESULT="pass"
      break
    else
      # Process already gone -- only a pass if it exited cleanly.
      if wait "$XVFB_PID"; then
        RESULT="pass"
      else
        RESULT="fail-nonzero-exit"
      fi
      break
    fi
  fi

  sleep 2
done

if [ -z "$RESULT" ]; then
  RESULT="fail-timeout"
fi

echo "verify-boot: result=$RESULT"
echo "--- log tail ---"
tail -n 40 "$LOG" || true
echo "--- app-data dir ---"
ls -la "$APP_DATA_DIR" 2>&1 || echo "(app-data dir not found: $APP_DATA_DIR)"

if [ "$RESULT" = "pass" ]; then
  echo "verify-boot: PASS -- main+worker booted, sqlite present under $APP_DATA_DIR, no error markers in $LOG"
  exit 0
else
  echo "verify-boot: FAIL ($RESULT) -- see $LOG" >&2
  exit 1
fi
