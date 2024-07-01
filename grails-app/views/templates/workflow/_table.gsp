<%@ page import="de.laser.helper.Icons; de.laser.workflow.WfCheckpoint; de.laser.workflow.WfChecklist; de.laser.WorkflowService; de.laser.utils.AppUtils; de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

%{-- CHECKLISTS --}%
<g:if test="${checklists}">

<table class="ui celled table la-js-responsive-table la-table">
    <thead>
        <tr>
            <th class="one wide" scope="col" rowspan="2">${message(code:'default.status.label')}</th>
            <th class="six wide" scope="col" rowspan="2">${message(code:'workflow.label')}</th>
            <th class="five wide" scope="col" rowspan="2">${message(code:'default.progress.label')}</th>
            <th class="two wide la-smaller-table-head" scope="col">${message(code:'default.lastUpdated.label')}</th>
            <th class="two wide" scope="col" rowspan="2">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head" scope="col">${message(code:'default.dateCreated.label')}</th>
        <tr>
    </thead>
    <tbody>
        <g:each in="${workflowService.sortByLastUpdated(checklists)}" var="clist">%{-- !? sortBy bug --}%
            <g:set var="clistInfo" value="${clist.getInfo()}" />
            <tr>
                <td class="center aligned">
                    <uiWorkflow:statusIcon checklist="${clist}" size="normal" />
                </td>
                <td>
                    <strong>${clist.title}</strong>
                    <g:if test="${clist.template}">&nbsp;&nbsp;<span class="ui label tiny">Vorlage</span></g:if>
                    <g:if test="${clist.description}"><br /> ${clist.description}</g:if>
                </td>
                <td>
                    <div class="ui buttons">
                        <g:set var="cpoints" value="${clist.getSequence()}" />
                        <g:each in="${cpoints}" var="cpoint" status="ci">
                            <uiWorkflow:checkpoint checkpoint="${cpoint}" params="${[key: '' + clistInfo.target.class.name + ':' + clistInfo.target.id + ':workflows:' + WfCheckpoint.KEY + ':' + cpoint.id]}" />
                        </g:each>
                    </div>
                <td>
                    ${DateUtils.getLocalizedSDF_noTime().format(clistInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                </td>
                <td class="center aligned">
                    <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>
                    </g:if>
                    <g:elseif test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
                        <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon search"></i></button>
                    </g:elseif>
                    <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                data-confirm-term-how="delete"
                                controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}"]}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icons.CMD_DELETE} icon"></i>
                        </g:link>
                    </g:if>
                </td>
        </g:each>
    </tbody>
</table>

</g:if>

<div id="wfModal" class="ui modal"></div>
<div id="wfFlyout" class="ui eight wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.wfModalLink').on ('click', function(e) {
        e.preventDefault();
        var func = bb8.ajax4SimpleModalFunction ("#wfModal", $(e.currentTarget).attr ('href'));
        func();
    });

    $('button[data-wfId]').on ('click', function(e) {
        var trigger = $(this).hasClass ('la-modern-button');
        var key     = "${WfChecklist.KEY}:" + $(this).attr ('data-wfId');

        $('button[data-wfId]').addClass ('la-modern-button');
        $('#wfFlyout').flyout ({
            onHidden: function (e) { %{-- after animation --}%
                $('button[data-wfId]').addClass ('la-modern-button');
                document.location = document.location.origin + document.location.pathname;
            }
        });

        if (trigger) {
            $(this).removeClass ('la-modern-button');

            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="workflowFlyout"/>",
                data: {
                    key: key
                }
            }).done (function (response) {
                $('#wfFlyout').html (response).flyout ('show');
                r2d2.initDynamicUiStuff ('#wfFlyout');
                r2d2.initDynamicXEditableStuff ('#wfFlyout');
            })
        }
    });

    <g:if test="${info}">
        $('button[data-wfId=' + '${info}'.split(':')[3] + ']').trigger ('click');
    </g:if>
</laser:script>
