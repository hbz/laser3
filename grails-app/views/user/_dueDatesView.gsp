<%@ page import="de.laser.ui.Icon; de.laser.utils.SqlDateUtils; de.laser.utils.DateUtils; java.text.SimpleDateFormat; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>
<laser:serviceInjection />
    <g:if test="${ ! dueDates}">
        <ui:msg class="info" message="profile.noDashboardReminderDates" args="${[createLink(controller:'profile', action:'index')]}"/>
    </g:if>
    <g:set var="dashboard_last_update" value="${DashboardDueDate.executeQuery("select max(lastUpdated) from DashboardDueDate ")[0]}" />
    <g:if test="${dashboard_last_update != null}" >

            <g:if test="${ ! SqlDateUtils.isYesterdayOrToday(dashboard_last_update)}">
                <ui:msg class="warning" showIcon="true" header="${message(code: 'myinst.message.attention')}">
                    ${message(code:'myinst.dash.due_dates.lastUpdate')} ${DateUtils.getLocalizedSDF_noTime().format(dashboard_last_update)}
                    <br />
                    ${message(code:'myinst.dash.due_dates.tooltip')}
                </ui:msg>
            </g:if>

    </g:if>

    <g:if test="${dueDates}">
        <div id="container-table">
            <laser:render template="/user/tableDueDates"/>
        </div>
        <ui:paginate offset="${dashboardDueDatesOffset ? dashboardDueDatesOffset : '0'}" max="${max ?: contextService.getUser().getPageSizeOrDefault()}" params="${[view:'dueDatesView']}" total="${dueDatesCount}"/>
    </g:if>