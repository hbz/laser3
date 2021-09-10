<%@ page import="de.laser.workflow.*; de.laser.WorkflowService" %>

<g:if test="${cmd == 'create'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" message="default.created2.message" args="${[obj.title]}" />
    </g:if>
    <g:else>
        <semui:errors bean="${obj}" />
    </g:else>
</g:if>
<g:if test="${cmd == 'edit'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" message="default.updated2.message" args="${[obj.title]}" />
    </g:if>
    <g:else>
        <semui:errors bean="${obj}" />
    </g:else>
</g:if>
<g:if test="${cmd == 'instantiate'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" message="default.instantiated2.message" args="${[obj.title]}" />
    </g:if>
    <g:else>
        <semui:errors bean="${obj}" />
    </g:else>
</g:if>
<g:if test="${cmd == 'delete'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" text="Das Objekt wurde erfolgreich gelöscht." />
    </g:if>
    <g:else>
        <semui:msg class="negative" message="default.not.deleted2.message" args="${[obj.title]}" />
    </g:else>
</g:if>