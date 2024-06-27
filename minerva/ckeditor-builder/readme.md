# Build our own CKEditor 5

## Woher kommen die Dateien?

Von der [CKEditor Builder Homepage](https://ckeditor.com/ckeditor-5/builder/) bekomme ich: index.html, main.js und style.css.

Wenn ich mittels npm ein 'ckeditor5-vite-example' Musterprojekt anlege, bekomme ich auch die package.json.

In der main.js wird der CKEditor zusammengestellt.

## install node_modules

In this folder execute `npm install`. I'm using npm version 10.2.4, node version 20.9.0.

## Build for production

Vite is used as bundler.
In this folder execute `npm run build`. The result is in dist folder.

Copy dist/assets/index-...css to /src/main/resources/web/css/ckeditor-vite.css

Copy dist/assets/index-...js to /src/main/resources/web/js/ckeditor-vite.js

Commit those ckeditor-vite... files.
