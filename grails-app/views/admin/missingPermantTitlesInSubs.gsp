<%@ page import="de.laser.storage.RDStore; de.laser.IssueEntitlement; de.laser.PermanentTitle" %>

<laser:htmlStart message="menu.admin.missingPermantTitlesInSubs" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.missingPermantTitlesInSubs" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.missingPermantTitlesInSubs" type="admin" total="${subs.size()}"/>

<ui:messages data="${flash}" />

<g:set var="totalIes" value="${0}"/>
<g:set var="totalPTs" value="${0}"/>
<g:set var="totalDiffs" value="${0}"/>

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'default.start.label')} / ${message(code: 'default.end.label')}</th>
            <th>Permant Titles</th>
            <th>Titles</th>
            <th>Diff.</th>
        </tr>
    </thead>
    <tbody>
    <g:each in="${subs}" var="s" status="i">
        <tr>
            <td class="center aligned">
                ${ (params.int('offset') ?: 0)  + i + 1 }
            </td>
            <th scope="row" class="la-th-column">
                <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                    <g:if test="${s.name}">
                        ${s.name}
                        <g:if test="${s?.referenceYear}">
                            ( ${s.referenceYear} )
                        </g:if>

                    </g:if>
                    <g:else>
                        -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                    </g:else>
                    <g:if test="${s.instanceOf}">
                        <g:if test="${s.consortia && s.consortia == institution}">
                            ( ${s.subscriber?.name} )
                        </g:if>
                    </g:if>
                </g:link>
            </th>
            <td>
                ${s.type?.getI10n('value')}
            </td>
            <td>
                <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:"><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></span>
            </td>
            <td>
                <g:set var="countPT" value="${PermanentTitle.executeQuery('''select count(*) from PermanentTitle
                        where owner = :owner and tipp in (select ie.tipp from IssueEntitlement ie where ie.subscription = :sub and ie.tipp.status != :removed and ie.status != :removed)''', [owner: s.subscriber, sub: s, removed: RDStore.TIPP_STATUS_REMOVED])[0]}"/>
                <g:link controller="subscription" action="index" id="${s.id}" params="[hasPerpetualAccess: 1]">${countPT}</g:link>
            </td>
            <td>
                <g:set var="countTitles" value="${IssueEntitlement.executeQuery('''select count(*) from IssueEntitlement ie where ie.subscription = :sub and ie.tipp.status != :removed and ie.status != :removed''', [sub: s, removed: RDStore.TIPP_STATUS_REMOVED])[0]}"/>
                <g:link controller="subscription" action="index" id="${s.id}" params="[hasPerpetualAccess: 2]">${countTitles}</g:link>
            </td>
            <td>
                ${countTitles-countPT}
            </td>
            <g:set var="totalPTs" value="${totalPTs+countPT}"/>
            <g:set var="totalIes" value="${totalIes+countTitles}"/>
            <g:set var="totalDiffs" value="${totalDiffs+(countTitles-countPT)}"/>
        </tr>
    </g:each>
    <tr>
        <td>Total:</td>
        <td></td>
        <td></td>
        <td></td>
        <td>${totalPTs}</td>
        <td>${totalIes}</td>
        <td>${totalDiffs}</td>
    </tr>
    </tbody>
</table>

<laser:htmlEnd />
