<%@ page import="de.laser.storage.RDConstants" %>
<laser:serviceInjection />

<div class="ui grid la-clear-before">
    <div class="sixteen wide column">

        <table class="ui sortable celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <th class="two wide" rowspan="2" scope="col">${message(code: 'task.endDate.label')}</th>
                <th class="five wide" rowspan="2" scope="col">
                    ${message(code: 'default.title.label')}
                    <g:if test="${controllerName == 'myInstitution'}">
                        / ${message(code: 'task.object.label')}
                    </g:if>
                </th>
                <th class="three wide" rowspan="2" scope="col">
                    ${message(code: 'task.assignedTo.label')}
                </th>
                <th rowspan="2" scope="col">${message(code: 'task.status.label')}</th>
                <th class="two wide" rowspan="2" scope="col">${message(code: 'task.creator.label')}</th>
                <th class="two wide la-smaller-table-head" scope="col">${message(code: 'default.lastUpdated.label')}</th>
                <th class="two wide la-action-info" rowspan="2" scope="col">${message(code:'default.actions.label')}</th>
            </tr>
            <tr>
                <th class="two wide la-smaller-table-head" scope="col">${message(code: 'default.dateCreated.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${taskInstanceList}" var="taskInstance">
                <!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${userService.checkAffiliationAndCtxOrg(user, contextService.getOrg(), 'INST_EDITOR')} -->
                <g:set var="overwriteEditable" value="${editable || taskService.isTaskEditableBy(taskInstance, contextService.getUser(), contextService.getOrg())}" />
                <tr>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance?.endDate}"/>
                    </td>
                    <td>
                        ${fieldValue(bean: taskInstance, field: "title")}

                        <g:if test="${controllerName == 'myInstitution'}">
                            <g:if test="${taskInstance.license}">
                                <br /> <g:link controller="license" action="show" id="${taskInstance.license?.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link> <br />
                            </g:if>
                            <g:if test="${taskInstance.org}">
                                <br /> <g:link controller="organisation" action="show" id="${taskInstance.org?.id}">${fieldValue(bean: taskInstance, field: "org")}</g:link> <br />
                            </g:if>
                            <g:if test="${taskInstance.pkg}">
                                <br /> <g:link controller="package" action="show" id="${taskInstance.pkg?.id}">${fieldValue(bean: taskInstance, field: "pkg")}</g:link> <br />
                            </g:if>
                            <g:if test="${taskInstance.subscription}">
                                <br /> <g:link controller="subscription" action="show" id="${taskInstance.subscription?.id}">${fieldValue(bean: taskInstance, field: "subscription")}</g:link>
                            </g:if>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${taskInstance.responsibleOrg?.id == contextService.getOrg().id || taskInstance.responsibleUser?.id == contextService.getUser().id}">
                            <i class="icon hand point right sc_grey"></i>
                        </g:if>
                        <g:if test="${taskInstance.responsibleOrg}"> ${taskInstance.responsibleOrg.name} <br /> </g:if>
                        <g:if test="${taskInstance.responsibleUser}"> ${taskInstance.responsibleUser.display} </g:if>
                    </td>
                    <td>
                        <ui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${taskInstance}" field="status" overwriteEditable="${overwriteEditable}" />
                    </td>
                    <td>
                        <g:if test="${taskInstance.creator?.id == contextService.getUser().id}">
                            <i class="icon hand point right sc_grey"></i>${taskInstance.creator.display}
                        </g:if>
                        <g:else>
                            ${taskInstance.creator.display}
                        </g:else>
                    </td>

                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance.lastUpdated}"/>

                        <g:if test="${taskInstance.dateCreated != taskInstance.lastUpdated}">
                            <br />
                            <span class="sc_darkgrey"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance.createDate}"/></span>
                        </g:if>
                    </td>

                    <td class="center aligned">
                        <g:if test="${overwriteEditable}">
                            <a onclick="JSPC.app.editTask(${taskInstance.id});" class="ui icon button blue la-modern-button"
                               role="button" aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </a>
                        </g:if>
                        <g:if test="${(user == taskInstance.creator && user.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')) || contextService.getUser().hasCtxAffiliation_or_ROLEADMIN('INST_ADM')}">
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

        <ui:paginate total="${taskInstanceCount}" params="${params}" />

    </div><!-- .sixteen -->
</div><!-- .grid -->

