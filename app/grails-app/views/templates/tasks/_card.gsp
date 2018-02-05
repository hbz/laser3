<semui:card message="task.plural" class="card-grey notes">

    <g:each in="${tasks}" var="tsk">
        <div class="ui small feed">
            <!--<div class="event">-->
                <div class="content">
                    <div class="summary">
                        <g:link controller="task" action="show" id="${tsk.id}">${tsk?.title}</g:link>
        <br />
                        ${message(code:'task.endDate.label')}
                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk.endDate}"/>
                </div>
            </div>
        <!--</div>-->
        </div>
    </g:each>

    </div>
        <div class="extra content">
            <input type="submit" class="ui button" value="${message(code:'default.button.create_new.label')}" data-semui="modal" href="#modalCreateTask" />
</semui:card>

<g:render template="/templates/tasks/modal" />

<div class="modal hide fade" id="modalTasks"></div>
