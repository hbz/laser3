<semui:modal id="modalEditNote" text="${message(code:'template.readNote')}" hideSubmitButton="true">

    <g:form id="edit_note" class="ui form"  url="[controller:'doc', action:'show', id:noteInstance?.id]" method="post">

        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.title')}:</label>

            <input type="text" name="title" value="${noteInstance?.title}"/>
        </div>
        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.note')}:</label>

            <textarea name="content">${noteInstance?.content}</textarea>
        </div>

    </g:form>
</semui:modal>
