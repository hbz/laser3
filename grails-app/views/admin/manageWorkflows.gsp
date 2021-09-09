<%@ page import="de.laser.helper.DateUtils; de.laser.helper.WorkflowHelper; de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.manageWorkflows')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="${message(code:'menu.admin.manageWorkflows')}" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-noMargin-top"><semui:headerTitleIcon type="Workflow"/>
    ${message(code:'menu.admin.manageWorkflows')}
</h1>

<g:set var="tmplTab" value="${tab ?: 'prototypes'}" />

<div class="ui secondary stackable pointing tabular menu">
    <a data-tab="workflows" class="item <% if (tmplTab == 'workflows') { print 'active' } %>">${message(code:'workflow.plural')}</a>
    %{-- <a data-tab="templates" class="item <% if (tmplTab == 'templates') { print 'active' } %>">Templates</a> --}%
    <a data-tab="prototypes" class="item <% if (tmplTab == 'prototypes') { print 'active' } %>">${message(code:'default.prototype.plural')}</a>
</div>

<div data-tab="workflows" class="ui bottom attached tab <% if (tmplTab == 'workflows') { print 'active' } %>" style="margin-top:2em;">

    <div class="ui info message">
        <div class="ui list">
            <div class="item">
                <span>
                    <i class="icon circle large"></i>
                    <strong>${message(code: 'workflow.task.label')}</strong> ( ${message(code: 'workflow.condition.label')} )
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.priority.label')}:</strong>
                    <i class="icon circle large"></i>Normal -
                    <i class="icon arrow alternate circle up large"></i>Wichtig -
                    <i class="icon arrow alternate circle down large"></i>Optional
                </span>
            </div>
            <div class="item">
                <span>
                    <strong>${message(code:'default.status.label')}:</strong>
                    <i class="icon la-light-grey circle large"></i>Offen -
                    <i class="icon green circle large"></i>Erledigt -
                    <i class="icon orange circle large"></i>Abgebrochen
                </span>
            </div>
        </div>
    </div>

    <g:if test="${key == WfWorkflow.KEY}">
        <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>

    <g:each in="${WfWorkflow.executeQuery('select wfw from WfWorkflow wfw order by wfw.id desc')}" var="wfw">

        <div class="ui segment attached top">
            <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wfw.status)}"></i>
            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY + ':' + wfw.id, tab: 'workflows']}">
                <strong>${wfw.title}</strong>
            </g:link>
        </div>

        <div class="ui segment attached">
        <div class="ui mini steps">
            <g:set var="tasks" value="${wfw.getSequence()}" />
            <g:each in="${tasks}" var="wft">
                <div class="step">
                    <g:if test="${! wft.child}">
                        <div class="content">
                            <div class="title">

                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + wft.id, tab: 'workflows']}">
                                    <i class="icon large ${WorkflowHelper.getCssColorByStatus(wft.status)} ${WorkflowHelper.getCssIconByTaskPriority(wft.priority)}"></i>
                                    ${wft.title}
                                </g:link>
                                <g:if test="${wft.condition}">
                                    ( <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + wft.condition.id, tab: 'workflows']}">
                                        ${wft.condition.title}
                                    </g:link> )
                                </g:if>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <g:set var="children" value="${wft.child.getSequence()}" />
                        <g:if test="${children}">
                            <div class="ui mini vertical steps" style="width: 100% !important;">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + wft.id, tab: 'workflows']}">
                                                <i class="icon large ${WorkflowHelper.getCssColorByStatus(wft.status)} ${WorkflowHelper.getCssIconByTaskPriority(wft.priority)}"></i>
                                                ${wft.title}
                                            </g:link>
                                            <g:if test="${wft.condition}">
                                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + wft.condition.id, tab: 'workflows']}">
                                                    ${wft.condition.title}
                                                </g:link>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                                <g:each in="${children}" var="ch" status="ci">
                                    <div class="step">
                                        <div class="content">
                                            <div class="title">
                                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY + ':' + ch.id, tab: 'workflows']}">
                                                    <i class="icon large ${WorkflowHelper.getCssColorByStatus(ch.status)} ${WorkflowHelper.getCssIconByTaskPriority(ch.priority)}"></i>
                                                    ${ch.title}
                                                </g:link>
                                                <g:if test="${ch.condition}">
                                                    - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY + ':' + ch.condition.id, tab: 'workflows']}">
                                                        ${ch.condition.title}
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

        <div class="ui segment attached bottom" style="background-color: #f9fafb;">

            <div class="la-flexbox">
                <i class="icon tasks la-list-icon"></i>
                <g:link controller="subscription" action="workflows" id="${wfw.subscription.id}" params="${[info: 'subscription:' + wfw.subscription.id + ':' + WfWorkflow.KEY + ':' + wfw.id]}">
                    <strong>${wfw.title}</strong>
                </g:link>
            </div>
            <div class="la-flexbox">
                <i class="icon clipboard la-list-icon"></i>
                <g:link controller="subscription" action="show" params="${[id: wfw.subscription.id]}">
                    ${wfw.subscription}
                    <g:if test="${wfw.subscription.startDate || wfw.subscription.endDate}">
                        (${wfw.subscription.startDate ? DateUtils.getSDF_NoTime().format(wfw.subscription.startDate) : ''} -
                        ${wfw.subscription.endDate ? DateUtils.getSDF_NoTime().format(wfw.subscription.endDate) : ''})
                    </g:if>
                </g:link>
            </div>
            <div class="la-flexbox">
                <i class="icon university la-list-icon"></i>
                <g:link controller="organisation" action="show" params="${[id: wfw.owner.id]}">
                    ${wfw.owner}
                </g:link>
            </div>

            <g:link class="ui red icon small button right floated" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfWorkflow.KEY}:${wfw.id}", tab: 'workflows']}"><i class="trash alternate outline icon"></i></g:link>
            <br />

            <g:set var="wfInfo" value="${wfw.getInfo()}" />
            Bearbeitet: ${DateUtils.getSDF_NoTime().format(wfInfo.lastUpdated)} - Erstellt: ${DateUtils.getSDF_NoTime().format(wfw.dateCreated)}
        </div>

        <br />
    </g:each>

</div><!-- .workflows -->

%{-- <div data-tab="templates" class="ui bottom attached tab <% if (tmplTab == 'templates') { print 'active' } %>" style="margin-top:2em;"> --}%
<div data-tab="prototypes" class="ui bottom attached tab <% if (tmplTab == 'prototypes') { print 'active' } %>" style="margin-top:2em;">

    <div class="ui info message">
        <div class="ui list">
            <div class="item">
                <span class="ui pink circular label">id</span>
                &nbsp; ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)}
            </div>
            <div class="item">
                <span class="ui blue circular label">id</span>
                &nbsp; ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)}
                , Priorität:
                <span class="ui blue circular label"><i class="icon circle"></i>Normal&nbsp;</span>
                <span class="ui blue circular label"><i class="icon arrow alternate circle up"></i>Wichtig&nbsp;</span>
                <span class="ui blue circular label"><i class="icon arrow alternate circle down"></i>Optional&nbsp;</span>
            </div>
            <div class="item">
                <span class="ui teal circular label">id</span>
                &nbsp; ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)}
            </div>
        </div>
    </div>

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} <semui:totalNumber total="${WfWorkflowPrototype.findAll().size()}"/>
    </p>

    <table class="ui celled la-table compact table">
        <thead>
            <tr>
                <th>${message(code:'workflow.label')}</th>
                <th>${message(code:'workflow.task.label')} &darr;</th>
                <th>Zustand</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp order by id')}" var="wp">
                <tr data-wfwp="${wp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wp.id, tab: 'prototypes']}">
                            <span class="ui pink circular label" data-wfwp="${wp.id}">${wp.id}</span>
                            ${wp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${wp.task}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wp.task.title} (${wp.task.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + wp.task.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${wp.task.id}">${wp.task.id}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        ${wp.state?.getI10n('value')}
                    </td>
                    <td class="x">
                        <g:if test="${! wp.inUse()}">
                            <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfWorkflowPrototype.KEY}:${wp.id}", tab: 'prototypes']}"><i class="trash alternate outline icon"></i></g:link>
                        </g:if>
                        <g:if test="${wp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                            <g:link class="ui green icon small button tmpJSPrompt" controller="admin" action="manageWorkflows" params="${[cmd: "instantiate:${WfWorkflowPrototype.KEY}:${wp.id}", tab: 'prototypes']}"><i class="step forward icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:if test="${key == WfWorkflowPrototype.KEY}">
        <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
    </g:if>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfWorkflowPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} erstellen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} <semui:totalNumber total="${WfTaskPrototype.findAll().size()}"/>
    </p>

    <table class="ui celled la-table compact table">
        <thead>
            <tr>
                <th>${message(code:'workflow.task.label')}</th>
                <th>${message(code:'workflow.condition.label')} &darr;</th>
                <th>${message(code:'workflow.label')} &uarr;</th>
                %{-- <th>Typ</th> --}%
                <th>Nachfolger &rarr;</th>
                <th>Child &darr;</th>
                <th>Priorität</th>
                <th>Vorgänger &larr;</th>
                <th>Parent &uarr;</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp order by id')}" var="tp">
                <tr data-wftp="${tp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.id, tab: 'prototypes']}">
                            <span class="ui blue circular label" data-wftp="${tp.id}">${tp.id}</span>
                            ${tp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:if test="${tp.condition}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.condition.title} (${tp.condition.getTypeAsRefdataValue().getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + tp.condition.id, tab: 'prototypes']}">
                                    <span class="ui teal circular label" data-wfcp="${tp.condition.id}">${tp.condition.id}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where wp.task = :tp order by id', [tp: tp])}" var="wp">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wp.title}">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wp.id, tab: 'prototypes']}">
                                    <span class="ui pink circular label" data-wfwp="${wp.id}">${wp.id}</span>
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
                                    <span class="ui blue circular label" data-wftp="${tp.next.id}">${tp.next.id}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${tp.child}">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.child.title} (${tp.child.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.child.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.child.id}">${tp.child.id}</span>
                                </g:link>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        ${tp.priority?.getI10n('value')}
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.findByNext(tp)}" var="prev">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${prev.title} (${prev.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + prev.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${prev.id}">${prev.id}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.findByChild(tp)}" var="sup">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${sup.title} (${sup.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + sup.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${sup.id}">${sup.id}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td class="x">
                        <g:if test="${! tp.inUse()}">
                            <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfTaskPrototype.KEY}:${tp.id}", tab: 'prototypes']}"><i class="trash alternate outline icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:if test="${key == WfTaskPrototype.KEY}">
        <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
    </g:if>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfTaskPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} erstellen</g:link>

    <!-- -->

    <p class="ui header">
        ${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} <semui:totalNumber total="${WfConditionPrototype.findAll().size()}"/>
    </p>

    <table class="ui celled la-table compact table">
        <thead>
        <tr>
            <th>${message(code:'workflow.condition.label')}</th>
            <th>${message(code:'workflow.task.label')} &uarr;</th>
            <th>${message(code:'default.type.label')}</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${WfConditionPrototype.executeQuery('select cp from WfConditionPrototype cp order by id')}" var="cp">
                <tr data-wfcp="${cp.id}">
                    <td>
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + cp.id, tab: 'prototypes']}">
                            <span class="ui teal circular label" data-wfcp="${cp.id}">${cp.id}</span>
                            ${cp.title}
                        </g:link>
                    </td>
                    <td>
                        <g:each in="${WfTaskPrototype.executeQuery('select wp from WfTaskPrototype wp where wp.condition = :cp order by id', [cp: cp])}" var="tp">
                            <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${tp.title} (${tp.priority.getI10n('value')})">
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + tp.id, tab: 'prototypes']}">
                                    <span class="ui blue circular label" data-wftp="${tp.id}">${tp.id}</span>
                                </g:link>
                            </span>
                        </g:each>
                    </td>
                    <td>
                        ${cp.getTypeAsRefdataValue().getI10n('value')}
                    </td>
                    <td class="x">
                        <g:if test="${! cp.inUse()}">
                            <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfConditionPrototype.KEY}:${cp.id}", tab: 'prototypes']}"><i class="trash alternate outline icon"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:if test="${key == WfConditionPrototype.KEY}">
        <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
    </g:if>

    <g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfConditionPrototype.KEY, tab: 'prototypes']}">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} erstellen</g:link>

    <!-- .templates -->

    <br />
    <br />
    <div class="ui divider"></div>

    <g:set var="workflowTemplates" value="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by wfwp.id desc')}" />

    <p class="ui header">
        ${message(code: 'workflow.template.plural')} <semui:totalNumber total="${workflowTemplates.size()}"/>
    </p>

    <g:each in="${workflowTemplates}" var="wfwp">
        <p><strong>
            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY + ':' + wfwp.id, tab: 'prototypes']}">
                <span class="ui pink circular label" data-wfwp="${wfwp.id}">${wfwp.id}</span>
                ${wfwp.title}
            </g:link>
        </strong></p>

        <div class="ui mini steps">
            <g:set var="tasks" value="${wfwp.getSequence()}" />
            <g:each in="${tasks}" var="wftp">
                <div class="step">
                    <g:if test="${! wftp.child}">
                        <div class="content">
                            <div class="title">
                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.title} (${wftp.priority.getI10n('value')})">
                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + wftp.id, tab: 'prototypes']}">
                                        <span class="ui blue circular label" data-wftp="${wftp.id}">
                                            <i class="icon large ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
                                            ${wftp.id}
                                        </span>
                                    </g:link>
                                </span>
                                <g:if test="${wftp.condition}">
                                    <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.condition.title}">
                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + wftp.condition.id, tab: 'prototypes']}">
                                            <span class="ui teal circular label" data-wfcp="${wftp.condition.id}">${wftp.condition.id}</span>
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
                                                        ${wftp.id}
                                                    </span>
                                                </g:link>
                                            </span>
                                            <g:if test="${wftp.condition}">
                                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${wftp.condition.title}">
                                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + wftp.condition.id, tab: 'prototypes']}">
                                                        <span class="ui teal circular label" data-wfcp="${wftp.condition.id}">${wftp.condition.id}</span>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </div>
                                <g:each in="${children}" var="ch" status="ci">
                                    <div class="step">
                                        <div class="content">
                                            <div class="title">
                                                <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${ch.title} (${ch.priority.getI10n('value')})">
                                                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY + ':' + ch.id, tab: 'prototypes']}">
                                                        <span class="ui blue circular label" data-wftp="${ch.id}">
                                                            <i class="icon large ${WorkflowHelper.getCssIconByTaskPriority(ch.priority)}"></i>
                                                            ${ch.id}
                                                        </span>
                                                    </g:link>
                                                </span>
                                                <g:if test="${ch.condition}">
                                                    <span data-position="top center" class="la-popup-tooltip la-delay" data-content="${ch.condition.title}">
                                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY + ':' + ch.condition.id, tab: 'prototypes']}">
                                                            <span class="ui teal circular label" data-wfcp="${ch.condition.id}">${ch.condition.id}</span>
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

</div><!-- .prototypes -->

<div id="wfModal" class="ui modal"></div>


<laser:script file="${this.getGroovyPageFileName()}">
    $('.secondary.menu > a').tab();

    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), true);
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

</body>
</html>
