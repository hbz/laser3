<%@ page import="de.laser.helper.WorkflowHelper; de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : DEMO - Workflows</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="DEMO - Workflows" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">
    <i class="ui icon tasks"></i> Workflows (DEMO)
</h1>

<!-- -->

<h2 class="ui header">Workflows</h2>

<g:if test="${key == WfWorkflow.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
</g:if>

<g:each in="${WfWorkflow.executeQuery('select wfw from WfWorkflow wfw order by id')}" var="wfw">
    <p>
        <g:link class="ui red icon small button right floated" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfWorkflow.KEY}", id: wfw.id]}"><i class="trash alternate icon"></i></g:link>
        <strong>
            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY, id: wfw.id]}">
                <div class="ui circular label">${wfw.id}</div> <i class="ui icon tasks"></i> ${wfw.title}
            </g:link>
            <br />
            <g:link controller="subscription" action="show" params="${[id: wfw.subscription.id]}">
                <div class="ui circular label">${wfw.subscription.id}</div> <i class="ui icon clipboard"></i> ${wfw.subscription}
            </g:link>
        </strong>
    </p>

    <div class="ui mini steps">
        <g:set var="tasks" value="${wfw.getSequence()}" />
        <g:each in="${tasks}" var="wft">
            <div class="step">
                <g:if test="${! wft.child}">
                    <div class="content">
                        <div class="title">

                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY, id: wft.id]}">
                                <i class="ui icon large ${WorkflowHelper.getCssColorByStatus(wft.status)} ${WorkflowHelper.getCssIconByTaskPriority(wft.priority)}"></i>
                                ${wft.title}
                            </g:link>
                            <g:if test="${wft.condition}">
                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: wft.condition.id]}">
                                    ${wft.condition.title}
                                </g:link>
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
                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY, id: wft.id]}">
                                            <i class="ui icon large ${WorkflowHelper.getCssColorByStatus(wft.status)} ${WorkflowHelper.getCssIconByTaskPriority(wft.priority)}"></i>
                                            ${wft.title}
                                        </g:link>
                                        <g:if test="${wft.condition}">
                                            - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: wft.condition.id]}">
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
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY, id: ch.id]}">
                                                <i class="ui icon large ${WorkflowHelper.getCssColorByStatus(ch.status)} ${WorkflowHelper.getCssIconByTaskPriority(ch.priority)}"></i>
                                                ${ch.title}
                                            </g:link>
                                            <g:if test="${ch.condition}">
                                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: ch.condition.id]}">
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
</g:each>

<h2 class="ui header">Prototypen</h2>

<g:each in="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by id')}" var="wfwp">
    <p><strong>
        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wfwp.id]}">
            <span class="ui purple circular label" data-wfwp="${wfwp.id}">${wfwp.id}</span>
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
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">
                                <span class="ui blue circular label" data-wftp="${wftp.id}">${wftp.id}</span>
                                <i class="ui icon grey large ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
                            </g:link>
                            <g:if test="${wftp.condition}">
                                -
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: wftp.condition.id]}">
                                    <span class="ui teal circular label" data-wfcp="${wftp.condition.id}">${wftp.condition.id}</span>
                                </g:link>
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
                                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">
                                            <span class="ui blue circular label" data-wftp="${wftp.id}">${wftp.id}</span>
                                            <i class="ui icon grey large ${WorkflowHelper.getCssIconByTaskPriority(wftp.priority)}"></i>
                                        </g:link>
                                        <g:if test="${wftp.condition}">
                                            -
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: wftp.condition.id]}">
                                                <span class="ui teal circular label" data-wfcp="${wftp.condition.id}">${wftp.condition.id}</span>
                                            </g:link>
                                        </g:if>
                                    </div>
                                </div>
                            </div>
                            <g:each in="${children}" var="ch" status="ci">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: ch.id]}">
                                                <span class="ui blue circular label" data-wftp="${ch.id}">${ch.id}</span>
                                                <i class="ui icon grey large ${WorkflowHelper.getCssIconByTaskPriority(ch.priority)}"></i>
                                            </g:link>
                                            <g:if test="${ch.condition}">
                                                -
                                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: ch.condition.id]}">
                                                    <span class="ui teal circular label" data-wfcp="${ch.condition.id}">${ch.condition.id}</span>
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
</g:each>

<!-- -->

<h2 class="ui header">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} : ${WfWorkflowPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Workflow</th>
            <th>Aufgabe &darr;</th>
            <th>Zustand</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp order by id')}" var="wp">
            <tr data-wfwp="${wp.id}">
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wp.id]}">
                        <span class="ui purple circular label" data-wfwp="${wp.id}">${wp.id}</span>
                        ${wp.title}
                    </g:link>
                </td>
                <td>
                    <g:if test="${wp.child}">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${wp.child.title} (${wp.child.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wp.child.id]}">
                                <span class="ui blue circular label" data-wftp="${wp.child.id}">${wp.child.id}</span>
                            </g:link>
                        </span>
                    </g:if>
                </td>
                <td>
                    ${wp.state?.getI10n('value')}
                </td>
                <td class="x">
                    <g:if test="${! wp.inUse()}">
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfWorkflowPrototype.KEY}", id: wp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                    <g:if test="${wp.state == RDStore.WF_WORKFLOW_STATE_ACTIVE}">
                        <g:link class="ui green icon small button tmpJSPrompt" controller="admin" action="manageWorkflows" params="${[cmd: "instantiate:${WfWorkflowPrototype.KEY}", id: wp.id]}"><i class="step forward icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfWorkflowPrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfWorkflowPrototype.KEY]}">${message(code: 'workflow.object.' + WfWorkflowPrototype.KEY)} erstellen</g:link>

<!-- -->

<h2 class="ui header">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} : ${WfTaskPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Aufgabe</th>
            <th>Bedingung &darr;</th>
            <th>Workflow &uarr;</th>
            %{-- <th>Typ</th> --}%
            <th>Nachfolger &rarr;</th>
            <th>Kind &darr;</th>
            <th>Priorität</th>
            <th>Vorgänger &larr;</th>
            <th>Super &uarr;</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp order by id')}" var="tp">
            <tr data-wftp="${tp.id}">
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.id]}">
                        <span class="ui blue circular label" data-wftp="${tp.id}">${tp.id}</span>
                        ${tp.title}
                    </g:link>
                </td>
                <td>
                    <g:if test="${tp.condition}">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${tp.condition.title} (${tp.condition.getTypeAsRefdataValue().getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: tp.condition.id]}">
                                <span class="ui teal circular label" data-wfcp="${tp.condition.id}">${tp.condition.id}</span>
                            </g:link>
                        </span>
                    </g:if>
                </td>
                <td>
                    <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where wp.child = :tp order by id', [tp: tp])}" var="wp">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${wp.title}">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wp.id]}">
                                <span class="ui purple circular label" data-wfwp="${wp.id}">${wp.id}</span>
                            </g:link>
                        </span>
                    </g:each>
                </td>
                %{-- <td>
                    ${tp.type?.getI10n('value')}
                </td> --}%
                <td>
                    <g:if test="${tp.next}">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${tp.next.title} (${tp.next.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.next.id]}">
                                <span class="ui blue circular label" data-wftp="${tp.next.id}">${tp.next.id}</span>
                            </g:link>
                        </span>
                    </g:if>
                </td>
                <td>
                    <g:if test="${tp.child}">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${tp.child.title} (${tp.child.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.child.id]}">
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
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${prev.title} (${prev.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: prev.id]}">
                                <span class="ui blue circular label" data-wftp="${prev.id}">${prev.id}</span>
                            </g:link>
                        </span>
                    </g:each>
                </td>
                <td>
                    <g:each in="${WfTaskPrototype.findByChild(tp)}" var="sup">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${sup.title} (${sup.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: sup.id]}">
                                <span class="ui blue circular label" data-wftp="${sup.id}">${sup.id}</span>
                            </g:link>
                        </span>
                    </g:each>
                </td>
                <td class="x">
                    <g:if test="${! tp.inUse()}">
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfTaskPrototype.KEY}", id: tp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfTaskPrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfTaskPrototype.KEY]}">${message(code: 'workflow.object.' + WfTaskPrototype.KEY)} erstellen</g:link>

<!-- -->

<h2 class="ui header">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} : ${WfConditionPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
    <tr>
        <th>Bedingung</th>
        <th>Aufgabe &uarr;</th>
        <th>Typ</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${WfConditionPrototype.executeQuery('select cp from WfConditionPrototype cp order by id')}" var="cp">
            <tr data-wfcp="${cp.id}">
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: cp.id]}">
                        <span class="ui teal circular label" data-wfcp="${cp.id}">${cp.id}</span>
                        ${cp.title}
                    </g:link>
                </td>
                <td>
                    <g:each in="${WfTaskPrototype.executeQuery('select wp from WfTaskPrototype wp where wp.condition = :cp order by id', [cp: cp])}" var="tp">
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${tp.title} (${tp.priority.getI10n('value')})">
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.id]}">
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
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfConditionPrototype.KEY}", id: cp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfConditionPrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:condition]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfConditionPrototype.KEY]}">${message(code: 'workflow.object.' + WfConditionPrototype.KEY)} erstellen</g:link>

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
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
