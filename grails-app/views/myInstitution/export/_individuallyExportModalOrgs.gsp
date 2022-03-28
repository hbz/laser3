<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportOrgFieldsForUI()}"/>
<g:if test="${actionName in ['listInstitution']}">
    <g:set var="exportFileName" value="${message(code: 'menu.institutions')}"/>
</g:if>
<g:else>
    <g:set var="exportFileName" value="${message(code: 'subscription.details.consortiaMembers.label')}"/>
</g:else>

<semui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params+[exportClickMeExcel: true]}">

        <g:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: exportFileName]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

