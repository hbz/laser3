<%@ page import="de.laser.reporting.OrganisationConfig;de.laser.reporting.SubscriptionConfig;" %>

<g:if test="${key == OrganisationConfig.KEY}">
    <h3 class="ui header">3. Details</h3>

    <div class="ui message success">
        <p>${label}</p>
    </div>

    <div class="ui segment">
        <table class="ui table la-table compact">
            %{-- <thead>
            <tr>
                <th></th>
                <th></th>
                <th></th>
            </tr>
            </thead> --}%
            <tbody>
                <g:each in="${list}" var="org" status="i">
                    <tr>
                        <td>${i + 1}.</td>
                        <td>${org.sortname}</td>
                        <td><g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link></td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>
<g:if test="${key == SubscriptionConfig.KEY}">
    <h3 class="ui header">3. Details</h3>

    <div class="ui message success">
        <p>${label}</p>
    </div>

    <div class="ui segment">
        <table class="ui table la-table compact">
            %{-- <thead>
            <tr>
                <th></th>
                <th></th>
            </tr>
            </thead> --}%
            <tbody>
                <g:each in="${list}" var="sub" status="i">
                    <tr>
                        <td>${i + 1}.</td>
                        <td><g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link></td>
                        <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.startDate}" /></td>
                        <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.endDate}" /></td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>
