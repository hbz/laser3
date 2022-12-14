// module: assets/javascripts/modules/wysiwyg.js

wysiwyg = {

    initEditor: function (editorSelector) {
        console.log('wysiwyg.initEditor( ' + editorSelector + ' )')

        let $wrapper = $(editorSelector);

        let btns = [
            // ['viewHTML'],
            ['formatting'],
            ['strong', 'em', 'underline', 'del'],
            ['superscript', 'subscript'],
            // ['link'], // -- TODO: unsecure
            // ['insertImage'],
            // ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'], // -- TODO: markup
            ['unorderedList', 'orderedList'],
            // ['horizontalRule'],
            ['removeformat'], // -- TODO
            ['historyUndo', 'historyRedo'],
            ['fullscreen']
        ];

        $wrapper.trumbowyg ({
            btns: btns,
            lang: JSPC.vars.locale,
            svgPath: '/assets/trumbowyg/ui/icons.svg',
            resetCss: true,
            removeformatPasted: false,  // if false -> fallback
            tagsToKeep: [],
            tagsToRemove: ['embed', 'img', 'link', 'object', 'script'] // fallback
        })
        .on ('tbwinit',     function () { wysiwyg.sanitize ($wrapper); })
        .on ('tbwblur',     function () { wysiwyg.sanitize ($wrapper); })
        .on ('tbwpaste',    function () { wysiwyg.sanitize ($wrapper); });
        //.on ('tbwfocus',    function () { console.log('focus'); });
        //.on('tbwchange',    function(){ console.log('change'); });
    },

    resetContent: function (editorSelector) {
        console.log('wysiwyg.resetContent( ' + editorSelector + ' )')

        let $wrapper = $(editorSelector);
        $wrapper.trumbowyg('html', '');
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
                'em', 'del', 'strong', 'u', 'sub', 'sub'
            ],
            ALLOWED_ATTR: [ /* 'style', 'href', 'target', 'title' */ ], // -- TODO
            ALLOW_ARIA_ATTR: false,
            ALLOW_DATA_ATTR: false
        });

        $elem.trumbowyg('html', clean);
    },

    analyzeNote_TMP: function ($elem, $wrapper, showWarnings) {
        let result = {
            script: false, style: false
        };

        result.script = $elem.find ('script').length > 0;
        result.style  = $elem.find ('style').length > 0 || $elem.find ('*[style]').length > 0;

        if (! result.script) {
            $elem.find ('*').each ( function (i, e) {
                if (! result.script) {
                    Array.from(e.attributes).forEach(function (a) {
                        if (a.nodeName.startsWith('on') && a.nodeValue != null) { result.script = true; }
                    })
                }
            });
        }
        if (showWarnings) {
            if (result.style)  { $wrapper.prepend('<div class="ui label orange" style="margin-bottom:1em;">Style-Warnung</div>') }
            if (result.script) { $wrapper.prepend('<div class="ui label red" style="margin-bottom:1em;">Script-Warnung</div>') }
        }
        return result;
    }
}

JSPC.modules.add( 'wysiwyg', wysiwyg );

