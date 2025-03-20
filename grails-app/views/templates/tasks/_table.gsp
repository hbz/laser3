<%@ page import="de.laser.AccessService; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants" %>
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
                <th class="three wide" rowspan="2" scope="col">${message(code: 'task.assignedTo.label')}</th>
                <th rowspan="2" scope="col">${message(code: 'task.status.label')}</th>
                <th class="two wide" rowspan="2" scope="col">${message(code: 'task.creator.label')}</th>
                <th class="two wide la-smaller-table-head" scope="col">${message(code: 'default.lastUpdated.label')}</th>
                <th class="two wide center aligned" rowspan="2" scope="col">
                    <ui:optionsIcon />
                </th>
            </tr>
            <tr>
                <th class="two wide la-smaller-table-head" scope="col">${message(code: 'default.dateCreated.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${cmbTaskInstanceList}" var="taskInstance">
                <g:set var="overwriteEditable" value="${accessService.hasAccessToTask(taskInstance, AccessService.WRITE)}" />
                <tr>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${taskInstance.endDate}"/>
                    </td>
                    <td>
                        ${fieldValue(bean: taskInstance, field: "title")} <br />

                        <g:if test="${controllerName == 'myInstitution'}">
                            <g:if test="${taskInstance.license}">
                                <i class="${Icon.LICENSE} la-list-icon"></i> <g:link controller="license" action="show" id="${taskInstance.license.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link>
                            </g:if>
                            <g:if test="${taskInstance.org}">
                                <ui:customerTypeIcon org="${taskInstance.org}" /> <g:link controller="organisation" action="show" id="${taskInstance.org.id}">${fieldValue(bean: taskInstance, field: "org")}</g:link>
                            </g:if>
                            <g:if test="${taskInstance.provider}">
                                <i class="${Icon.PROVIDER} la-list-icon"></i> <g:link controller="provider" action="show" id="${taskInstance.provider.id}">${fieldValue(bean: taskInstance, field: "provider")}</g:link>
                            </g:if>
                            <g:if test="${taskInstance.subscription}">
                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i> <g:link controller="subscription" action="show" id="${taskInstance.subscription.id}">${fieldValue(bean: taskInstance, field: "subscription")}</g:link>
                            </g:if>
                            <g:if test="${taskInstance.tipp}">
                                <i class="${Icon.TIPP} la-list-icon"></i> <g:link controller="tipp" action="show" id="${taskInstance.tipp.id}">${fieldValue(bean: taskInstance, field: "tipp")}</g:link>
                            </g:if>
                            <g:if test="${taskInstance.vendor}">
                                <i class="${Icon.VENDOR} la-list-icon"></i> <g:link controller="vendor" action="show" id="${taskInstance.vendor.id}">${fieldValue(bean: taskInstance, field: "vendor")}</g:link>
                            </g:if>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${taskInstance.responsibleOrg?.id == contextService.getOrg().id || taskInstance.responsibleUser?.id == user.id}">
                            <i class="${Icon.SIG.MY_OBJECT} yellow"></i>
                        </g:if>
                        <g:if test="${taskInstance.responsibleOrg}"> ${taskInstance.responsibleOrg.sortname ?: taskInstance.responsibleOrg.name} <br /> </g:if>
                        <g:if test="${taskInstance.responsibleUser}"> ${taskInstance.responsibleUser.display} </g:if>
                    </td>
                    <td>
                        <ui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${taskInstance}" field="status" overwriteEditable="${overwriteEditable}" />
                    </td>
                    <td>
                        <g:if test="${taskInstance.creator?.id == user.id}">
                            <i class="${Icon.SIG.MY_OBJECT} yellow"></i>
                        </g:if>
                        ${taskInstance.creator.display}
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
                            <a onclick="JSPC.app.editTask(${taskInstance.id});" class="${Btn.MODERN.SIMPLE}" role="button" aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </a>
                        </g:if>
                        <g:elseif test="${accessService.hasAccessToTask(taskInstance, AccessService.READ)}">
                            <a onclick="JSPC.app.readTask(${taskInstance.id});" class="${Btn.MODERN.SIMPLE}" role="button" aria-label="${message(code: 'ariaLabel.read.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.READ}"></i>
                            </a>
                        </g:elseif>
                        <g:if test="${overwriteEditable}"> %{-- TODO: responsibleOrg == contextService.getOrg() + INST_ADM ? --}%
                            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.task")}"
                                    data-confirm-term-how="delete"
                                    action="deleteTask" controller="task" params="[deleteId:taskInstance.id]"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.task')}">
                                <i class="${Icon.CMD.DELETE}"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <ui:paginate total="${cmbTaskInstanceCount}" params="${params}" />

    </div><!-- .sixteen -->
</div><!-- .grid -->

