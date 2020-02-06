<%@ page import="com.k_int.kbplus.Subscription" %>
<%-- r:require module="annotations" / --%>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code:'default.subscription.label')}</title>
</head>
<body>

<div>
    <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="myInstitution" action="currentSubscriptions">${message(code:'myinst.currentSubscriptions.label')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="subscription" action="index" id="${subscriptionInstance.id}">${message(code:'default.subscription.label')} ${subscriptionInstance.id} - ${message(code:'subscription.details.details.label')}</g:link> </li>
    </ul>
</div>

<semui:messages data="${flash}" />
<div>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${subscriptionInstance.name}</h1>
    <g:render template="nav"  />
</div>

<div>

    <dl>
        <g:if test="${num_ie_rows > max}">
            <dt>${message(code:'title.plural')} ( ${message(code:'default.paginate.offset', args:[(offset+1),lastie,num_ie_rows])} ) </dt>
        </g:if>
        <g:set var="counter" value="${offset+1}" />

        <dd>

            <table  class="ui sortable celled la-rowspan table">
                <thead>

                <tr>
                    <th rowspan="2">${message(code:'sidewide.number')}</th>
                    <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label')}" />
                    <th>ISSN</th>
                    <g:sortableColumn params="${params}" property="coreStatus" title="${message(code:'subscription.details.core')}" />
                    <g:sortableColumn params="${params}" property="startDate" title="${message(code:'subscription.details.coverageStartDate')}" />
                    <g:sortableColumn params="${params}" property="coreStatusStart" title="${message(code:'subscription.details.coreStartDate')}" />
                    <th rowspan="2">${message(code:'default.actions.label')}</th>
                </tr>

                <tr>
                    <th>${message(code:'subscription.details.access_dates')}</th>
                    <th>eISSN</th>
                    <th></th>
                    <g:sortableColumn params="${params}" property="endDate" title="${message(code:'subscription.details.coverageEndDate')}" />
                    <g:sortableColumn params="${params}" property="coreStatusEnd" title="${message(code:'subscription.details.coreEndDate')}"  />
                </tr>


                </thead>
                <tbody>

                <g:if test="${titlesList}">
                    <g:each in="${titlesList}" var="ie">
                        <tr>
                            <td>${counter++}</td>
                            <td>
                                <g:link controller="issueEntitlement" id="${ie.id}" action="show">${ie.tipp.title.title}</g:link>
                                <g:if test="${ie.tipp?.hostPlatformURL}">( <a href="${ie.tipp?.hostPlatformURL}" TITLE="${ie.tipp?.hostPlatformURL}">${message(code:'tipp.hostPlatformURL')}</a>
                                    <a href="${ie.tipp?.hostPlatformURL}" TITLE="${ie.tipp?.hostPlatformURL} (${message(code:'default.new_window')})" target="_blank"><i class="icon-share-alt"></i></a>)</g:if> <br/>
                                ${message(code:'default.access.label')}: ${ie.availabilityStatus?.value}
                                <g:if test="${ie.availabilityStatus?.value=='Expected'}">
                                    ${message(code:'default.on')} <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessStartDate}"/>
                                </g:if>
                                <g:if test="${ie.availabilityStatus?.value=='Expired'}">
                                    ${message(code:'default.on')} <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessEndDate}"/>
                                </g:if>
                                <br/> ${message(code:'subscription.details.record_status')}: ${ie.status}
                                <br/> ${message(code:'subscription.details.access_start')}: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessStartDate}"/>
                                <br/> ${message(code:'subscription.details.access_end')}: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.accessEndDate}"/>

                            </td>
                            <td>
                                ${ie?.tipp?.title?.getIdentifierValue('ISSN')}<br/>
                                ${ie?.tipp?.title?.getIdentifierValue('eISSN')}
                            </td>
                            <td>
                                ${ie.coreStatus}
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.startDate}"/> <br/>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.endDate}"/>
                            </td>
                            <td>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.coreStatusStart}"/> <br/>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ie.coreStatusEnd}"/>
                            </td>
                            <td>
<!--  Review for LAS:eR usage
                                <g:if test="${institutional_usage_identifier}">
                                    <g:if test="${ie?.tipp?.title?.getIdentifierValue('ISSN')}">
                                        | <a href="https://www.jusp.mimas.ac.uk/secure/v2/ijsu/?id=${institutional_usage_identifier.value}&issn=${ie?.tipp?.title?.getIdentifierValue('ISSN')}">ISSN Usage</a>
                                    </g:if>
                                    <g:if test="${ie?.tipp?.title?.getIdentifierValue('eISSN')}">
                                        | <a href="https://www.jusp.mimas.ac.uk/secure/v2/ijsu/?id=${institutional_usage_identifier.value}&issn=${ie?.tipp?.title?.getIdentifierValue('eISSN')}">eISSN Usage</a>
                                    </g:if>
                                </g:if>-->
                            </td>
                        </tr>
                    </g:each>
                </g:if>
                </tbody>
            </table>
        </dd>
    </dl>


        <g:if test="${titlesList}" >
            <semui:paginate  action="${screen}" controller="subscription" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" maxsteps="${max}" total="${num_ie_rows}" />
        </g:if>

</div>
</body>

</html>
