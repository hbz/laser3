<%@ page import="de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

<h3 class="ui header">3. Details</h3>

<div class="ui message success">
    <p>${label}</p>
</div>

<g:set var="plusListNames" value="${plusList.collect{ it.name }}" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>Name</th>
            <th>Typ / Medium</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="tipp" status="i">
                <g:if test="${plusListNames.contains(tipp.name)}">
                    <tr class="positive">
                        <td><strong>${i + 1}.</strong></td>
                </g:if>
                <g:else>
                    <tr>
                        <td>${i + 1}.</td>
                </g:else>
                    <td>
                        ${tipp.name}
                        %{--<g:link controller="issueEntitlement" action="show" id="${tipp.id}" target="_blank">${tipp.name}</g:link>--}%
                    </td>
                    <td>
                        ${tipp.titleType}
                        <g:if test="${tipp.medium}">
                            <g:if test="${tipp.titleType}"> / </g:if>
                            ${tipp.medium.getI10n('value')}
                        </g:if>
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
                <th>Name</th>
                <th>Typ / Medium</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="tipp" status="i">
                    <tr class="negative">
                        <td>${i + 1}.</td>
                        <td>
                            ${tipp.name}
                            %{--<g:link controller="issueEntitlement" action="show" id="${tipp.id}" target="_blank">${tipp.name}</g:link>--}%
                        </td>
                        <td>
                            ${tipp.titleType}
                            <g:if test="${tipp.medium}">
                                <g:if test="${tipp.titleType}"> / </g:if>
                                ${tipp.medium.getI10n('value')}
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>