<ui:modal id="modalCreateNote" text="${message(code:'template.addNote')}">

    <g:form id="create_note" class="ui form" url="[controller:'doc', action:'createNote']" method="post">
        <div class="ui label red" style="float:right">Feature in Entwicklung</div><br />

        <input type="hidden" name="ownerid" value="${ownobj.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj.class.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/>

        <div class="field">
            <label for="noteTitle">${message(code:'default.title.label')}:</label>
            <input type="text" id="noteTitle" name="noteTitle" />
        </div>
        <div class="field">
            <label for="noteContent">${message(code:'default.content.label')}:</label>
            <div id="noteContent" placeholder="Tipp: Formatierung beim Einfügen verloren? Einfach erneut mit STRG+A und danach STRG+V versuchen"></div>

            <laser:script file="${this.getGroovyPageFileName()}">
                wysiwyg.initEditor ('#modalCreateNote #noteContent');

                JSPC.callbacks.modal.show.modalCreateNote = function(trigger) {
                    // r2d2.resetModalForm ('#modalCreateNote');
                    $('#modalCreateNote #noteTitle').val('');
                    wysiwyg.resetContent ('#modalCreateNote #noteContent');
                };
            </laser:script>
        </div>
    </g:form>
</ui:modal>
