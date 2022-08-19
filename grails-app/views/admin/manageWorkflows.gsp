<%@ page import="de.laser.utils.DateUtils; de.laser.workflow.WorkflowHelper; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>

<laser:htmlStart message="menu.admin.manageWorkflows" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="${message(code:'menu.admin.manageWorkflows')}" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.manageWorkflows" type="Workflow"/>

<g:set var="tmplTab" value="${tab ?: 'prototypes'}" />

<div class="ui secondary stackable pointing tabular la-tab-with-js menu">
    <a data-tab="workflows" class="item <% if (tmplTab == 'workflows') { print 'active' } %>">${message(code:'workflow.plural')}</a>
    <a data-tab="prototypes" class="item <% if (tmplTab == 'prototypes') { print 'active' } %>">${message(code:'default.prototype.plural')}</a>
    <a data-tab="templates" class="item <% if (tmplTab == 'templates') { print 'active' } %>">Templates</a>
    <a data-tab="help" class="item <% if (tmplTab == 'help') { print 'active' } %>">?</a>
</div>

<div data-tab="workflows" class="ui bottom attached tab <% if (tmplTab == 'workflows') { print 'active' } %>" style="margin-top:2em;">

    <ui:msg class="info" noClose="true">
        <div class="ui list">
            <div class="item">
                <span>
                    <i class="icon circle large"></i>
                    <strong>${message(code: 'workflow.task.label')}</strong> &rArr; ( ${message(code: 'workflow.condition.label')} )
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.priority.label')}:</strong>
                    &nbsp;&nbsp;
                    <i class="icon circle large"></i>Normal
                    <i class="icon arrow circle up large"></i>Wichtig
                    <i class="icon arrow circle down large"></i>Optional
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.status.label')}:</strong>
                    &nbsp;&nbsp;
                    <i class="icon sc_darkgrey circle large"></i>Offen
                    <i class="icon green circle large"></i>Erledigt
                    <i class="icon orange circle large"></i>Abgebrochen
                </span>
            </div>
        </div>
    </ui:msg>

    <g:if test="${key == WfWorkflow.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>
    <g:elseif test="${key == WfTask.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
    </g:elseif>
    <g:elseif test="${key == WfCondition.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
    </g:elseif>

    <g:each in="${WfWorkflow.executeQuery('select wfw from WfWorkflow wfw order by wfw.id desc')}" var="wf">

        <g:set var="wfInfo" value="${wf.getInfo()}" />

        <div class="ui segment attached top">
            <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i>

            <g:if test="${wf.status == RDStore.WF_WORKFLOW_STATUS_DONE}">
                <g:if test="${wfInfo.tasksImportantBlocking}">
                    <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.blockingTasks.important')}">
                        <i class="ui icon red exclamation triangle"></i>
                    </span>
                </g:if>
                <g:elseif test="${wfInfo.tasksNormalBlocking}">
                    <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'workflow.blockingTasks.normal')}">
                        <i class="ui icon red exclamation triangle"></i>
                    </span>
                </g:elseif>
            </g:if>

            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY + ':' + wf.id, tab: 'workflows']}">
                <strong>${wf.title}</strong>
            </g:link>
        </div>

        <g:set var="tasks" value="${wf.getSequence()}" />
        <g:if test="${tasks}">

            <div class="ui segment attached">
                <div class="ui mini steps">
                <g:each in="${tasks}" var="task">
                <div class="step">
                    <g:if test="${! task.child}">
                        <div class="content">
                            <div class="title">

                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + task.id, tab: 'workflows']}">
                                    <i class="icon large ${WorkflowHelper.getCssColorByStatus(task.status)} ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                    ${task.title}
                                </g:link>
                                <g:if test="${task.condition}">
                                    &rArr; ( <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + task.condition.id, tab: 'workflows']}">
                                        ${task.condition.title}
                                    </g:link> )
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <g:set var="children" value="${task.child.getSequence()}" />
                        <g:if test="${children}">
                            <div class="ui mini vertical steps" style="width: 100% !important;">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + task.id, tab: 'workflows']}">
                                                <i class="icon large ${WorkflowHelper.getCssColorByStatus(task.status)} ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                                ${task.title}
                                            </g:link>
                                            <g:if test="${task.condition}">
                                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + task.condition.id, tab: 'workflows']}">
                                                    ${task.condition.title}
                                                </g:link>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                                <g:each in="${children}" var="child" status="ci">
                                    <div class="step">
                                        <div class="content">
                                            <div class="title">
                                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + child.id, tab: 'workflows']}">
                                                    <i class="icon large ${WorkflowHelper.getCssColorByStatus(child.status)} ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                                    ${child.title}
                                                </g:link>
                                                <g:if test="${child.condition}">
                                                    - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + child.condition.id, tab: 'workflows']}">
                                                        ${child.condition.title}
                                                    </g:link>
                                                </g:if>
                                            </div>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </g:if>
                    </g:else>
                </div>
                </g:each>
                </div>
            </div>
        </g:if>

        <div class="ui segment attached bottom" style="background-color: #f9fafb;">

            <div class="la-flexbox">
                <i class="icon clipboard la-list-icon"></i>
                <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                    ${wf.subscription}
                    <g:if test="${wf.subscription.startDate || wf.subscription.endDate}">
                        (${wf.subscription.startDate ? DateUtils.getLocalizedSDF_noTime().format(wf.subscription.startDate) : ''} -
                        ${wf.subscription.endDate ? DateUtils.getLocalizedSDF_noTime().format(wf.subscription.endDate) : ''})
                    </g:if>
                </g:link>
            </div>
            <div class="la-flexbox">
                <i class="icon university la-list-icon"></i>
                <g:link controller="organisation" action="show" params="${[id: wf.owner.id]}">
                    ${wf.owner}
                </g:link>
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
                    controller="subscription" action="workflows" id="${wf.subscription.id}"
                    params="${[info: 'subscription:' + wf.subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                <i class="icon edit"></i>
            </g:link>

            <br />
            <span>
                Zuletzt bearbeitet: ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}
                - Erstellt: ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                <span class="${wf.prototypeVersion == wf.getPrototype().prototypeVersion ? '' : 'sc_darkgrey'}">
                    - Version: ${wf.prototypeVersion}
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
                <span class="ui brown circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)}
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
        ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} <ui:totalNumber total="${WfWorkflowPrototype.count()}"/>
    </p>

    <g:if test="${key == WfWorkflowPrototype.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th class="seven wide">${message(code:'workflow.label')}</th>
                <th class="five wide">${message(code:'workflow.task.label')}</th>
                <th class="two wide">Zustand</th>
                <th class="one wide">${message(code:'default.version.label')}</th>
                <th class="one wide"></th>
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
                        ${wfp.state?.getI10n('value')}
                    </td>
                    <td>
                        ${wfp.prototypeVersion}
                    </td>
                    <td class="x">
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
                        <g:if test="${wfp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                            <g:link class="ui green icon small button tmpJSPrompt la-modern-button" controller="admin" action="manageWorkflows" params="${[cmd: "instantiate:${WfWorkflowPrototype.KEY}:${wfp.id}", tab: 'prototypes']}"><i class="step forward icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfWorkflowPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} erstellen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} <ui:totalNumber total="${WfTaskPrototype.count()}"/>
    </p>

    <g:if test="${key == WfTaskPrototype.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
            <tr>
                <th class="seven wide">${message(code:'workflow.task.label')}</th>
                <th class="two wide">${message(code:'workflow.condition.label')}</th>
                <th class="one wide">Priorität</th>
                <th class="one wide">&uarr;&uarr;&uarr;</th>
                %{-- <th>${message(code:'default.type.label')}</th> --}%
                <th class="one wide">&rarr;&rarr;</th>
                <th class="one wide">&darr;&darr;</th>
                <th class="one wide">&larr;</th>
                <th class="one wide">&uarr;</th>
                <th class="one wide"></th>
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
                    <td>
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
                    <td>
                        <g:if test="${tp.next}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.next.title} (${tp.next.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.next.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.next.id}">${tpIdTable[tp.next.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${tp.child}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.child.title} (${tp.child.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.child.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.child.id}">${tpIdTable[tp.child.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.findByNext(tp)}" var="prev">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${prev.title} (${prev.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + prev.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${prev.id}">${tpIdTable[prev.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.findByChild(tp)}" var="sup">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${sup.title} (${sup.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + sup.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${sup.id}">${tpIdTable[sup.id] ?: '?'}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td class="x">
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

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfTaskPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} erstellen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} <ui:totalNumber total="${WfConditionPrototype.count()}"/>
    </p>

    <g:if test="${key == WfConditionPrototype.KEY}">
        <laser:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
    </g:if>

    <table class="ui celled la-js-responsive-table la-table compact table">
        <thead>
        <tr>
            <th class="seven wide">${message(code:'workflow.condition.label')}</th>
            <th class="two wide">${message(code:'workflow.task.label')}</th>
            <th class="six wide">${message(code:'default.type.label')}</th>
            <th class="one wide"></th>
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
                    <td class="x">
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

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfConditionPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} erstellen</g:link>

</div><!-- .prototypes -->

<div data-tab="templates" class="ui bottom attached tab <% if (tmplTab == 'templates') { print 'active' } %>" style="margin-top:2em;">

    <ui:msg class="info" noClose="true">
        <div class="ui list">
            <div class="item">
                <span class="ui brown circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)}
                , <strong>Zustand:</strong>
                <span><i class="icon check circle green"></i>Ready to use </span>
                <span><i class="icon minus circle red"></i>Test only </span>
            </div>
            <div class="item">
                <span class="ui blue circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}
                , <strong>Priorität:</strong>
                <span class="ui blue circular label"><i class="icon circle"></i>Normal&nbsp;</span>
                <span class="ui blue circular label"><i class="icon arrow circle up"></i>Wichtig&nbsp;</span>
                <span class="ui blue circular label"><i class="icon arrow circle down"></i>Optional&nbsp;</span>
            </div>
            <div class="item">
                <span class="ui teal circular label">id</span> &nbsp; ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}
            </div>
        </div>
    </ui:msg>

    <g:set var="workflowTemplates" value="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by wfwp.id desc')}" />

    <p class="ui header">
        ${message(code: 'workflow.template.plural')} <ui:totalNumber total="${workflowTemplates.size()}"/>
    </p>

    <g:each in="${workflowTemplates}" var="wfwp">

        <p style="padding-left:10px">
            <g:if test="${wfwp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                <i class="icon check circle large green"></i>
            </g:if>
            <g:else>
                <i class="icon minus circle large red"></i>
            </g:else>
            <strong>${wfwp.title}</strong>
            <span class="sc_grey">(${message(code:'default.version.label')} ${wfwp.prototypeVersion})</span>
        </p>

        <g:set var="tasks" value="${wfwp.getSequence()}" />

        <div class="ui mini steps">
            <div class="step">
                <div class="content">
                    <div class="title">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wfwp.id, tab: 'prototypes']}">
                            <span class="ui brown circular label" data-wfwp="${wfwp.id}">${wfpIdTable[wfwp.id] ?: '?'}</span>
                        </g:link>
                    </div>
                </div>
            </div>

            <g:each in="${tasks}" var="wftp">
                <div class="step">
                    <g:if test="${! wftp.child}">
                        <div class="content">
                            <div class="title">
                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.title} (${wftp.priority.getI10n('value')})">
                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + wftp.id, tab: 'prototypes']}">
                                        <span class="ui blue circular label" data-wftp="${wftp.id}">
                                            <i class="icon large ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
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
                    </g:if>
                    <g:else>
                        <g:set var="children" value="${wftp.child.getSequence()}" />
                        <g:if test="${children}">
                            <div class="ui mini vertical steps" style="width: 100% !important;">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.title} (${wftp.priority.getI10n('value')})">
                                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' +  wftp.id, tab: 'prototypes']}">
                                                    <span class="ui blue circular label" data-wftp="${wftp.id}">
                                                        <i class="icon large ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
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
                                <g:each in="${children}" var="child" status="ci">
                                    <div class="step">
                                        <div class="content">
                                            <div class="title">
                                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${child.title} (${child.priority.getI10n('value')})">
                                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + child.id, tab: 'prototypes']}">
                                                        <span class="ui blue circular label" data-wftp="${child.id}">
                                                            <i class="icon large ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                                            ${tpIdTable[child.id] ?: '?'}
                                                        </span>
                                                    </g:link>
                                                </span>
                                                <g:if test="${child.condition}">
                                                    <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${child.condition.title} (${child.condition.getTypeAsRefdataValue().getI10n('value')})">
                                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + child.condition.id, tab: 'prototypes']}">
                                                            <span class="ui teal circular label" data-wfcp="${child.condition.id}">${cpIdTable[child.condition.id] ?: '?'}</span>
                                                        </g:link>
                                                    </span>
                                                </g:if>
                                            </div>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </g:if>
                    </g:else>
                </div>
            </g:each>
        </div>

    </g:each>

</div><!-- .templates -->

<div data-tab="help" class="ui bottom attached tab <% if (tmplTab == 'help') { print 'active' } %>" style="margin-top:2em;">

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
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });

    $('.tmpJSPrompt').on('click', function(e) {
        e.preventDefault();
        var subId = prompt('Subscription-ID ?')
        if (subId) {
            window.location = $(this).attr('href') + '&subId=' + subId;
        }
    });
</laser:script>

<laser:htmlEnd />
