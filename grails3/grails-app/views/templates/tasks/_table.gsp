<laser:serviceInjection />


<div class="ui grid la-clear-before">

    <div class="sixteen wide column">

        <h2 class="ui header">${message(code: 'task.myTasks.header')} <semui:totalNumber total="${taskInstanceCount}"/></h2>

        <table class="ui sortable celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <g:sortableColumn property="lower(t.title)" title="${message(code: 'task.title.label')}"/>
                <g:sortableColumn property="t.endDate" title="${message(code: 'task.endDate.label')}"/>
                <g:sortableColumn property="t.status" title="${message(code: 'task.status.label')}"/>
                <g:if test="${controllerName == 'myInstitution'}">
                    <th>${message(code: 'task.object.label')}</th>
                </g:if>
                <th>
                    ${message(code: 'task.assignedTo.label')}
                </th>
                <g:sortableColumn property="lower(t.creator.username)" title="${message(code: 'task.creator.label')}"/>
                <g:sortableColumn property="t.createDate" title="${message(code: 'task.createDate.label')}"/>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${taskInstanceList}" var="taskInstance">
                <!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')} -->
                <g:set var="overwriteEditable" value="${editable || taskService.isTaskEditableBy(taskInstance, contextService.getUser(), contextService.getOrg())}" />
                <tr>
                    <th scope="row" class="la-th-column la-main-object" >${fieldValue(bean: taskInstance, field: "title")}</th>

                    <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance?.endDate}"/></td>

                    <td>
                        <semui:xEditableRefData config="${de.laser.helper.RDConstants.TASK_STATUS}" owner="${taskInstance}" field="status" overwriteEditable="${overwriteEditable}" />
                    </td>
                    <g:if test="${controllerName == 'myInstitution'}">
                    <td>
                        <g:if test="${taskInstance.license}">
                            <g:link controller="license" action="show" id="${taskInstance.license?.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.org}">
                            <g:link controller="organisation" action="show" id="${taskInstance.org?.id}">${fieldValue(bean: taskInstance, field: "org")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.pkg}">
                            <g:link controller="package" action="show" id="${taskInstance.pkg?.id}">${fieldValue(bean: taskInstance, field: "pkg")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.subscription}">
                            <g:link controller="subscription" action="show" id="${taskInstance.subscription?.id}">${fieldValue(bean: taskInstance, field: "subscription")}</g:link>
                        </g:if>
                    </td>
                    </g:if>

                    <td>
                        <g:if test="${taskInstance.responsibleOrg}">${taskInstance.responsibleOrg.name} <br /></g:if>
                        <g:if test="${taskInstance.responsibleUser}">${taskInstance.responsibleUser.display}</g:if>
                    </td>

                    <td>${taskInstance.creator.display}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance.createDate}"/></td>

                    <td class="x">
                        <g:if test="${overwriteEditable}">
                            <a onclick="JSPC.app.taskedit(${taskInstance.id});" class="ui icon button blue la-modern-button"
                               role="button"
                               aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </a>
                        </g:if>
                        <g:if test="${(user == taskInstance.creator) || contextService.getUser().hasAffiliation("INST_ADM")}">
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.task")}"
                                    data-confirm-term-how="delete"
                                    action="deleteTask" controller="ajax" params="[deleteId:taskInstance.id]"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.task')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <semui:paginate total="${taskInstanceCount}" params="${params}" />


    </div><!-- .sixteen -->

</div><!-- .grid -->

