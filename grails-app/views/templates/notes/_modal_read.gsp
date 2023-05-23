<ui:modal id="modalReadNote" text="${noteInstance.title ?: message(code: 'license.notes.noTitle')}" hideSubmitButton="true">

    <div class="ui form">
%{--        <div class="field">--}%
%{--            <label for="title">${message(code:'default.title.label')}:</label>--}%
%{--            <div id="title" class="ui segment" style="margin:0; padding:0.5em 1em; box-shadow:none;">${noteInstance.title ?: message(code: 'license.notes.noTitle')}</div>--}%
%{--        </div>--}%
%{--        <div class="field">--}%
%{--            <label for="content">${message(code:'default.note.label')}:</label>--}%
%{--            <div id="content" class="ui segment trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">${raw(noteInstance.content)}</div>--}%
%{--        </div>--}%

        <div id="note-wrapper-${noteInstance.id}">
            <article id="note-${noteInstance.id}" class="trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">
                ${raw(noteInstance.content)}
            </article>
            <laser:script file="${this.getGroovyPageFileName()}">
                wysiwyg.analyzeNote_TMP( $("#note-${noteInstance.id}"), $("#note-wrapper-${noteInstance.id}"), true );
            </laser:script>
        </div>
    </div>
</ui:modal>
