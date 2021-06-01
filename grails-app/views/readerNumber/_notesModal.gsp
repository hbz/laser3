<semui:modal id="${formId}" text="${title}">

    <g:form class="ui form" url="[controller: 'readerNumber', action: 'editNote']" method="POST">
        <g:hiddenField name="orgid" value="${params.id}"/>
        <g:hiddenField name="dateGroup" value="${dateGroup}"/>
        <div class="field">
            <label for="note">
                <g:message code="readerNumber.note.label"/>
            </label>
            <input type="text" id="note" name="note" value="${note}"/>
        </div>

    </g:form>

</semui:modal>