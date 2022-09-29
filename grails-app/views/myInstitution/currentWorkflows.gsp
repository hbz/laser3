<%@ page import="de.laser.License; de.laser.Subscription; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.*; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService" %>

<laser:htmlStart message="menu.my.workflows" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.workflows" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.workflows" type="Workflow" total="${total}" floated="true" />

<ui:filter showFilterButton="true" addFilterJs="true">
    <form class="ui form">
        <div class="three fields">
            <div class="field">
                <label>Basierend auf ${message(code: 'workflow.label')}</label>
                <g:select class="ui dropdown" name="filterPrototypeMeta"
                          from="${ currentPrototypes }"
                          optionKey="${{it.hash}}"
                          optionValue="${{it.title + ' - ' + it.variant}}"
                          value="${params.filterPrototypeMeta}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code:'default.relation.label')}</label>
                <ui:select class="ui dropdown la-not-clearable" name="filterTargetType"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.WF_WORKFLOW_TARGET_TYPE)}"
                           value="${params.filterTargetType}"
                           optionKey="id"
                           optionValue="value" />

            </div>
            %{--<div class="field">
                <label>${message(code: 'default.priority.label')}</label>
                <ui:select class="ui dropdown" name="filterPriority"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.WF_TASK_PRIORITY) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.filterPriority}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>--}%
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <ui:select class="ui dropdown" name="filterStatus"
                  from="${ RefdataCategory.getAllRefdataValues(RDConstants.WF_WORKFLOW_STATUS) }"
                  optionKey="id"
                  optionValue="value"
                  value="${params.filterStatus}"
                  noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
%{--        <div class="three fields">
            <div class="field">
                <label>${message(code:'default.relation.label')}</label>
                <ui:select class="ui dropdown la-not-clearable" name="filterTargetType"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_TARGET_TYPE )}"
                           value="${params.filterTargetType}"
                           optionKey="id"
                           optionValue="value" />

            </div>
            <div class="field">
                <label>${RefdataCategory.findByDesc(RDConstants.WF_WORKFLOW_TARGET_ROLE).getI10n('desc')}</label>
                <ui:select class="ui dropdown la-not-clearable" name="filterTargetRole"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${RefdataCategory.getAllRefdataValues( RDConstants.WF_WORKFLOW_TARGET_ROLE )}"
                           value="${params.filterTargetRole}"
                           optionKey="id"
                           optionValue="value" />
            </div>
        </div>--}%

%{--        <div class="three fields">
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
        </div>--}%
        <div class="field la-field-right-aligned">
            <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: false]}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
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
            <th class="one wide" rowspan="2">${message(code:'default.status.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'workflow.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'default.relation.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="two wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
            <th class="one wide" rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="wf">
            <g:set var="wfInfo" value="${wf.getInfo()}" />
            <tr>
                <td>
                    <uiWorkflow:statusIcon workflow="${wf}" size="large" />
                </td>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'myInstitution:' + wf.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                        <strong>${wf.title}</strong>
                    </g:link>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="ui icon ${wfInfo.targetIcon} la-list-icon"></i>
                        <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">
                            ${wfInfo.targetName}
                            <br/>
                            <g:if test="${wfInfo.target instanceof Subscription || wfInfo.target instanceof License}">
                                <g:if test="${wfInfo.target.startDate || wfInfo.target.endDate}">
                                    (${wfInfo.target.startDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.startDate) : ''} -
                                    ${wfInfo.target.endDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.endDate) : ''})
                                </g:if>
                            </g:if>
                        </g:link>
                    </div>
                </td>
                <td>
                    <div class="ui buttons">
                        <g:set var="tasks" value="${wf.getSequence()}" />
                        <g:each in="${tasks}" var="task" status="ti">
                            <uiWorkflow:task task="${task}" params="${[key: 'myInstitution:' + wf.id + ':' + WfTask.KEY + ':' + task.id]}" />
                        </g:each>
                    </div>
                </td>
                <td>
                    ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                </td>
                <td class="x">
                    <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui blue icon button la-modern-button" controller="${wfInfo.targetController}" action="workflows" id="${wfInfo.target.id}" params="${[info: '' + wfInfo.target.class.name + ':' + wfInfo.target.id + ':' + WfWorkflow.KEY + ':' + wf.id]}"><i class="icon edit"></i></g:link>
                        %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
                    </g:if>
                    <g:if test="${workflowService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                                data-confirm-term-how="delete"
                                controller="myInstitution" action="currentWorkflows" params="${[cmd:"delete:${WfWorkflow.KEY}:${wf.id}"]}"
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

<ui:paginate action="currentWorkflows" controller="myInstitution" total="${total}" max="${params.max}" />

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
</laser:script>


<laser:htmlEnd />
