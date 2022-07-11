<%@ page import="de.laser.utils.SqlDateUtils; de.laser.utils.DateUtils; java.text.SimpleDateFormat; org.springframework.context.i18n.LocaleContextHolder; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>
<laser:serviceInjection />
    <g:if test="${ ! dueDates}">
        <ui:msg class="info" header="" message="profile.noDashboardReminderDates" args="${[createLink(controller:'profile', action:'index')]}"/>
    </g:if>
    <g:set var="dashboard_last_update" value="${DashboardDueDate.executeQuery("select max(lastUpdated) from DashboardDueDate ")[0]}" />
    <g:if test="${dashboard_last_update != null}" >
        <g:set var="message_lastUpdated" value="${message(code:'myinst.dash.due_dates.lastUpdate')}" />
            <g:if test="${ ! SqlDateUtils.isYesterdayOrToday(dashboard_last_update)}">
                <ui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${message_lastUpdated} ${DateUtils.getLocalizedSDF_noTime().format(dashboard_last_update)}" >
                    <i class="exclamation alternate triangle icon"  id="noData" data-content="${message(code:'myinst.dash.due_dates.tooltip')}"></i>
                </ui:msg>
            </g:if>

    </g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    $('#noData').popup();
</laser:script>

    <g:if test="${dueDates}">
        <div id="container-table">
            <laser:render template="/user/tableDueDates"/>
        </div>
        <ui:paginate offset="${dashboardDueDatesOffset ? dashboardDueDatesOffset : '0'}" max="${max ?: contextService.getUser().getPageSizeOrDefault()}" params="${[view:'dueDatesView']}" total="${dueDatesCount}"/>
    </g:if>