<%@ page import="de.laser.Org; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.storage.RDConstants;" %>

<laser:htmlStart message="org.nav.accessPoints" serviceInjection="true" />
        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:controlButtons>
        <g:if test="${editable}">
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="accessPoints" params="${params + [exportXLSX: true]}">${message(code: 'accessPoint.exportAccessPoints')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
        </g:if>

        <laser:render template="actions" />

%{--        <g:if test="${editable}">

            <ui:actionsDropdown>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ip']" message="accessPoint.create_ip"/>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'proxy']" message="accessPoint.create_proxy"/>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ezproxy']" message="accessPoint.create_ezproxy"/>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'oa']" message="accessPoint.create_oa"/>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'shibboleth']" message="accessPoint.create_shibboleth"/>
                <ui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'mailDomain']" message="accessPoint.create_mailDomain"/>
            </ui:actionsDropdown>

        </g:if>--}%
    </ui:controlButtons>

    <laser:render template="breadcrumb"
                model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}" />

    <ui:messages data="${flash}" />

    <div class="la-inline-lists">

        <div class="ui top attached stackable tabular la-tab-with-js menu">

            <g:each in="${de.laser.RefdataCategory.getAllRefdataValues(RDConstants.ACCESS_POINT_TYPE)}"
                    var="accessPointType">
                <a class="${activeTab == accessPointType.value ? 'active' : ''} item"
                   data-tab="${accessPointType.value}">${accessPointType.getI10n('value')}  <div class="ui circular label">${orgAccessPointList[accessPointType.value] ? orgAccessPointList[accessPointType.value].size() : 0}</div></a>
            </g:each>

        </div>

<g:each in="${de.laser.RefdataCategory.getAllRefdataValues(RDConstants.ACCESS_POINT_TYPE)}" var="accessPointType">

    <div class="ui bottom attached ${activeTab == accessPointType.value ? 'active' : ''} tab segment"
         data-tab="${accessPointType.value}">

        <g:link action="create" controller="accessPoint" class="ui right floated icon button" params="[id: orgInstance.id, accessMethod: accessPointType.value]">
            <i class="plus icon"></i>
        </g:link>
        <br>
        <br>

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
                <th class="four wide">${message(code: 'default.name.label')}</th>
                <g:if test="${accessPointType.value in ['ip', 'proxy']}">
                    <th class="four wide">IPv4</th>
                    <th class="four wide">IPv6</th>
                </g:if>
                <g:elseif test="${accessPointType.value == 'ezproxy'}">
                    <th class="two wide">IPv4</th>
                    <th class="three wide">IPv6</th>
                    <th class="three wide"><g:message code="accessPoint.url"/></th>
                </g:elseif>
                <g:else>
                    <th class="eight wide">${message(code: 'accessRule.plural')}</th>
                </g:else>
                <th class="two wide">${message(code: 'default.notes.label')}</th>
                <g:if test="${editable}">
                    <th class="la-action-info two wide">${message(code: 'default.actions.label')}</th>
                </g:if>
            </tr>
            </thead>
            <tbody>
            <g:each in="${orgAccessPointList[accessPointType.value]}" var="accessPointItem">
                <g:set var="accessPoint" value="${accessPointItem.oap}"/>
                <tr>
                    <td>
                        <g:if test="${editable}">
                            <g:link controller="accessPoint"
                                    action="edit_${accessPoint.accessMethod.value.toLowerCase()}"
                                    id="${accessPoint.id}">
                                ${accessPoint.name}
                            </g:link>
                        </g:if>
                        <g:else>
                            ${accessPoint.name}
                        </g:else>
                    </td>
                    <g:if test="${accessPointType.value in ['ip', 'proxy']}">
                        <td>
                            <g:each in="${accessPoint.getIpRangeStrings('ipv4', 'ranges')}" var="ipv4Range">
                                <div>${ipv4Range}</div>
                            </g:each>
                        </td>
                        <td>
                            <g:each in="${accessPoint.getIpRangeStrings('ipv6', 'ranges')}" var="ipv6Range">
                                <div>${ipv6Range}</div>
                            </g:each>
                        </td>
                    </g:if>
                    <g:elseif test="${accessPointType.value == 'ezproxy'}">
                        <td>
                            <g:each in="${accessPoint.getIpRangeStrings('ipv4', 'ranges')}" var="ipv4Range">
                                <div>${ipv4Range}</div>
                            </g:each>
                        </td>
                        <td>
                            <g:each in="${accessPoint.getIpRangeStrings('ipv6', 'ranges')}" var="ipv6Range">
                                <div>${ipv6Range}</div>
                            </g:each>
                        </td>
                        <td>
                            <g:if test="${accessPoint.hasProperty('url')}">
                                ${accessPoint.url}
                            </g:if>
                        </td>
                    </g:elseif>
                    <g:elseif test="${accessPointType.value == 'mailDomain'}">
                        <td>
                            <g:each in="${accessPoint.accessPointData}" var="apd">
                                <div>${apd.data}</div>
                            </g:each>
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
                    <td>
                        ${accessPoint.note}
                    </td>
                    <g:if test="${editable}">
                        <td class="center aligned">

                            <g:link controller="accessPoint"
                                    action="edit_${accessPoint.accessMethod.value.toLowerCase()}"
                                    id="${accessPoint.id}" class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>

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
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-content="${message(code: 'accessPoint.list.deleteDisabledInfo', args: [accessPointItem['platformLinkCount'], accessPointItem['subscriptionLinkCount']])}">
                                    <div class="ui negative icon button la-modern-button disabled"><i class="trash alternate outline icon"></i>
                                    </div>
                                </span>
                            </g:else>

                        </td>
                    </g:if>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</g:each>


<laser:htmlEnd />