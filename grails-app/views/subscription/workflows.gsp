<%@ page import="de.laser.helper.DateUtils; de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.RefdataCategory; de.laser.workflow.*; de.laser.WorkflowService; de.laser.helper.WorkflowHelper" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'workflow.plural')}</title>
    </head>
<body>

    <g:render template="breadcrumb" model="${[ subscription:subscription, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="history" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <g:render template="nav" />

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
            <th>${message(code:'default.progress.label')}</th>
            <th>${message(code:'workflow.dates.plural')}</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${workflows}" var="wf">
            <g:set var="wfInfo" value="${wf.getInfo()}" />
            <tr>
                <td>
                    <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'subscription:' + subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                        <i class="ui icon large ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i> ${wf.title}
                    </g:link>
                </td>
                <td>
                    <g:set var="tasks" value="${wf.getSequence()}" />
                    <g:each in="${tasks}" var="task" status="ti">
                        <g:if test="${task.child}">
                            [
                                <laser:workflowTask task="${task}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}" />

                                <g:set var="children" value="${task.child.getSequence()}" />
                                <g:each in="${children}" var="child" status="ci">
                                    <laser:workflowTask task="${child}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + child.id]}" />
                                </g:each>
                            ]
                        </g:if>
                        <g:else>
                            <laser:workflowTask task="${task}" params="${[key: 'subscription:' + subscription.id + ':' + WfTask.KEY + ':' + task.id]}" />
                        </g:else>
                    </g:each>
                </td>
                <td>
                    ${DateUtils.getSDF_NoTime().format(wfInfo.lastUpdated)}
                    <br />
                    ${DateUtils.getSDF_NoTime().format(wf.dateCreated)}
                </td>
                <td class="x">
                    <button class="ui small icon button" data-wfId="${wf.id}"><i class="icon info"></i></button>
                    <button class="ui small icon button" onclick="alert('Editierfunktion für Einrichtungsadministratoren. Noch nicht implementiert.')"><i class="icon cogs"></i></button>
                    <g:link class="ui red icon small button" controller="subscription" action="workflows" params="${[id: "${subscription.id}", cmd: "delete:${WfWorkflow.KEY}:${wf.id}"]}"><i class="trash alternate icon"></i></g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>


    <g:each in="${workflows}" var="wf"> %{-- TMP : TODO --}%

        <div data-wfId="${wf.id}" style="margin-top:5em; margin-bottom:5em; position:relative; display:none;">

            <div class="ui header center aligned">Detailansicht</div>

            <table class="ui celled table la-table">
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
                            <i class="ui icon big ${WorkflowHelper.getCssIconAndColorByStatus(wf.status)}"></i>
                        </td>
                        <td>
                            <div class="header"><strong>${wf.title}</strong></div>
                            <div class="description">
                                ${wf.description}
                                <br />
                                <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                                    <i class="icon clipboard"></i>${wf.subscription.name}
                                </g:link>
                                <g:if test="${wf.comment}">
                                    <div style="margin: 1em; padding-left: 1em; border-left: 5px solid #E0E0E0">
                                        ${wf.comment}
                                    </div>
                                </g:if>
                                <div class="ui right aligned">
                                    Zuletzt bearbeitet am: ${DateUtils.getSDF_NoTime().format(wfInfo.lastUpdated)}<br />
                                    Erstellt am: ${DateUtils.getSDF_NoTime().format(wf.dateCreated)}
                                </div>
                            </div>
                        </td>
                        <td class="x">
                            <g:set var="wfKey" value="subscription:${subscription.id}:${WfWorkflow.KEY}:${wf.id}" />
                            <g:link class="wfModalLink ui button icon compact" controller="ajaxHtml" action="useWfXModal" params="${[key: wfKey, info: wfKey]}">
                                <i class="ui icon pencil"></i>
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
                                        <div style="margin: 1em; padding-left: 1em; border-left: 5px solid #E0E0E0">
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
                                        </div>
                                    </div>
                                </g:if>

                            </td>
                            <td class="x">
                                <g:set var="tKey" value="subscription:${subscription.id}:${WfTask.KEY}:${task.id}" />
                                <g:link class="wfModalLink ui button icon compact" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                    <i class="ui icon pencil"></i>
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
                                                <div style="margin: 1em; padding-left: 1em; border-left: 5px solid #E0E0E0">
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
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td class="x">
                                        <g:set var="tKey" value="subscription:${subscription.id}:${WfTask.KEY}:${child.id}" />
                                        <g:link class="wfModalLink ui button icon compact" controller="ajaxHtml" action="useWfXModal" params="${[key: tKey, info: wfKey]}">
                                            <i class="ui icon pencil"></i>
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

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.wfModalLink').on('click', function(e) {
            e.preventDefault();
            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), true);
            func();
        });
        $('button[data-wfId]').on('click', function(e) {
            $('div[data-wfId]').hide();

            if (! $(this).hasClass('grey')) {
                $('button[data-wfId]').removeClass('grey');
                $('div[data-wfId=' + $(this).addClass('grey').attr('data-wfId') + ']').show();
            } else {
                $('button[data-wfId]').removeClass('grey');
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
