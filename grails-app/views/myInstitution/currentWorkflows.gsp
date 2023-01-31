<%@ page import="de.laser.workflow.light.WfCheckpoint; de.laser.workflow.light.WfChecklist; de.laser.License; de.laser.Subscription; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.*; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowLightService" %>

<laser:htmlStart message="menu.my.workflows" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.workflows.light" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.workflows.light" type="Workflow" total="${total}" floated="true" />

<ui:filter>
    <form id="wfFilterForm" class="ui form">
        <div class="two fields">
            <div class="field">
                <label>${message(code:'default.relation.label')}</label>
                <ui:select class="ui dropdown la-not-clearable" name="filterTargetType"
                           required="required"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.WF_WORKFLOW_TARGET_TYPE)}"
                           value="${filterTargetType}"
                           optionKey="id"
                           optionValue="value" />

            </div>
%{--            <div class="field">--}%
%{--                <label>Basierend auf ${message(code: 'workflow.label')}</label>--}%
%{--                <g:select class="ui dropdown" name="filterPrototypeMeta"--}%
%{--                          from="${ currentPrototypes }"--}%
%{--                          optionKey="${{it.id}}"--}%
%{--                          optionValue="${{it.title + ' (' + it.lastUpdated + '/' + it.dateCreated + ')'}}"--}%
%{--                          value="${filterPrototypeMeta}"--}%
%{--                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>--}%
%{--            </div>--}%
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <ui:select class="ui dropdown" name="filterStatus"
                  from="${[RDStore.WF_WORKFLOW_STATUS_OPEN, RDStore.WF_WORKFLOW_STATUS_DONE]}"
                  optionKey="id"
                  optionValue="value"
                  value="${filterStatus}"
                  noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="field la-field-right-aligned">
            <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: 'reset']}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
        </div>
        <input type="hidden" name="filter" value="true" />
        <input type="hidden" name="tab" value="${tab}" />
    </form>
</ui:filter>

<g:if test="${status == workflowLightService.OP_STATUS_DONE}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="positive" message="workflow.delete.ok" />
    </g:if>
    <g:else>
        <ui:msg class="positive" message="workflow.edit.ok" />
    </g:else>
</g:if>
<g:elseif test="${status == workflowLightService.OP_STATUS_ERROR}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="negative" message="workflow.delete.error" />
    </g:if>
    <g:else>
        <ui:msg class="negative" message="workflow.edit.error" />
    </g:else>
</g:elseif>

%{--<div id="wfTabs" class="ui secondary stackable pointing tabular la-tab-with-js menu" style="margin-top:2em;">--}%
%{--    <a class="${tab == 'open' ? 'active item':'item'}" data-tab="open">--}%
%{--        ${RDStore.WF_WORKFLOW_STATUS_OPEN.getI10n('value')} <div class="ui  circular label">${openWorkflows.size()}</div>--}%
%{--    </a>--}%
%{--    <a class="${tab == 'done'  ? 'active item':'item'}" data-tab="done">--}%
%{--        ${RDStore.WF_WORKFLOW_STATUS_DONE.getI10n('value')} <div class="ui  circular label">${doneWorkflows.size()}</div>--}%
%{--    </a>--}%
%{--</div>--}%

%{--<g:each in="${['open':openWorkflows, 'done':doneWorkflows]}" var="wfmap">--}%
%{--    <g:set var="currentTab" value="${wfmap.key}" />--}%
%{--    <g:set var="currentWorkflows" value="${wfmap.value}" />--}%

%{--    <div class="ui bottom attached tab ${tab == currentTab ? 'active':''}" data-tab="${currentTab}">--}%
%{--    <g:if test="${tab == currentTab}">--}%

%{--        <div>--}%

<table class="ui celled table la-js-responsive-table la-table">
    <thead>
        <tr>
            <th class="one wide" rowspan="2">${message(code:'sidewide.number')}</th>
            <th class="three wide" rowspan="2">${message(code:'workflow.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'default.relation.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="two wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
            <th class="two wide" rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="wf" status="wfi">
            <g:set var="clistInfo" value="${wf.getInfo()}" />

            <tr>
                <td class="center aligned">
                    ${wfi + 1 + offset}
                </td>
                <td>
                    <g:if test="${currentTab != 'open'}">
                        <uiWorkflow:statusIcon checklist="${wf}" size="normal" />
                    </g:if>
                    <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[info: '' + clistInfo.target.class.name + ':' + clistInfo.target.id + ':' + WfChecklist.KEY + ':' + wf.id]}">
                        <strong>${wf.title}</strong>
                    </g:link>
                </td>
                <td>
                    <div class="la-flexbox">
                        <i class="ui icon ${clistInfo.targetIcon} la-list-icon"></i>
                        <g:link controller="${clistInfo.targetController}" action="show" params="${[id: clistInfo.target.id]}">
                            ${clistInfo.targetName}
                            <br/>
                            <g:if test="${clistInfo.target instanceof Subscription || clistInfo.target instanceof License}">
                                <g:if test="${clistInfo.target.startDate || clistInfo.target.endDate}">
                                    (${clistInfo.target.startDate ? DateUtils.getLocalizedSDF_noTime().format(clistInfo.target.startDate) : ''} -
                                    ${clistInfo.target.endDate ? DateUtils.getLocalizedSDF_noTime().format(clistInfo.target.endDate) : ''})
                                </g:if>
                            </g:if>
                        </g:link>
                    </div>
                </td>
                <td>
                    <div class="ui buttons">
                        <g:set var="cpoints" value="${wf.getSequence()}" />
                        <g:each in="${cpoints}" var="cpoint" status="cp">
                            <uiWorkflow:checkpoint checkpoint="${cpoint}" params="${[key: 'myInstitution:' + cpoint.id + ':' + WfCheckpoint.KEY + ':' + cpoint.id]}" />
                        </g:each>
                    </div>
                </td>
                <td>
                    ${DateUtils.getLocalizedSDF_noTime().format(clistInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                </td>
                <td class="center aligned">
                    <g:if test="${workflowLightService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'myInstitution:' + wf.id + ':' + WfChecklist.KEY + ':' + wf.id]}" />--}%
                        <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon pencil"></i></button>
                    </g:if>
                    <g:elseif test="${workflowLightService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'myInstitution:' + wf.id + ':' + WfChecklist.KEY + ':' + wf.id]}" />--}%
                        <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon pencil"></i></button>
                    </g:elseif>
                    <g:if test="${workflowLightService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                                data-confirm-term-how="delete"
                                controller="myInstitution" action="currentWorkflows" params="${[cmd:"delete:${WfChecklist.KEY}:${wf.id}"]}"
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

%{--        </div>--}%

%{--        <ui:paginate action="currentWorkflows" controller="myInstitution"--}%
%{--                     max="${max}" offset="${offset}"--}%
%{--                     total="${(currentTab == 'open' ? openWorkflows.size() : doneWorkflows.size())}"--}%
%{--                     params="${params + [tab: currentTab]}" />--}%

%{--    </g:if>--}%
%{--    </div>--}%

%{--</g:each>--}%

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
</laser:script>

<laser:htmlEnd />
