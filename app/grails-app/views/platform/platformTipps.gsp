<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser')} : <g:message code="platform.nav.platformTipps"/></title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="platform" action="index" message="platform.show.all"/>
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}"/>
</semui:breadcrumbs>
<br>

<semui:modeSwitch controller="platform" action="show" params="${params}"/>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>

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

<g:render template="nav"/>

<semui:messages data="${flash}"/>

<g:render template="/package/filter" model="${[params:params]}"/>


<h3 class="ui header la-clear-before">${message(code: 'platform.show.availability', default: 'Availability of titles in this platform by package')}
<semui:totalNumber total="${countTipps}"/>
</h3>

<g:render template="/templates/tipps/table"
          model="[tipps: tipps, showPackage: true, showPlattform: false, showBulkFlag: false]"/>

<g:if test="${countTipps}" >
    <semui:paginate action="platformTipps" controller="platform" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" maxsteps="${max}" total="${countTipps}" />
</g:if>

</body>
</html>