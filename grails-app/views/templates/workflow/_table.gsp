<%@ page import="de.laser.WorkflowService; de.laser.workflow.*; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
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

<table class="ui celled table la-js-responsive-table la-table">
    <thead>
    <tr>
        <th class="one wide" rowspan="2">${message(code:'default.status.label')}</th>
        <th class="six wide" rowspan="2">${message(code:'workflow.label')}</th>
        <th class="five wide" rowspan="2">${message(code:'default.progress.label')}</th>
        <th class="three wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
        <th class="one wide" rowspan="2">${message(code:'default.actions.label')}</th>
    </tr>
    <tr>
        <th class="two wide la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
    <tr>
    </thead>
    <tbody>
    <g:each in="${workflows}" var="wf">
        <g:set var="wfInfo" value="${wf.getInfo()}" />
        <tr>
            <td>
                <uiWorkflow:statusIcon workflow="${wf}" size="large" />
            </td>
            <td>
                <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: '' + wfInfo.target.class.name + ':' + wfInfo.target.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
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
            <td class="x">
                <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                    <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon edit"></i></button>
                    %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
                </g:if>
                <g:elseif test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
                    <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="ellipsis horizontal icon"></i></button>
                    %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
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