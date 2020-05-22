<%@ page import="java.text.SimpleDateFormat; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.SqlDateUtils; com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated; de.laser.DashboardDueDate" %>
<laser:serviceInjection />
    <g:if test="${ ! dueDates}">
        <semui:msg class="info" header="" message="profile.noDashboardReminderDates" />
    </g:if>
    <g:set var="dashboard_last_update" value="${DashboardDueDate.executeQuery("select max(lastUpdated) from DashboardDueDate ")[0]}" />
    <g:if test="${dashboard_last_update != null}" >
        <g:set var="message_lastUpdated" value="${message(code:'myinst.dash.due_dates.lastUpdate')}" />
        <%
            def sdf = de.laser.helper.DateUtil.getSDF_NoTime()
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
<r:script>
    $('#noData')
            .popup()
    ;
</r:script>

    <g:if test="${dueDates}">
        <div id="container-table">
            <g:render template="/user/tableDueDates"/>
        </div>
        <semui:paginate offset="${dashboardDueDatesOffset ? dashboardDueDatesOffset : '0'}" max="${max ?: contextService.getUser().getDefaultPageSizeTMP()}" params="${[view:'dueDatesView']}" total="${dueDatesCount}"/>
    </g:if>