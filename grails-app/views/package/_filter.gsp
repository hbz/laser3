<%@ page import="de.laser.ui.Btn" %>
<ui:filter>
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
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'package.compare.filter.submit.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>