<ui:modal id="modalEditNote" text="${message(code:'template.editNote')}" isEditModal="true">

    <g:form id="edit_note" class="ui form"  url="[controller:'doc', action:'editNote', id:noteInstance?.id]" method="post">
        <div class="ui label red" style="float:right">Feature in Entwicklung</div><br />

        <div class="field">
            <label for="title">${message(code:'default.title.label')}:</label>
            <input type="text" id="title" name="title" value="${noteInstance?.title}"/>
        </div>
%{--        <div class="field">--}%
%{--            <label for="content">${message(code:'default.note.label')}:</label>--}%
%{--            <textarea id="content" class="la-textarea-resize-vertical" name="content">${noteInstance?.content}</textarea>--}%
%{--        </div>--}%
        <div class="field">
            <div class="field">
                <label for="content">${message(code:'default.content.label')}:</label>
                <div id="content">${raw(noteInstance?.content)}</div>

                <laser:script file="${this.getGroovyPageFileName()}">
                    let sanitize = function ($elem) {
                        let raw = $elem.trumbowyg('html');
                        let clean = DOMPurify.sanitize( raw, {
                                ALLOWED_TAGS: [
                                        'h1', 'h2', 'h3', 'h4',
                                        'p', 'blockquote',
                                        'li', 'ol', 'ul',
                                        'em', 'del', 'strong', 'sub', 'sub'
                                    ],
                                ALLOWED_ATTR: [ /* 'style' */ ], // -- TODO
                                ALLOW_ARIA_ATTR: false,
                                ALLOW_DATA_ATTR: false
                            });

                        $elem.trumbowyg('html', clean);
                    };

                    $('#content').trumbowyg ({
                        btns: [
                            // ['viewHTML'],
                            ['formatting'],
                            ['strong', 'em', 'del'],
                            ['superscript', 'subscript'],
                            // ['link'], -- TODO
                            // ['insertImage'],
                            // ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'], -- TODO
                            ['unorderedList', 'orderedList'],
                            // ['horizontalRule'],
                            ['removeformat'],
                            ['fullscreen']
                        ],
                        lang: 'de',
                        svgPath: '/assets/trumbowyg/ui/icons.svg',
                        resetCss: true,
                        removeformatPasted: true,
                        tagsToKeep: [],
                    })
                    .on('tbwpaste',  function(){ console.log('paste'); sanitize($('#content')); })
                    .on('tbwblur',   function(){ console.log('blur'); sanitize($('#content')); });
%{--                    .on('tbwinit',   function(){ console.log('init'); })--}%
%{--                    .on('tbwfocus',  function(){ console.log('focus'); })--}%
%{--                    .on('tbwchange', function(){ console.log('change'); })--}%
                </laser:script>
            </div>
        </div>
    </g:form>
</ui:modal>
