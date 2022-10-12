// module: assets/javascripts/modules/wysiwyg.js

wysiwyg = {

    go: function () {
        console.log('wysiwyg -\> loaded')
    },

    initEditor: function (cssSel, modalSel) {
        console.log('wysiwyg.initEditor( ' + cssSel + ', ' + modalSel + ')')

        let $wrapper = $(cssSel);
        // let $form = $(modalSel + ' form');
        // let $submit = $(modalSel + ' .actions input[type=submit]');

        let btns = [
            // ['viewHTML'],
            ['formatting'],
            ['strong', 'em', 'del'],
            ['superscript', 'subscript'],
            // ['link'], // -- TODO: unsecure
            // ['insertImage'],
            // ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'], // -- TODO: markup
            ['unorderedList', 'orderedList'],
            // ['horizontalRule'],
            ['removeformat'],
            ['fullscreen']
        ];

        // if (! $submit[0]) {
        //     btns = btns.filter(i => i[0] !== 'link')
        // }
        // else {
        //     $submit.off ('click').on ('click', function (event) {
        //         event.preventDefault();
        //         wysiwyg.sanitize($wrapper);
        //         $form.submit();
        //     })
        // }

        $wrapper.trumbowyg ({
            btns: btns,
            lang: 'de',
            svgPath: '/assets/trumbowyg/ui/icons.svg',
            resetCss: true,
            removeformatPasted: false,  // if false -> fallback
            tagsToKeep: [],
            tagsToRemove: ['embed', 'img', 'link', 'object', 'script'] // fallback
        })
        .on ('tbwpaste', function () {
            wysiwyg.sanitize($wrapper);
        })
        .on ('tbwblur', function () {
            // if (! $submit[0]) {
                wysiwyg.sanitize($wrapper);
            // }
        });
        //.on('tbwinit',   function(){ console.log('init'); })
        //.on('tbwfocus',  function(){ console.log('focus'); })
        //.on('tbwchange', function(){ console.log('change'); })
    },

    sanitize: function ($elem) {
        // blocklist always win over allowlist
        // elements content is kept when element is removed
        // https://github.com/cure53/DOMPurify/blob/1.0.8/src/tags.js
        // https://github.com/cure53/DOMPurify/blob/1.0.8/src/attrs.js

        let raw = $elem.trumbowyg('html');
        let clean = DOMPurify.sanitize (raw, {
            ALLOWED_TAGS: [
                /*'a',  'br', */ // -- TODO
                'h1', 'h2', 'h3', 'h4',
                'p', 'blockquote',
                'li', 'ol', 'ul',
                'em', 'del', 'strong', 'sub', 'sub'
            ],
            ALLOWED_ATTR: [ /* 'style', 'href', 'target', 'title' */ ], // -- TODO
            ALLOW_ARIA_ATTR: false,
            ALLOW_DATA_ATTR: false
        });

        $elem.trumbowyg('html', clean);
    }
}

