<%@ page import="de.laser.helper.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition; de.laser.reporting.OrganisationConfig;" %>

<h3 class="ui header">3. Details</h3>

<div class="ui message success">
    <p>${label}</p>
</div>

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>Sortiername</th>
            <th>${label.split('>').first().trim()}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${org.sortname}</td>
                    <g:if test="${query == ''}">
                        <td><g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link></td>
                    </g:if>
                    <g:else>  %{-- default --}%
                        <td><g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link></td>
                    </g:else>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>