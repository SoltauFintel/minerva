import {
	DecoupledEditor,
	Autoformat,
	BlockQuote,
	Bold,
	Code,
	CodeBlock,
	Essentials,
	FontBackgroundColor,
	FontColor,
	FontSize,
	GeneralHtmlSupport,
	Heading,
	ImageBlock,
	ImageInline,
	ImageUpload,
	ImageToolbar, ImageStyle, ImageCaption,
	Italic,
	Link,
	List,
	Paragraph,
	PasteFromOffice,
	Strikethrough,
	Style,
	Table,
	TableCaption,
	TableCellProperties,
	TableColumnResize,
	TableProperties,
	TableToolbar,
	Underline,
	Undo
} from 'ckeditor5';

import translationsDE from 'ckeditor5/translations/de.js';
import translationsEN from 'ckeditor5/translations/en.js';

import 'ckeditor5/ckeditor5.css';

import './style.css';

window.moco = window.moco || {}; // namespace

window.moco.createEditor = function(lang, id, heading, extraPlugins, setter) {
    const editorConfig = {
        toolbar: {
            items: [
                'undo',
                'redo',
                '|',
                'heading',
                'style',
                '|',
                'fontSize',
                'fontColor',
                'fontBackgroundColor',
                '|',
                'bold',
                'italic',
                'underline',
                'strikethrough',
                'code',
                '|',
                'link',
                'uploadImage',
                'insertTable',
                'blockQuote',
                '|',
                'bulletedList',
                'numberedList',
            ],
            shouldNotGroupWhenFull: false
        },
        plugins: [
            Autoformat,
            BlockQuote,
            Bold,
            Code,
            CodeBlock,
            Essentials,
            FontBackgroundColor,
            FontColor,
            FontSize,
            GeneralHtmlSupport,
            Heading,
            ImageBlock,
            ImageInline,
            ImageUpload,
            ImageToolbar, ImageStyle, ImageCaption,
            Italic,
            Link,
            List,
            Paragraph,
            PasteFromOffice,
            Strikethrough,
            Style,
            Table,
            TableCaption,
            TableCellProperties,
            TableColumnResize,
            TableProperties,
            TableToolbar,
            Underline,
            Undo
        ],
        fontSize: { options: ['tiny', 'small', 'default', 'big', 'huge'] },
        heading: heading,
        htmlSupport: {
            allow: [
                {
                    name: /^.*$/,
                    styles: true,
                    attributes: true,
                    classes: true
                }
            ]
        },
        language: lang,
        style: {
            definitions:
             [
                {
                    name: 'Button',
                    element: 'span',
                    classes: ['xmapbutton']
                },
                {
                    name: 'Register',
                    element: 'span',
                    classes: ['xmaptab']
                },
                {
                    name: 'Abschnitt',
                    element: 'span',
                    classes: ['xmapcollapsible']
                },
                {
                    name: 'Frame',
                    element: 'span',
                    classes: ['xmapframe']
                },
            ]
        },
        table: {
            contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
        },
        translations: [ lang == 'de' ? translationsDE : translationsEN ],
        extraPlugins: extraPlugins,
        image: {
            toolbar: [
                'imageStyle:inline',
                'imageStyle:wrapText',
                'imageStyle:breakText',
                '|',
                'toggleImageCaption',
                'imageTextAlternative',
            ],
            insert: { type: 'inline' }
        }
    };
    DecoupledEditor
        .create(document.querySelector('#' + id), editorConfig)
        .then( it => {
            const toolbarContainer = setter(it);
            toolbarContainer.appendChild(it.ui.view.toolbar.element);
            return it;
        } )
        .catch( error => {
            console.error(error);
        } );
}
