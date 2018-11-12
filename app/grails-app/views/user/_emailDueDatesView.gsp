<!doctype html>
<html>

<body>

<div>

    <g:if test="${dueDates}">

        <div>
            <g:message code="profile.dashboardReminderPeriod" default="Your search found ${resultsTotal} records"
                       args="${user.getSettingsValue(UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}"/>
        </div>

        <hr>

        ${dueDates.toString()}
        %{--<table class="ui celled table">--}%
            %{--<thead>--}%
            %{--<tr>--}%
                %{--<th>${message(code:'myinst.dash.due_dates.attribute.label')}</th>--}%
        %{--<th>${message(code:'myinst.dash.due_date.date.label')}</th>--}%
        %{--<th>${message(code:'myinst.dash.due_dates.name.label')}</th>--}%
        %{--</tr></thead>--}%
        %{--<tbody>--}%
        %{--<g:each in="${dueObjects}" var="obj">--}%
            %{--<tr>--}%
                %{--<td>--}%
                    %{--<g:if test="${obj instanceof Subscription || obj instanceof License}">--}%
                        %{--<g:if test="${obj.manualCancellationDate}">--}%
                            %{--${message(code:'myinst.dash.due_date.noticePeriod.label')}--}%
                            %{--<br>--}%
                        %{--</g:if>--}%
                        %{--${message(code:'myinst.dash.due_date.enddate.label')}--}%
                    %{--</g:if>--}%
                    %{--<g:elseif test="${obj instanceof Task}">--}%
                        %{--${message(code:'myinst.dash.due_date.task.label')}--}%
                    %{--</g:elseif>--}%
                    %{--<g:elseif test="${obj instanceof AbstractProperty}">--}%
                        %{--<i class="icon tags la-list-icon"></i>--}%
                        %{--${obj.type?.name}--}%
                    %{--</g:elseif>--}%
                    %{--<g:else>--}%
                        %{--Not implemented yet!--}%
                    %{--</g:else>--}%
                %{--</td>--}%
                %{--<td>--}%
                    %{--<g:if test="${obj instanceof Subscription || obj instanceof License}">--}%
                        %{--<g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.manualCancellationDate}"/>--}%
                        %{--<br>--}%
                    %{--</g:if>--}%
                    %{--<g:if test="${obj instanceof AbstractProperty}">--}%
                        %{--<g:if test="${obj.dateValue}">--}%
                            %{--<g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.dateValue}"/>--}%
                        %{--</g:if>--}%
                    %{--</g:if>--}%
                    %{--<g:else>--}%
                        %{--<g:if test="${obj.endDate}">--}%
                            %{--<g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.endDate}"/>--}%
                        %{--</g:if>--}%
                    %{--</g:else>--}%
                %{--</td>--}%
                %{--<td>--}%
                    %{--<div class="la-flexbox">--}%
                        %{--<g:if test="${obj instanceof Subscription}">--}%
                            %{--<i class="icon folder open la-list-icon"></i>--}%
                            %{--<g:link controller="subscriptionDetails" action="show" id="${obj.id}">${obj.name}</g:link>--}%
                        %{--</g:if>--}%
                        %{--<g:elseif test="${obj instanceof License}">--}%
                            %{--<i class="icon balance scale la-list-icon"></i>--}%
                            %{--<g:link controller="licenseDetails" action="show" id="${obj.id}">${obj.name}</g:link>--}%
                        %{--</g:elseif>--}%
                        %{--<g:elseif test="${obj instanceof Task}">--}%
                            %{--<span data-position="top right" data-tooltip="Aufgabe">--}%
                                %{--<i class="icon tasks la-list-icon"></i>--}%
                            %{--</span>--}%
                            %{--<a class="header" onclick="taskedit(${obj?.id});">${obj?.title}</a>--}%
                            %{--&nbsp(Status: ${obj.status?.getI10n("value")})--}%
                        %{--</g:elseif>--}%
                        %{--<g:elseif test="${obj instanceof AbstractProperty}">--}%
                            %{--<g:if test="${obj.owner instanceof Person}">--}%
                                %{--<i class="icon address book la-list-icon"></i>--}%
                                %{--<g:link controller="person" action="show" id="${obj.owner.id}">${obj.owner?.first_name}&nbsp${obj.owner?.last_name}</g:link>--}%
                            %{--</g:if>--}%
                            %{--<g:elseif test="${obj.owner instanceof Subscription}">--}%
                                %{--<i class="icon folder open la-list-icon"></i>--}%
                                %{--<g:link controller="subscriptionDetails" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>--}%
                            %{--</g:elseif>--}%
                            %{--<g:elseif test="${obj.owner instanceof License}">--}%
                                %{--<i class="icon balance scale la-list-icon"></i>--}%
                                %{--<g:link controller="licenseDetails" action="show" id="${obj.owner?.id}">${obj.owner?.reference}</g:link>--}%
                            %{--</g:elseif>--}%
                            %{--<g:elseif test="${obj.owner instanceof Org}">--}%
                                %{--<i class="icon university la-list-icon"></i>--}%
                                %{--<g:link controller="organisations" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>--}%
                            %{--</g:elseif>--}%
                            %{--<g:else>--}%
                                %{--${obj.owner?.name}--}%
                            %{--</g:else>--}%
                        %{--</g:elseif>--}%
                        %{--<g:else>--}%
                            %{--Not implemented yet!--}%
                        %{--</g:else>--}%
                    %{--</div>--}%
                %{--</td>--}%
            %{--</tr>--}%
        %{--</g:each>--}%

    </g:if>
</div>

</body>
</html>