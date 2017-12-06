<semui:modal id="modalCreateNote" text="${message(code:'template.addNote', default: 'Create New Note')}">

    <g:form id="create_note" class="ui form" url="[controller:'docWidget', action:'createNote']" method="post">
        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <dl>
            <dt>
                <label>${message(code:'template.addNote.note', default: 'Note')}:</label>
            </dt>
            <dd>
                <textarea name="licenseNote"></textarea>
            </dd>
        </dl>
        <input type="hidden" name="licenseNoteShared" value="0"/>
    </g:form>
</semui:modal>
