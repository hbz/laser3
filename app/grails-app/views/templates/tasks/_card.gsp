<semui:card message="task.plural" class="card-grey notes">
    <g:each in="${tasks}" var="tsk">
        <g:link controller="task" action="show" id="${tsk.id}">${tsk?.title}</g:link>
        /
        ${tsk?.endDate}
        <br />
    </g:each>
    <input type="submit" class="ui primary button" value="${message(code:'task.create.new')}" data-toggle="modal" href="#modalCreateTask" />
</semui:card>

<g:render template="/templates/tasks/modal" />

<div class="modal hide fade" id="modalTasks"></div>
