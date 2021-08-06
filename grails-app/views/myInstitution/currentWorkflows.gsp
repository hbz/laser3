<%@ page import="de.laser.helper.DateUtils; de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.helper.WorkflowHelper" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.my.workflows')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.my.workflows" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">
    <i class="ui icon tasks"></i> ${message(code:'menu.my.workflows')}
</h1>

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
        <th>${message(code:'subscription.label')}</th>
        <th></th>
        <th></th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="wf">
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution::' + WfWorkflow.KEY + ':' + wf.id]}">
                        <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i> ${wf.title}
                    </g:link>
                </td>
                <td>
                    <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                        <i class="ui icon clipboard"></i>${wf.subscription.name}
                        <g:if test="${wf.subscription.startDate || wf.subscription.endDate}">
                            (${wf.subscription.startDate ? DateUtils.getSDF_NoTime().format(wf.subscription.startDate) : ''} -
                            ${wf.subscription.endDate ? DateUtils.getSDF_NoTime().format(wf.subscription.endDate) : ''})
                        </g:if>
                    </g:link>
                </td>
                <td>
                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <g:if test="${task.child}">
                                <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(task)}">
                                    <g:link class="ui compact button ${WorkflowHelper.getCssColorByStatus(task.status)} wfModalLink"
                                            controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution::' + WfTask.KEY + ':' + task.id]}">
                                                <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                                ${(char) (65 + ti)}.1
                                    </g:link>
                                </span>
                                <g:set var="children" value="${task.child.getSequence()}" />
                                <g:each in="${children}" var="child" status="ci">
                                    <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(child)}">
                                        <g:link class="ui compact button ${WorkflowHelper.getCssColorByStatus(child.status)} wfModalLink"
                                                controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution::' + WfTask.KEY + ':' + child.id]}">
                                                    <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i>
                                                    ${(char) (65 + ti)}.${ci + 2}
                                        </g:link>
                                    </span>
                                </g:each>
                        </g:if>
                        <g:else>
                            <span class="la-popup-tooltip la-delay" data-position="top center" data-html="${WorkflowHelper.getTaskTooltipMarkup(task)}">
                                <g:link class="ui compact button ${WorkflowHelper.getCssColorByStatus(task.status)} wfModalLink"
                                        controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution::' + WfTask.KEY + ':' + task.id]}">
                                            <i class="ui icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i>
                                            ${(char) (65 + ti)}
                                </g:link>
                            </span>
                        </g:else>
                    </g:each>
                </td>
                <td>
                    <g:link class="ui red icon small button right floated" controller="myInstitution" action="currentWorkflows" params="${[cmd: "delete:${WfWorkflow.KEY}:${wf.id}"]}"><i class="trash alternate icon"></i></g:link>
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

    <g:if test="${forwardedKey}">
        /* forwarded */
        $('.wfModalLink[href="${g.createLink(controller:'ajaxHtml', action:'useWfXModal', params:[key: forwardedKey])}"]').trigger('click');
    </g:if>
</laser:script>


</body>
</html>
