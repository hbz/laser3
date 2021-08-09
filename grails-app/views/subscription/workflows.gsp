<%@ page import="de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.helper.WorkflowHelper" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'workflow.plural.label')}</title>
    </head>
<body>

    <g:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <g:render template="nav" />

    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <g:if test="${cmd == 'delete'}">
            <semui:msg class="positive" text="Objekt wurde gelöscht." />
        </g:if>
        <g:else>
            <semui:msg class="positive" text="OK ( ${cmd} )" />
        </g:else>
    </g:if>
    <g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
        <g:if test="${cmd == 'delete'}">
            <semui:msg class="negative" text="Objekt konnte nicht gelöscht werden." />
        </g:if>
        <g:else>
            <semui:msg class="negative" text="FEHLER ( ${cmd} )" />
        </g:else>
    </g:elseif>

    <table class="ui celled table la-table">
        <thead>
        <tr>
            <th>${message(code:'workflow.label')}</th>
            <th></th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${workflows}" var="wf">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                        <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i> ${wf.title}
                    </g:link>
                </td>
                <td>
                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <g:if test="${task.child}">
                            <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(task)}">

                                <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}">
                                    <span class="ui ${WorkflowHelper.getCssColorByStatus(task.status)} label">
                                        <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                        ${ti + 1}.1
                                    </span>
                                </g:link>
                            </span>
                            <g:set var="children" value="${task.child.getSequence()}" />
                            <g:each in="${children}" var="child" status="ci">
                                <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(child)}">

                                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + child.id]}">
                                        <span class="ui ${WorkflowHelper.getCssColorByStatus(child.status)} label">
                                            <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                            ${ti + 1}.${ci + 2}
                                        </span>
                                    </g:link>
                                </span>
                            </g:each>
                        </g:if>
                        <g:else>
                            <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(task)}">

                                <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}">
                                    <span class="ui ${WorkflowHelper.getCssColorByStatus(task.status)} label">
                                        <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                        ${ti + 1}
                                    </span>
                                </g:link>
                            </span>
                        </g:else>
                    </g:each>
                </td>
                <td>
                    %{-- <g:link class="ui positive icon small button right floated" controller="admin" action="manageWorkflows" params="${[cmd: "todo"]}"><i class="edit icon"></i></g:link> --}%
                    <g:link class="ui red icon small button right floated" controller="subscription" action="workflows" params="${[id: "${subscription.id}", cmd: "delete:${WfWorkflow.KEY}:${wf.id}"]}"><i class="trash alternate icon"></i></g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div id="wfModal" class="ui modal"></div>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.wfModalLink').on('click', function(e) {
            e.preventDefault();
            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), true);
            func();
        });
    </laser:script>

</body>
</html>
