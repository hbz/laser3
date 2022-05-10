<%@ page import="de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

<laser:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th scope="col" class="center aligned">
                ${message(code:'sidewide.number')}
            </th>
            <th scope="col">${message(code:'tipp.name')}</th>

            <g:if test="${query != 'tipp-publisherName'}">
                <th scope="col">${message(code:'tipp.publisher')}</th>
            </g:if>
            <g:if test="${query != 'tipp-seriesName'}">
                <th scope="col">${message(code:'tipp.seriesName')}</th>
            </g:if>
            <g:if test="${query != 'tipp-subjectReference'}">
                <th scope="col">${message(code:'tipp.subjectReference')}</th>
            </g:if>
            <g:if test="${query != 'tipp-titleType'}">
                <th scope="col">${message(code:'tipp.titleType')}</th>
            </g:if>
            <g:if test="${query != 'tipp-medium'}">
                <th scope="col">${message(code:'tipp.medium')}</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="tipp" status="i">
                <tr>
                    <td class="center aligned">${i + 1}</td>
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

<laser:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />