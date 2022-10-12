<ui:modal id="modalCreateNote" text="${message(code:'template.addNote')}">

    <g:form id="create_note" class="ui form" url="[controller:'doc', action:'createNote']" method="post">
        <div class="ui label red" style="float:right">Feature in Entwicklung</div><br />

        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="field">
            <label for="licenseNoteTitle">${message(code:'default.title.label')}:</label>
            <input type="text" id="licenseNoteTitle" name="licenseNoteTitle" />
        </div>
%{--        <div class="field">--}%
%{--            <label for="licenseNote">${message(code:'default.note.label')}:</label>--}%
%{--            <textarea class="la-textarea-resize-vertical" id="licenseNote" name="licenseNote"></textarea>--}%
%{--        </div>--}%
        <div class="field">
            <label for="licenseNote">${message(code:'default.content.label')}:</label>
            <div id="licenseNote"></div>

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

                $('#licenseNote').trumbowyg ({
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
                .on('tbwpaste',  function(){ console.log('paste'); sanitize($('#licenseNote')); })
                .on('tbwblur',   function(){ console.log('blur'); sanitize($('#licenseNote')); });
            %{--                    .on('tbwinit',   function(){ console.log('init'); })--}%
            %{--                    .on('tbwfocus',  function(){ console.log('focus'); })--}%
            %{--                    .on('tbwchange', function(){ console.log('change'); })--}%

            </laser:script>
        </div>
    </g:form>
</ui:modal>
