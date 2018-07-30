<semui:modal id="modalCreateNote" text="${message(code:'template.addNote', default: 'Create New Note')}">

    <g:form id="create_note" class="ui form" url="[controller:'docWidget', action:'createNote']" method="post">
        <input type="hidden" name="ownerid" value="${ownobj?.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj?.class?.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.title', default: 'Titel')}:</label>

            <input type="text" name="licenseNoteTitle" />
        </div>
        <div class="field fieldcontain">
            <label>${message(code:'template.addNote.note', default: 'Note')}:</label>

            <textarea name="licenseNote"></textarea>
        </div>

        <input type="hidden" name="licenseNoteShared" value="0"/>
    </g:form>
</semui:modal>
