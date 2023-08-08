<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
<laser:htmlStart text="${message(code: "default.edit.label", args: [entityName])}" serviceInjection="true"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

<laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

<g:set var="isInstEditorOrRoleAdmin" value="${contextService.is_INST_EDITOR_with_PERMS_BASIC_or_ROLEADMIN(inContextOrg)}"/>

<g:if test="${isInstEditorOrRoleAdmin}">
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="edit_ip"
                        params="[id: accessPoint.id, exportXLSX: true]">${message(code: 'accessPoint.exportAccessPoint')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </ui:controlButtons>
</g:if>

<ui:h1HeaderWithIcon text="${orgInstance.name}"/>

<laser:render template="/organisation/nav"
              model="${[orgInstance: accessPoint.org, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>


<ui:messages data="${flash}"/>


<ui:tabs>
    <g:each in="${de.laser.RefdataCategory.getAllRefdataValues(RDConstants.ACCESS_POINT_TYPE)}"
            var="accessPointType">
        <ui:tabsItem controller="organisation" action="accessPoints"
                     params="${[id: orgInstance.id, activeTab: accessPointType.value]}"
                     text="${accessPointType.getI10n('value')}"
                     class="${accessPointType.value == accessPoint.accessMethod.value ? 'active' : ''}"
                     counts="${OrgAccessPoint.countByAccessMethodAndOrg(accessPointType, orgInstance)}"/>

    </g:each>
</ui:tabs>

<div class="ui bottom attached active tab segment">



    <div class="la-inline-lists">
        <div class="ui card">
            <div class="content">
                <dl>
                    <dt><g:message code="default.name.label"/></dt>
                    <dd><ui:xEditable owner="${accessPoint}" field="name"/></dd>
                </dl>
                <dl>
                    <dt><g:message code="default.note.label"/></dt>
                    <dd><ui:xEditable owner="${accessPoint}" field="note"/></dd>
                </dl>
            </div>
        </div>
    </div>

    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a class="${tab == 'IPv4' ? 'active' : ''} item" data-tab="IPv4">IPv4 <ui:totalNumber
                total="${accessPointDataList.ipv4Ranges.size()}"/></a>
        <a class="${tab == 'IPv6' ? 'active' : ''} item" data-tab="IPv6">IPv6 <ui:totalNumber
                total="${accessPointDataList.ipv6Ranges.size()}"/></a>
    </div>


    <div class="ui bottom attached ${tab == 'IPv4' ? 'active' : ''} tab segment" data-tab="IPv4">

        <table class="ui celled la-js-responsive-table la-table table very compact">
            <thead>
            <tr>
                <th class="five wide">${message(code: 'accessPoint.ip.format.input')}
                    &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv4Inputs()">
                    <i class="copy outline icon"></i>
                    </button>
                </th>
                <th class="five wide">${message(code: 'accessPoint.ip.format.range')}
                    &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv4Ranges()">
                        <i class="copy outline icon"></i>
                    </button>
                </th>
                <th class="five wide">${message(code: 'accessPoint.ip.format.cidr')}
                    &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv4Cidrs()">
                        <i class="copy outline icon"></i>
                    </button>
                </th>
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
                        <g:if test="${isInstEditorOrRoleAdmin}">
                            <g:link action="deleteAccessPointData" controller="accessPoint" id="${accessPointData.id}"
                                    params="[tab: 'IPv4', orgInstance: orgInstance.id]"
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

        <g:if test="${!accessPoint.hasProperty('entityId') && isInstEditorOrRoleAdmin}">
            <div class="ui divider"></div>

            <div class="content">
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange', params: [tab: 'IPv4']]"
                        method="POST">
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
                        <input type="submit" class="ui button"
                               value="${message(code: 'accessPoint.button.add')}"/>
                    </div>
                </g:form>
            </div>
        </g:if>

    </div>

<div class="ui bottom attached tab ${tab == 'IPv6' ? 'active' : ''} segment" data-tab="IPv6">

    <table class="ui celled la-js-responsive-table la-table table very compact">
        <thead>
        <tr>
            <th class="five wide">${message(code: 'accessPoint.ip.format.input')}
                &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv6Inputs()">
                    <i class="copy outline icon"></i>
                </button>
            </th>
            <th class="five wide">${message(code: 'accessPoint.ip.format.range')}
                &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv6Ranges()">
                    <i class="copy outline icon"></i>
                </button>
            </th>
            <th class="five wide">${message(code: 'accessPoint.ip.format.cidr')}
                &nbsp; <button class="ui small icon button" onclick="JSPC.app.copyToClipboardIpv6Cidrs()">
                    <i class="copy outline icon"></i>
                </button>
            </th>
            <th class="one wide">${message(code: 'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${accessPointDataList.ipv6Ranges}" var="accessPointData">
            <tr>
                <td>${accessPointData.ipInput}</td>
                <td>
                    <g:set var="ipv6AdressRange" value="${accessPointData.ipRange.split('-')}"/>
                    ${ipv6AdressRange[0]} - <br/>
                    ${ipv6AdressRange.size() > 1 ? ipv6AdressRange[1] : ""}
                </td>
                <td>${accessPointData.ipCidr}</td>
                <td class="center aligned">
                    <g:if test="${isInstEditorOrRoleAdmin}">
                        <g:link action="deleteAccessPointData" controller="accessPoint" id="${accessPointData.id}"
                                params="[orgInstance: orgInstance.id, tab: 'IPv6']"
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

    <g:if test="${!accessPoint.hasProperty('entityId') && isInstEditorOrRoleAdmin}">
        <div class="ui divider"></div>

        <div class="content">
            <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange', params: [tab: 'IPv6']]"
                    method="POST">
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
                    <input type="submit" class="ui button"
                           value="${message(code: 'accessPoint.button.add')}"/>
                </div>
            </g:form>
        </div>
        </div>
    </g:if>

    <br/>

    <div class="la-inline-lists">
        <laser:render template="link"
                      model="${[accessPoint: accessPoint, params: params, linkedPlatforms: linkedPlatforms, linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages]}"/>
    </div>

</div>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.jsonIpv4Inputs = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv4Ranges?.collect { it.ipInput })%>;
    JSPC.app.jsonIpv4Ranges = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv4Ranges?.collect { it.ipRange })%>;
    JSPC.app.jsonIpv4Cidrs = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv4Ranges?.collect { it.ipCidr })%>;

    JSPC.app.jsonIpv6Inputs = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv6Ranges?.collect { it.ipInput })%>;
    JSPC.app.jsonIpv6Ranges = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv6Ranges?.collect { it.ipRange })%>;
    JSPC.app.jsonIpv6Cidrs = <%=groovy.json.JsonOutput.toJson((Set) accessPointDataList?.ipv6Ranges?.collect { it.ipCidr })%>;

    JSPC.app.copyToClipboardIpv4Inputs = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv4Inputs);
        }

    JSPC.app.copyToClipboardIpv4Ranges = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv4Ranges);
        }

    JSPC.app.copyToClipboardIpv4Cidrs = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv4Cidrs);
        }

    JSPC.app.copyToClipboardIpv6Inputs = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv6Inputs);
        }

    JSPC.app.copyToClipboardIpv6Ranges = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv6Ranges);
        }

    JSPC.app.copyToClipboardIpv6Cidrs = function () {
            navigator.clipboard.writeText(JSPC.app.jsonIpv6Cidrs);
        }

</laser:script>

<laser:htmlEnd/>
