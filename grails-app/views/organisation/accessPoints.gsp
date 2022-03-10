<%@ page import="de.laser.Org; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.helper.RDStore; de.laser.helper.RDConstants;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <g:set var="entityName" value="${message(code: 'org.label')}" />
        <title>${message(code:'laser')} : ${message(code:'org.nav.accessPoints')}</title>
    </head>
    <body>
    <semui:controlButtons>
        <g:if test="${editable}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="accessPoints" params="${params + [exportXLSX: true]}">${message(code: 'accessPoint.exportAccessPoints')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
        </g:if>
        <g:if test="${editable}">

            <semui:actionsDropdown>
                <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ip']" message="accessPoint.create_ip"/>
                <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'proxy']" message="accessPoint.create_proxy"/>
                <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ezproxy']" message="accessPoint.create_ezproxy"/>
                <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'oa']" message="accessPoint.create_openAthens"/>
                <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'shibboleth']" message="accessPoint.create_shibboleth"/>
            </semui:actionsDropdown>

        </g:if>
    </semui:controlButtons>

    <semui:breadcrumbs>
        <g:if test="${!inContextOrg}">
            <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${orgInstance.name}</h1>

    <g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}" />

    <semui:messages data="${flash}" />

    <div class="la-inline-lists">

<g:each in="${orgAccessPointList}" var="accessPointListItem">
        <div class="ui card">
            <div class="content">
                <div class="header">
                    <h4 class="ui header">${RDStore.getRefdataValue(accessPointListItem.key, RDConstants.ACCESS_POINT_TYPE).getI10n('value')}</h4></div>
            </div>
            <div class="content">

                <table  class="ui celled la-js-responsive-table la-table table">
                    <thead>
                        <tr>
                            <th class="four wide">${message(code: 'accessPoint.name')}</th>
                            <g:if test="${accessPointListItem.key in ['ip', 'proxy']}">
                                <th class="five wide">IPv4</th>
                                <th class="five wide">IPv6</th>
                            </g:if>
                            <g:elseif test="${accessPointListItem.key == 'ezproxy'}">
                                <th class="three wide">IPv4</th>
                                <th class="three wide">IPv6</th>
                                <th class="four wide"><g:message code="accessPoint.url"/></th>
                            </g:elseif>
                            <g:else>
                                <th class="ten wide">${message(code: 'accessRule.plural')}</th>
                            </g:else>
                            <g:if test="${editable}">
                                <th class="la-action-info two wide">${message(code: 'default.actions.label')}</th>
                            </g:if>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${accessPointListItem.value}" var="accessPointItem">
                        <g:set var="accessPoint" value="${accessPointItem.oap}"/>
                        <tr>
                            <th scope="row" class="la-th-column la-main-object" >
                                <g:if test="${editable}">
                                    <g:link controller="accessPoint" action="edit_${accessPoint.accessMethod.value.toLowerCase()}" id="${accessPoint.id}">
                                        ${accessPoint.name}
                                    </g:link>
                                </g:if>
                                <g:else>
                                    ${accessPoint.name}
                                </g:else>
                            </th>
                            <g:if test="${accessPointListItem.key in ['ip', 'proxy']}">
                                <td>
                                    <g:each in="${accessPoint.getIpRangeStrings('ipv4', 'ranges')}" var="ipv4Range">
                                        <div >${ipv4Range}</div>
                                    </g:each>
                                </td>
                                <td>
                                    <g:each in="${accessPoint.getIpRangeStrings('ipv6', 'ranges')}" var="ipv6Range">
                                        <div >${ipv6Range}</div>
                                    </g:each>
                                </td>
                            </g:if>
                            <g:elseif test="${accessPointListItem.key == 'ezproxy'}">
                                <td>
                                    <g:each in="${accessPoint.getIpRangeStrings('ipv4', 'ranges')}" var="ipv4Range">
                                        <div >${ipv4Range}</div>
                                    </g:each>
                                </td>
                                <td>
                                    <g:each in="${accessPoint.getIpRangeStrings('ipv6', 'ranges')}" var="ipv6Range">
                                        <div >${ipv6Range}</div>
                                    </g:each>
                                </td>
                                <td>
                                    <g:if test="${accessPoint.hasProperty('url')}">
                                        ${accessPoint.url}
                                    </g:if>
                                </td>
                            </g:elseif>
                            <g:else>
                                <td>
                                    <g:if test="${accessPoint.hasProperty('url')}">
                                       Proxy URL: ${accessPoint.url}
                                    </g:if>
                                    <g:if test="${accessPoint.hasProperty('entityId')}">
                                        EntityId: ${accessPoint.entityId}
                                    </g:if>
                                </td>
                            </g:else>
                            <g:if test="${editable}">
                                <td class="center aligned">
                                    <g:if test="${accessPointItem['platformLinkCount'] == 0 && accessPointItem['subscriptionLinkCount'] == 0}">
                                        <g:link action="delete" controller="accessPoint" id="${accessPoint?.id}"
                                                class="ui negative icon button la-modern-button js-open-confirm-modal"
                                                data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.accessPoint', args: [accessPoint.name])}"
                                                data-confirm-term-how="delete"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="trash alternate outline icon"></i>
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        <div data-tooltip="${message(code: 'accessPoint.list.deleteDisabledInfo', args: [accessPointItem['platformLinkCount'], accessPointItem['subscriptionLinkCount']])}" data-position="bottom center">
                                            <div class="ui icon button la-modern-button disabled">
                                                <i class="trash alternate outline icon"></i>
                                            </div>
                                        </div>
                                    </g:else>

                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                    </tbody>
                </table>


            </div>
        </div>
</g:each>
    </div>





    </body>
</html>