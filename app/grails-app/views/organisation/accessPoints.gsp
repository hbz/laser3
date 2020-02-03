<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'org.nav.accessPoints')}</title>

        <g:javascript src="properties.js"/>
    </head>
    <body>

    <semui:breadcrumbs>
        <g:if test="${!inContextOrg}">
            <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
    </semui:breadcrumbs>
    <br>
    <g:if test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR')
        || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
    </g:if>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${orgInstance.name}</h1>

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
                    <th>${message(code: 'accessPoint.name', default: 'Name')}</th>
                    <th>${message(code: 'accessMethod.label', default: 'Access Method')}</th>
                    <th>${message(code: 'accessRule.plural', default: 'Access Rules')}</th>
                    <g:if test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
                        <th class="la-action-info">${message(code: 'default.actions.label')}</th>
                    </g:if>
                </tr>
                </thead>
                <tbody>
                    <g:each in="${orgAccessPointList}" var="accessPoint">
                        <tr>
                        <td class="la-main-object" >
                        <g:link controller="accessPoint" action="edit_${accessPoint.accessMethod.value.toLowerCase()}" id="${accessPoint.id}">
                            ${accessPoint.name}
                        </g:link>
                        </td>
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
                            <g:if test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR') && inContextOrg)}">
                                <td class="center aligned">
                                <g:link action="delete" controller="accessPoint" id="${accessPoint?.id}" class="ui negative icon button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.accessPoint', args: [accessPoint.name])}"
                                        data-confirm-term-how="delete">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                 </tbody>
            </table>
        </g:form>
    </body>
</html>