<%@ page import="de.laser.workflow.light.WfCheckpoint; de.laser.workflow.light.WfChecklist; de.laser.WorkflowService; de.laser.workflow.*; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

%{-- CHECKLISTS --}%
<g:each in="${checklists}" var="clist">

    <g:set var="clistInfo" value="${clist.getInfo()}" />

    <div data-wfId="${clist.id}" class="workflow-details" style="margin:5rem; position:relative; display:none;">

        <div class="ui piled segments wf-details">

            <div class="ui segment">
                <p style="text-align: center">
                    Sie bearbeiten den Workflow: <strong>${clist.title}</strong>
                </p>
            </div>

            <div class="ui segment">
                <div class="ui grid">
                    <div class="row">
                        <div class="two wide column wf-centered">
                            <uiWorkflow:statusIcon checklist="${clist}" size="big" />
                        </div>
                        <div class="ten wide column">

                            <div class="ui grid">
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'workflow.label')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <div class="ui header">
                                            <ui:xEditable owner="${clist}" field="title" type="text" />
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'default.description.label')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditable owner="${clist}" field="description" type="textarea" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'default.comment.label')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditable owner="${clist}" field="comment" type="textarea" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'workflow.template')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditableBoolean owner="${clist}" field="template" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>Objekt</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <i class="icon ${clistInfo.targetIcon} la-list-icon"></i>
                                        <g:link controller="${clistInfo.targetController}" action="show" params="${[id: clistInfo.target.id]}">
                                            ${clistInfo.targetName}
                                        </g:link>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <div class="two wide column wf-centered">
                            <div class="${DateUtils.isDateToday(clist.lastUpdated) ? '' : 'sc_darkgrey'}" style="text-align: right">
                                ${DateUtils.getLocalizedSDF_noTime().format(clist.lastUpdated)}<br />
                                ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                            </div>
                        </div>

                        <g:set var="wfKey" value="${clistInfo.target.class.name}:${clistInfo.target.id}:${WfChecklist.KEY}:${clist.id}" />
                        <div class="two wide column wf-centered"></div>
                    </div>
                </div>
            </div>

            <g:set var="cpoints" value="${clist.getSequence()}" />
            <g:each in="${cpoints}" var="cpoint" status="ti">

                <div class="ui segment">
                    <div class="ui grid">
                        <div class="row">
                            <div class="two wide column wf-centered">
                                <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(cpoint.done ? RDStore.WF_TASK_STATUS_DONE : RDStore.WF_TASK_STATUS_OPEN)}"></i>
                            </div>
                            <div class="ten wide column">

                                <div class="ui grid">
                                    <div class="row">
                                        <div class="four wide column">
                                            <strong>${message(code: 'workflow.task.label')}</strong>
                                        </div>
                                        <div class="twelve wide column">
                                            <div class="ui header">
                                                <ui:xEditable owner="${cpoint}" field="title" type="text" />
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="four wide column">
                                            <strong>${message(code: 'default.description.label')}</strong>
                                        </div>
                                        <div class="twelve wide column">
                                            <ui:xEditable owner="${cpoint}" field="description" type="textarea" />
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="four wide column">
                                            <strong>${message(code: 'default.comment.label')}</strong>
                                        </div>
                                        <div class="twelve wide column">
                                            <ui:xEditable owner="${cpoint}" field="comment" type="textarea" />
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="four wide column">
                                            <strong>${message(code: 'workflow.checkpoint.done')}</strong>
                                        </div>
                                        <div class="twelve wide column">
                                            <ui:xEditableBoolean owner="${cpoint}" field="done" />
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="four wide column">
                                            <strong>${message(code: 'workflow.checkpoint.date')}</strong>
                                        </div>
                                        <div class="twelve wide column">
                                            <ui:xEditable owner="${cpoint}" field="date" type="date" />
                                        </div>
                                    </div>
                                </div>

                            </div>
                            <div class="two wide column wf-centered">
                                <div class="${DateUtils.isDateToday(cpoint.lastUpdated) ? '' : 'sc_darkgrey'}" style="text-align: right">
                                    ${DateUtils.getLocalizedSDF_noTime().format(cpoint.lastUpdated)}
                                </div>
                            </div>

                            <g:set var="tKey" value="${clistInfo.target.class.name}:${clistInfo.target.id}:${WfCheckpoint.KEY}:${cpoint.id}" />%{-- todo --}%

                            <div class="two wide column wf-centered">

                                <g:if test="${workflowLightService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                    <g:if test="${ti > 0}">
                                        <g:link class="ui icon button blue compact la-modern-button" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"moveUp:${WfCheckpoint.KEY}:${cpoint.id}"]}">
                                            <i class="icon arrow up"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                    </g:else>
                                    <g:if test="${ti < cpoints.size()-1}">
                                        <g:link class="ui icon button blue compact la-modern-button" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"moveDown:${WfCheckpoint.KEY}:${cpoint.id}"]}">
                                            <i class="icon arrow down"></i>
                                        </g:link>
                                    </g:if>
                                    <g:elseif test="${ti == cpoints.size()-1}">
                                        <g:link class="ui icon button blue compact la-modern-button" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"create:${WfCheckpoint.KEY}:${cpoint.id}"]}">
                                            <i class="icon plus"></i>
                                        </g:link>
                                    </g:elseif>
                                    <g:else>
                                        <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                    </g:else>
                                </g:if>

                                <g:if test="${workflowLightService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                                    <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.checkpoint", args: [cpoint.title])}"
                                            data-confirm-term-how="delete"
                                            controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfCheckpoint.KEY}:${cpoint.id}"]}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </g:link>
                                </g:if>

                            </div>
                        </div>
                    </div>
                </div>

            </g:each>

            <div class="ui segment">
            </div>
        </div>

    </div>

    <style>
        .ui.grid .row {
            padding-top: 0.5rem;
            padding-bottom: 0.5rem;
        }
        .ui.grid .row:first-of-type {
            padding-top: 1rem;
        }
        .ui.grid .row:last-of-type {
            padding-bottom: 1rem;
        }
    </style>
</g:each>

%{-- WORKFLOWS --}%
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
                                    <g:if test="${wf.user}">
                                        <g:if test="${wf.user.id == contextService.getUser().id}">
                                            <i class="icon user la-list-icon"></i> ${wf.user?.display}
                                        </g:if>
                                        <g:else>
                                            <i class="icon user outline la-list-icon"></i> ${wf.user?.display}
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <i class="icon users la-list-icon"></i> ${message(code:'workflow.user.noCurrentUser')}
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

                        <g:set var="wfKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfWorkflow.KEY}:${wf.id}" />

                        <div class="one wide column wf-centered">

                            <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: wfKey, info: wfKey]}">
                                    <i class="icon pencil"></i>
                                </g:link>
                            </g:if>

                        </div>
                        <div class="one wide column wf-centered">

                            <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                    <g:link class="wfModalLink ui icon button red compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: wfKey, info: wfKey]}">
                                        <i class="icon sliders horizontal"></i>
                                    </g:link>
                                </span>
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

                            <g:set var="tKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfTask.KEY}:${task.id}" />

                            <div class="one wide column wf-centered">

                                <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                    <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                        <i class="icon pencil"></i>
                                    </g:link>
                                </g:if>

                            </div>
                            <div class="one wide column wf-centered">

                                <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                    <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                        <g:link class="wfModalLink ui icon button red compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: tKey, info: wfKey]}">
                                            <i class="icon sliders horizontal"></i>
                                        </g:link>
                                    </span>
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

                                <g:set var="cKey" value="${wfInfo.target.class.name}:${wfInfo.target.id}:${WfCondition.KEY}:${task.condition.id}" />

                                <div class="one wide column wf-centered">
                                </div>
                                <div class="one wide column wf-centered">

                                    <g:if test="${workflowService.hasUserPerm_wrench()}"><!-- TODO: workflows-permissions -->
                                        <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                            <g:link class="wfModalLink ui icon button red compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: cKey, info: wfKey]}">
                                                <i class="icon sliders horizontal"></i>
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