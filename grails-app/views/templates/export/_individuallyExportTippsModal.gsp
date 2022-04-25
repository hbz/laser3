<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportTippFieldsForUI()}"/>

<semui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params+[id:params.id, exportClickMeExcel: true]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: filename ?: message(code:'default.title.label')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

