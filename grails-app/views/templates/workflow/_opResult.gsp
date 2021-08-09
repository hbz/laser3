<%@ page import="de.laser.workflow.*; de.laser.WorkflowService" %>

<g:if test="${cmd == 'create'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" text="(${obj.id} : ${obj.title}) wurde erfolgreich gespeichert." />
    </g:if>
    <g:else>
        <semui:errors bean="${obj}" />
    </g:else>
</g:if>
<g:if test="${cmd == 'edit'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" text="(${obj.id} : ${obj.title}) wurde erfolgreich geändert." />
    </g:if>
    <g:else>
        <semui:errors bean="${obj}" />
    </g:else>
</g:if>
<g:if test="${cmd == 'instantiate'}">
    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <semui:msg class="positive" text="(${obj.id} : ${obj.title}) wurde erfolgreich instanziiert." />
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
        <semui:msg class="negative" text="(${obj.id} : ${obj.title}) konnte nicht gelöscht werden." />
    </g:else>
</g:if>