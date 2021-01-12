<%@ page import="de.laser.UserSetting; de.laser.system.SystemAnnouncement; de.laser.helper.RDConstants; de.laser.AccessService; de.laser.helper.SqlDateUtils; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>

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
                            <g:link controller="myInstitution" action="changes">${message(code: 'myinst.pendingChanges.label')}</g:link>
                        </div>
                        <semui:securedMainNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" affiliation="INST_USER" controller="myInstitution" action="reporting" message="myinst.reporting" />
                        <semui:securedMainNavItem affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
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
            default: us_dashboard_tab = user.getSetting(UserSetting.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', RDConstants.USER_SETTING_DASHBOARD_TAB))
            break
        }
    %>
    <div class="ui secondary pointing tabular menu">
        <a class="${us_dashboard_tab.getValue().value=='Due Dates' || us_dashboard_tab.getValue()=='Due Dates' ? 'active item':'item'}" data-tab="duedates">
            <i class="checked alarm end icon large"></i>
            ${dueDatesCount}
            ${message(code:'myinst.dash.due_dates.label')}
        </a>

        <g:if test="${editable}">
            <a class="${us_dashboard_tab.getValue().value == 'PendingChanges' || us_dashboard_tab.getValue() == 'PendingChanges' ? 'active item':'item'}" data-tab="pendingchanges">
                <i class="history icon large"></i>
                ${pendingCount}
                ${message(code:'myinst.pendingChanges.label')}
            </a>
        </g:if>
        <a class="${us_dashboard_tab.getValue().value == 'AcceptedChanges' || us_dashboard_tab.getValue() == 'AcceptedChanges' ? 'active item':'item'}" data-tab="acceptedchanges">
            <i class="bullhorn icon large"></i>
            ${notificationsCount}
            ${message(code:'myinst.acceptedChanges.label')}
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
                ${surveys.size()}
                ${message(code:'myinst.dash.survey.label')}
        </a>


        <a class="${us_dashboard_tab.getValue().value=='Announcements' || us_dashboard_tab.getValue() == 'Announcements' ? 'active item':'item'}" data-tab="news" id="jsFallbackAnnouncements">
            <i class="warning circle icon large"></i>
            ${systemAnnouncements.size()}
            ${message(code:'announcement.plural')}
        </a>

       %{-- <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
            <a class="${us_dashboard_tab.getValue().value=='Surveys' || us_dashboard_tab.getValue()=='Surveys' ? 'active item':'item'}" data-tab="six">
                <i class="checked tasks icon large"></i>
                ${surveysConsortia?.size()}
                ${message(code:'myinst.dash.surveyConsortia.label')}
            </a>
        </g:if>--}%


    </div><!-- secondary -->
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'Due Dates' || us_dashboard_tab.getValue()=='Due Dates' ? 'active':''}" data-tab="duedates">
            <div>
                <g:render template="/user/dueDatesView"
                          model="[user: user, dueDates: dueDates, dueDatesCount: dueDatesCount]"/>
            </div>
        </div>

        <g:if test="${editable}">
            <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'PendingChanges' || us_dashboard_tab.getValue() == 'PendingChanges' ? 'active':''}" data-tab="pendingchanges">
                <div class="la-float-right">
                    <g:if test="${packages}">
                        <g:form controller="pendingChange" action="processAll">
                            <g:select from="${packages}" noSelection="${['':message(code:'default.select.choose.label')]}" name="acceptChangesForPackages" class="ui select search multiple dropdown" optionKey="${{it.id}}" optionValue="${{it.pkg.name}}"/>
                            <div class="ui buttons">
                                <g:submitButton class="ui button positive" name="acceptAll" value="${message(code:'pendingChange.takeAll')}"/>
                                <div class="or" data-text="${message(code:'default.or')}"></div>
                                <g:submitButton class="ui button negative" name="rejectAll" value="${message(code:'pendingChange.rejectAll')}"/>
                            </div>
                        </g:form>
                    </g:if>
                </div>
                <div class="ui internally celled grid">
                    <div class="row">
                        <div class="two wide column">
                            <g:message code="profile.dashboard.changes.eventtype"/>
                        </div><!-- .column -->
                        <div class="two wide column">
                            <g:message code="profile.dashboard.changes.objecttype"/>
                        </div><!-- .column -->
                        <div class="two wide column">
                            <g:message code="profile.dashboard.changes.object"/>
                        </div><!-- .column -->
                        <div class="seven wide column">
                            <g:message code="profile.dashboard.changes.event"/>
                        </div><!-- .column -->
                        <div class="three wide column">
                            <g:message code="profile.dashboard.changes.action"/>
                        </div><!-- .column -->
                    </div>
                    <g:each in="${pending}" var="entry">
                        <g:set var="row" value="${pendingChangeService.printRow(entry.change)}" />
                        <g:set var="event" value="${row.eventData}"/>
                        <div class="row">
                            <div class="two wide column">
                                ${raw(row.eventIcon)}
                            </div><!-- .column -->
                            <div class="two wide column">
                                ${raw(row.instanceIcon)}
                            </div><!-- .column -->
                            <div class="two wide column">
                                <g:if test="${entry.change.subscription}">
                                    <g:link controller="subscription" action="index" id="${entry.target.id}">${entry.target.dropdownNamingConvention()}</g:link>
                                </g:if>
                                <g:elseif test="${entry.change.costItem}">
                                    <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.target.sub.id]}">${entry.target.sub.dropdownNamingConvention()}</g:link>
                                </g:elseif>
                            </div><!-- .column -->
                            <div class="seven wide column">
                                ${raw(row.eventString)}

                                <g:if test="${entry.change.msgToken == "pendingChange.message_SU_NEW_01" && accessService.checkPerm('ORG_INST')}">
                                    <div class="right aligned wide column">
                                        <g:link class="ui button" controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(entry.change.subscription._getCalculatedPrevious()), targetObjectId: genericOIDService.getOID(entry.change.subscription)]}">
                                            <g:message code="myinst.copyMyElements"/>
                                        </g:link>
                                    </div>
                                </g:if>
                            </div><!-- .column -->
                            <div class="three wide column">
                                <div class="ui buttons">
                                    <g:link class="ui positive button" controller="pendingChange" action="accept" id="${entry.change.id}"><g:message code="default.button.accept.label"/></g:link>
                                    <div class="or" data-text="${message(code:'default.or')}"></div>
                                    <g:link class="ui negative button" controller="pendingChange" action="reject" id="${entry.change.id}"><g:message code="default.button.reject.label"/></g:link>
                                </div>
                            </div><!-- .column -->
                        </div><!-- .row -->

                    </g:each>
                </div><!-- .grid -->
                <div>
                    <semui:paginate offset="${pendingOffset ? pendingOffset : '0'}" max="${max}" params="${[view:'PendingChanges']}" total="${pendingCount}"/>
                </div>
            </div>
        </g:if>
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value == 'AcceptedChanges' || us_dashboard_tab.getValue() == 'AcceptedChanges' ? 'active':''}" data-tab="acceptedchanges">
            <div class="la-float-right">
                <%--<g:link action="changes" class="ui button"><g:message code="myinst.changes.submit.label"/></g:link>--%>
            </div>
            <div class="ui internally celled grid">
                <div class="row">
                    <div class="two wide column">
                        <g:message code="profile.dashboard.changes.eventtype"/>
                    </div><!-- .column -->
                    <div class="two wide column">
                        <g:message code="profile.dashboard.changes.objecttype"/>
                    </div><!-- .column -->
                    <div class="two wide column">
                        <g:message code="profile.dashboard.changes.object"/>
                    </div><!-- .column -->
                    <div class="ten wide column">
                        <g:message code="profile.dashboard.changes.event"/>
                    </div><!-- .column -->
                </div>
                <g:each in="${notifications}" var="entry">
                    <g:set var="row" value="${pendingChangeService.printRow(entry.change)}" />
                    <g:set var="event" value="${row.eventData}"/>
                    <div class="row">
                        <div class="two wide column">
                            ${raw(row.eventIcon)}
                        </div><!-- .column -->
                        <div class="two wide column">
                            ${raw(row.instanceIcon)}
                            <g:if test="${entry.memberSubscriptions}">
                                (${entry.memberSubscriptions.size()})
                            </g:if>
                        </div><!-- .column -->
                        <div class="two wide column">
                            <g:if test="${entry.change.subscription}">
                                <g:link controller="subscription" action="index" id="${entry.target.id}">${entry.target.dropdownNamingConvention()}</g:link>
                            </g:if>
                            <g:elseif test="${entry.change.costItem}">
                                <g:link controller="subscription" action="index" mapping="subfinance" params="${[sub:entry.target.sub.id]}">${entry.target.sub.dropdownNamingConvention()}</g:link>
                            </g:elseif>
                        </div><!-- .column -->
                        <div class="ten wide column">
                            ${raw(row.eventString)}
                        </div><!-- .column -->
                    </div><!-- .row -->
                </g:each>
            </div><!-- .grid -->
            <div>
                <semui:paginate offset="${acceptedOffset ? acceptedOffset : '0'}" max="${max}" params="${[view:'AcceptedChanges']}" total="${notificationsCount}"/>
            </div>
        </div>
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value=='Announcements' || us_dashboard_tab.getValue() == 'Announcements' ? 'active':''}" data-tab="news">

            <g:message code="profile.dashboardSysAnnTimeWindow"
                       args="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" />

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${systemAnnouncements}" var="sa">
                    <div class="item">

                        <div class="ui internally celled grid">
                            <div class="row">
                                <div class="two wide column">
                                    <g:formatDate date="${sa.lastPublishingDate}" formatName="default.date.format.noZ"/>
                                </div><!-- .column -->
                                <div class="fourteen wide column">

                                    <div class="header" style="margin:0 0 1em 0">
                                        <% print sa.title; /* avoid auto encodeAsHTML() */ %>
                                    </div>
                                    <div>
                                        <div class="widget-content"><% print sa.content; /* avoid auto encodeAsHTML() */ %></div>
                                    </div>

                                </div><!-- .column -->
                            </div><!-- .row -->
                        </div><!-- .grid -->

                    </div>
                </g:each>
            </div>
        </div>

        <%--<div class="ui bottom attached tab ${us_dashboard_tab.getValue().value=='Announcements' || us_dashboard_tab.getValue() == 'Announcements' ? 'active':''}" data-tab="news">
            %{--
            <g:if test="${editable}">
                <div class="la-float-right">
                    <g:link action="announcements" class="ui button">${message(code:'myinst.ann.view.label')}</g:link>
                </div>
            </g:if>
            --}%

            <g:message code="profile.dashboardItemsTimeWindow"
                       default="You see events from the last {0} days."
                       args="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)}" />

            <br />

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${recentAnnouncements}" var="ra">
                    <div class="item">

                        <div class="ui internally celled grid">
                            <div class="row">
                                <div class="three wide column">

                                    <strong>${message(code:'myinst.ann.posted_by')}</strong>
                                    <g:link controller="user" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link>
                                    <br /><br />
                                    <g:formatDate date="${ra.dateCreated}" formatName="default.date.format.noZ"/>

                                </div><!-- .column -->
                                <div class="thirteen wide column">

                                    <div class="header" style="margin:0 0 1em 0">
                                        <g:set var="ann_nws" value="${ra.title?.replaceAll(' ', '')}"/>
                                        ${message(code:"announcement.${ann_nws}", default:"${ra.title}")}
                                    </div>
                                    <div>
                                        <div class="widget-content"><% print ra.content; /* avoid auto encodeAsHTML() */ %></div>
                                    </div>

                                </div><!-- .column -->
                            </div><!-- .row -->
                        </div><!-- .grid -->

                    </div>
                </g:each>
            </div>
            <div>
                <semui:paginate offset="${announcementOffset ? announcementOffset : '0'}" max="${contextService.getUser().getDefaultPageSize()}" params="${[view:'announcementsView']}" total="${recentAnnouncementsCount}"/>
            </div>
        </div>--%>

        <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <div class="ui bottom attached tab ${us_dashboard_tab.getValue().value=='Tasks' || us_dashboard_tab.getValue() == 'Tasks' ? 'active':''}" data-tab="tasks">

            <g:if test="${editable}">
                <div class="ui right aligned grid">
                    <div class="right floated right aligned sixteen wide column">
                        <a onclick="JSPC.app.taskcreate();" class="ui icon button">
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
                                                <i class="folder open icon"></i>
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
                        <g:link controller="survey" action="currentSurveysConsortia" class="ui button">${message(code:'menu.my.surveys')}</g:link>
                    </g:if>
                    <g:else>
                        <g:link action="currentSurveys" class="ui button">${message(code:'menu.my.surveys')}</g:link>
                    </g:else>

                </div>
                    <g:render template="surveys"/>
        </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.taskcreate = bb8.ajax4SimpleModalFunction("#modalCreateTask", "<g:createLink controller="ajaxHtml" action="createTask"/>", true);

        JSPC.app.taskedit = function(id) {
            var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
            func();
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

    </laser:script>

    <semui:debugInfo>
        <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </semui:debugInfo>
    </body>
</html>
