# Build our own CKEditor 5

## install node_modules

In this folder execute `npm install`. I'm using npm version 10.2.4, node version 20.9.0.

## Build for production

Vite is used as bundler.
In this folder execute `npm run build`. The result is in dist folder.

Copy dist/assets/index-...css to /src/main/resources/web/css/ckeditor-vite.css

Copy dist/assets/index-...js to /src/main/resources/web/js/ckeditor-vite.js

Commit those ckeditor-vite... files.
