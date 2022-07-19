<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.helper.RDConstants" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
    <title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

    <g:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

    <g:if test="${(accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg)
            || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="edit_ip"
                            params="[id: accessPoint.id, exportXLSX: true]">${message(code: 'accessPoint.exportAccessPoint')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
        </semui:controlButtons>
    </g:if>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

    <g:render template="/organisation/nav" model="${[orgInstance: accessPoint.org, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

    <h2 class="ui header la-noMargin-top"><g:message code="default.edit.label" args="[entityName]"/></h2>
    <semui:messages data="${flash}"/>


    <div class="la-inline-lists">
        <div class="ui card">
            <div class="content">
                <dl>
                    <dt><g:message code="default.name.label" default="Name"/></dt>
                    <dd><semui:xEditable owner="${accessPoint}" field="name"/></dd>
                </dl>
                <dl>
                    <dt><g:message code="accessMethod.label" default="Access Method"/></dt>
                    <dd>
                        ${accessPoint.accessMethod.getI10n('value')}
                        <g:hiddenField id="accessMethod_id_${accessPoint.accessMethod.id}" name="accessMethod" value="${accessPoint.accessMethod.id}"/>
                    </dd>
                </dl>
            </div>
        </div>
    </div>

    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a class="${tab == 'IPv4' ? 'active': ''} item" data-tab="IPv4">IPv4 <semui:totalNumber total="${accessPointDataList.ipv4Ranges.size()}"/></a>
        <a class="${tab == 'IPv6' ? 'active': ''} item" data-tab="IPv6">IPv6 <semui:totalNumber total="${accessPointDataList.ipv6Ranges.size()}"/></a>
    </div>


    <div class="ui bottom attached ${tab == 'IPv4' ? 'active': ''} tab segment" data-tab="IPv4">

        <h3 class="ui header">${message(code: 'accessPoint.ip.configuration')}
        %{--<span class="la-long-tooltip la-popup-tooltip la-delay" data-html='${message(code:'accessPoint.permittedIpRanges')}'>
            <i class="question circle icon la-popup"></i>
        </span>--}%
        </h3>

        <table class="ui celled la-js-responsive-table la-table table very compact">
            <thead>
            <tr>
                <th class="five wide">${message(code: 'accessPoint.ip.format.input')}</th>
                <th class="five wide">${message(code: 'accessPoint.ip.format.range')}</th>
                <th class="five wide">${message(code: 'accessPoint.ip.format.cidr')}</th>
                <th class="one wide">${message(code: 'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${accessPointDataList.ipv4Ranges}" var="accessPointData">
                <tr>
                    <td>${accessPointData.ipInput}</td>
                    <td>${accessPointData.ipRange}</td>
                    <td>${accessPointData.ipCidr}</td>
                    <td class="center aligned">
                        <g:if test="${(accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
                            <g:link action="deleteIpRange" controller="accessPoint" id="${accessPointData.id}" params="[tab: 'IPv4']"
                                    class="ui negative icon button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.accessPoint.ip', args: [accessPointData.ipInput])}"
                                    data-confirm-term-how="delete"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash very alternate icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <g:if test="${!accessPoint.hasProperty('entityId') && (accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
            <div class="ui divider"></div>

            <div class="content">
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange', params: [tab: 'IPv4']]" method="POST">
                    <g:hiddenField name="id" id="ipv4_id" value="${accessPoint.id}"/>
                    <g:hiddenField name="accessMethod" id="ipv4_accessMethod" value="${accessPoint.accessMethod}"/>

                    <div class="ui form">
                        <div class="field">
                            <label for="ipv4_ip">${message(code: 'accessPoint.ip.data')}
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-content="${message(code: "accessPoint.ip.input")}">
                                <i class="question circle icon la-popup"></i></span>
                            </label>

                            <g:if test="${autofocus == true}">
                                <g:field type="text" name="ip" id="ipv4_ip" value="${ip}" autofocus=""/>
                            </g:if>
                            <g:else>
                                <g:field type="text" name="ip" id="ipv4_ip" value="${ip}"/>
                            </g:else>
                        </div>
                        <input type="Submit" class="ui button"
                               value="${message(code: 'accessPoint.button.add')}" />
                    </div>
                </g:form>
            </div>
        </g:if>

    </div>

<div class="ui bottom attached tab ${tab == 'IPv6' ? 'active': ''} segment" data-tab="IPv6">
    <h3 class="ui header">${message(code: 'accessPoint.ip.configuration')}
    %{--<span class="la-long-tooltip la-popup-tooltip la-delay" data-html='${message(code:'accessPoint.permittedIpRanges')}'>
        <i class="question circle icon la-popup"></i>
    </span>--}%
    </h3>

    <table class="ui celled la-js-responsive-table la-table table very compact">
        <thead>
        <tr>
            <th class="five wide">${message(code: 'accessPoint.ip.format.input')}</th>
            <th class="five wide">${message(code: 'accessPoint.ip.format.range')}</th>
            <th class="five wide">${message(code: 'accessPoint.ip.format.cidr')}</th>
            <th class="one wide">${message(code: 'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${accessPointDataList.ipv6Ranges}" var="accessPointData">
            <tr>
                <td>${accessPointData.ipInput}</td>
                <td>
                    <g:set var="ipv6AdressRange" value="${accessPointData.ipRange.split('-')}"/>
                    ${ipv6AdressRange[0]} -<br>
                    ${ipv6AdressRange.size() > 1 ? ipv6AdressRange[1] : ""}
                </td>
                <td>${accessPointData.ipCidr}</td>
                <td class="center aligned">
                    <g:if test="${(accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
                        <g:link action="deleteIpRange" controller="accessPoint" id="${accessPointData.id}" params="[tab: 'IPv6']"
                                class="ui negative icon button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.accessPoint.ip', args: [accessPointData.ipInput])}"
                                data-confirm-term-how="delete"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash very alternate icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <g:if test="${!accessPoint.hasProperty('entityId') && (accessService.checkPermAffiliation('ORG_BASIC_MEMBER', 'INST_EDITOR') && inContextOrg) || (accessService.checkPermAffiliation('ORG_CONSORTIUM', 'INST_EDITOR'))}">
        <div class="ui divider"></div>

        <div class="content">
            <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange', params: [tab: 'IPv6']]" method="POST">
                <g:hiddenField name="id" id="ipv6_id" value="${accessPoint.id}"/>
                <g:hiddenField name="accessMethod" id="ipv6_accessMethod" value="${accessPoint.accessMethod}"/>

                <div class="ui form">
                    <div class="field">
                        <label for="ipv6_ip">${message(code: 'accessPoint.ip.data')}
                            <span class="la-long-tooltip la-popup-tooltip la-delay"
                                  data-content="${message(code: "accessPoint.ip.input")}">
                                <i class="question circle icon la-popup"></i></span>
                        </label>

                        <g:if test="${autofocus == true}">
                            <g:field type="text" name="ip" id="ipv6_ip" value="${ip}" autofocus=""/>
                        </g:if>
                        <g:else>
                            <g:field type="text" name="ip" id="ipv6_ip" value="${ip}"/>
                        </g:else>
                    </div>
                    <input type="Submit" class="ui button"
                           value="${message(code: 'accessPoint.button.add')}" />
                </div>
            </g:form>
        </div>
        </div>
    </g:if>

<br />

<div class="la-inline-lists">
    <g:render template="link"
              model="${[accessPoint: accessPoint, params: params, linkedPlatforms: linkedPlatforms, linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages]}"/>
</div>

</body>
</html>
