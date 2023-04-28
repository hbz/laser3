<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportIssueEntitlementFieldsForUI()}"/>

<ui:modal modalSize="large" id="${modalID}" text="ClickMich-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="index" controller="subscription" params="${params+[id:params.id]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(subscription.name) + "_" + message(code:'default.ie')]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

