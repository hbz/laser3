<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionMembersFieldsForUI(institution, subscription)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="members" controller="subscription" params="${params+[id:params.id]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(subscription.name) + "_" + message(code:'subscriptionDetails.members.members'), contactSwitch: true, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

