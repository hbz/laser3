<%@ page import="com.k_int.kbplus.Platform" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.my.platforms')}</title>
</head>
<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb message="menu.my.platforms" class="active" />
</semui:breadcrumbs>


<h1 class="ui left aligned icon header">${message(code:'menu.my.platforms')}
    <semui:totalNumber total="${platformInstanceTotal}"/>
</h1>

<semui:messages data="${flash}" />

<semui:filter>
    <g:form action="currentPlatforms" method="get" class="ui form">
        <div class="two fields">
            <div class="field">
                <label>${message(code:'default.search.text', default:'Search text')}</label>
                <input type="text" name="q" placeholder="${message(code:'default.search.ph', default:'enter search term...')}" value="${params.q}" />
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}" />
            </div>
        </div>
    </g:form>
</semui:filter>

<table class="ui sortable celled la-table table">
    <thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <g:sortableColumn property="name" title="${message(code: 'platform.name.label', default: 'Name')}" />
        <th>${message(code:'default.provider.label')}</th>
        <th>${message(code:'org.url.label')}</th>
        <th>Zugeordnete aktive Lizenzen</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${platformInstanceList}" var="platformInstance" status="jj">
        <tr>
            <td>
                ${ (params.int('offset') ?: 0)  + jj + 1 }
            </td>
            <td>
                <g:link controller="platform" action="show" id="${platformInstance.id}">${fieldValue(bean: platformInstance, field: "name")}</g:link>
            </td>
            <td>
                <g:if test="${platformInstance.org}">
                    <g:link controller="organisation" action="show" id="${platformInstance.org?.id}">${platformInstance.org?.getDesignation()}</g:link>
                </g:if>
            </td>
            <td>
                <g:if test="${platformInstance.primaryUrl}">
                    ${platformInstance.primaryUrl}
                    <a href="<g:createLink url="${platformInstance.primaryUrl}" />" target="_blank"><i class="external alternate icon"></i></a>
                </g:if>
            </td>
            <td>
                <g:each in="${subscriptionMap.get('platform_' + platformInstance.id)}" var="sub">
                    <g:link controller="subscription" action="show" id="${sub.id}">${sub}</g:link> <br />
                </g:each>
            </td>
            <%--<td class="x">
            </td>--%>
        </tr>
    </g:each>
    </tbody>
</table>

<%--
<semui:paginate action="currentPlatforms" controller="myInstitution" params="${params}"
                next="${message(code:'default.paginate.next', default:'Next')}"
                prev="${message(code:'default.paginate.prev', default:'Prev')}"
                max="${max}" total="${platformInstanceTotal}" />
--%>

</body>
</html>
