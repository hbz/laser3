<semui:modal id="modalEditNote" text="${message(code:'template.editNote', default: 'Edit Note')}" editmodal="true">

    <g:form id="edit_note" class="ui form"  url="[controller:'doc',action:'edit',id:noteInstance?.id]" method="post">

        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.title', default: 'Titel')}:</label>

            <input type="text" name="title" value="${noteInstance?.title}"/>
        </div>
        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.note', default: 'Note')}:</label>

            <textarea name="content">${noteInstance?.content}</textarea>
        </div>

    </g:form>
</semui:modal>
