<%@ page import="de.laser.Platform" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.my.platforms')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.my.platforms" class="active" />
</semui:breadcrumbs>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>${message(code:'menu.my.platforms')}
    <semui:totalNumber total="${platformInstanceTotal}"/>
</h1>

<semui:messages data="${flash}" />

<%-- WORKAROUND
<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="currentPlatforms" method="get" class="ui form">
        <div class="two fields">
            <div class="field">
                <label>${message(code:'default.search.text')}</label>
                <input type="text" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.filterreset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</semui:filter>
--%>

<table class="ui sortable celled la-table table">
    <thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
        <th>${message(code:'default.provider.label')}</th>
        <th>${message(code:'org.url.label')}</th>
        <th>${message(code:'accessPoint.plural')}</th>
        <th>${message(code:'myinst.currentPlatforms.assignedSubscriptions')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${platformInstanceList}" var="platformInstance" status="jj">
        <tr>
            <td>
                ${ (params.int('offset') ?: 0)  + jj + 1 }
            </td>
            <th scope="row" class="la-th-column">
                <g:link class="la-main-object"  controller="platform" action="show" id="${platformInstance.id}">${fieldValue(bean: platformInstance, field: "name")}</g:link>
            </th>
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
                <g:each in="${platformInstance.getContextOrgAccessPoints(contextOrg)}" var="oap" >
                    <g:link controller="accessPoint" action="edit_${oap.accessMethod}" id="${oap.id}">${oap.name} (${oap.accessMethod.getI10n('value')})</g:link> <br />
                </g:each>
            </td>
            <td>
                <g:each in="${subscriptionMap.get('platform_' + platformInstance.id)}" var="sub">
                    <g:link controller="subscription" action="show" id="${sub.id}">${sub}<br /></g:link>

                    <g:if test="${sub.packages}">
                        <g:each in="${sub.deduplicatedAccessPointsForOrgAndPlatform(contextOrg, platformInstance)}" var="orgap">
                            <div class="la-flexbox">
                                <span data-position="top right"
                                      class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'myinst.currentPlatforms.tooltip.thumbtack.content')}">
                                    <i class="icon la-thumbtack slash scale la-list-icon"></i>
                                </span>
                                <g:link controller="accessPoint" action="edit_${orgap.accessMethod}"
                                        id="${orgap.id}">${orgap.name} (${orgap.accessMethod.getI10n('value')})</g:link>
                            </div>
                        </g:each>
                    </g:if>
                </g:each>
            </td>
            <%--<td class="x">
            </td>--%>
        </tr>
    </g:each>
    </tbody>
</table>

<semui:debugInfo>
    <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
</semui:debugInfo>

</body>
</html>
