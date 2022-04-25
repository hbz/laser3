<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionMembersFieldsForUI(subscription, institution)}"/>

<semui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="members" controller="subscription" params="${params+[id:params.id, exportClickMeExcel: true]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(subscription.name) + "_" + message(code:'subscriptionDetails.members.members')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

