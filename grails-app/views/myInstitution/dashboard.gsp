<%@ page import="de.laser.CustomerTypeService; de.laser.workflow.WfChecklist; de.laser.workflow.WfCheckpoint; de.laser.storage.RDStore; de.laser.utils.DateUtils; de.laser.workflow.WorkflowHelper; de.laser.workflow.WfWorkflow; de.laser.UserSetting; de.laser.system.SystemAnnouncement; de.laser.storage.RDConstants; de.laser.AccessService; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>

<laser:htmlStart message="menu.institutions.dash" serviceInjection="true"/>

        <ui:breadcrumbs>
            <ui:crumb text="${message(code:'menu.institutions.dash')}" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon text="${institution.name}" />

%{--<pre>--}%
%{--    ORG_CONSORTIUM_BASIC: ${accessService.checkCtxPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}--}%
%{--    ORG_CONSORTIUM_PRO: ${accessService.checkCtxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)}--}%
%{--    ORG_INST_BASIC: ${accessService.checkCtxPerm(CustomerTypeService.ORG_INST_BASIC)}--}%
%{--    ORG_INST_PRO: ${accessService.checkCtxPerm(CustomerTypeService.ORG_INST_PRO)}--}%

%{--    getCustomerType: ${institution.getCustomerType()}--}%
%{--</pre>--}%

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
                        <ui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" affiliation="INST_USER" controller="myInstitution" action="reporting" message="myinst.reporting" />
                    </div>
                </div>

                <div class="column">
                    <div class="ui divided relaxed list">
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="tasks" message="task.plural" />
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
                    </div>
                </div>
            </div>
        </div>

        <ui:messages data="${flash}" />
        <br />
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
                us_dashboard_tab = user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))
            break
        }
    %>
    <div class="ui secondary stackable pointing tabular la-tab-with-js menu">
        <a class="${us_dashboard_tab.value == 'Due Dates' ? 'active item':'item'}" data-tab="duedates">
            <i class="bell icon large"></i>
            ${dueDatesCount} ${message(code:'myinst.dash.due_dates.label')}
        </a>

        <g:if test="${institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()}">
            <a class="${us_dashboard_tab.value == 'PendingChanges' ? 'active item':'item'}" data-tab="pendingchanges">
                <i class="history icon large"></i>
                <span id="pendingCount">${message(code:'myinst.pendingChanges.label', args: [message(code:'myinst.loadPending')])}</span>
            </a>
        </g:if>
        <a class="${us_dashboard_tab.value == 'AcceptedChanges' ? 'active item':'item'}" data-tab="acceptedchanges">
            <i class="warning circle icon large"></i>
            <span id="notificationsCount">${message(code:'myinst.acceptedChanges.label', args: [message(code:'myinst.loadPending')])}</span>
        </a>

        <a class="${us_dashboard_tab.value == 'Announcements' ? 'active item':'item'}" data-tab="news" id="jsFallbackAnnouncements">
            <i class="flag icon large"></i>
            ${systemAnnouncements.size()} ${message(code:'announcement.plural')}
        </a>

        <g:if test="${accessService.checkCtxPerm(CustomerTypeService.PERMS_INST_BASIC_CONSORTIUM_PRO)}">
            <a class="${us_dashboard_tab.value == 'Surveys' ? 'active item' : 'item'}" data-tab="surveys">
                <i class="chart pie icon large"></i>
                <span id="surveyCount">${message(code: 'myinst.dash.survey.label', args: [message(code: 'myinst.loadPending')])}</span>
            </a>
        </g:if>

        <g:if test="${accessService.checkCtxPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
            <a class="${us_dashboard_tab.value == 'Tasks' ? 'active item':'item'}" data-tab="tasks">
                <i class="calendar check outline icon large"></i>
                ${tasksCount} ${message(code:'myinst.dash.task.label')}
            </a>
        </g:if>

        <g:if test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
            <a class="${us_dashboard_tab.value == 'Workflows' ? 'active item':'item'}" data-tab="workflows">
                <i class="tasks icon large"></i>
%{--                ${myWorkflowsCount + allWorkflowsCount} ${message(code:'workflow.plural')}--}%
                ${allChecklistsCount} ${message(code:'workflow.plural')}
            </a>
        </g:if>

    </div><!-- secondary -->
        <div class="ui bottom attached tab ${us_dashboard_tab.value == 'Due Dates' ? 'active':''}" data-tab="duedates">
            <div>
                <laser:render template="/user/dueDatesView"
                          model="[user: user, dueDates: dueDates, dueDatesCount: dueDatesCount]"/>
            </div>
        </div>

        <g:if test="${institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()}">
            <div class="ui bottom attached tab ${us_dashboard_tab.value == 'PendingChanges' ? 'active':''}" data-tab="pendingchanges" id="pendingChanges">
            </div>
        </g:if>
        <div class="ui bottom attached tab ${us_dashboard_tab.value == 'AcceptedChanges' ? 'active':''}" data-tab="acceptedchanges" id="acceptedChanges">
        </div>
        <div class="ui bottom attached tab ${us_dashboard_tab.value =='Announcements' ? 'active':''}" data-tab="news">

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

        <g:if test="${accessService.checkCtxPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
        <div class="ui bottom attached tab ${us_dashboard_tab.value == 'Tasks' ? 'active':''}" data-tab="tasks">

            <g:if test="${editable}">
                <div class="ui right aligned grid">
                    <div class="right floated right aligned sixteen wide column">
                        <a onclick="JSPC.app.createTask();" class="ui button">
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
                                <ui:xEditableRefData config="${RDConstants.TASK_STATUS}" owner="${tsk}" field="status" />
                                </span>
                            </div>
                        </div>

                        <div class="content">
                            <div class="meta">
                                <div class="">Fällig: <strong><g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk?.endDate}"/></strong></div>
                            </div>
                            <a class="header" onclick="JSPC.app.editTask(${tsk?.id});">${tsk?.title}</a>

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
                                <i class="calendar check outline icon"></i>
                                ${message(code: 'task.general')}
                            </g:else>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>

        </g:if>

        <g:if test="${accessService.checkCtxPerm(CustomerTypeService.PERMS_INST_BASIC_CONSORTIUM_PRO)}">
            <div class="ui bottom attached tab segment ${us_dashboard_tab.value == 'Surveys' ? 'active' : ''}"
                 data-tab="surveys" style="border-top: 1px solid #d4d4d5; ">
                <div class="la-float-right">
                    <g:if test="${accessService.checkCtxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                        <g:link controller="survey" action="workflowsSurveysConsortia"
                                class="ui button">${message(code: 'menu.my.surveys')}</g:link>
                    </g:if>
                    <g:else>
                        <g:link action="currentSurveys" class="ui button">${message(code: 'menu.my.surveys')}</g:link>
                    </g:else>

                </div>

                <div id="surveyWrapper">
                    <%--<laser:render template="surveys"/>--%>
                </div>
            </div>
        </g:if>

        <g:if test="${workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
            <div id="wfFlyout" class="ui eight wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

            <div class="ui bottom attached tab ${us_dashboard_tab.value == 'Workflows' ? 'active':''}" data-tab="workflows">

                <g:if test="${allChecklists}">
                    <g:if test="${allChecklistsCount > user.getPageSizeOrDefault()}">
                        <ui:msg class="info" noClose="true">

                            ${message(code:'workflow.dashboard.msg.more', args:[user.getPageSizeOrDefault(), allChecklistsCount,
                                                                                g.createLink(controller:'myInstitution', action:'currentWorkflows', params:[filter:'reset', max:500]) ])}
%{--                        ${message(code:'workflow.dashboard.msg.new', args:[message(code:'profile.itemsTimeWindow'), user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)])}--}%
                        </ui:msg>
                    </g:if>

                    <table class="ui celled table la-js-responsive-table la-table">
                        <thead>
                            <tr>
                                <th class="four wide" rowspan="2">${message(code:'workflow.label')}</th>
                                <th class="four wide" rowspan="2">${message(code:'default.relation.label')}</th>
                                <th class="four wide" rowspan="2">${message(code:'default.progress.label')}</th>
                                <th class="two wide la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>
                                <th class="two wide" rowspan="2">${message(code:'default.actions.label')}</th>
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
                                            <i class="ui icon tasks la-list-icon"></i>
                                            <g:link controller="${clistInfo.targetController}" action="workflows" id="${clistInfo.target.id}"
                                                    params="${[info: '' + clistInfo.target.class.name + ':' + clistLinkParamPart]}">
                                                <strong>${clist.title}</strong>
                                            </g:link>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="la-flexbox">
                                            <i class="ui icon ${clistInfo.targetIcon} la-list-icon"></i>
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
                                        <g:if test="${workflowService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->
                                            <button class="ui icon button blue la-modern-button" data-wfId="${clist.id}"><i class="icon pencil"></i></button>

                                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [clist.title])}"
                                                    data-confirm-term-how="delete"
                                                    controller="myInstitution" action="dashboard" id="${clistInfo.target.id}" params="${[cmd:"delete:${WfChecklist.KEY}:${clist.id}", view:'Workflows']}"
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


%{--                <g:if test="${myWorkflows || allWorkflows}">--}%
%{--                    <ui:msg class="info" noClose="true">--}%
%{--                        <g:if test="${Math.max(myWorkflowsCount, allWorkflowsCount) > user.getPageSizeOrDefault()}">--}%
%{--                            ${message(code:'workflow.dashboard.msg.more', args:[user.getPageSizeOrDefault(), g.createLink(controller:'myInstitution', action:'currentWorkflows', params:[filter:'reset', max:500]) ])}--}%
%{--                            <br />--}%
%{--                        </g:if>--}%
%{--                        ${message(code:'workflow.dashboard.msg.new', args:[message(code:'profile.itemsTimeWindow'), user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)])}--}%
%{--                    </ui:msg>--}%
%{--                </g:if>--}%

%{--                <g:each in="${[myWorkflows, allWorkflows]}" var="currentWorkflows">--}%

%{--                    <g:if test="${currentWorkflows}">--}%
%{--                    <table class="ui celled table la-js-responsive-table la-table">--}%
%{--                        <thead>--}%
%{--                            <tr>--}%
%{--                                <th class="five wide" rowspan="2">${message(code:'workflow.label')}</th>--}%
%{--                                <th class="six wide" rowspan="2">${message(code:'default.relation.label')}</th>--}%
%{--                                <th class="two wide"  rowspan="2">${message(code:'default.progress.label')}</th>--}%
%{--                                <th class="two wide"  class="la-smaller-table-head">${message(code:'default.lastUpdated.label')}</th>--}%
%{--                                <th class="one wide"  rowspan="2"></th>--}%
%{--                            </tr>--}%
%{--                            <tr>--}%
%{--                                <th class="la-smaller-table-head">${message(code:'default.dateCreated.label')}</th>--}%
%{--                            <tr>--}%
%{--                        </thead>--}%
%{--                        <tbody>--}%
%{--                            <g:each in="${currentWorkflows}" var="wf">--}%
%{--                                <g:set var="wfInfo" value="${wf.getInfo()}" />--}%
%{--                                <g:set var="wfLinkParamPart" value="${wfInfo.target.id + ':' + WfWorkflow.KEY + ':' + wf.id}" />--}%

%{--                                <tr>--}%
%{--                                    <td>--}%
%{--                                        <div class="la-flexbox">--}%
%{--                                            <i class="ui icon tasks la-list-icon"></i>--}%
%{--                                            <g:link controller="${wfInfo.targetController}" action="workflows" id="${wfInfo.target.id}"--}%
%{--                                                    params="${[info: '' + wfInfo.target.class.name + ':' + wfLinkParamPart]}">--}%
%{--                                                <strong>${wf.title}</strong>--}%
%{--                                            </g:link>--}%
%{--                                        </div>--}%
%{--                                    </td>--}%
%{--                                    <td>--}%
%{--                                        <div class="la-flexbox">--}%
%{--                                            <i class="ui icon ${wfInfo.targetIcon} la-list-icon"></i>--}%
%{--                                            <g:link controller="${wfInfo.targetController}" action="show" params="${[id: wfInfo.target.id]}">--}%
%{--                                                ${wfInfo.targetName}--}%
%{--                                                <g:if test="${wfInfo.target instanceof Subscription || wfInfo.target instanceof License}">--}%
%{--                                                    <g:if test="${wfInfo.target.startDate || wfInfo.target.endDate}">--}%
%{--                                                        (${wfInfo.target.startDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.startDate) : ''} ---}%
%{--                                                        ${wfInfo.target.endDate ? DateUtils.getLocalizedSDF_noTime().format(wfInfo.target.endDate) : ''})--}%
%{--                                                    </g:if>--}%
%{--                                                </g:if>--}%
%{--                                            </g:link>--}%
%{--                                        </div>--}%
%{--                                    </td>--}%
%{--                                    <td>--}%
%{--                                        <g:if test="${wfInfo.tasksOpen}">--}%
%{--                                            <span class="ui label circular">${wfInfo.tasksOpen}</span>--}%
%{--                                        </g:if>--}%
%{--                                        <g:if test="${wfInfo.tasksCanceled}">--}%
%{--                                            <span class="ui label circular orange">${wfInfo.tasksCanceled}</span>--}%
%{--                                        </g:if>--}%
%{--                                        <g:if test="${wfInfo.tasksDone}">--}%
%{--                                            <span class="ui label circular green">${wfInfo.tasksDone}</span>--}%
%{--                                        </g:if>--}%
%{--                                    </td>--}%
%{--                                    <td>--}%
%{--                                        ${DateUtils.getLocalizedSDF_noTime().format(wfInfo.lastUpdated)}--}%
%{--                                        <br />--}%
%{--                                        ${DateUtils.getLocalizedSDF_noTime().format(wf.dateCreated)}--}%
%{--                                    </td>--}%
%{--                                    <td class="center aligned" style="position:relative;">--}%
%{--                                        <g:if test="${workflowOldService.hasUserPerm_edit()}"><!-- TODO: workflows-permissions -->--}%
%{--                                            <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'dashboard:' + wfLinkParamPart]}" />--}%
%{--                                        </g:if>--}%
%{--                                        <g:elseif test="${workflowOldService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->--}%
%{--                                            <uiWorkflow:usageIconLinkButton workflow="${wf}" params="${[key: 'dashboard:' + wfLinkParamPart]}" />--}%
%{--                                        </g:elseif>--}%
%{--                                        <g:if test="${workflowOldService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->--}%
%{--                                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"--}%
%{--                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.workflow", args: [wfInfo.target.title])}"--}%
%{--                                                    data-confirm-term-how="delete"--}%
%{--                                                    controller="myInstitution" action="dashboard" params="${[cmd:"delete:${WfWorkflow.KEY}:${wfInfo.target.id}"]}"--}%
%{--                                                    role="button"--}%
%{--                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">--}%
%{--                                                <i class="trash alternate outline icon"></i>--}%
%{--                                            </g:link>--}%
%{--                                        </g:if>--}%
%{--                                        <g:if test="${wf.isFlaggedAsNew( user )}">--}%
%{--                                            <div class="ui floating right aligned mini label yellow" style="top:0.5em;right:-1em;">${message(code:'default.new')}</div>--}%
%{--                                        </g:if>--}%
%{--                                    </td>--}%
%{--                                </tr>--}%
%{--                            </g:each>--}%
%{--                        </tbody>--}%
%{--                    </table>--}%
%{--                    </g:if>--}%

%{--                </g:each>--}%
            </div>

            <div id="wfModal" class="ui modal"></div>
        </g:if>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('.wfModalLink').on('click', function(e) {
            e.preventDefault();
            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
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

        JSPC.app.createTask = bb8.ajax4SimpleModalFunction("#modalCreateTask", "<g:createLink controller="ajaxHtml" action="createTask"/>", true);

        JSPC.app.editTask = function (id) {
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
                r2d2.initDynamicUiStuff('#pendingChanges');
            })
        }

        JSPC.app.loadSurveys = function() {
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getSurveys" params="${params}"/>"
            }).done(function(response){
                $("#surveyWrapper").html(response);
                  r2d2.initDynamicUiStuff('#surveyWrapper');
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

    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>
<laser:htmlEnd />
