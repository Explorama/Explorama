## Developing

> [!IMPORTANT]
>
> This section is currently very incomplete. We will add more information in the future.

There are currently three versions of Explorama
- bundles/browser
- bundles/electron
- bundles/server

### General Prequisites
- [Leiningen](https://leiningen.org/)
- [Java](https://adoptium.net/)
- [Node.js](https://nodejs.org/en/)
- Make
- [babashka](https://github.com/babashka/babashka) - just for some tools

### Browser
The browser version is the easiest to develop for:
```bash
cd bundles/browser
npm install
bb gather-assets.bb.clj dev
clj -M:dev   # Figwheel on port 8020
```

### Electron
`bundles/electron` is split into `backend/` (Clojure backend + Electron
main/worker process) and `frontend/` (ClojureScript UI), each with its own
`deps.edn`/`package.json`. Run `make assets build_mode=dev` once, then start
each half's Figwheel REPL in its own terminal (`cd backend && clj -M:dev`,
`cd frontend && clj -M:dev` - both default to port 8020, so only one half can
bind it at a time). App packaging: `make build-linux`
runs the full prepare/verify/bundle chain and produces a boot-verified
`dist/electron/Explorama-linux.AppImage`; `make build-win` mirrors it but is
untested (no Windows/wine toolchain on hand). `make dev-app` compiles and
launches just the Electron main process for main-process dev - its UI/worker
windows won't load, since the per-half Figwheel dev builds don't emit the
`js/frontend.js`/`js/backend.js` files those windows expect (see the
Makefile's `dev-app` target for details).

### Server
See [bundles/server/README.md](../bundles/server/README.md): local dev
(`clj.deps.edn`/`cljs.deps.edn` + Figwheel) and a Docker Compose harness for
production-like auth/routing are both supported now.

### Folder structure
There are currently four places where you can find code:

#### Plugins
The plugins are shared code between all versions and are separated into backend and frontend code. There are also some tests 🥸.

#### Bundles

Bundles are containing the specific code for each version of Explorama.

#### Tools

Additional tooling for development.

#### Styles

Repoistory for the stylesheets and images.

## Contributing

We want to hear your feedback! Please create an issue if you have any problems or suggestions. If you want to contribute to the project you can also create a pull request. We are happy about any help we can get.
Since the project is rather new we might need some time to review your pull requests. We will also update this section with more information in the future.