<%@ page import="com.k_int.kbplus.Subscription" %>
<r:require module="annotations" />

<!doctype html>
<html>
<head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
</head>
<body>

<div class="container">
    <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <g:if test="${subscriptionInstance.subscriber}">
            <li> <g:link controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:subscriptionInstance.subscriber.shortcode]}"> ${subscriptionInstance.subscriber.name} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}</g:link> <span class="divider">/</span> </li>
        </g:if>
        <li> <g:link controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}">${message(code:'subscription.label', default:'Subscription')} ${subscriptionInstance.id} - ${message(code:'subscription.details.details.label', default:'Details')}</g:link> </li>
</div>

<g:if test="${flash.message}">
    <div class="container"><bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert></div>
</g:if>

<g:if test="${flash.error}">
    <div class="container"><bootstrap:alert class="alert-error">${flash.error}</bootstrap:alert></div>
</g:if>

<div class="container">
    <h1>${subscriptionInstance.name}</h1>
    <g:render template="nav"  />
</div>

<div class="container">

    <dl>
        <g:if test="${num_ie_rows > max}">
            <dt>${message(code:'title.plural', default:'Titles')} ( ${message(code:'default.paginate.offset', args:[(offset+1),lastie,num_ie_rows])} ) </dt>
        </g:if>
        <g:set var="counter" value="${offset+1}" />

        <dd>

            <table  class="table table-striped table-bordered">
                <thead>

                <tr>
                    <th rowspan="2">#</th>
                    <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                    <th>ISSN</th>
                    <g:sortableColumn params="${params}" property="coreStatus" title="${message(code:'subscription.details.core', default:'Core')}" />
                    <g:sortableColumn params="${params}" property="startDate" title="${message(code:'subscription.details.coverageStartDate', default:'Coverage Start Date')}" />
                    <g:sortableColumn params="${params}" property="coreStatusStart" title="${message(code:'subscription.details.coreStartDate', default:'Core Start Date')}" />
                    <th rowspan="2">${message(code:'default.actions.label', default:'Actions')}</th>
                </tr>

                <tr>
                    <th>${message(code:'subscription.details.access_dates', default:'Access Dates')}</th>
                    <th>eISSN</th>
                    <th></th>
                    <g:sortableColumn params="${params}" property="endDate" title="${message(code:'subscription.details.coverageEndDate', default:'Coverage End Date')}" />
                    <g:sortableColumn params="${params}" property="coreStatusEnd" title="${message(code:'subscription.details.coreEndDate', default:'Core End Date')}"  />
                </tr>


                </thead>
                <tbody>

                <g:if test="${titlesList}">
                    <g:each in="${titlesList}" var="ie">
                        <tr>
                            <td>${counter++}</td>
                            <td>
                                <g:link controller="issueEntitlement" id="${ie.id}" action="show">${ie.tipp.title.title}</g:link>
                                <g:if test="${ie.tipp?.hostPlatformURL}">( <a href="${ie.tipp?.hostPlatformURL}" TITLE="${ie.tipp?.hostPlatformURL}">${message(code:'tipp.hostPlatformURL', default:'Host Link')}</a>
                                    <a href="${ie.tipp?.hostPlatformURL}" TITLE="${ie.tipp?.hostPlatformURL} (${message(code:'default.new_window', default:'In new window')})" target="_blank"><i class="icon-share-alt"></i></a>)</g:if> <br/>
                                ${message(code:'default.access.label', default:'Access')}: ${ie.availabilityStatus?.value}
                                <g:if test="${ie.availabilityStatus?.value=='Expected'}">
                                    ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessStartDate}"/>
                                </g:if>
                                <g:if test="${ie.availabilityStatus?.value=='Expired'}">
                                    ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessEndDate}"/>
                                </g:if>
                                <br/> ${message(code:'subscription.details.record_status', default:'Record Status')}: ${ie.status}
                                <br/> ${message(code:'subscription.details.access_start', default:'Access Start')}: <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessStartDate}"/>
                                <br/> ${message(code:'subscription.details.access_end', default:'Access End')}: <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessEndDate}"/>

                            </td>
                            <td>
                                ${ie?.tipp?.title?.getIdentifierValue('ISSN')}<br/>
                                ${ie?.tipp?.title?.getIdentifierValue('eISSN')}
                            </td>
                            <td>
                                ${ie.coreStatus}
                            <td>
                                <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.startDate}"/> <br/>
                                <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.endDate}"/>
                            </td>
                            <td>
                                <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.coreStatusStart}"/> <br/>
                                <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.coreStatusEnd}"/>
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

    <div class="pagination" style="text-align:center">
        <g:if test="${titlesList}" >
            <bootstrap:paginate  action="${screen}" controller="subscriptionDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_ie_rows}" />
        </g:if>
    </div>

</div>
</body>

</html>
