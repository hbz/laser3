<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>

        <g:javascript src="properties.js"/>
    </head>
    <body>

    <semui:breadcrumbs>
        <semui:crumb controller="organisation" action="show" id="${orgInstance.id}" text="${orgInstance.getDesignation()}" />
        <semui:crumb message="org.nav.accessPoints" class="active"/>
    </semui:breadcrumbs>

    <g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
    </g:if>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        ${orgInstance.name} - ${message(code:'org.nav.accessPoints')}</h1>
    </h1>

    <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}" />

    <semui:messages data="${flash}" />
    
    
%{--
    <semui:filter>
        <g:form action="accessPoints" role="form" class="ui form" method="get" params="${params}">
          <div class="fields">
            <div class="field">
              <label for="filter" class="control-label">${message(code: 'accessPoint.show')}</label>
              <g:select id="filter"
                        name="filter"
                        from="${[
                                    [key:'all',value:"${message(code: 'accessPoint.all')}"],
                                    [key:'valid',value:"${message(code:'accessPoint.valid')}"],
                                    [key:'invalid',value:"${message(code: 'accessPoint.invalid')}"]
                               ]}"
                        optionKey="key" optionValue="value" value="${params.filter}"
                        onchange="this.form.submit()"/>
            </div>
          </div>
        </g:form>
    </semui:filter>
--}%

        <g:form class="ui form" url="[controller: 'accessPoint', action: 'create']" method="POST">
            <table  class="ui celled la-table table">
                <thead>
                        <tr>
                            <g:sortableColumn property="AccessPoint" title="${message(code: 'accessPoint.name', default: 'Name')}" />
                            <g:sortableColumn property="accessMethod" title="${message(code: 'accessMethod.label', default: 'Access Method')}" />
                            <g:sortableColumn property="rules" title="${message(code: 'accessRule.plural', default: 'Access Rules')}" />
                            <th>${message(code: 'accessPoint.actions', default: 'Actions')}</th>
                        </tr>
                </thead>
                <tbody>
                    <g:each in="${orgAccessPointList}" var="accessPoint">
                        <tr>
                            <td class="la-main-object" >${accessPoint.name}</td>
                            <td>${accessPoint.accessMethod.getI10n('value')}</td>
                            <td>
                                <g:each in="${accessPoint.getIpRangeStrings('ipv4', 'ranges')}" var="ipv4Range">
                                    <div >${ipv4Range}</div>
                                </g:each>
                                <g:each in="${accessPoint.getIpRangeStrings('ipv6', 'ranges')}" var="ipv6Range">
                                    <div >${ipv6Range}</div>
                                </g:each>
                                <div>
                                    <g:if test="${accessPoint.hasProperty('url')}">
                                        URL: ${accessPoint.url}
                                    </g:if>
                                </div>
                                <div>
                                    <g:if test="${accessPoint.hasProperty('entityId')}">
                                        EntityId: ${accessPoint.entityId}
                                    </g:if>
                                </div>
                            </td>
                            <td class="center aligned">
                                <g:link action="edit_${accessPoint.accessMethod.value.toLowerCase()}" controller="accessPoint" id="${accessPoint?.id}" class="ui icon button">
                                    <i class="write icon"></i>
                                </g:link>
                                <g:link action="delete" controller="accessPoint" id="${accessPoint?.id}" class="ui negative icon button js-open-confirm-modal"
                                        data-confirm-term-what="${message(code: 'accessPoint.delete.what', args: [accessPoint.name])}"
                                        data-confirm-term-content="${message(code: 'accessPoint.details.delete.confirm', args: [(accessPoint.name ?: 'this access point')])}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </td>
                        </tr>
                    </g:each>
                 </tbody>
            </table>
        </g:form>
    </body>
</html>