<%@ page import="de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.helper.WorkflowHelper" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Meine Workflows</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb text="Meine Workflows" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">
    <i class="ui icon tasks"></i> Meine Workflows
</h1>

<table class="ui celled table la-table">
    <thead>
    <tr>
        <th>Workflow</th>
        <th>Lizenz</th>
        <th></th>
        <th></th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${WfWorkflow.executeQuery('select wf from WfWorkflow wf where wf.owner = :ctxOrg order by id', [ctxOrg: contextService.getOrg()])}" var="wf">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfWorkflow.KEY, id: wf.id]}">
                        <i class="ui icon tasks"></i>${wf.title}
                    </g:link>
                </td>
                <td>
                    <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                        <i class="ui icon clipboard"></i>${wf.subscription}
                    </g:link>
                </td>
                <td>
                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <g:if test="${task.child}">
                            [
                                <div class="ui circular ${WorkflowHelper.getCssClassByStatus(task.status)} label">
                                    <g:if test="${task.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                        <i class="ui icon exclamation circle orange"></i>
                                    </g:if>
                                    <g:if test="${task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                        <i class="ui icon minus circle grey"></i>
                                    </g:if>
                                    ${ti + 1}.1
                                </div>
                                <g:set var="children" value="${task.child.getSequence()}" />
                                <g:each in="${children}" var="child" status="ci">
                                    <div class="ui circular ${WorkflowHelper.getCssClassByStatus(task.child.status)} label">
                                        <g:if test="${task.child.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                            <i class="ui icon exclamation circle orange"></i>
                                        </g:if>
                                        <g:if test="${task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                            <i class="ui icon minus circle grey"></i>
                                        </g:if>
                                        ${ti + 1}.${ci + 2}
                                    </div>
                                </g:each>
                            ]
                        </g:if>
                        <g:else>
                            <div class="ui circular ${WorkflowHelper.getCssClassByStatus(task.status)} label">
                                <g:if test="${task.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                    <i class="ui icon exclamation circle orange"></i>
                                </g:if>
                                <g:if test="${task.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                    <i class="ui icon minus circle grey"></i>
                                </g:if>
                                ${ti + 1}
                            </div>
                        </g:else>
                    </g:each>
                </td>
                <td>
                    <g:link class="ui red icon small button right floated" controller="myInstitution" action="currentWorkflows" params="${[cmd: "delete:${WfWorkflow.KEY}", id: wf.id]}"><i class="trash alternate icon"></i></g:link>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<g:each in="${WfWorkflow.executeQuery('select wfw from WfWorkflow wfw where wfw.owner = :ctxOrg order by id', [ctxOrg: contextService.getOrg()])}" var="wfw">
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
                                ${wft.title}
                                <g:if test="${wft.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                    <i class="ui icon large exclamation circle orange"></i>
                                </g:if>
                                <g:if test="${wft.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                    <i class="ui icon large minus circle grey"></i>
                                </g:if>
                            </g:link>
                            <g:if test="${wft.condition}">
                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: wft.condition.id]}">${wft.condition.title}</g:link>
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
                                            ${wft.title}
                                            <g:if test="${wft.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                                <i class="ui icon large exclamation circle orange"></i>
                                            </g:if>
                                            <g:if test="${wft.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                                <i class="ui icon large minus circle grey"></i>
                                            </g:if>
                                        </g:link>
                                        <g:if test="${wft.condition}">
                                            - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: wft.condition.id]}">${wft.condition.title}</g:link>
                                        </g:if>
                                    </div>
                                </div>
                            </div>
                            <g:each in="${children}" var="ch" status="ci">
                                <div class="step">
                                    <div class="content">
                                        <div class="title">
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfTask.KEY, id: ch.id]}">
                                                ${ch.title}
                                                <g:if test="${ch.priority == RDStore.WF_TASK_PRIORITY_IMPORTANT}">
                                                    <i class="ui icon large exclamation circle orange"></i>
                                                </g:if>
                                                <g:if test="${ch.priority == RDStore.WF_TASK_PRIORITY_OPTIONAL}">
                                                    <i class="ui icon large minus circle grey"></i>
                                                </g:if>
                                            </g:link>
                                            <g:if test="${ch.condition}">
                                                - <g:link class="wfModalLink" controller="ajaxHtml" action="editWfXModal" params="${[key: WfCondition.KEY, id: ch.condition.id]}">${ch.condition.title}</g:link>
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

<div id="wfModal" class="ui modal"></div>

</body>
</html>
