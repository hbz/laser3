<%@ page import="de.laser.WorkflowService;" %>

<g:if test="${status == WorkflowService.OP_STATUS_DONE}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="positive" message="workflow.delete.ok" />
    </g:if>
    <g:elseif test="${cmd == 'create' || cmd == 'instantiate'}">
        <ui:msg class="positive" message="workflow.create.ok" />
    </g:elseif>
    <g:else>
        <ui:msg class="positive" message="workflow.edit.ok" />
    </g:else>
</g:if>
<g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="negative" message="workflow.delete.error" />
    </g:if>
    <g:elseif test="${cmd == 'create' || cmd == 'instantiate'}">
        <ui:msg class="negative" message="workflow.create.error" />
    </g:elseif>
    <g:else>
        <ui:msg class="negative" message="workflow.edit.error" />
    </g:else>
</g:elseif>

