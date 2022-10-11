<ui:modal id="modalEditNote" text="${message(code:'template.editNote')}" isEditModal="true">

    <g:form id="edit_note" class="ui form"  url="[controller:'doc', action:'editNote', id:noteInstance?.id]" method="post">
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
                <label for="content">${message(code:'default.note.label')}:</label>
                <div id="content">${raw(noteInstance?.content)}</div>

                <laser:script file="${this.getGroovyPageFileName()}">
                    $('#content').trumbowyg({
                        btns: [
                            ['viewHTML'],
                            ['formatting'],
                            ['strong', 'em', 'del'],
                            ['superscript', 'subscript'],
                            // ['link'],
                            // ['insertImage'],
                            ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'],
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
                    });
                </laser:script>
            </div>
        </div>
    </g:form>
</ui:modal>
