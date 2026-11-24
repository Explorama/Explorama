# data-mappings/traffic

Mapping descriptors for freely available road, air and bikeshare incident
datasets, written for the CLI data transformer in `tools/cli-data-transformer`.

Each file is a self-contained mapping: it evaluates to a description map that
`gen` uses to turn a source CSV into Explorama's import EDN.

## Running

```bash
cd tools/cli-data-transformer
clojure -M:run check ../../data-mappings/traffic/<mapping>.clj
clojure -M:run gen ../../data-mappings/traffic/<mapping>.clj <source>.csv <target>.edn
```

Or against a packaged build from `tools/release/build-release.sh cli`:

```bash
./dist/release/cli/builder.sh gen data-mappings/traffic/<mapping>.clj <source>.csv <target>.edn
```

`check` validates the descriptor against `schema.cljc`; `gen` additionally
resolves every row and reports how many features had to be dropped.
`:date-schema` patterns use java.time (`DateTimeFormatter`) syntax when run
through the CLI — in particular `y`, not `Y`, for years.

## Datasets

### `unfallatlas.clj` — Verkehrsunfälle mit Personenschaden (Destatis)

```bash
curl -O https://www.opengeodata.nrw.de/produkte/transport_verkehr/unfallatlas/Unfallorte2024_EPSG25832_CSV.zip
unzip -j Unfallorte2024_EPSG25832_CSV.zip 'csv/Unfallorte2024_LinRef.csv'
```

268,519 rows, `;`-separated, dl-de/by-2-0. Code columns are resolved to English
labels translated from `DSB_Unfallatlas.pdf`; the column names themselves stay
as the source has them. Coordinates are `XGCSWGS84`/`YGCSWGS84`
(WGS84 with German decimal commas) — the `LINREF*` pair is UTM zone 32N and is
not used.

The dataset deliberately carries no day of month, only `UJAHR` and `UMONAT`, so
every accident is dated to the first of its month.

### `uk-stats19.clj` — GB road safety collisions (DfT)

```bash
curl -O https://data.dft.gov.uk/road-accidents-safety-data/dft-road-casualty-statistics-collision-2023.csv
```

104,258 rows, OGL v3.0. Code labels follow the DfT data guide; the DfT's `-1`
"data missing or out of range" sentinel is preserved rather than blanked, both
as a context label and as a fact value.

12 of the 104,258 rows have no coordinates and lose their feature (see
*Rows without coordinates* below).

### `ntsb-aviation.clj` — NTSB aviation accidents

The bulk distribution is a Microsoft Access database, so the `events` table has
to be exported first:

```bash
curl -o avall.zip 'https://data.ntsb.gov/avdata/FileDirectory/DownloadFile?fileID=C%3A%5Cavdata%5Cavall.zip'
unzip avall.zip
mdb-export -D '%Y-%m-%d' avall.mdb events > ntsb-events.csv
```

The `-D '%Y-%m-%d'` matters: the mapping's date schema expects `ev_date` in that
form, and `mdb-export` defaults to `%m/%d/%y %H:%M:%S`.

US public domain. Contexts keep the raw NTSB codes (`ACC`, `FATL`, `VMC`, …)
rather than expanded labels — the code meanings live in the
`eADMSPUB_DataDictionary` table of the same database, which is worth turning
into lookup maps here if you want readable names.

Coordinates come from `dec_latitude`/`dec_longitude`; events without them lose
their feature, which affects a much larger share of rows than for the other
sources. Drop the `:locations` entry if you would rather keep every event and
give up the map.

### `divvy.clj` — Divvy bikeshare trips (Chicago)

```bash
curl -O https://divvy-tripdata.s3.amazonaws.com/202406-divvy-tripdata.zip
unzip 202406-divvy-tripdata.zip
```

710,721 rows for a single month, released under the Divvy Data License
Agreement. Trips, not accidents — included because it exercises `start-at`/
`end-at` date pairs, which none of the accident sources do.

Trip duration is computed from the two timestamps as a decimal fact in minutes.
About 20% of rows have no station name; those become `unknown station` rather
than an empty context. Only the start coordinates are mapped.

Mapping files running through the CLI use the sandbox-exposed
`de.explorama.shared.common.unification.time` namespace for timestamp
arithmetic; clj-time is no longer available in the sandbox.

## Shared caveats

**Row limits.** Every mapping sets `:csv {:limit 1000}`, matching the samples in
`data/`. Raise or remove it for a full import — the browser bundle keeps
everything in IndexedDB, so a full Divvy month is not a realistic target.

**Rows without coordinates.** A feature's location list is fixed by the
descriptor, so there is no way to emit "no location" for a single row. A blank
coordinate therefore fails to convert and the whole feature is dropped; the item
survives with `:features []`. The mappings deliberately do not substitute `0.0`,
which would place the record off the coast of Ghana. `gen` reports the dropped
count.

**Converter edge cases.** `integer-conversion` and `decimal-conversion` in
`mapping.cljc` both throw on an empty string; `integer-conversion` additionally
throws on zero-padded values such as `"07"`, and `decimal-conversion` throws on
values with no decimal separator such as `"0"`. Every mapping therefore
normalises numbers through small `int-str`/`dec-str` helpers before handing them
over. Keep that in mind when adding fields.
