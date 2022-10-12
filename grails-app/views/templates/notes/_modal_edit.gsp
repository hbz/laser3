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
                    wysiwyg.initEditor('#content', '#modalEditNote');
                </laser:script>
            </div>
        </div>
    </g:form>
</ui:modal>
