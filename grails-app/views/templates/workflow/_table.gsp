<%@ page import="de.laser.workflow.light.WfCheckpoint; de.laser.workflow.light.WfChecklist; de.laser.utils.AppUtils; de.laser.WorkflowService; de.laser.workflow.*; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

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

%{-- CHECKLISTS --}%
<g:if test="${checklists}">
<table class="ui celled table la-js-responsive-table la-table">
    <thead>
        <tr>
            <th class="one wide" scope="col" rowspan="2">${message(code:'default.status.label')}</th>
            <th class="six wide" scope="col" rowspan="2">${message(code:'workflow.label')}</th>
            <th class="five wide" scope="col" rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="two wide la-smaller-table-head" scope="col" >${message(code:'default.lastUpdated.label')}</th>
            <th class="two wide" scope="col" rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head" scope="col" >${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${checklists}" var="clist">
            <g:set var="clistInfo" value="${clist.getInfo()}" />
            <tr>
                <td class="center aligned">
                    <uiWorkflow:statusIcon checklist="${clist}" size="normal" />
                </td>
                <td>
                    <strong>${clist.title}</strong>
                    <g:if test="${clist.description}">
                        <br /> ${clist.description}
                    </g:if>
                </td>
                <td>
                    <div class="ui buttons">
                        <g:set var="cpoints" value="${clist.getSequence()}" />
                        <g:each in="${cpoints}" var="cpoint" status="ci">
                            <uiWorkflow:checkpoint checkpoint="${cpoint}" params="${[key: '' + clistInfo.target.class.name + ':' + clistInfo.target.id + ':' + WfCheckpoint.KEY + ':' + cpoint.id]}" />
                        </g:each>
                    </div>
                <td>
                    ${DateUtils.getLocalizedSDF_noTime().format(clistInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                </td>
                <td class="center aligned">
                    <g:if test="${workflowLightService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${clist}" params="${[key: wfLinkParam]}" />--}%
                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>
                    </g:if>
                    <g:elseif test="${workflowLightService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
%{--                        <uiWorkflow:usageIconLinkButton workflow="${clist}" params="${[key: wfLinkParam]}" />--}%
                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>
                    </g:elseif>
                    <g:if test="${workflowLightService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                data-confirm-term-how="delete"
                                controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}"]}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash alternate outline icon"></i>
                        </g:link>
                    </g:if>
                </td>
        </g:each>
    </tbody>
</table>
</g:if>

<g:if test="${AppUtils.getCurrentServer() in [AppUtils.DEV, AppUtils.LOCAL]}">

<br />

%{-- WORKFLOWS --}%
<g:if test="${workflows}">
<table class="ui celled table la-js-responsive-table la-table">
    <thead>
    <tr>
        <th class="one wide" scope="col" rowspan="2">${message(code:'default.status.label')}</th>
        <th class="six wide" scope="col" rowspan="2">${message(code:'workflow.label')}</th>
        <th class="five wide" scope="col" rowspan="2">${message(code:'default.progress.label')}</th>
        <th class="two wide la-smaller-table-head" scope="col" >${message(code:'default.lastUpdated.label')}</th>
        <th class="two wide" scope="col" rowspan="2">${message(code:'default.actions.label')}</th>
    </tr>
    <tr>
        <th class="two wide la-smaller-table-head" scope="col" >${message(code:'default.dateCreated.label')}</th>
    <tr>
    </thead>
    <tbody>
    <g:each in="${workflows}" var="wf">
        <g:set var="wfInfo" value="${wf.getInfo()}" />
        <g:set var="wfLinkParam" value="${wfInfo.target.class.name + ':' + wfInfo.target.id + ':' + WfWorkflow.KEY + ':' + wf.id}" />

        <tr>
            <td class="center aligned">
                <uiWorkflow:statusIcon workflow="${wf}" size="normal" />
            </td>
            <td>
                <g:link controller="${wfInfo.targetController}" action="workflows" id="${wfInfo.target.id}" params="${[info: wfLinkParam]}">
                    ${wf.title}
                </g:link>
            </td>
            <td>
                <div class="ui buttons">
                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <uiWorkflow:task task="${task}" params="${[key: '' + wfInfo.target.class.name + ':' + wfInfo.target.id + ':' + WfTask.KEY + ':' + task.id]}" />
                    </g:each>
                </div>
            </td>
            <td>
                ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}
                <br />
                ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
            </td>
            <td class="center aligned">
                <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                    <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: wfLinkParam]}" />
                    <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon pencil"></i></button>
                </g:if>
                <g:elseif test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
                    <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: wfLinkParam]}" />
                    <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon pencil"></i></button>
                </g:elseif>
                <g:if test="${workflowService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                    <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                            data-confirm-term-how="delete"
                            controller="${wfInfo.targetController}" action="workflows" id="${wfInfo.target.id}" params="${[cmd:"delete:${WfWorkflow.KEY}:${wf.id}"]}"
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
</g:if>

<div id="wfModal" class="ui modal"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
    $('button[data-wfId]').on('click', function(e) {
        var trigger = $(this).hasClass('la-modern-button');
        $('div[data-wfId]').hide();
        $('button[data-wfId]').addClass('la-modern-button');
        if (trigger) {
            $('div[data-wfId=' + $(this).removeClass('la-modern-button').attr('data-wfId') + ']').show();
        }
    });

    <g:if test="${info}">
        $('button[data-wfId=' + '${info}'.split(':')[3] + ']').trigger('click');
    </g:if>
    <g:else>
        if ($('button[data-wfId]').length == 1) {
            $('button[data-wfId]').trigger('click');
        }
    </g:else>
</laser:script>

</g:if> %{-- [AppUtils.DEV, AppUtils.LOCAL] --}%