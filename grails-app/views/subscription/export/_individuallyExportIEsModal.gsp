<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportIssueEntitlementFieldsForUI()}"/>

<semui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="index" controller="subscription" params="${params+[id:params.id, exportClickMeExcel: true]}">

        <g:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(subscription.name) + "_" + message(code:'subscriptionDetails.members.members')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

