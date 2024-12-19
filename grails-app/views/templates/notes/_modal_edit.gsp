<ui:modal id="modalEditNote" text="${message(code:'template.editNote')}" isEditModal="true">

    <g:form id="edit_note" class="ui form" url="[controller: 'note', action: 'editNote', params:[dctx: docContext.id]]" method="post">
        <div class="field">
            <label for="title">${message(code:'default.title.label')}:</label>
            <input type="text" id="title" name="title" value="${noteInstance.title}"/>
        </div>
        <div class="field">
            <div class="field">
                <label for="content">${message(code:'default.content.label')}:</label>
                <div id="content">${raw(noteInstance.content)}</div>

                <laser:script file="${this.getGroovyPageFileName()}">
                    wysiwyg.initEditor('#modalEditNote #content');

                    $('#edit_note').form({
                        on: 'blur',
                        inline: true,
                        fields: {
                            title: {
                                identifier: 'title',
                                rules: [
                                    {
                                        type: 'maxLength[255]',
                                        prompt: '<g:message code="validation.maxLength" args="[255]" />'
                                    }
                                ]
                            }
                        }
                    });
                </laser:script>
            </div>
        </div>
    </g:form>
</ui:modal>
