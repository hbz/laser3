<laser:render template="/templates/filter/javascript" />
<ui:filter showFilterButton="true">
    <g:form action="${actionName}" params="${params}" method="get" class="ui form">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">
        <div class="fields two">
            <div class="field">
                <label for="filter">${message(code:'package.compare.filter.title')}</label>
                <input id="filter" name="filter" value="${params.filter}"/>
            </div>
            <div class="field">
                <label for="coverageNoteFilter">${message(code:'default.note.label')}</label>
                <input id="coverageNoteFilter" name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
            </div>
        </div>
        <div class="three fields">
            <div class="field">
                <ui:datepicker label="package.compare.filter.coverage_startsBefore" id="startsBefore" name="startsBefore" value="${params.startsBefore}" />
            </div>
            <div class="field">
                <ui:datepicker label="package.compare.filter.coverage_endsAfter" id="endsAfter" name="endsAfter" value="${params.endsAfter}" />
            </div>
            <div class="field">

            </div>
        </div>
        <div class="three fields">
            <div class="field">
                <ui:datepicker label="package.compare.filter.accessStartDate" id="accessStartDate" name="accessStartDate" value="${params.accessStartDate}" />
            </div>
            <div class="field">
                <ui:datepicker label="package.compare.filter.accessEndDate" id="accessEndDate" name="accessEndDate" value="${params.accessEndDate}" />
            </div>
            <div class="field la-field-right-aligned">

                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'package.compare.filter.submit.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>