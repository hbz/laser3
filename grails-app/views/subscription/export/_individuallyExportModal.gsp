<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionMembersFieldsForUI(subscription, institution)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="members" controller="subscription" params="${params+[id:params.id, exportClickMeExcel: true]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(subscription.name) + "_" + message(code:'subscriptionDetails.members.members')]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

