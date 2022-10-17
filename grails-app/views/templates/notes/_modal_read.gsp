<ui:modal id="modalReadNote" text="${message(code:'template.readNote')}" hideSubmitButton="true">

    <g:form class="ui form">
        <div class="field">
            <label for="title">${message(code:'default.title.label')}:</label>
            <div id="title" class="ui segment" style="margin:0; padding:0.5em 1em; box-shadow:none;">${noteInstance.title ?: 'Ohne Titel'}</div>
        </div>
        <div class="field">
            <label for="content">${message(code:'default.note.label')}:</label>
            <div id="content" class="ui segment" style="margin:0; padding:0.5em 1em; box-shadow:none;">${raw(noteInstance.content)}</div>
        </div>
    </g:form>
</ui:modal>
