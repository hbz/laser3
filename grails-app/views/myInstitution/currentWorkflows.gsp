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

<h1 class="ui icon header la-noMargin-top"><semui:headerTitleIcon type="Workflow"/>${message(code:'menu.my.workflows')}
<semui:totalNumber total="${total}"/>
</h1>

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <form class="ui form">
        <div class="three fields">
            <div class="field">
                <label>${message(code: 'workflow.label')}</label>
                <g:select class="ui dropdown" name="filterPrototype"
                          from="${ currentPrototypes }"
                          optionKey="id"
                          optionValue="title"
                          value="${params.filterPrototype}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'default.priority.label')}</label>
                <laser:select class="ui dropdown" name="filterPriority"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.WF_TASK_PRIORITY) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.filterPriority}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <laser:select class="ui dropdown" name="filterStatus"
                  from="${ RefdataCategory.getAllRefdataValues(RDConstants.WF_WORKFLOW_STATUS) }"
                  optionKey="id"
                  optionValue="value"
                  value="${params.filterStatus}"
                  noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="three fields">
            <div class="field">
                <label>${message(code: 'default.provider.label')}</label>
                <g:select class="ui dropdown" name="filterProvider"
                          from="${ currentProviders }"
                          optionKey="id"
                          optionValue="name"
                          value="${params.filterProvider}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'default.subscription.label')}</label>
                <g:select class="ui dropdown" name="filterSubscription"
                          from="${ currentSubscriptions }"
                          optionKey="id"
                          optionValue="name"
                          value="${params.filterSubscription}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="field la-field-right-aligned">
            <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: false]}" class="ui reset primary button">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
        </div>
    </form>
</semui:filter>

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
        <th>${message(code:'default.status.label')}</th>
        <th>${message(code:'workflow.label')}</th>
        <th>${message(code:'subscription.label')}</th>
        <th>${message(code:'default.progress.label')}</th>
        <th>${message(code:'workflow.dates.plural')}</th>
        <th>${message(code:'default.actions.label')}</th>
    </tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="wf">
            <g:set var="wfInfo" value="${wf.getInfo()}" />
            <tr>
                <td>
                    <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i>
                </td>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution:' + wf.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                        <strong>${wf.title}</strong>
                    </g:link>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="ui icon clipboard la-list-icon"></i>
                        <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                            ${wf.subscription.name}
                            <br/>
                            <g:if test="${wf.subscription.startDate || wf.subscription.endDate}">
                                (${wf.subscription.startDate ? DateUtils.getSDF_NoTime().format(wf.subscription.startDate) : ''} -
                                ${wf.subscription.endDate ? DateUtils.getSDF_NoTime().format(wf.subscription.endDate) : ''})
                            </g:if>
                        </g:link>
                    </div>
                </td>
                <td>
                    <div class="ui buttons workflowOverrideCss">
                        <g:set var="tasks" value="${wf.getSequence()}" />
                        <g:each in="${tasks}" var="task" status="ti">
                            <g:if test="${task.child}">
                                <div style="width:8px"></div>
                                <laser:workflowTask task="${task}" params="${[key: 'myInstitution:' + wf.id + ':' + WfTask.KEY + ':' + task.id]}" />

                                <g:set var="children" value="${task.child.getSequence()}" />
                                <g:each in="${children}" var="child" status="ci">
                                    <laser:workflowTask task="${child}" params="${[key: 'myInstitution:' + wf.id + ':' + WfTask.KEY + ':' + child.id]}" />
                                </g:each>
                                <div style="width:8px"></div>
                            </g:if>
                            <g:else>
                                <laser:workflowTask task="${task}" params="${[key: 'myInstitution:' + wf.id + ':' + WfTask.KEY + ':' + task.id]}" />
                            </g:else>
                        </g:each>
                    </div>
                </td>
                <td>
                    ${DateUtils.getSDF_NoTime().format(wfInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getSDF_NoTime().format(wf.dateCreated)}
                </td>
                <td class="x">
                    <g:link class="ui icon small button" controller="subscription" action="workflows" id="${wf.subscription.id}" params="${[info: 'subscription:' + wf.subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}"><i class="icon info"></i></g:link>
                    %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
                    <g:link class="ui red icon small button" controller="myInstitution" action="currentWorkflows" params="${[cmd: "delete:${WfWorkflow.KEY}:${wf.id}"]}"><i class="trash alternate outline icon"></i></g:link>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<semui:paginate action="currentWorkflows" controller="myInstitution" total="${total}" max="${params.max}"
                next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" />

<div id="wfModal" class="ui modal"></div>

<style>
.workflowOverrideCss .label {
    margin-right: 3px !important;
}
.workflowOverrideCss .label .icon {
    margin: 0 !important;
    padding-top: 1px;
}
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), true);
        func();
    });
</laser:script>


</body>
</html>
