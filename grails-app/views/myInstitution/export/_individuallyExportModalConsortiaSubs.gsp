<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionMembersFieldsForUI(institution)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="manageConsortiaSubscriptions" controller="myInstitution" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: message(code: 'subscription.details.consortiaMembers.label'), contactSwitch: true, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

