<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionFieldsForUI()}"/>

<semui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params+[exportClickMeExcel: true]}">

        <g:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: message(code: 'subscription.plural')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

