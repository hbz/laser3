<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform" %>
<r:require module="annotations"/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : <g:message code="default.show.label"
                                                                     args="[entityName]"/></title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="platform" action="index" message="platform.show.all"/>
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}"/>
</semui:breadcrumbs>

<semui:modeSwitch controller="platform" action="show" params="${params}"/>

<h1 class="ui left aligned icon header"><semui:headerIcon/>

    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax"
                                                           action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</h1>

<g:render template="nav" contextPath="."/>

<semui:messages data="${flash}"/>

<fieldset class="inline-lists">
    <dl>

        <dt>${message(code: 'platform.name', default: 'Platform Name')}</dt>
        <dd><semui:xEditable owner="${platformInstance}" field="name"/></dd>

        <dt>${message(code: 'platform.gokbId', default: 'GOKb ID')}</dt>
        <dd>
            ${platformInstance?.gokbId}

            <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                    var="gokbAPI">
                <g:if test="${platformInstance?.gokbId}">
                    <a target="_blank"
                       href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + platformInstance?.gokbId : '#'}"><i
                            title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                </g:if>
            </g:each>
        </dd>

        <dt>${message(code: 'platform.org', default: 'Platform Provider')}</dt>
        <dd>
            <g:if test="${platformInstance.org}">
            <g:link controller="organisation" action="show"
                    id="${platformInstance.org.id}">${platformInstance.org.name}</g:link>
            </g:if>
        </dd>

        <dt>${message(code: 'platform.primaryUrl', default: 'Primary URL')}</dt>
        <dd>
            <semui:xEditable owner="${platformInstance}" field="primaryUrl"/>
            <g:if test="${platformInstance?.primaryUrl}">
                <a class="ui icon mini blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.tooltip.callUrl')}"
                   href="${platformInstance?.primaryUrl?.contains('http') ? platformInstance?.primaryUrl : 'http://' + platformInstance?.primaryUrl}"
                   target="_blank"><i class="share square icon"></i></a>
            </g:if>
        </dd>

        <dt>${message(code: 'platform.serviceProvider', default: 'Service Provider')}</dt>
        <dd><semui:xEditableRefData owner="${platformInstance}" field="serviceProvider" config="YN"/></dd>

        <dt>${message(code: 'platform.softwareProvider', default: 'Software Provider')}</dt>
        <dd><semui:xEditableRefData owner="${platformInstance}" field="softwareProvider" config="YN"/></dd>

        <g:if test="${params.mode == 'advanced'}">

            <dt>${message(code: 'platform.type', default: 'Type')}</dt>
            <dd><semui:xEditableRefData owner="${platformInstance}" field="type" config="YNO"/></dd>

            <dt>${message(code: 'platform.status', default: 'Status')}</dt>
            <dd><semui:xEditableRefData owner="${platformInstance}" field="status" config="UsageStatus"/></dd>

            <dt><g:message code="platform.globalUID.label" default="Global UID"/></dt>
            <dd><g:fieldValue bean="${platformInstance}" field="globalUID"/></dd>

        </g:if>
    </dl>
</fieldset>

<h3 class="ui left aligned icon header">${message(code: 'platform.show.availability', default: 'Availability of titles in this platform by package')}
<semui:totalNumber total="${tipps.size()}"/>
</h3>

<g:render template="/templates/tipps/table"
          model="[tipps: tipps, showPackage: true, showPlattform: false, showBulkFlag: false]"/>

</body>
</html>
