<%@ page import="de.laser.helper.DateUtils; de.laser.helper.WorkflowHelper; de.laser.workflow.WfWorkflow; de.laser.UserSetting; de.laser.system.SystemAnnouncement; de.laser.helper.RDConstants; de.laser.AccessService; de.laser.helper.SqlDateUtils; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'menu.institutions.dash')}</title>
    </head>
    <body>

        <laser:serviceInjection />

        <semui:breadcrumbs>
            <semui:crumb text="${message(code:'menu.institutions.dash')}" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${institution.name}</h1>

        <div class="ui equal width grid la-clear-before">
            <div class="row">

                <div class="column">
                    <div class="ui divided relaxed list">
                        <div class="item">
                            <g:link controller="myInstitution" action="currentSubscriptions">${message(code:'menu.my.subscriptions')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="currentLicenses">${message(code:'menu.my.licenses')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="currentTitles">${message(code:'menu.my.titles')}</g:link>
                        </div>
                    </div>
                </div>

                <div class="column">
                    <div class="ui divided relaxed list">
                        <div class="item">
                            <g:link controller="org" action="show" id="${institution.id}">${message(code: 'menu.institutions.org_info')}</g:link>
                        </div>
                        <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                        <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="reporting" message="myinst.reporting" />
                    </div>
                </div>

                <div class="column">
                    <div class="ui divided relaxed list">
                        <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="myInstitution" action="tasks" message="task.plural" />
                        <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
                        <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
                    </div>
                </div>
            </div>
        </div>

        <semui:messages data="${flash}" />
        <br />
    <%-- should be made overridable by pagination setting --%>
    <%
        def us_dashboard_tab
        switch(params.view) {
            case "PendingChanges": us_dashboard_tab = RefdataValue.getByValueAndCategory('PendingChanges', RDConstants.USER_SETTING_DASHBOARD_TAB)
            break
            case "AcceptedChanges": us_dashboard_tab = RefdataValue.getByValueAndCategory('AcceptedChanges', RDConstants.USER_SETTING_DASHBOARD_TAB)
            break
            case "Surveys": us_dashboard_tab = RefdataValue.getByValueAndCategory('Surveys', RDConstants.USER_SETTING_DASHBOARD_TAB)
                break
            default: us_dashboard_tab = user.getSetting(UserSetting.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))
            break
        }
    %>
    <div class="ui secondary stackable pointing tabular la-tab-with-js menu">
        <a class="${us_dashboard_tab.getValue().value=='Due Dates' || us_dashboard_tab.getValue()=='Due Dates' ? 'active item':'item'}" data-tab="duedates">
            <i class="checked alarm end icon large"></i>
            ${dueDatesCount}
            ${message(code:'myinst.dash.due_dates.label')}
        </a>

        <g:if test="${editable && institution.getCustomerType() in ['ORG_INST', 'ORG_CONSORTIUM']}">
            <a class="${us_dashboard_tab.getValue().value == 'PendingChanges' || us_dashboard_tab.getValue() == 'PendingChanges' ? 'active item':'item'}" data-tab="pendingchanges">
                <i class="history icon large"></i>
                <span id="pendingCount">${message(code:'myinst.pendingChanges.label', args: [message(code:'myinst.loadPending')])}</span>
            </a>
        </g:if>
        <a class="${us_dashboard_tab.getValue().value == 'AcceptedChanges' || us_dashboard_tab.getValue() == 'AcceptedChanges' ? 'active item':'item'}" data-tab="acceptedchanges">
            <i class="bullhorn icon large"></i>
            <span id="notificationsCount">${message(code:'myinst.acceptedChanges.label', args: [message(code:'myinst.loadPending')])}</span>
        </a>

        <a class="${us_dashboard_tab.getValue().value=='Announcements' || us_dashboard_tab.getValue() == 'Announcements' ? 'active item':'item'}" data-tab="news" id="jsFallbackAnnouncements">
            <i class="warning circle icon large"></i>
            ${systemAnnouncements.size()}
            ${message(code:'announcement.plural')}
        </a>

        <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
            <a class="${us_dashboard_tab.getValue().value=='Tasks' || us_dashboard_tab.getValue()=='Tasks' ? 'active item':'item'}" data-tab="tasks">
                <i class="checked calendar icon large"></i>
                ${tasksCount}
                ${message(code:'myinst.dash.task.label')}
            </a>
        </g:if>

        <a class="${us_dashboard_tab.getValue().value=='Surveys' || us_dashboard_tab.getValue()=='Surveys' ? 'active item':'item'}" data-tab="surveys">
            <i class="chart pie icon large"></i>
            <span id="surveyCount">${message(code:'myinst.dash.survey.label', args: [message(code: 'myinst.loadPending')])}</span>
        </a>

        <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: reporting-permissions -->
            <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                <a class="${us_dashboard_tab.getValue().value=='Workflows' || us_dashboard_tab.getValue() == 'Workflows' ? 'active item':'item'}" data-tab="workflows">
                    <i class="tasks icon large"></i>
                    ${currentWorkflowsCount}
                    ${message(code:'workflow.plural')}
                </a>
            </g:if>
        </sec:ifAnyGranted>

    </div><!-- secondary -->
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'Due Dates' || us_dashboard_tab.getValue()=='Due Dates' ? 'active':''}" data-tab="duedates">
            <div>
                <g:render template="/user/dueDatesView"
                          model="[user: user, dueDates: dueDates, dueDatesCount: dueDatesCount]"/>
            </div>
        </div>

        <g:if test="${editable}">
            <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'PendingChanges' || us_dashboard_tab.getValue() == 'PendingChanges' ? 'active':''}" data-tab="pendingchanges" id="pendingChanges">

            </div>
        </g:if>
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'AcceptedChanges' || us_dashboard_tab.getValue() == 'AcceptedChanges' ? 'active':''}" data-tab="acceptedchanges" id="acceptedChanges">

        </div>
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value=='Announcements' || us_dashboard_tab.getValue() == 'Announcements' ? 'active':''}" data-tab="news">

            <g:message code="profile.dashboardSysAnnTimeWindow"
                       args="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" />
            <g:if test="${systemAnnouncements.size() > 0 }">
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
                                        <g:formatDate date="${sa.lastPublishingDate}" formatName="default.date.format.onlytime"/>
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

        <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value=='Tasks' || us_dashboard_tab.getValue() == 'Tasks' ? 'active':''}" data-tab="tasks">

            <g:if test="${editable}">
                <div class="ui right aligned grid">
                    <div class="right floated right aligned sixteen wide column">
                        <a onclick="JSPC.app.taskcreate();" class="ui button">
                            ${message(code:'task.create.new')}
                        </a>
                    </div>
                </div>
            </g:if>

            <div class="ui cards">
                <g:each in="${tasks}" var="tsk">
                    <div class="ui card">

                        <div class="ui label">
                            <div class="right floated author">
                                Status:
                                <span>
                                <semui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${tsk}" field="status" />
                                </span>
                            </div>
                        </div>

                        <div class="content">
                            <div class="meta">
                                <div class="">Fällig: <strong><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk?.endDate}"/></strong></div>
                            </div>
                            <a class="header" onclick="JSPC.app.taskedit(${tsk?.id});">${tsk?.title}</a>

                            <div class="description">
                                <g:if test="${tsk.description}">
                                    <span><em>${tsk.description}</em></span> <br />
                                </g:if>
                            </div>
                        </div>
                        <div class="extra content">
                            <g:if test="${tsk.getObjects()}">
                                <g:each in="${tsk.getObjects()}" var="tskObj">
                                    <div class="item">
                                        <span  class="la-popup-tooltip la-delay" data-content="${message(code: 'task.' + tskObj.controller)}" data-position="left center" data-variation="tiny">
                                            <g:if test="${tskObj.controller == 'organisation'}">
                                                <i class="university icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('subscription')}">
                                                <i class="clipboard outline icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('package')}">
                                                <i class="gift icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('license')}">
                                                <i class="book icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('survey')}">
                                                <i class="chart pie icon"></i>
                                            </g:if>
                                        </span>
                                    <g:if test="${tskObj.controller.contains('survey')}">
                                        <g:link controller="${tskObj.controller}" action="show" params="${[id: tskObj.object?.surveyInfo.id, surveyConfigID:tskObj.object?.id]}">${tskObj.object.getSurveyName()}</g:link>
                                    </g:if>
                                        <g:else>
                                            <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object?.id]}">${tskObj.object}</g:link>
                                        </g:else>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <i class="checked calendar icon"></i>
                                ${message(code: 'task.general')}
                            </g:else>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>

        </g:if>

        <div class="ui bottom attached tab segment ${us_dashboard_tab.getValue().value == 'Surveys' || us_dashboard_tab.getValue()=='Surveys' ? 'active':''}" data-tab="surveys" style="border-top: 1px solid #d4d4d5; ">
                <div class="la-float-right">
                    <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                        <g:link controller="survey" action="workflowsSurveysConsortia" class="ui button">${message(code:'menu.my.surveys')}</g:link>
                    </g:if>
                    <g:else>
                        <g:link action="currentSurveys" class="ui button">${message(code:'menu.my.surveys')}</g:link>
                    </g:else>

                </div>
            <div id="surveyWrapper">
                <%--<g:render template="surveys"/>--%>
            </div>
        </div>

        <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: reporting-permissions -->
        <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
            <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'Workflows' || us_dashboard_tab.getValue() == 'Workflows' ? 'active':''}" data-tab="workflows">
                <div>
                    <g:if test="${currentWorkflows.size() != currentWorkflowsCount}">
                        <semui:msg class="info" text="${message(code:'workflow.dashboard.msg.more', args:[currentWorkflows.size(), currentWorkflowsCount, g.createLink(controller:'myInstitution', action:'currentWorkflows', params:[max:200])])}" />
                    </g:if>
                    <table class="ui celled table la-js-responsive-table la-table">
                        <thead>
                            <tr>
                                <th rowspan="2">${message(code:'workflow.label')}</th>
                                <th rowspan="2">${message(code:'subscription.label')}</th>
                                <th rowspan="2">${message(code:'default.progress.label')}</th>
                                <th class="la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
                                <th rowspan="2"></th>
                            </tr>
                            <tr>
                                <th class="la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>
                            <tr>
                        </thead>
                        <tbody>
                            <g:each in="${currentWorkflows}" var="wf">
                                <g:set var="wfInfo" value="${wf.getInfo()}" />
                                <tr>
                                    <td>
                                        <div class="la-flexbox">
                                            <i class="ui icon tasks la-list-icon"></i>
                                            <g:link class="wfModalLink" controller="ajaxHtml" action="useWfXModal" params="${[key: 'dashboard:' + wf.subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                                                <strong>${wf.title}</strong>
                                            </g:link>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="la-flexbox">
                                            <i class="ui icon clipboard la-list-icon"></i>
                                            <g:link controller="subscription" action="show" params="${[id: wf.subscription.id]}">
                                                ${wf.subscription.name}
                                                <g:if test="${wf.subscription.startDate || wf.subscription.endDate}">
                                                    (${wf.subscription.startDate ? DateUtils.getSDF_NoTime().format(wf.subscription.startDate) : ''} -
                                                    ${wf.subscription.endDate ? DateUtils.getSDF_NoTime().format(wf.subscription.endDate) : ''})
                                                </g:if>
                                            </g:link>
                                        </div>
                                    </td>
                                    <td>
                                        <g:if test="${wfInfo.tasksOpen}">
                                            <span class="ui label circular">${wfInfo.tasksOpen}</span>
                                        </g:if>
                                        <g:if test="${wfInfo.tasksCanceled}">
                                            <span class="ui label circular orange">${wfInfo.tasksCanceled}</span>
                                        </g:if>
                                        <g:if test="${wfInfo.tasksDone}">
                                            <span class="ui label circular green">${wfInfo.tasksDone}</span>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${DateUtils.getSDF_NoTime().format(wfInfo.lastUpdated)}
                                        <br />
                                        ${DateUtils.getSDF_NoTime().format(wf.dateCreated)}
                                    </td>
                                    <td class="x">
                                        <g:link controller="subscription" action="workflows" id="${wf.subscription.id}"
                                                class="ui icon button blue la-modern-button"
                                                params="${[info: 'subscription:' + wf.subscription.id + ':' + WfWorkflow.KEY + ':' + wf.id]}">
                                            <i class="icon edit"></i>
                                        </g:link>
                                    </td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                </div>
            </div>

            <div id="wfModal" class="ui modal"></div>
        </g:if>
        </sec:ifAnyGranted>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('.wfModalLink').on('click', function(e) {
            e.preventDefault();
            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
            func();
        });

        JSPC.app.taskcreate = bb8.ajax4SimpleModalFunction("#modalCreateTask", "<g:createLink controller="ajaxHtml" action="createTask"/>", true);

        JSPC.app.taskedit = function(id) {
            var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
            func();
        }

        JSPC.app.loadChanges = function() {
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
                r2d2.initDynamicSemuiStuff('#pendingChanges');
            })
        }

        JSPC.app.loadSurveys = function() {
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getSurveys" params="${params}"/>"
            }).done(function(response){
                $("#surveyWrapper").html(response);
                  r2d2.initDynamicSemuiStuff('#surveyWrapper');
            })
        }

                /* $('.item .widget-content').readmore({
                    speed: 250,
                    collapsedHeight: 21,
                    startOpen: false,
                    moreLink: '<a href="#">[ ${message(code:'default.button.show.label')} ]</a>',
                    lessLink: '<a href="#">[ ${message(code:'default.button.hide.label')} ]</a>'
                }); */
                $('.xEditableManyToOne').editable({
                }).on('hidden', function() {
                        //location.reload();
                });

        JSPC.app.loadChanges();
        JSPC.app.loadSurveys();
    </laser:script>

    <semui:debugInfo>
        <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>
    </body>
</html>
