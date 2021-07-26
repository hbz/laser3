<%@ page import="de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : DEMO - Workflows</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb test="DEMO - Workflows" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">DEMO - Workflows</h1>

<!-- -->

<h2 class="ui header">Workflows</h2>

%{-- <ul>
    <g:each in="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by id')}" var="wfwp">
        <li><g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wfwp.id]}">(${wfwp.id}) ${wfwp.title}</g:link></li>
    </g:each>
</ul> --}%

<g:each in="${WfWorkflowPrototype.executeQuery('select wfwp from WfWorkflowPrototype wfwp order by id')}" var="wfwp">
    <p><strong>
            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wfwp.id]}">(${wfwp.id}) ${wfwp.title}</g:link>
    </strong></p>

    <div class="ui mini steps">
        <g:set var="tasks" value="${wfwp.getStructure()}" />
        <g:each in="${tasks}" var="wftp">
            <div class="step">
                <g:if test="${! wftp.child}">
                    <div class="content">
                        <div class="title">
                            <g:if test="${wftp.priority == RDStore.WF_TASK_PRIORITY_BLOCKER}">
                                <i class="ui icon exclamation triangle orange"></i>
                            </g:if>
                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">${wftp.title}</g:link>
                        </div>
                    </div>
                </g:if>
                <g:else>
                    <g:set var="children" value="${wftp.child.getStructure()}" />
                    <g:if test="${children}">
                        <div class="content">
                            <div class="title">
                                <g:if test="${wftp.priority == RDStore.WF_TASK_PRIORITY_BLOCKER}">
                                    <i class="ui icon exclamation triangle orange"></i>
                                </g:if>
                                <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wftp.id]}">${wftp.title}</g:link>
                            </div>
                        </div>
                        <div class="ui mini vertical steps" style="width: 100% !important;">
                            <g:each in="${children}" var="ch" status="ci">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <g:if test="${ch.priority == RDStore.WF_TASK_PRIORITY_BLOCKER}">
                                                <i class="ui icon exclamation triangle orange"></i>
                                            </g:if>
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: ch.id]}">${ch.title}</g:link>
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

<h2 class="ui header">WfWorkflowPrototype : ${WfWorkflowPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Workflow</th>
            <th>Task</th>
            <th>Zustand</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp order by id')}" var="wp">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wp.id]}">(${wp.id}) ${wp.title}</g:link>
                </td>
                <td>
                    <g:if test="${wp.child}">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: wp.child.id]}">
                            (${wp.child.id}) <i class="check green icon"></i>
                        </g:link>
                    </g:if>
                </td>
                <td>
                    ${wp.state?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${! wp.inStructure()}">
                        <g:link class="ui red icon small button" controller="admin" action="manageWorkflows" params="${[cmd: "delete:${WfWorkflowPrototype.KEY}", id: wp.id]}"><i class="trash alternate icon"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${key == WfWorkflowPrototype.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfWorkflowPrototype.KEY]}">Workflow (Prototyp) erstellen</g:link>

<!-- -->

<h2 class="ui header">WfTaskPrototype : ${WfTaskPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
        <tr>
            <th>Task</th>
            <th>Condition</th>
            <th>Workflow</th>
            <th>Priorität</th>
            %{-- <th>Typ</th> --}%
            <th>Has Next</th>
            <th>Has Child</th>
            <th>Has Previous</th>
            <th>Has Parent</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp order by id')}" var="tp">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.id]}">(${tp.id}) ${tp.title}</g:link>
                </td>
                <td>
                    <g:if test="${tp.condition}">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: tp.condition.id]}">
                            (${tp.condition.id}) <i class="check green icon"></i>
                        </g:link>
                    </g:if>
                </td>
                <td>
                    <g:each in="${WfWorkflowPrototype.executeQuery('select wp from WfWorkflowPrototype wp where wp.child = :tp order by id', [tp: tp])}" var="wp">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflowPrototype.KEY, id: wp.id]}">
                            (${wp.id}) <i class="check green icon"></i>
                        </g:link>
                    </g:each>
                </td>
                <td>
                    ${tp.priority?.getI10n('value')}
                </td>
                %{-- <td>
                    ${tp.type?.getI10n('value')}
                </td> --}%
                <td>
                    <g:if test="${tp.next}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${tp.child}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByNext(tp)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${WfTaskPrototype.findByChild(tp)}"> <i class="check green icon"></i> </g:if>
                </td>
                <td>
                    <g:if test="${! tp.inStructure()}">
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

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfTaskPrototype.KEY]}">Aufgabe (Prototyp) erstellen</g:link>

<!-- -->

<h2 class="ui header">WfConditionPrototype : ${WfConditionPrototype.findAll().size()}</h2>

<table class="ui celled la-table compact table">
    <thead>
    <tr>
        <th>Condition</th>
        <th>Task</th>
        <th>Typ</th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${WfConditionPrototype.executeQuery('select cp from WfConditionPrototype cp order by id')}" var="cp">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfConditionPrototype.KEY, id: cp.id]}">(${cp.id}) ${cp.title}</g:link>
                </td>
                <td>
                    <g:each in="${WfTaskPrototype.executeQuery('select wp from WfTaskPrototype wp where wp.condition = :cp order by id', [cp: cp])}" var="tp">
                        <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTaskPrototype.KEY, id: tp.id]}">
                            (${tp.id}) <i class="check green icon"></i>
                        </g:link>
                    </g:each>
                </td>
                <td>
                    ${cp.getTypeAsRefdataValue().getI10n('value')}
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfXModal" params="${[key: WfConditionPrototype.KEY]}">Bedingung (Prototyp) erstellen</g:link>

%{--
<!-- -->

<h2 class="ui header">WfWorkflow : ${WfWorkflow.findAll().size()}</h2>

<g:each in="${WfWorkflow.executeQuery('select w from WfWorkflow w order by id')}" var="w">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfWorkflow.KEY, id: w.id]}">(${w.id}) ${w.title}</g:link>
</g:each>

<g:if test="${key == WfWorkflow.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:workflow]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfWorkflow.KEY]}">Workflow erstellen</g:link>

<!-- -->

<h2 class="ui header">WfTask : ${WfTask.findAll().size()}</h2>

<g:each in="${WfTask.executeQuery('select t from WfTask t order by id')}" var="t">
    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfComponentModal" params="${[key: WfTask.KEY, id: t.id]}">(${t.id}) ${t.title}}</g:link><br/>
</g:each>

<g:if test="${key == WfTask.KEY}">
    <g:render template="/templates/workflow/opResult" model="${[key:key, cmd:cmd, status:status, obj:task]}" />
</g:if>

<g:link class="wfModalLink ui button" controller="ajaxHtml" action="createWfComponentModal" params="${[key: WfTask.KEY]}">Task erstellen</g:link>

<!-- -->
--}%

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.target).attr('href'), true);
        func();
    });
</laser:script>

</body>
</html>
