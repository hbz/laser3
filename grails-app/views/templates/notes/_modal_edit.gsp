<ui:modal id="modalEditNote" text="${message(code:'template.editNote')}" isEditModal="true">

    <g:form id="edit_note" class="ui form"  url="[controller:'doc', action:'editNote', id:noteInstance?.id]" method="post">
        <div class="field">
            <label for="title">${message(code:'default.title.label')}:</label>
            <input type="text" id="title" name="title" value="${noteInstance?.title}"/>
        </div>
        <div class="field">
            <label for="content">${message(code:'default.note.label')}:</label>
            <textarea id="content" name="content">${noteInstance?.content}</textarea>
        </div>
    </g:form>
</ui:modal>
