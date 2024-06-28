cls
call npm run build
copy /Y dist\ckeditor-vite.css ..\src\main\resources\web\css
copy /Y dist\ckeditor-vite.js ..\src\main\resources\web\js
