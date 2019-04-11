<semui:modal id="modalCreateNote" text="${message(code:'template.addNote', default: 'Create New Note')}">

    <g:form id="create_note" class="ui form" url="[controller:'docWidget', action:'createNote']" method="post">
        <input type="hidden" name="ownerid" value="${ownobj?.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj?.class?.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="field fieldcontain">
            <label for="licenseNoteTitle">${message(code:'template.addNote.title', default: 'Titel')}:</label>

            <input type="text" id="licenseNoteTitle" name="licenseNoteTitle" />
        </div>
        <div class="field fieldcontain">
            <label for="licenseNote">${message(code:'template.addNote.note', default: 'Note')}:</label>

            <textarea id="licenseNote" name="licenseNote"></textarea>
        </div>
    </g:form>
</semui:modal>
