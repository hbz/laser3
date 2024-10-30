<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>
<laser:serviceInjection/>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
<laser:htmlStart text="${message(code: "default.edit.label", args: [entityName])}" serviceInjection="true"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

<laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

<g:if test="${contextService.is_INST_EDITOR_with_PERMS_BASIC(inContextOrg)}">
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="edit_${accessPoint.accessMethod.value.toLowerCase()}"
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
                <g:if test="${accessPoint.hasProperty('url')}">
                    <dl>
                        <dt><g:message code="accessPoint.url"/></dt>
                        <dd><ui:xEditable owner="${accessPoint}" field="url"/></dd>
                    </dl>
                </g:if>

                <g:if test="${accessPoint.hasProperty('entityId')}">
                    <dl>
                        <dt><g:message code="accessPoint.entitiyId.label"/></dt>
                        <dd><ui:xEditable owner="${accessPoint}" field="entityId"/></dd>
                    </dl>
                </g:if>

            </div>
        </div>
    </div>

    <br/>

    <div class="la-inline-lists">
        <laser:render template="link"
                      model="${[accessPoint: accessPoint, params: params, linkedPlatforms: linkedPlatforms, linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages]}"/>
    </div>

</div>
<laser:htmlEnd/>