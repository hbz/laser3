<%@ page import="java.time.LocalDate; de.laser.system.SystemMessage; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.CustomerTypeService; de.laser.workflow.WfChecklist; de.laser.workflow.WfCheckpoint; de.laser.storage.RDStore; de.laser.utils.DateUtils; de.laser.workflow.WorkflowHelper; de.laser.UserSetting; de.laser.storage.RDConstants; de.laser.AccessService; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated;" %>

<laser:htmlStart message="menu.institutions.dash" />

        <ui:breadcrumbs>
            <ui:crumb text="${message(code:'menu.institutions.dash')}" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon text="${contextService.getOrg().name}" type="${contextService.getOrg().getCustomerType()}"/>

        <laser:render template="/templates/system/messages" model="${[type: SystemMessage.TYPE_DASHBOARD]}"/>

        <g:if test="${dashboardService.showTopMenu()}">
            <laser:render template="/myInstitution/dashboard/topmenu" />
        </g:if>

        <ui:messages data="${flash}" />

    <%
        String currentTab
        switch (params.view) {
            case "Workflows":   currentTab = RefdataValue.getByValueAndCategory('Workflows', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            default:
                                currentTab = user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB, RDStore.US_DASHBOARD_TAB_DUE_DATES)
            break
        }
    %>

    <div class="ui tabular la-tab-with-js top attached small menu">
        <a class="${currentTab == 'Service Messages' ? 'active item' : 'item'}" data-tab="servicemessages">
            ${message(code:'serviceMessage.plural')} <ui:bubble count="${serviceMessages.size()}" />
        </a>

        <a class="${currentTab == 'Due Dates' ? 'active item':'item'}" data-tab="duedates">
            ${message(code:'myinst.dash.due_dates.label')} <ui:bubble count="${dueDatesCount}" />
        </a>

        <g:if test="${taskService.hasREAD()}">
            <a class="${currentTab == 'Tasks' ? 'active item':'item'}" data-tab="tasks">
                ${message(code:'myinst.dash.task.label')} <ui:bubble count="${tasks.size()}${tasksCount > tasks.size() ? '+' : ''}" />
            </a>
        </g:if>

        <g:if test="${workflowService.hasREAD()}">
            <a class="${currentTab == 'Workflows' ? 'active item':'item'}" data-tab="workflows">
                ${message(code:'workflow.plural')} <ui:bubble count="${wfList.size()}${wfListCount > wfList.size() ? '+' : ''}" />
            </a>
        </g:if>

    </div><!-- secondary -->
        <div class="ui bottom attached segment tab ${currentTab == 'Due Dates' ? 'active':''}" data-tab="duedates">
            <g:render template="dashboardTabHelper" model="${[tmplKey: 'DUEDATES']}" />

            <laser:render template="/user/dueDatesView" model="[user: user, dueDates: dueDates, dueDatesCount: dueDatesCount]"/>
        </div>

        <div class="ui bottom attached segment tab ${currentTab == 'Service Messages' ? 'active' : ''}" data-tab="servicemessages">
            <g:render template="dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_SERVICE_MESSAGES]}" />

            <g:if test="${serviceMessages.size() > 0 }">
                <div class="">
                    <g:each in="${serviceMessages}" var="msg">

                        <div class="ui icon message" style="line-height:1.5em">
                            <i class="${Icon.SERVICE_MESSAGE} ${(DateUtils.dateToLocalDate(msg.lastPublishingDate) >= LocalDate.now().minusDays(14)) ? 'blue':'grey'}"></i>
                            <div class="content">
                                <div class="header">
                                    <% print msg.title; /* avoid auto encodeAsHTML() */ %>
                                </div>
                                <div style="margin:0.5em 0">
                                    <% print msg.content; /* avoid auto encodeAsHTML() */ %>
                                </div>
                                <div style="text-align:right">
                                    <g:formatDate date="${msg.lastPublishingDate}" formatName="default.date.format.notime"/>
                                    ${DateUtils.getSDF_onlyTime().format(msg.lastPublishingDate)}
                                </div>
                            </div>
                        </div>
                    </g:each>
                </div>
            </g:if>
        </div>

        <div class="ui bottom attached segment tab ${currentTab == 'Tasks' ? 'active':''}" data-tab="tasks">
            <g:render template="dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_TASKS]}" />

            <div class="ui cards">
                <g:each in="${tasks}" var="tsk">
                    <g:render template="/templates/tasks/dashboardCard" model="${[tsk: tsk]}" />
                </g:each>
            </div>
        </div>

        <g:if test="${workflowService.hasREAD()}"><!-- TODO: workflows-permissions -->
            <div id="wfFlyout" class="ui very wide flyout"></div>

            <div class="ui bottom attached segment tab ${currentTab == 'Workflows' ? 'active':''}" data-tab="workflows">

                <g:if test="${wfList}">
                    <g:render template="dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_WORKFLOWS]}" />

                    <table class="ui celled table la-js-responsive-table la-table">
                        <thead>
                            <tr>
                                <th class="four wide" rowspan="2">${message(code:'workflow.label')}</th>
                                <th class="four wide" rowspan="2">${message(code:'default.relation.label')}</th>
                                <th class="four wide" rowspan="2">${message(code:'default.progress.label')}</th>
                                <th class="two wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
                                <th class="two wide center aligned" rowspan="2">
                                    <ui:optionsIcon />
                                </th>
                            </tr>
                            <tr>
                                <th class="la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
                            <tr>
                        </thead>
                        <tbody>
                            <g:each in="${workflowService.sortByLastUpdated(wfList)}" var="clist">%{-- !? sorting--}%
                                <g:set var="clistInfo" value="${clist.getInfo()}" />
                                <g:set var="clistLinkParamPart" value="${clistInfo.target.id + ':' + WfChecklist.KEY + ':' + clist.id}" />
                                <tr>
                                    <td>
                                        <div class="la-flexbox">
                                            <i class="${Icon.WORKFLOW} la-list-icon"></i>
                                            <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}"
                                                    params="${[info: '' + clistInfo.target.class.name + ':' + clistLinkParamPart]}">
                                                <strong>${clist.title}</strong>
                                            </g:link>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="la-flexbox">
                                            <i class="icon ${clistInfo.targetIcon} la-list-icon"></i>
                                            <g:link controller="${clistInfo.targetController}" action="show" params="${[id: clistInfo.target.id]}">
                                                ${clistInfo.targetName}
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
                                                <uiWorkflow:checkpoint checkpoint="${cpoint}" params="${[key: 'myInstitution::dashboard:' + WfCheckpoint.KEY + ':' + cpoint.id]}" />
                                            </g:each>
                                        </div>
                                    </td>
                                    <td>
                                        ${DateUtils.getLocalizedSDF_noTime().format(clistInfo.lastUpdated)}
                                        <br />
                                        ${DateUtils.getLocalizedSDF_noTime().format(clist.dateCreated)}
                                    </td>
                                    <td class="center aligned">
                                        <g:if test="${workflowService.hasWRITE()}"><!-- TODO: workflows-permissions -->
                                            <button class="${Btn.MODERN.SIMPLE}" data-wfId="${clist.id}"><i class="${Icon.CMD.EDIT}"></i></button>

                                            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                                    data-confirm-term-how="delete"
                                                    controller="myInstitution" action="dashboard" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}", view:'Workflows']}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="${Icon.CMD.DELETE}"></i>
                                            </g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </g:if>
            </div>

            <div id="wfModal" class="ui modal"></div>
        </g:if>

    <laser:script file="${this.getGroovyPageFileName()}">

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }

            JSPC.app.dashboard.initWorkflows = function() {
                $('.wfModalLink').on('click', function(e) {
                    e.preventDefault();
                    var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'));
                    func();
                });

                $('button[data-wfId]').on ('click', function(e) {
                    var trigger = $(this).hasClass ('la-modern-button');
                    var key     = "${WfChecklist.KEY}:" + $(this).attr ('data-wfId');

                    $('button[data-wfId]').addClass ('la-modern-button');
                    $('#wfFlyout').flyout ({
                        onHidden: function (e) { %{-- after animation --}%
                            $('button[data-wfId]').addClass ('la-modern-button');
                            document.location = document.location.origin + document.location.pathname + '?view=Workflows';
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
            };

            JSPC.app.dashboard.editTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
                func();
            };
            JSPC.app.dashboard.readTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalReadTask", "<g:createLink controller="ajaxHtml" action="readTask"/>?id=" + id);
                func();
            };

        JSPC.app.dashboard.initWorkflows()
    </laser:script>
<laser:htmlEnd />
