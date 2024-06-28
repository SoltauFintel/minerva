export default {
    build: {
        minify: false,
        rollupOptions: {
            output: {
                entryFileNames: 'ckeditor-vite.js',
                assetFileNames: 'ckeditor-vite.css',
            }
        }
    }
}
