<ui:modal id="modalCreateNote" text="${message(code:'template.addNote')}">

    <g:form id="create_note" class="ui form" url="[controller:'doc', action:'createNote']" method="post">
        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="field">
            <label for="licenseNoteTitle">${message(code:'default.title.label')}:</label>

            <input type="text" id="licenseNoteTitle" name="licenseNoteTitle" />
        </div>
        <div class="field">
            <label for="licenseNote">${message(code:'default.note.label')}:</label>

            <textarea class="la-textarea-resize-vertical" id="licenseNote" name="licenseNote"></textarea>
        </div>
    </g:form>
</ui:modal>
