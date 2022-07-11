<%@ page import="de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.workflow.WorkflowHelper" %>

<laser:htmlStart message="menu.my.workflows" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.workflows" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.workflows" type="Workflow" total="${total}" floated="true" />

<laser:render template="/templates/filter/javascript" />
<ui:filter showFilterButton="true">
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
</ui:filter>

<g:if test="${status == WorkflowService.OP_STATUS_DONE}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="positive" message="workflow.delete.ok" />
    </g:if>
    <g:else>
        <ui:msg class="positive" message="workflow.edit.ok" />
    </g:else>
</g:if>
<g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="negative" message="workflow.delete.error" />
    </g:if>
    <g:else>
        <ui:msg class="negative" message="workflow.edit.error" />
    </g:else>
</g:elseif>

<table class="ui celled table la-js-responsive-table la-table">
    <thead>
        <tr>
            <th rowspan="2">${message(code:'default.status.label')}</th>
            <th rowspan="2">${message(code:'workflow.label')}</th>
            <th rowspan="2">${message(code:'subscription.label')}</th>
            <th rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
            <th rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="wf">
            <g:set var="wfInfo" value="${wf.getInfo()}" />
            <tr>
                <td>
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
                                (${wf.subscription.startDate ? DateUtils.getLocalizedSDF_noTime().format(wf.subscription.startDate) : ''} -
                                ${wf.subscription.endDate ? DateUtils.getLocalizedSDF_noTime().format(wf.subscription.endDate) : ''})
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
                    ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                </td>
                <td class="x">
                    <g:link class="ui blue icon button la-modern-button" controller="subscription" action="workflows" id="${wf.subscription.id}" params="${[info: 'subscription:' + wf.subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}"><i class="icon edit"></i></g:link>
                    %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
                    <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                            data-confirm-term-how="delete"
                            controller="myInstitution" action="currentWorkflows" params="${[cmd:"delete:${WfWorkflow.KEY}:${wf.id}"]}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                        <i class="trash alternate outline icon"></i>
                    </g:link>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<ui:paginate action="currentWorkflows" controller="myInstitution" total="${total}" max="${params.max}" />

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
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
</laser:script>


<laser:htmlEnd />
