<%@ page import="de.laser.workflow.WorkflowHelper; de.laser.workflow.WfCheckpoint; de.laser.workflow.WfChecklist; de.laser.WorkflowOldService; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:if test="${clist}">

    <g:set var="clistInfo" value="${clist.getInfo()}" />
    <g:set var="overEdit" value="${workflowService.hasUserPerm_edit()}" />

    <div class="ui header center aligned">
        ${clist.title}
    </div>
    <div class="content">

        <div class="ui vertical segment">
            <div class="ui grid">
                <div class="row">
                    <div class="one wide column wf-centered">
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
                                        <ui:xEditable overwriteEditable="${overEdit}" owner="${clist}" field="title" type="text" />
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="four wide column">
                                    <strong>${message(code: 'default.description.label')}</strong>
                                </div>
                                <div class="twelve wide column">
                                    <ui:xEditable overwriteEditable="${overEdit}" owner="${clist}" field="description" type="textarea" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="four wide column">
                                    <strong>${message(code: 'default.comment.label')}</strong>
                                </div>
                                <div class="twelve wide column">
                                    <ui:xEditable overwriteEditable="${overEdit}" owner="${clist}" field="comment" type="textarea" />
                                </div>
                            </div>
                            <div class="row">
                                <div class="four wide column">
                                    <strong>${message(code: 'workflow.template')}</strong>
                                </div>
                                <div class="twelve wide column">
                                    <ui:xEditableBoolean overwriteEditable="${overEdit}" owner="${clist}" field="template" />
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
                    <div class="three wide column wf-centered">
                    </div>
                </div>
            </div>
        </div>

        <g:set var="cpoints" value="${clist.getSequence()}" />
        <g:each in="${cpoints}" var="cpoint" status="ti">

            <div class="ui vertical segment">
                <div class="ui grid">
                    <div class="row">
                        <div class="one wide column wf-centered">
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
                                            <ui:xEditable overwriteEditable="${overEdit}" owner="${cpoint}" field="title" type="text" />
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'default.description.label')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditable overwriteEditable="${overEdit}" owner="${cpoint}" field="description" type="textarea" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'default.comment.label')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditable overwriteEditable="${overEdit}" owner="${cpoint}" field="comment" type="textarea" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'workflow.checkpoint.done')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditableBoolean overwriteEditable="${overEdit}" owner="${cpoint}" field="done" />
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="four wide column">
                                        <strong>${message(code: 'workflow.checkpoint.date')}</strong>
                                    </div>
                                    <div class="twelve wide column">
                                        <ui:xEditable overwriteEditable="${overEdit}" owner="${cpoint}" field="date" type="date" />
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

                        <div class="three wide column wf-centered">

                            <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                <g:if test="${ti > 0}">
                                    <g:link class="ui icon button blue compact la-modern-button" controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"moveUp:${WfCheckpoint.KEY}:${cpoint.id}"]}">
                                        <i class="icon arrow up"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                </g:else>
                                <g:if test="${ti < cpoints.size()-1}">
                                    <g:link class="ui icon button blue compact la-modern-button" controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"moveDown:${WfCheckpoint.KEY}:${cpoint.id}"]}">
                                        <i class="icon arrow down"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <div class="ui icon button compact la-hidden"><i class="coffee icon"></i></div>
                                </g:else>
                            </g:if>

                            <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
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

        <div class="ui vertical segment">
            <div class="ui grid">
                <div class="row">
                    <div class="one wide column wf-centered"></div>
                    <div class="ten wide column"></div>
                    <div class="two wide column wf-centered"></div>
                    <div class="three wide column wf-centered">
                        <g:link class="ui icon button blue compact la-modern-button" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"add:${WfChecklist.KEY}:${clist.id}"]}">
                            <i class="icon plus"></i>
                        </g:link>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <style>
        .ui.grid .row               { padding-top: 0.35rem; padding-bottom: 0.35rem; }
        .ui.grid .row:first-of-type { padding-top: 1.2rem; }
        .ui.grid .row:last-of-type  { padding-bottom: 1.2rem; }
    </style>
</g:if>

%{--<laser:script file="${this.getGroovyPageFileName()}">--}%
%{--    docs.init('.workflow-details');--}%
%{--</laser:script>--}%