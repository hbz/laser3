<%@ page import="de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

<laser:render template="/subscription/reporting/details/timeline/base.part1" />

<g:if test="${minusList}">
    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a data-tab="positive" class="item active">${message(code:'reporting.local.subscription.timeline.chartLabel.package.3')}</a>
        <a data-tab="minus" class="item">${message(code:'reporting.local.subscription.timeline.chartLabel.package.1')}</a>
    </div>
    <div data-tab="positive" class="ui bottom attached tab segment active">
</g:if>
<g:else>
    <div class="ui segment">
</g:else>

        <g:set var="plusListNames" value="${plusList.collect{ it.name }}" />

        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'package.label')}</th>
                <th scope="col">${message(code:'default.ie')}%{-- / ${message(code:'title.plural')}--}%</th>
                <th scope="col">${message(code:'platform.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${list}" var="pkg" status="i">
                    <g:if test="${plusListNames.contains(pkg.name)}">
                        <tr>
                            <td class="center aligned"><span class="ui label circular green">${i + 1}</span></td>
                    </g:if>
                    <g:else>
                        <td class="center aligned">${i + 1}</td>
                    </g:else>
                        <td>
                            <g:link controller="package" action="show" id="${pkg.id}" target="_blank">${pkg.name}</g:link>
                        </td>
                        <td>
                            <%
                                List<Long> ieIdList = IssueEntitlement.executeQuery(
                                        'select ie.id from IssueEntitlement ie join ie.tipp tipp where tipp.pkg.id = :pkgId and ie.subscription.id = :id',
                                        [pkgId: pkg.id, id: id]
                                )
                                println ieIdList.size()

//                                List<Long> tippIdList = TitleInstancePackagePlatform.executeQuery(
//                                        'select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId', [pkgId: pkg.id]
//                                )
//
//                                println '' + tippIdList.size() + ' / ' + ieIdList.size()
                            %>
                        </td>
                        <td>
                            <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}" target="_blank">${pkg.nominalPlatform.name}</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>

<g:if test="${minusList}">

    <div data-tab="minus" class="ui bottom attached tab segment">
        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'package.label')}</th>
                <th scope="col">${message(code:'default.ie')}</th>
                <th scope="col">${message(code:'platform.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="pkg" status="i">
                    <tr>
                        <td class="center aligned"><span class="ui label circular red">${i + 1}</span></td>
                        <td>
                            <g:link controller="package" action="show" id="${pkg.id}" target="_blank">${pkg.name}</g:link>
                        </td>
                        <td>
%{--                            <%--}%
%{--                                List<Long> ieIdList2 = IssueEntitlement.executeQuery(--}%
%{--                                        'select ie.id from IssueEntitlement ie join ie.tipp tipp where tipp.pkg.id = :pkgId and ie.subscription.id = :id',--}%
%{--                                        [pkgId: pkg.id, id: id]--}%
%{--                                )--}%
%{--                                println ieIdList2.size()--}%
%{--                            %>--}%
                        </td>
                        <td>
                            <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}" target="_blank">${pkg.nominalPlatform.name}</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>

<laser:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />