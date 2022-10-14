<%@ page import="de.laser.WorkflowService; de.laser.workflow.*; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:each in="${workflows}" var="wf">

    <g:set var="wfInfo" value="${wf.getInfo()}" />

    <div data-wfId="${wf.id}" class="workflow-details" style="margin-top:5em; margin-bottom:5em; position:relative; display:none;">

        <div class="ui piled segments wf-details">

            <div class="ui segment">
                <p style="text-align: center">
                    <strong>Detailansicht</strong>
                </p>
            </div>

            <div class="ui segment">
                <div class="ui grid">
                    <div class="row">
                        <div class="two wide column wf-centered">

                            <uiWorkflow:statusIcon workflow="${wf}" size="big" />

                        </div>
                        <div class="ten wide column">

                            <div class="ui header">${wf.title}</div>
                            <div class="description">
                                ${wf.description}
                                <br />
                                <g:if test="${wf.description}"><br /></g:if>

                                <div class="la-flexbox">
                                    <i class="icon ${wfInfo.targetIcon} la-list-icon"></i>
                                    <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">
                                        ${wfInfo.targetName}
                                    </g:link>
                                </div>
                                <div class="la-flexbox">
                                    <i class="icon user la-list-icon"></i>
                                    <g:if test="${wf.user}">
                                        ${wf.user?.display}
                                    </g:if>
                                    <g:else>
                                        Kein verantwortlicher Nutzer zugewiesen. (todo: wird entfernt)
                                    </g:else>
                                </div>
                                <g:if test="${wf.comment}">
                                    <div style="margin:1em 2em 0 2em; padding-left:1em; border-left:5px solid #E0E0E0">
                                        ${wf.comment}
                                    </div>
                                </g:if>

                                <div class="sc_darkgrey" style="margin:1em 0 0 0; text-align:right;">
                                    Vorlage: ${wf.prototypeTitle} (${wf.prototypeVariant})
                                </div>
                            </div>

                        </div>
                        <div class="two wide column wf-centered">

                            <div class="${DateUtils.isDateToday(wf.lastUpdated) ? '' : 'sc_darkgrey'}" style="text-align: right">
                                ${DateUtils.getLocalizedSDF_noTime().format(wf.lastUpdated)}<br />
                                ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                            </div>

                        </div>
                        <div class="two wide column wf-centered">

                            <g:set var="wfKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfWorkflow.KEY}:${wf.id}" />
                            <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                    <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: wfKey, info: wfKey]}">
                                        <i class="icon wrench"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: wfKey, info: wfKey]}">
                                    <i class="icon pencil"></i>
                                </g:link>
                            </g:if>

                        </div>
                    </div>
                </div>
            </div>

            <g:set var="tasks" value="${wf.getSequence()}" />
            <g:each in="${tasks}" var="task" status="ti">

                <div class="ui segment">
                    <div class="ui grid">
                        <div class="row">
                            <div class="two wide column wf-centered">

                                <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(task.status)}"></i>

                            </div>
                            <div class="ten wide column">

                                <div class="header">
                                    <strong>${task.title}</strong>
                                    <span class="sc_darkgrey">
                                        ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i> ${task.priority.getI10n('value')} )
                                    </span>
                                </div>
                                <div class="description">${task.description}
                                    <g:if test="${task.comment}">
                                        <div style="margin:1em 2em 0 2em; padding-left:1em; border-left:5px solid #E0E0E0">
                                            ${task.comment}
                                        </div>
                                    </g:if>
                                </div>

                            </div>
                            <div class="two wide column wf-centered">

                                <div class="${DateUtils.isDateToday(task.lastUpdated) ? '' : 'sc_darkgrey'}" style="text-align: right">
                                    ${DateUtils.getLocalizedSDF_noTime().format(task.lastUpdated)}
                                </div>

                            </div>
                            <div class="two wide column wf-centered">

                                <g:set var="tKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfTask.KEY}:${task.id}" />
                                <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                    <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                        <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: tKey, info: wfKey]}">
                                            <i class="icon wrench"></i>
                                        </g:link>
                                    </span>
                                </g:if>
                                <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                    <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                        <i class="icon pencil"></i>
                                    </g:link>
                                </g:if>

                            </div>
                        </div>

                        <g:if test="${task.condition}">
                            <div class="row">
                                <div class="two wide column wf-centered">

                                </div>
                                <div class="one wide column">

                                </div>
                                <div class="nine wide column">

                                    <div class="header"><strong>${task.condition.title}</strong></div>
                                    <div class="description">
                                        <g:if test="${task.condition.description}">
                                            ${task.condition.description}
                                        </g:if>
                                        <!-- -->
                                        <div class="ui list" style="margin-top:1em">
                                            <g:each in="${task.condition.getFields()}" var="field" status="fi">
                                                <uiWorkflow:taskConditionField condition="${task.condition}" field="${field}" isListItem="true"/>
                                            </g:each>
                                        </div>
                                    </div>
                                </div>
                                <div class="two wide column wf-centered">

                                    <div class="${DateUtils.isDateToday(task.condition.lastUpdated) ? '' : 'sc_darkgrey'}" style="text-align: right">
                                        ${DateUtils.getLocalizedSDF_noTime().format(task.condition.lastUpdated)}
                                    </div>

                                </div>
                                <div class="two wide column wf-centered">

                                    <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                        <g:set var="cKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfCondition.KEY}:${task.condition.id}" />
                                        <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                            <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: cKey, info: wfKey]}">
                                                <i class="icon wrench"></i>
                                            </g:link>
                                        </span>
                                    </g:if>

                                </div>
                            </div>
                        </g:if>

                    </div>
                </div>

            </g:each>

            <div class="ui segment">
            </div>
        </div>

    </div>

</g:each>

<laser:script file="${this.getGroovyPageFileName()}">
    docs.init('.workflow-details');
</laser:script>