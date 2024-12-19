<%@ page import="de.laser.WorkflowService;" %>

<g:if test="${status == WorkflowService.OP_STATUS_DONE}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="success" message="workflow.delete.ok" />
    </g:if>
    <g:elseif test="${cmd == 'create' || cmd == 'instantiate'}">
        <ui:msg class="success" message="workflow.create.ok" />
    </g:elseif>
    <g:else>
        <ui:msg class="success" message="workflow.edit.ok" />
    </g:else>
</g:if>
<g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="error" message="workflow.delete.error" />
    </g:if>
    <g:elseif test="${cmd == 'create' || cmd == 'instantiate'}">
        <ui:msg class="error" message="workflow.create.error" />
    </g:elseif>
    <g:else>
        <ui:msg class="error" message="workflow.edit.error" />
    </g:else>
</g:elseif>
<g:elseif test="${status == WorkflowService.OP_STATUS_FORBIDDEN}">
    <ui:msg class="error" message="default.noPermissions" />
</g:elseif>

