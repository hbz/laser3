<%@ page import="de.laser.workflow.WfCheckpoint; de.laser.workflow.WfChecklist; de.laser.License; de.laser.Subscription; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.*; de.laser.RefdataCategory; de.laser.WorkflowService" %>

<laser:htmlStart message="menu.my.workflows" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.my.workflows" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.workflows" type="Workflow" total="${total}" floated="true" />

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
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <ui:select class="ui dropdown" name="filterStatus"
                          from="${[RDStore.WF_WORKFLOW_STATUS_OPEN, RDStore.WF_WORKFLOW_STATUS_DONE]}"
                          optionKey="id"
                          optionValue="value"
                          value="${filterStatus}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code: 'workflow.template.plural')}</label>
                <g:select class="ui dropdown" name="filterTemplates"
                          from="${ ['yes':'Nur Vorlagen', 'no':'Keine Vorlagen'] }"
                          optionKey="${{it.key}}"
                          optionValue="${{it.value}}"
                          value="${filterTemplates}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="field la-field-right-aligned">
            <g:link controller="myInstitution" action="currentWorkflows" params="${[filter: 'reset']}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</g:link>
            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}" />
        </div>
        <input type="hidden" name="filter" value="true" />
%{--        <input type="hidden" name="tab" value="${tab}" />--}%
    </form>
</ui:filter>

<g:if test="${status == workflowService.OP_STATUS_DONE}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="positive" message="workflow.delete.ok" />
    </g:if>
    <g:else>
        <ui:msg class="positive" message="workflow.edit.ok" />
    </g:else>
</g:if>
<g:elseif test="${status == workflowService.OP_STATUS_ERROR}">
    <g:if test="${cmd == 'delete'}">
        <ui:msg class="negative" message="workflow.delete.error" />
    </g:if>
    <g:else>
        <ui:msg class="negative" message="workflow.edit.error" />
    </g:else>
</g:elseif>

<table class="ui sortable celled table la-js-responsive-table la-table">
    <thead>
        <tr>
            <th class="one wide" rowspan="2">${message(code:'sidewide.number')}</th>
            <th class="one wide" rowspan="2"></th>
            <th class="four wide" rowspan="2">${message(code:'workflow.label')}</th>
            <th class="four wide" rowspan="2">${message(code:'default.relation.label')}</th>
            <th class="three wide" rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="two wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
            <th class="one wide" rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${currentWorkflows}" var="clist" status="ci">
            <g:set var="clistInfo" value="${clist.getInfo()}" />

            <tr>
                <td class="center aligned">
                    ${ci + 1 + offset}
                </td>
                <td class="center aligned">
                    <uiWorkflow:statusIcon checklist="${clist}" size="normal" />
                </td>
                <td style="position:relative">
                    <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[info: '' + clistInfo.target.class.name + ':' + clistInfo.target.id + ':' + WfChecklist.KEY + ':' + clist.id]}">
                        <strong>${clist.title}</strong>
                    </g:link>
                    <g:if test="${clist.template}">&nbsp;&nbsp;<span class="ui label tiny">Vorlage</span></g:if>
                    <g:if test="${clist.description}"><br /> ${clist.description}</g:if>
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
                        <g:set var="cpoints" value="${clist.getSequence()}" />
                        <g:each in="${cpoints}" var="cpoint" status="cp">
                            <uiWorkflow:checkpoint checkpoint="${cpoint}" params="${[key: 'myInstitution::currentWorkflows:' + WfCheckpoint.KEY + ':' + cpoint.id]}" />
                        </g:each>
                    </div>
                </td>
                <td>
                    ${DateUtils.getLocalizedSDF_noTime().format(clistInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                </td>
                <td class="center aligned">
                    <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'myInstitution:' + clist.id + ':' + WfChecklist.KEY + ':' + clist.id]}" />--}%
%{--                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>--}%
                    </g:if>
                    <g:elseif test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'myInstitution:' + clist.id + ':' + WfChecklist.KEY + ':' + clist.id]}" />--}%
%{--                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>--}%
                    </g:elseif>
                    <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                data-confirm-term-how="delete"
                                controller="myInstitution" action="currentWorkflows" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}"]}"
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

<ui:paginate action="currentWorkflows" controller="myInstitution" max="${max}" offset="${offset}" total="${total}" />

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'));
        func();
    });
</laser:script>

<laser:htmlEnd />
