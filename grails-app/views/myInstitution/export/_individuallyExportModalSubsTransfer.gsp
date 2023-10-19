<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModalSubsTransfer.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionFieldsForUI(institution, true)}"/>

<ui:modal modalSize="fullscreen" id="${modalID}" text="Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: message(code: 'export.my.currentSubscriptionsTransfer'), csvFieldSeparator: ',']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModalSubsTransfer.gsp -->

