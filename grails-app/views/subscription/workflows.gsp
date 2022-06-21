<%@ page import="de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.workflow.WorkflowHelper" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'workflow.plural')}</title>
    </head>
<body>

    <laser:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
        <laser:render template="iconSubscriptionIsChild"/>
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <laser:render template="nav" />
    <laser:render template="message"/>

    <g:if test="${status == WorkflowService.OP_STATUS_DONE}">
        <g:if test="${cmd == 'delete'}">
            <semui:msg class="positive" message="workflow.delete.ok" />
        </g:if>
        <g:else>
            <semui:msg class="positive" message="workflow.edit.ok" />
        </g:else>
    </g:if>
    <g:elseif test="${status == WorkflowService.OP_STATUS_ERROR}">
        <g:if test="${cmd == 'delete'}">
            <semui:msg class="negative" message="workflow.delete.error" />
        </g:if>
        <g:else>
            <semui:msg class="negative" message="workflow.edit.error" />
        </g:else>
    </g:elseif>

    <table class="ui celled table la-js-responsive-table la-table">
        <thead>
            <tr>
                <th rowspan="2">${message(code:'default.status.label')}</th>
                <th rowspan="2">${message(code:'workflow.label')}</th>
                <th rowspan="2">${message(code:'default.progress.label')}</th>
                <th class="la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
                <th rowspan="2">${message(code:'default.actions.label')}</th>
            </tr>
            <tr>
                <th class="la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
            <tr>
        </thead>
        <tbody>
        <g:each in="${workflows}" var="wf">
            <g:set var="wfInfo" value="${wf.getInfo()}" />
            <tr>
                <td>
                    <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i>

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
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                         ${wf.title}
                    </g:link>
                </td>
                <td>
                    <div class="ui buttons workflowOverrideCss">
                        <g:set var="tasks" value="${wf.getSequence()}" />
                        <g:each in="${tasks}" var="task" status="ti">
                            <g:if test="${task.child}">
                                <div style="width:8px"></div>
                                    <laser:workflowTask task="${task}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}" />

                                    <g:set var="children" value="${task.child.getSequence()}" />
                                    <g:each in="${children}" var="child" status="ci">
                                        <laser:workflowTask task="${child}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + child.id]}" />
                                    </g:each>
                                <div style="width:8px"></div>
                            </g:if>
                            <g:else>
                                <laser:workflowTask task="${task}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}" />
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
                    <button class="ui icon button blue la-modern-button" data-wfId="${wf.id}"><i class="icon edit"></i></button>
                    %{-- <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button> --}%
                    <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wf.title])}"
                            data-confirm-term-how="delete"
                            controller="subscription" action="workflows" id="${subscription.id}" params="${[cmd:"delete:${WfWorkflow.KEY}:${wf.id}"]}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                        <i class="trash alternate outline icon"></i>
                    </g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>


    <g:each in="${workflows}" var="wf"> %{-- TMP : TODO --}%

        <div data-wfId="${wf.id}" style="margin-top:5em; margin-bottom:5em; position:relative; display:none;">

            <div class="ui header center aligned">Detailansicht</div>

            <table class="ui celled table la-js-responsive-table la-table">
                <thead>
                    <tr>
                        <th style="width:10%"></th>
                        <th style="width:80%"></th>
                        <th style="width:10%"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="ui center aligned">
                            <i class="icon big ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i>
                        </td>
                        <td>
                            <div class="header"><strong>${wf.title}</strong></div>
                            <div class="description">
                                ${wf.description}
                                <br />
                                <br />
                                <div class="ui la-flexbox">
                                    <i class="icon clipboard la-list-icon"></i>
                                    <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                                        ${wf.subscription.name}
                                    </g:link>
                                </div>
                                <g:if test="${wf.comment}">
                                    <div style="margin: 1em 2em; padding: 0.1em 0.5em; border-bottom: 1px dashed #BBBBBB">
                                        ${wf.comment}
                                    </div>
                                </g:if>
                                <div class="ui right aligned">
                                    Zuletzt bearbeitet am: ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}<br />
                                    Erstellt am: ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}
                                </div>
                            </div>
                        </td>
                        <td class="x">
                            <g:set var="wfKey" value="subscription:${subscription.id}:${WfWorkflow.KEY}:${wf.id}" />
                            <g:if test="${contextService.getUser().hasAffiliation('INST_ADM') || SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")}"><!-- TODO: reporting-permissions -->
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                    <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: wfKey, info: wfKey]}">
                                        <i class="icon cogs"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: wfKey, info: wfKey]}">
                                <i class="icon pencil"></i>
                            </g:link>
                        </td>
                    </tr>

                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <tr>
                            <td class="ui center aligned">
                                <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(task.status)}"></i>
                            </td>
                            <td>
                                <div class="header">
                                    <strong>${task.title}</strong>
                                    <span style="color: darkgrey">
                                        ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(task.priority)}"></i> ${task.priority.getI10n('value')} )
                                    </span>
                                </div>
                                <div class="description">${task.description}
                                    <g:if test="${task.comment}">
                                        <div style="margin: 1em 2em; padding: 0.1em 0.5em; border-bottom: 1px dashed #BBBBBB">
                                            ${task.comment}
                                        </div>
                                    </g:if>
                                </div>
                                <g:if test="${task.condition}">
                                    <div style="margin:1.5em 0 0 5em">
                                        <div class="header"><strong>${task.condition.title}</strong></div>
                                        <div class="description">
                                            <g:if test="${task.condition.description}">
                                                ${task.condition.description} <br />
                                            </g:if>
                                            <!-- -->
                                            <g:each in="${task.condition.getFields()}" var="field" status="fi">
                                                <br/>
                                                <laser:workflowTaskConditionField condition="${task.condition}" field="${field}" />
                                            </g:each>
                                            <!-- -->
                                            <g:if test="${contextService.getUser().hasAffiliation('INST_ADM') || SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")}"><!-- TODO: reporting-permissions -->
                                                <g:set var="cKey" value="subscription:${subscription.id}:${WfCondition.KEY}:${task.condition.id}" />
                                                <span style="float:right">
                                                    <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                                        <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: cKey, info: wfKey]}">
                                                            <i class="icon cogs"></i>
                                                        </g:link>
                                                    </span>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </g:if>

                            </td>
                            <td class="x">
                                <g:set var="tKey" value="subscription:${subscription.id}:${WfTask.KEY}:${task.id}" />
                                <g:if test="${contextService.getUser().hasAffiliation('INST_ADM') || SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")}"><!-- TODO: reporting-permissions -->
                                    <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                        <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: tKey, info: wfKey]}">
                                            <i class="icon cogs"></i>
                                        </g:link>
                                    </span>
                                </g:if>
                                <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                    <i class="icon pencil"></i>
                                </g:link>
                            </td>
                        </tr>

                        <g:if test="${task.child}">
                            <g:each in="${task.child.getSequence()}" var="child" status="ci">

                                <tr>
                                    <td class="ui center aligned">
                                        <i class="icon large ${WorkflowHelper.getCssIconAndColorByStatus(child.status)}"></i>
                                    </td>
                                    <td>
                                        <div class="header">
                                            <strong>${child.title}</strong>
                                            <span style="color: darkgrey">
                                                ( <i class="icon ${WorkflowHelper.getCssIconByTaskPriority(child.priority)}"></i> ${child.priority.getI10n('value')} )
                                            </span>
                                        </div>
                                        <div class="description">${child.description}
                                            <g:if test="${child.comment}">
                                                <div style="margin: 1em 2em; padding: 0.1em 0.5em; border-bottom: 1px dashed #BBBBBB">
                                                    ${child.comment}
                                                </div>
                                            </g:if>
                                        </div>

                                        <g:if test="${child.condition}">
                                            <div style="margin:1.5em 0 0 5em">
                                                <div class="header"><strong>${child.condition.title}</strong></div>
                                                <div class="description">
                                                    <g:if test="${child.condition.description}">
                                                        ${child.condition.description} <br />
                                                    </g:if>
                                                    <!-- -->
                                                    <g:each in="${child.condition.getFields()}" var="field" status="fi">
                                                        <br/>
                                                        <laser:workflowTaskConditionField condition="${child.condition}" field="${field}" />
                                                    </g:each>
                                                    <!-- -->
                                                    <g:if test="${contextService.getUser().hasAffiliation('INST_ADM') || SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")}"><!-- TODO: reporting-permissions -->
                                                        <g:set var="cKey" value="subscription:${subscription.id}:${WfCondition.KEY}:${child.condition.id}" />
                                                        <span style="float:right">
                                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                                                <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: cKey, info: wfKey]}">
                                                                    <i class="icon cogs"></i>
                                                                </g:link>
                                                            </span>
                                                        </span>
                                                    </g:if>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td class="x">
                                        <g:set var="tKey" value="subscription:${subscription.id}:${WfTask.KEY}:${child.id}" />
                                        <g:if test="${contextService.getUser().hasAffiliation('INST_ADM') || SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")}"><!-- TODO: reporting-permissions -->
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code: 'workflow.edit.ext.perms')}">
                                                <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="editWfXModal" params="${[key: tKey, info: wfKey]}">
                                                    <i class="icon cogs"></i>
                                                </g:link>
                                            </span>
                                        </g:if>
                                        <g:link class="wfModalLink ui icon button blue compact la-modern-button" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                            <i class="icon pencil"></i>
                                        </g:link>
                                    </td>
                                </tr>

                            </g:each>
                        </g:if>
                    </g:each>

                </tbody>
            </table>

        </div>

    </g:each>

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

</body>
</html>
