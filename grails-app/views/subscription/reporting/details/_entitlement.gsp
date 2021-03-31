<%@ page import="de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>Name</th>

            <g:if test="${query != 'tipp-publisherName'}">
                <th>Herausgeber</th>
            </g:if>
            <g:if test="${query != 'tipp-seriesName'}">
                <th>Name der Reihe</th>
            </g:if>
            <g:if test="${query != 'tipp-subjectReference'}">
                <th>Fachbereich</th>
            </g:if>
            <g:if test="${query != 'tipp-titleType'}">
                <th>Titel-Typ</th>
            </g:if>
            <g:if test="${query != 'tipp-medium'}">
                <th>Medium</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="tipp" status="i">
                <tr>
                    <td style="text-align: center">${i + 1}.</td>
                    <td>
                        <%
                            Long ieId = IssueEntitlement.executeQuery(
                                'select ie.id from IssueEntitlement ie where ie.subscription.id = :id and ie.tipp = :tipp',
                                [id: id, tipp: tipp]
                            )[0]
                        %>
                        <g:link controller="issueEntitlement" action="show" id="${ieId}" target="_blank">${tipp.name}</g:link>
                    </td>
                    <g:if test="${query != 'tipp-publisherName'}">
                        <td>${tipp.publisherName}</td>
                    </g:if>
                    <g:if test="${query != 'tipp-seriesName'}">
                        <td>${tipp.seriesName}</td>
                    </g:if>
                    <g:if test="${query != 'tipp-subjectReference'}">
                        <td>${tipp.subjectReference}</td>
                    </g:if>
                    <g:if test="${query != 'tipp-titleType'}">
                        <td>${tipp.titleType}</td>
                    </g:if>
                    <g:if test="${query != 'tipp-medium'}">
                        <td>${tipp.medium?.getI10n('value')}</td>
                    </g:if>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>