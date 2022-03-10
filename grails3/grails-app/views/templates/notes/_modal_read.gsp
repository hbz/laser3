<semui:modal id="modalReadNote" text="${message(code:'template.readNote')}" hideSubmitButton="true">

    <g:form id="read_note" class="ui form"  url="[controller:'doc', action:'show', id:noteInstance.id]" method="post">
        <div class="field">
            <label for="title">${message(code:'template.addNote.title')}:</label>
            <input type="text" id="title" name="title" value="${noteInstance.title}"/>
        </div>
        <div class="field">
            <label for="content">${message(code:'template.addNote.note')}:</label>
            <textarea id="content" name="content">${noteInstance.content}</textarea>
        </div>
    </g:form>
</semui:modal>
