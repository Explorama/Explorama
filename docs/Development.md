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
The browser version is the easiest to develop for. Just run `make dev` and open the `localhost:4000` file in your browser.

> [!IMPORTANT]
>
> Currently you have to use:
```
npx shadow-cljs build
vite build --mode development
npx shadow-cljs watch app
```

### Electron
The electron version is a bit more complicated. You need to run `make dev` and `make dev-app` in two different terminals. Then you can open the electron app.

### Server
The server version simply does not work at the moment.

### Folder structure
There are currently three places where you can find code:

#### Plugins
The plugins are shared code between all versions and are separated into backend and frontend code. There are also some tests 🥸.

#### Bundles

Bundles are containing the specific code for each version of Explorama.

#### Libs

Libs are also shared code like plugins but are more libary like. We will see how to deal with them later.

#### Tools

Additional tooling for development.

#### Styles

Repoistory for the stylesheets and images.

## Contributing

We want to hear your feedback! Please create an issue if you have any problems or suggestions. If you want to contribute to the project you can also create a pull request. We are happy about any help we can get.
Since the project is rather new we might need some time to review your pull requests. We will also update this section with more information in the future.