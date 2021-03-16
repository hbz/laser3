<%@ page import="de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

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
                    <tr>
                        <td style="text-align: center"><span class="ui label circular green">${i + 1}.</span></td>
                </g:if>
                <g:else>
                    <td style="text-align: center">${i + 1}.</td>
                </g:else>
                    <td>
                        <%
                            Long ieId = IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription.id = :id and ie.tipp = :tipp',
                                [id: id, tipp: tipp]
                            )[0]
                        %>
                        <g:link controller="issueEntitlement" action="show" id="${ieId}" target="_blank">${tipp.name}</g:link>
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
                        <td style="text-align: center">${i + 1}.</td>
                        <td>
                            <%
                                Long ieId2 = IssueEntitlement.executeQuery(
                                        'select ie.id from IssueEntitlement ie where ie.subscription.id = :id and ie.tipp = :tipp',
                                        [id: id, tipp: tipp]
                                )[0]
                            %>
                            <g:link controller="issueEntitlement" action="show" id="${ieId2}" target="_blank">${tipp.name}</g:link>
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