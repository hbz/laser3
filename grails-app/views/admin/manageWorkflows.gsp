<%@ page import="de.laser.utils.DateUtils; de.laser.License; de.laser.Subscription; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowOldService" %>

<laser:htmlStart message="menu.admin.manageWorkflows" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="${message(code:'menu.admin.manageWorkflows')}" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.manageWorkflows" type="admin"/>

<g:set var="tmplTab" value="${tab ?: 'prototypes'}" />

<div class="ui secondary stackable pointing tabular la-tab-with-js menu">
    <a data-tab="prototypes" class="item <% if (tmplTab == 'prototypes') { print 'active' } %>">${message(code:'default.prototype.plural')}</a>
    <a data-tab="templates" class="item <% if (tmplTab == 'templates') { print 'active' } %>">${message(code: 'workflow.template.plural')}</a>
    <a data-tab="workflows" class="item <% if (tmplTab == 'workflows') { print 'active' } %>">${message(code:'workflow.plural')}</a>
    <a data-tab="help" class="item <% if (tmplTab == 'help') { print 'active' } %>">?</a>
</div>

<div data-tab="workflows" class="ui bottom attached tab <% if (tmplTab == 'workflows') { print 'active' } %>" style="margin-top:2em;">

    <ui:msg class="info" noClose="true">
        <div class="ui list">
            <div class="item">
                <span>
                    <i class="icon circle"></i>
                    <strong>${message(code: 'workflow.task.label')}</strong> &rArr; ( ${message(code: 'workflow.condition.label')} )
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.priority.label')}:</strong>
                    &nbsp;
                    <i class="icon circle"></i>Normal
                    <i class="icon arrow circle up"></i>Wichtig
                    <i class="icon arrow circle down"></i>Optional
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.status.label')}:</strong>
                    &nbsp;
                    <i class="icon sc_darkgrey circle"></i>Offen
                    <i class="icon green circle"></i>Erledigt
                    <i class="icon orange circle"></i>Abgebrochen
                </span>
            </div>
        </div>
    </ui:msg>

    <g:set var="currentWorkflows" value="${workflowOldService.sortByLastUpdated( WfWorkflow.findAll() )}" />
    <p class="ui header">
        * Workflows insgesamt: <ui:totalNumber total="${currentWorkflows.size()}"/>
    </p>
    <br />

    <g:if test="${key == WfWorkflow.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>
    <g:elseif test="${key == WfTask.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
    </g:elseif>
    <g:elseif test="${key == WfCondition.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
    </g:elseif>

    <g:each in="${currentWorkflows}" var="wf">

        <g:set var="wfInfo" value="${wf.getInfo()}" />

        <div class="ui segment attached top">
            <uiWorkflow:statusIcon workflow="${wf}" size="large" />

            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY + ':' + wf.id, tab: 'workflows']}">
                <strong>${wf.title}</strong>
            </g:link>
        </div>

        <g:set var="tasks" value="${wf.getSequence()}" />
        <g:if test="${tasks}">

            <div class="ui segment attached">
                <div class="ui relaxed divided list">
                <g:each in="${tasks}" var="task">
                <div class="item">
                        <div class="content">
                            <div class="title">

                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + task.id, tab: 'workflows']}">
                                    <i class="icon ${WorkflowHelper.getCssColorByStatus(task.status)} ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                    ${task.title}
                                </g:link>
                                <g:if test="${task.condition}">
                                    &rArr; ( <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + task.condition.id, tab: 'workflows']}">
                                        ${task.condition.title}
                                    </g:link> )
                                </g:if>
                            </div>
                        </div>
                </div>
                </g:each>
                </div>
            </div>
        </g:if>

        <div class="ui segment attached bottom" style="background-color: #f9fafb;">

            <div class="la-flexbox">
                <i class="icon ${wfInfo.targetIcon} la-list-icon"></i>
                <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">
                    ${wfInfo.targetName}
                    <g:if test="${wfInfo.target instanceof Subscription || wfInfo.target instanceof License}">
                        <g:if test="${wfInfo.target.startDate || wfInfo.target.endDate}">
                            (${wfInfo.target.startDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.startDate) : ''} -
                            ${wfInfo.target.endDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.endDate) : ''})
                        </g:if>
                    </g:if>
                </g:link>
            </div>
            <div class="la-flexbox">
                <i class="icon university la-list-icon"></i>
                <g:link controller="organisation" action="show" params="${[id: wf.owner.id]}">
                    ${wf.owner}
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

            <g:link class="ui small icon negative button right floated la-modern-button js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                    data-confirm-term-how="delete"
                    controller="admin" action="manageWorkflows" params="${[cmd:"delete:${WfWorkflow.KEY}:${wf.id}", tab:'workflows']}"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                <i class="trash alternate outline icon"></i>
            </g:link>

            <g:link class="ui small icon blue button right floated la-modern-button"
                    controller="${wfInfo.targetController}" action="workflows" id="${wfInfo.target.id}"
                    params="${[info: '' + wfInfo.target.class.name + ':' + wfInfo.target.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                <i class="icon edit"></i>
            </g:link>

            <g:link class="ui small icon blue button right floated la-modern-button wfModalLink"
                    controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY + ':' + wf.id, tab: 'workflows']}">
                <i class="icon expand"></i>
            </g:link>

            <br />
            <span>
                ${message(code:'default.lastUpdated.label')}: ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}
                - ${message(code:'default.dateCreated.label')}: ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                <span class="${wf.prototypeVariant == wf.getPrototype().variant ? '' : 'sc_darkgrey'}">
                    - Version: ${wf.prototypeVariant}
                </span>
            </span>
        </div>

        <br />
    </g:each>

</div><!-- .workflows -->

<div data-tab="prototypes" class="ui bottom attached tab <% if (tmplTab == 'prototypes') { print 'active' } %>" style="margin-top:2em;">

    <ui:msg class="info" noClose="true">
        <div class="ui list">
            <div class="item">
                <span class="ui brown circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} ,
                <strong>${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_STATE).getI10n('desc')}:</strong>
                <span><i class="icon check circle green"></i>${RDStore.WF_WORKFLOW_STATE_ACTIVE.getI10n('value')}</span>
                <span><i class="icon minus circle red"></i>${RDStore.WF_WORKFLOW_STATE_TEST.getI10n('value')}</span>
            </div>
            <div class="item">
                <span class="ui blue circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}
            </div>
            <div class="item">
                <span class="ui teal circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}
            </div>
        </div>
    </ui:msg>

    <p class="ui header">
        * Elemente für Workflow-${message(code: 'workflow.template.plural')}: <ui:totalNumber total="${WfWorkflowPrototype.count() + WfTaskPrototype.count() + WfConditionPrototype.count()}"/>
    </p>
    <br />

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} <ui:totalNumber total="${WfWorkflowPrototype.count()}"/>
    </p>

    <g:if test="${key == WfWorkflowPrototype.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th scope="col" class="eight wide">${message(code:'workflow.label')}</th>
                <th scope="col" class="two wide">${message(code:'workflow.task.label')}</th>
                <th scope="col" class="four wide">Details</th>
                <th scope="col" class="one wide"></th>
                <th scope="col" class="one wide"></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${wfpList}" var="wfp">
                <tr data-wfwp="${wfp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wfp.id, tab: 'prototypes']}">
                            <span class="ui brown circular label" data-wfwp="${wfp.id}">${wfpIdTable[wfp.id] ?: '?'}</span>
                            ${wfp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${wfp.task}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wfp.task.title} (${wfp.task.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + wfp.task.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${wfp.task.id}">${tpIdTable[wfp.task.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        ${wfp.targetType.getI10n('value')} <br />
                        ${wfp.targetRole.getI10n('value')} <br />
                        ${wfp.variant}
                    </td>
                    <td>
                        <g:if test="${wfp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                            <i class="icon check circle green"></i>
                        </g:if>
                        <g:else>
                            <i class="icon minus circle red"></i>
                        </g:else>
                        <g:if test="${wfp.hasCircularReferences()}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.error.circularReferences')}">
                                <i class="icon exclamation triangle orange"></i>
                            </span>
                        </g:if>
                    </td>
                    <td class="center aligned">
                        <g:if test="${! wfp.inUse()}">
                            <g:link class="ui small icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [wfp.title])}"
                                    data-confirm-term-how="delete"
                                    controller="admin" action="manageWorkflows" params="${[cmd:"delete:${WfWorkflowPrototype.KEY}:${wfp.id}", tab:'prototypes']}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfWorkflowPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} anlegen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} <ui:totalNumber total="${WfTaskPrototype.count()}"/>
    </p>

    <g:if test="${key == WfTaskPrototype.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th scope="col" class="eight wide">${message(code:'workflow.task.label')}</th>
                <th scope="col" class="two wide">${message(code:'workflow.condition.label')}</th>
                <th scope="col" class="two wide">Priorität</th>
                <th scope="col" class="one wide center aligned"><i class="icon angle up"></i></th>
                %{-- <th>${message(code:'default.type.label')}</th> --}%
                <th scope="col" class="one wide center aligned"><i class="icon angle right"></i></th>
%{--                <th class="one wide">&darr;&darr;</th>--}%
                <th scope="col" class="one wide center aligned"><i class="icon angle left"></i></th>
%{--                <th class="one wide">&uarr;</th>--}%
                <th scope="col" class="one wide"></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${tpList}" var="tp">
                <tr data-wftp="${tp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.id, tab: 'prototypes']}">
                            <span class="ui blue circular label" data-wftp="${tp.id}">${tpIdTable[tp.id] ?: '?'}</span>
                            ${tp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${tp.condition}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.condition.title} (${tp.condition.getTypeAsRefdataValue().getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + tp.condition.id, tab: 'prototypes']}">
                                    <span class="ui teal circular label" data-wfcp="${tp.condition.id}">${cpIdTable[tp.condition.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        ${tp.priority?.getI10n('value')}
                    </td>
                    <td class="center aligned">
                        <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where wp.task = :tp order by id', [tp: tp])}" var="wfp">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wfp.title}">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wfp.id, tab: 'prototypes']}">
                                    <span class="ui brown circular label" data-wfwp="${wfp.id}">${wfpIdTable[wfp.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    %{-- <td>
                        ${tp.type?.getI10n('value')}
                    </td> --}%
                    <td class="center aligned">
                        <g:if test="${tp.next}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.next.title} (${tp.next.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.next.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.next.id}">${tpIdTable[tp.next.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td class="center aligned">
                        <g:each in="${WfTaskPrototype.findAllByNext(tp, [sort: 'id'])}" var="prev">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${prev.title} (${prev.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + prev.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${prev.id}">${tpIdTable[prev.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td class="center aligned">
                        <g:if test="${! tp.inUse()}">
                            <g:link class="ui small icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [tp.title])}"
                                    data-confirm-term-how="delete"
                                    controller="admin" action="manageWorkflows" params="${[cmd:"delete:${WfTaskPrototype.KEY}:${tp.id}", tab:'prototypes']}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfTaskPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} anlegen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} <ui:totalNumber total="${WfConditionPrototype.count()}"/>
    </p>

    <g:if test="${key == WfConditionPrototype.KEY}">
        <laser:render template="/templates/workflow/old/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
        <tr>
            <th scope="col" class="eight wide">${message(code:'workflow.condition.label')}</th>
            <th scope="col" class="two wide">${message(code:'workflow.task.label')}</th>
            <th scope="col" class="five wide">${message(code:'default.type.label')}</th>
            <th scope="col" class="one wide"></th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${cpList}" var="cp">
                <tr data-wfcp="${cp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + cp.id, tab: 'prototypes']}">
                            <span class="ui teal circular label" data-wfcp="${cp.id}">${cpIdTable[cp.id] ?: '?'}</span>
                            ${cp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.executeQuery('select wp from WfTaskPrototype wp where wp.condition = :cp order by id', [cp: cp])}" var="tp">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.title} (${tp.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.id}">${tpIdTable[tp.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td>
                        ${cp.getTypeAsRefdataValue().getI10n('value')}
                    </td>
                    <td class="center aligned">
                        <g:if test="${! cp.inUse()}">
                            <g:link class="ui small icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [cp.title])}"
                                    data-confirm-term-how="delete"
                                    controller="admin" action="manageWorkflows" params="${[cmd:"delete:${WfConditionPrototype.KEY}:${cp.id}", tab:'prototypes']}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfConditionPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} anlegen</g:link>

</div><!-- .prototypes -->

<div data-tab="templates" class="ui bottom attached tab <% if (tmplTab == 'templates') { print 'active' } %>" style="margin-top:2em;">

    <ui:msg class="info" noClose="true">
        <div class="ui list">
            <div class="item">
                <span class="ui brown circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} ,
                <strong>${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_STATE).getI10n('desc')}:</strong>
                <span><i class="icon check circle green"></i>${RDStore.WF_WORKFLOW_STATE_ACTIVE.getI10n('value')}</span>
                <span><i class="icon minus circle red"></i>${RDStore.WF_WORKFLOW_STATE_TEST.getI10n('value')}</span>
            </div>
            <div class="item">
                <span class="ui blue circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} ,
                <strong>Priorität:</strong>
                <span><i class="icon circle"></i>Normal&nbsp;</span>
                <span><i class="icon arrow circle up"></i>Wichtig&nbsp;</span>
                <span><i class="icon arrow circle down"></i>Optional&nbsp;</span>
            </div>
            <div class="item">
                <span class="ui teal circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}
            </div>
        </div>
    </ui:msg>

    <g:set var="workflowTemplates" value="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by wfwp.id desc')}" />

    <p class="ui header">
        * Workflow-${message(code: 'workflow.template.plural')}: <ui:totalNumber total="${workflowTemplates.size()}"/>
    </p>
    <br />

    <g:each in="${workflowTemplates.sort{ a,b -> b.getInfo().lastUpdated <=> a.getInfo().lastUpdated }}" var="wfwp">

        <p style="padding-left:10px">
            <g:if test="${wfwp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                <i class="icon check circle green"></i>
            </g:if>
            <g:else>
                <i class="icon minus circle red"></i>
            </g:else>
            <g:if test="${wfwp.hasCircularReferences()}">
                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.error.circularReferences')}">
                    <i class="icon exclamation triangle orange"></i>
                </span>
            </g:if>
            <strong>${wfwp.title}</strong>
            <span class="sc_grey" style="padding-left:0.5em;">
                (<g:formatDate date="${wfwp.getInfo().lastUpdated}" format="${message(code:'default.date.format.notime')}"/>)
%{--                <g:if test="${wfwp.targetType == RDStore.WF_WORKFLOW_TARGET_TYPE_LICENSE}">--}%
%{--                    <i class="icon balance"></i>--}%
%{--                </g:if>--}%
%{--                <g:elseif test="${wfwp.targetType == RDStore.WF_WORKFLOW_TARGET_TYPE_SUBSCRIPTION}">--}%
%{--                    <i class="icon clipboard outline"></i>--}%
%{--                </g:elseif>--}%
%{--                <g:elseif test="${wfwp.targetType in [RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER, RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION]}">--}%
%{--                    <i class="icon university"></i>--}%
%{--                </g:elseif>--}%
%{--                - ${message(code:'default.version.label')} ${wfwp.variant} --}%

            </span>

        </p>

        <g:set var="tasks" value="${wfwp.getSequence()}" />

        <div class="ui mini steps">
            <div class="step">
                <div class="content">
                    <div class="title">
                        <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wfwp.title} (${wfwp.targetType.getI10n('value')} - ${wfwp.targetRole.getI10n('value')} - ${wfwp.variant})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wfwp.id, tab: 'prototypes']}">
                                <span class="ui brown circular label" data-wfwp="${wfwp.id}">${wfpIdTable[wfwp.id] ?: '?'}</span>
                            </g:link>
                        </span>
                    </div>
                </div>
            </div>

            <g:each in="${tasks}" var="wftp">
                <div class="step">
                        <div class="content">
                            <div class="title">
                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.title} (${wftp.priority.getI10n('value')})">
                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + wftp.id, tab: 'prototypes']}">
                                        <span class="ui blue circular label" data-wftp="${wftp.id}">
                                            <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
                                            ${tpIdTable[wftp.id] ?: '?'}
                                        </span>
                                    </g:link>
                                </span>
                                <g:if test="${wftp.condition}">
                                    <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.condition.title} (${wftp.condition.getTypeAsRefdataValue().getI10n('value')})">
                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + wftp.condition.id, tab: 'prototypes']}">
                                            <span class="ui teal circular label" data-wfcp="${wftp.condition.id}">${cpIdTable[wftp.condition.id] ?: '?'}</span>
                                        </g:link>
                                    </span>
                                </g:if>
                            </div>
                        </div>
                </div>
            </g:each>
        </div>

    </g:each>

</div><!-- .templates -->

<div data-tab="help" class="ui bottom attached tab <% if (tmplTab == 'help') { print 'active' } %>" style="margin-top:2em;">

    <div class="ui icon message info">
        <i class="icon info"></i>
        <div class="content">
            <div class="header">Hinweis zur Version 3.0</div>
            <p>Das Anlegen von verschachtelten Aufgaben (Childs) wurde vorerst deaktiviert.</p>
        </div>
    </div>
    <div class="ui segment">
        <div class="field">
            <div style="text-align:center; padding:2em 0">
                <asset:image src="help/workflows.png" absolute="true" style="width:85%" />
            </div>
        </div>
    </div><!-- .tab -->

</div><!-- .help -->

<div id="wfModal" class="ui modal"></div>


<laser:script file="${this.getGroovyPageFileName()}">
    $('.secondary.menu > a').tab();

    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'));
        func();
    });
</laser:script>

<laser:htmlEnd />
