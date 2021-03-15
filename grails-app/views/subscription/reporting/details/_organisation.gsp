<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

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
            <th>Name</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <g:if test="${plusList.contains(org)}">
                    <tr class="positive">
                        <td><strong>${i + 1}.</strong></td>
                </g:if>
                <g:else>
                    <tr>
                        <td>${i + 1}.</td>
                </g:else>
                    <td>${org.sortname}</td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<g:if test="${minusList}">
    <div class="ui segment">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th></th>
                <th>Sortiername</th>
                <th>Name</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="org" status="i">
                    <tr class="negative">
                        <td>${i + 1}.</td>
                        <td>${org.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>