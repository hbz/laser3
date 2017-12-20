<semui:card message="task.plural" class="card-grey notes">
    <ul>
    <g:each in="${tasks}" var="tsk">
        <li>
            <g:link controller="task" action="show" id="${tsk.id}">${tsk?.title}</g:link>
            <br />
            <i>
                ${message(code:'task.endDate.label')}
                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk.endDate}"/>
            </i>
        </li>
    </g:each>
    </ul>
    <input type="submit" class="ui fluid button" value="${message(code:'task.create.new')}" data-semui="modal" href="#modalCreateTask" />
</semui:card>

<g:render template="/templates/tasks/modal" />

<div class="modal hide fade" id="modalTasks"></div>
