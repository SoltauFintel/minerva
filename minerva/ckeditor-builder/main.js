import {
	DecoupledEditor,
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

window.minerva = window.minerva || {}; // namespace

window.minerva.createEditor = function(lang, id, heading, extraPlugins, setter) {
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
        fontSize: {
            options: [10, 12, 14, 'default', 18, 20, 22],
            supportAllValues: true
        },
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
        link: {
            addTargetToExternalLinks: true,
            defaultProtocol: 'https://',
        },
        style: {
            definitions:
             [
                {
                    name: 'Info box',
                    element: 'p',
                    classes: ['info-box']
                },
                {
                    name: 'Marker',
                    element: 'span',
                    classes: ['marker']
                },
                {
                    name: 'Spoiler',
                    element: 'span',
                    classes: ['spoiler']
                },
            ]
        },
        table: {
            contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
        },
        translations: [ lang == 'de' ? translationsDE : translationsEN ],
        extraPlugins: extraPlugins
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
