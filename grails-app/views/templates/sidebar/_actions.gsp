<%@page import="de.laser.*" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor()}">
    <ui:actionsDropdownItem data-ui="modal" href="#modalCreateNote" message="template.notes.add"/>
</g:if>
<g:if test="${taskService.hasWRITE()}">
    <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.new"/>
</g:if>
<g:if test="${contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
    <ui:actionsDropdownItem data-ui="modal" href="#modalCreateDocument" message="template.documents.add"/>
</g:if>
<g:if test="${workflowService.hasWRITE()}">
    <ui:actionsDropdownItem data-ui="modal" href="#modalCreateWorkflow" message="workflow.instantiate"/>
</g:if>
