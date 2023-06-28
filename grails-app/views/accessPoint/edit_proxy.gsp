<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
<laser:htmlStart text="${message(code: "default.edit.label", args: [entityName])}" serviceInjection="true"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

<laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

<g:set var="is_INST_EDITOR_with_PERMS_BASIC" value="${contextService.is_INST_EDITOR_with_PERMS_BASIC(inContextOrg)}"/>

<g:if test="${is_INST_EDITOR_with_PERMS_BASIC}">
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="edit_proxy"
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
        <a class="active item" data-tab="IPv4">IPv4 <ui:totalNumber
                total="${accessPointDataList.ipv4Ranges.size()}"/></a>
        <a class="item" data-tab="IPv6">IPv6 <ui:totalNumber total="${accessPointDataList.ipv6Ranges.size()}"/></a>
    </div>


    <div class="ui bottom attached active tab segment" data-tab="IPv4">

        <table class="ui celled la-js-responsive-table la-table table very compact">
            <thead>
            <tr>
                <th>${message(code: 'accessPoint.ip.format.input')}</th>
                <th>${message(code: 'accessPoint.ip.format.range')}</th>
                <th>${message(code: 'accessPoint.ip.format.cidr')}</th>
                <th>${message(code: 'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${accessPointDataList.ipv4Ranges}" var="accessPointData">
                <tr>
                    <td>${accessPointData.ipInput}</td>
                    <td>${accessPointData.ipRange}</td>
                    <td>${accessPointData.ipCidr}</td>
                    <td class="center aligned">
                        <g:if test="${is_INST_EDITOR_with_PERMS_BASIC}">
                            <g:link action="deleteAccessPointData" controller="accessPoint" id="${accessPointData.id}"
                                    class="ui negative icon button"
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

        <g:if test="${!accessPoint.hasProperty('entityId') && is_INST_EDITOR_with_PERMS_BASIC}">
            <div class="ui divider"></div>

            <div class="content">
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange']" method="POST">
                    <g:hiddenField name="id" id="ipv4_id" value="${accessPoint.id}"/>
                    <g:hiddenField name="accessMethod" id="ipv4_accessMethod" value="${accessPoint.accessMethod}"/>

                    <div class="ui form">
                        <div class="field">
                            <label for="ipv4_ip">${message(code: 'accessPoint.ip.data')}
                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                      data-content="${message(code: "accessPoint.ip.input")}">
                                    <i class="question circle icon"></i></span>
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

<div class="ui bottom attached tab segment" data-tab="IPv6">

    <table class="ui celled la-js-responsive-table la-table table very compact">
        <thead>
        <tr>
            <th>${message(code: 'accessPoint.ip.format.input')}</th>
            <th>${message(code: 'accessPoint.ip.format.range')}</th>
            <th>${message(code: 'accessPoint.ip.format.cidr')}</th>
            <th>${message(code: 'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${accessPointDataList.ipv6Ranges}" var="accessPointData">
            <tr>
                <td>${accessPointData.ipInput}</td>
                <td>${accessPointData.ipRange}</td>
                <td>${accessPointData.ipCidr}</td>
                <td class="center aligned">
                    <g:if test="${is_INST_EDITOR_with_PERMS_BASIC}">
                        <g:link action="deleteAccessPointData" controller="accessPoint" id="${accessPointData.id}"
                                class="ui negative icon button"
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

    <g:if test="${!accessPoint.hasProperty('entityId') && is_INST_EDITOR_with_PERMS_BASIC}">
        <div class="ui divider"></div>

        <div class="content">
            <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIpRange']" method="POST">
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
<laser:htmlEnd/>
