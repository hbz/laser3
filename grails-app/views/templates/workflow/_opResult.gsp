<%@ page import="de.laser.workflow.*; de.laser.WorkflowService" %>

<g:if test="${! obj}">
    <g:if test="${cmd == 'delete' && status == WorkflowService.OP_STATUS_DONE}">
        <ui:msg class="positive" message="default.deleted.general.message" />
    </g:if>
    <g:else>
        <ui:msg class="negative" message="default.not.found.general.message" />
    </g:else>
</g:if>
<g:else>
    <g:if test="${cmd == 'create'}">
        <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
            <ui:msg class="positive" message="default.created2.message" args="${[obj.title]}" />
        </g:if>
        <g:else>
            <ui:errors bean="${obj}" />
        </g:else>
    </g:if>
    <g:if test="${cmd == 'edit'}">
        <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
            <ui:msg class="positive" message="default.updated2.message" args="${[obj.title]}" />
        </g:if>
        <g:else>
            <ui:errors bean="${obj}" />
        </g:else>
    </g:if>
    <g:if test="${cmd == 'instantiate'}">
        <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
            <ui:msg class="positive" message="default.instantiated2.message" args="${[obj.title]}" />
        </g:if>
        <g:else>
            <ui:errors bean="${obj}" />
        </g:else>
    </g:if>
    <g:if test="${cmd == 'delete' && status != WorkflowService.OP_STATUS_DONE}">
        <ui:msg class="negative" message="default.not.deleted2.message" args="${[obj.title]}" />
    </g:if>
</g:else>