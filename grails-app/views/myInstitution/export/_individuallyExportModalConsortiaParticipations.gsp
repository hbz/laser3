<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportConsortiaParticipationFieldsForUI(institution)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: message(code: 'consortium.member.plural'), contactSwitch: true, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

