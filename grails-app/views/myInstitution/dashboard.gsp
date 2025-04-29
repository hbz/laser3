<%@ page import="de.laser.system.SystemMessage; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.CustomerTypeService; de.laser.workflow.WfChecklist; de.laser.workflow.WfCheckpoint; de.laser.storage.RDStore; de.laser.utils.DateUtils; de.laser.workflow.WorkflowHelper; de.laser.UserSetting; de.laser.system.SystemAnnouncement; de.laser.storage.RDConstants; de.laser.AccessService; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated;" %>

<laser:htmlStart message="menu.institutions.dash" />

        <ui:breadcrumbs>
            <ui:crumb text="${message(code:'menu.institutions.dash')}" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon text="${contextService.getOrg().name}" type="${contextService.getOrg().getCustomerType()}"/>

        <laser:render template="/templates/system/messages" model="${[type: SystemMessage.TYPE_DASHBOARD]}"/>

        <laser:render template="/myInstitution/dashboard/topmenu" />

        <ui:messages data="${flash}" />

        <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
            <g:if test="${user.getSetting(UserSetting.KEYS.DASHBOARD_SHOW_CHARTS, RDStore.YN_YES).value == RDStore.YN_YES}">
                <laser:render template="/myInstitution/dashboard/dataviz_inst" />
            </g:if>
            <laser:render template="/myInstitution/dashboard/testSubscriptions" model="${[cts: currentTestSubscriptions]}"/>
        </g:if>

        <div class="ui two cards">
            <laser:render template="/myInstitution/dashboard/rttp" />
            <laser:render template="/myInstitution/dashboard/wekbNews" model="${[wekbNews: wekbNews, tmplView: 'info']}"/>
        </div>

    <%
        RefdataValue us_dashboard_tab
        switch (params.view) {
            case "PendingChanges": us_dashboard_tab = RefdataValue.getByValueAndCategory('PendingChanges', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            case "AcceptedChanges": us_dashboard_tab = RefdataValue.getByValueAndCategory('AcceptedChanges', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            case "Surveys": us_dashboard_tab = RefdataValue.getByValueAndCategory('Surveys', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            case "Workflows": us_dashboard_tab = RefdataValue.getByValueAndCategory('Workflows', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            default:
                us_dashboard_tab = user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB, RDStore.US_DASHBOARD_TAB_DUE_DATES)
            break
        }
    %>

    <div class="ui tabular la-tab-with-js top attached small stackable menu">
        <a class="${us_dashboard_tab.value == 'Announcements' ? 'active item':'item'}" data-tab="news" id="jsFallbackAnnouncements">
            %{--            <i class="${Icon.ANNOUNCEMENT} large"></i>--}%
            ${message(code:'announcement.plural')} <ui:bubble count="${systemAnnouncements.size()}" />
        </a>

        <a class="${us_dashboard_tab.value == 'Due Dates' ? 'active item':'item'}" data-tab="duedates">
%{--            <i class="${Icon.DUE_DATE} large"></i>--}%
            ${message(code:'myinst.dash.due_dates.label')} <ui:bubble count="${dueDatesCount}" />
        </a>

        <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro()}">
            <a class="${us_dashboard_tab.value == 'PendingChanges' ? 'active item':'item'}" data-tab="pendingchanges">
%{--                <i class="history icon large"></i>--}%
                ${message(code:'myinst.pendingChanges.label', args:[''])} <span id="pendingCount" class="ui circular label blue">${message(code: 'myinst.loadPending')}</span>
            </a>
        </g:if>
        <a class="${us_dashboard_tab.value == 'AcceptedChanges' ? 'active item':'item'}" data-tab="acceptedchanges">
%{--            <i class="warning circle icon large"></i>--}%
            ${message(code:'myinst.acceptedChanges.label', args:[''])} <span id="notificationsCount" class="ui circular label blue">${message(code: 'myinst.loadPending')}</span>
        </a>

        <g:if test="${(contextService.getOrg().isCustomerType_Inst() || contextService.getOrg().isCustomerType_Consortium_Pro())}">
            <a class="${us_dashboard_tab.value == 'Surveys' ? 'active item' : 'item'}" data-tab="surveys">
%{--                <i class="${Icon.SURVEY} large"></i>--}%
                ${message(code:'myinst.dash.survey.label', args:[''])} <span id="surveyCount" class="ui circular label blue">${message(code: 'myinst.loadPending')}</span>
            </a>
        </g:if>

        <g:if test="${taskService.hasREAD()}">
            <a class="${us_dashboard_tab.value == 'Tasks' ? 'active item':'item'}" data-tab="tasks">
%{--                <i class="${Icon.TASK} large"></i>--}%
                ${message(code:'myinst.dash.task.label')} <ui:bubble count="${tasksCount}" />
            </a>
        </g:if>

        <g:if test="${workflowService.hasREAD()}">
            <a class="${us_dashboard_tab.value == 'Workflows' ? 'active item':'item'}" data-tab="workflows">
%{--                <i class="${Icon.WORKFLOW} large"></i>--}%
                ${message(code:'workflow.plural')} <ui:bubble count="${allChecklistsCount}" />
            </a>
        </g:if>

    </div>
        <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'Due Dates' ? 'active':''}" data-tab="duedates">
            <div>
                <laser:render template="/user/dueDatesView" model="[user: user, dueDates: dueDates, dueDatesCount: dueDatesCount]"/>
            </div>
        </div>

        <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro()}">
            <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'PendingChanges' ? 'active':''}" data-tab="pendingchanges" id="pendingChanges">
            </div>
        </g:if>
        <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'AcceptedChanges' ? 'active':''}" data-tab="acceptedchanges" id="acceptedChanges">
        </div>
        <div class="ui bottom attached segment tab ${us_dashboard_tab.value =='Announcements' ? 'active':''}" data-tab="news">

            <g:message code="profile.dashboardSysAnnTimeWindow"
                       args="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" />

            <g:if test="${systemAnnouncements.size() > 0 }">
                <br /><br /><br />

                <div class="ui segment la-timeLineSegment-announcement">
                    <div class="la-timeLineGrid">
                        <div class="ui grid stackable">
                            <g:each in="${systemAnnouncements}" var="sa">
                                <div class="row">
                                    <div class="one wide column">
                                        <i class="arrow alternate circle right outline large icon la-timeLineIcon la-timeLineIcon-announcement"></i>
                                    </div><!-- .column -->
                                    <div class="two wide column">
                                        <h2 class="ui header">
                                            <g:formatDate date="${sa.lastPublishingDate}" formatName="default.date.format.notime"/>
                                        </h2>
                                        ${DateUtils.getSDF_onlyTime().format(sa.lastPublishingDate)}
                                    </div><!-- .column -->
                                    <div class="one wide column">
                                        <i class="arrow right small icon"></i>
                                    </div><!-- .column -->
                                    <div class="twelve wide column">
                                        <h2 class="ui header">
                                            <% print sa.title; /* avoid auto encodeAsHTML() */ %>
                                        </h2>
                                        <% print sa.content; /* avoid auto encodeAsHTML() */ %>
                                    </div><!-- .column -->
                                </div><!-- .row -->
                            </g:each>
                        </div><!-- .grid -->
                    </div>
                </div>
            </g:if>
        </div>

        <g:if test="${taskService.hasREAD()}">
        <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'Tasks' ? 'active':''}" data-tab="tasks">

            <div class="ui four columns cards">
                <g:each in="${tasks}" var="tsk">
                    <g:render template="/templates/tasks/dashboardCard" model="${[tsk: tsk]}" />
                </g:each>
            </div>
        </div>

        </g:if>

        <g:if test="${(contextService.getOrg().isCustomerType_Inst() || contextService.getOrg().isCustomerType_Consortium_Pro())}">
            <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'Surveys' ? 'active' : ''}" data-tab="surveys">
                <div class="la-float-right">
                    <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro()}">
                        <g:link controller="survey" action="workflowsSurveysConsortia"
                                class="${Btn.SIMPLE}">${message(code: 'menu.my.surveys')}</g:link>
                    </g:if>
                    <g:else>
                        <g:link action="currentSurveys" class="${Btn.SIMPLE}">${message(code: 'menu.my.surveys')}</g:link>
                    </g:else>
                </div>

                <div id="surveyWrapper">
                    <%--<laser:render template="surveys"/>--%>
                </div>
            </div>
        </g:if>

        <g:if test="${workflowService.hasREAD()}">
            <div id="wfFlyout" class="ui eight wide flyout"></div>

            <div class="ui bottom attached segment tab ${us_dashboard_tab.value == 'Workflows' ? 'active':''}" data-tab="workflows">

                <g:if test="${allChecklists}">
                    <g:if test="${allChecklistsCount > user.getPageSizeOrDefault()}">
                        <ui:msg class="info" hideClose="true">

                            ${message(code:'workflow.dashboard.msg.more', args:[user.getPageSizeOrDefault(), allChecklistsCount,
                                                                                g.createLink(controller:'myInstitution', action:'currentWorkflows', params:[filter:'reset', max:500]) ])}
%{--                        ${message(code:'workflow.dashboard.msg.new', args:[message(code:'profile.dashboardTimeWindow'), user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)])}--}%
                        </ui:msg>
                    </g:if>

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
                            <g:each in="${workflowService.sortByLastUpdated(allChecklists)}" var="clist">%{-- !? sorting--}%
                                <g:set var="clistInfo" value="${clist.getInfo()}" />
                                <g:set var="clistLinkParamPart" value="${clistInfo.target.id + ':' + WfChecklist.KEY + ':' + clist.id}" />
                                <tr>
                                    <td>
                                        <div class="la-flexbox">
                                            <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}"
                                                    params="${[info: '' + clistInfo.target.class.name + ':' + clistLinkParamPart]}">
                                                <strong>${clist.title}</strong>
                                            </g:link>
%{--                                            <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}">--}%
%{--                                                <strong>${clist.title}</strong>--}%
%{--                                            </g:link>--}%
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

            JSPC.app.dashboard.loadChanges = function() {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="getChanges"/>",
                    data: {
                        max: ${max},
                        pendingOffset: ${pendingOffset},
                        acceptedOffset: ${acceptedOffset}
                    }
                }).done(function(response){
                    $("#pendingChanges").html($(response).filter("#pendingChangesWrapper"));
                    $("#acceptedChanges").html($(response).filter("#acceptedChangesWrapper"));
                    r2d2.initDynamicUiStuff('#pendingChanges');
                })
            };

            JSPC.app.dashboard.loadSurveys = function() {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="getSurveys" params="${params}"/>"
                }).done(function(response){
                    $("#surveyWrapper").html(response);
                    r2d2.initDynamicUiStuff('#surveyWrapper');
                })
            };

            JSPC.app.dashboard.editTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
                func();
            };
            JSPC.app.dashboard.readTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalReadTask", "<g:createLink controller="ajaxHtml" action="readTask"/>?id=" + id);
                func();
            };

        JSPC.app.dashboard.loadChanges()
        JSPC.app.dashboard.loadSurveys()
        JSPC.app.dashboard.initWorkflows()
    </laser:script>

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>
<laser:htmlEnd />
