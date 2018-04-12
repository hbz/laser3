<% def accService = grailsApplication.mainContext.getBean("accessService") %>
<!-- OVERWRITE editable for INST_EDITOR (precondition: Subscriber_Consortial): ${accService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')} -->
<g:set var="overwriteEditable" value="${editable || accService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')}" />

<semui:card message="task.plural" class="notes" href="#modalCreateTask" editable="${overwriteEditable}">
    <g:each in="${tasks}" var="tsk">
        <div class="ui small feed content">
            <!--<div class="event">-->
                    <div class="summary">
                        <g:link controller="task" action="show" id="${tsk.id}">${tsk?.title}</g:link>
                        <br />
                        <div class="content">
                            ${message(code:'task.endDate.label')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk.endDate}"/>
                        </div>
                    </div>
        <!--</div>-->
        </div>
    </g:each>
</semui:card>

<g:render template="/templates/tasks/modal" />
