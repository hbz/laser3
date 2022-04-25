<%@ page import="de.laser.helper.DateUtils; java.text.SimpleDateFormat; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>
<laser:serviceInjection />
    <g:if test="${ ! dueDates}">
        <semui:msg class="info" header="" message="profile.noDashboardReminderDates" args="${[createLink(controller:'profile', action:'index')]}"/>
    </g:if>
    <g:set var="dashboard_last_update" value="${DashboardDueDate.executeQuery("select max(lastUpdated) from DashboardDueDate ")[0]}" />
    <g:if test="${dashboard_last_update != null}" >
        <g:set var="message_lastUpdated" value="${message(code:'myinst.dash.due_dates.lastUpdate')}" />
        <%
            def sdf = DateUtils.getSDF_NoTime()
            def dateString = sdf.format(dashboard_last_update)
        %>

            <g:if test="${ ! SqlDateUtils.isYesterdayOrToday(dashboard_last_update)}">
                <semui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${message_lastUpdated} ${dateString}" >
                    <i class="exclamation alternate triangle icon"  id="noData" data-content="${message(code:'myinst.dash.due_dates.tooltip')}"></i>
                </semui:msg>
            </g:if>
            %{--<g:else>--}%
                %{--<semui:msg class="positive" header="${message(code: 'default.info.label')}" text="${message_lastUpdated} ${dateString}" />--}%
            %{--</g:else>--}%

    </g:if>
<laser:script file="${this.getGroovyPageFileName()}">
    $('#noData').popup();
</laser:script>

    <g:if test="${dueDates}">
        <div id="container-table">
            <laser:render template="/user/tableDueDates"/>
        </div>
        <semui:paginate offset="${dashboardDueDatesOffset ? dashboardDueDatesOffset : '0'}" max="${max ?: contextService.getUser().getDefaultPageSize()}" params="${[view:'dueDatesView']}" total="${dueDatesCount}"/>
    </g:if>