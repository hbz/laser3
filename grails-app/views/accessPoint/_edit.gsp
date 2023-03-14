<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>
<laser:serviceInjection/>

    <g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
<laser:htmlStart text="${message(code:"default.edit.label", args:[entityName])}" serviceInjection="true" />

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

    <laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

    <g:if test="${(accessService.checkPermAffiliation('ORG_MEMBER_BASIC','INST_EDITOR') && inContextOrg)
            || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR'))}">
        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:link class="item" action="edit_${accessPoint.accessMethod}"
                            params="[id: accessPoint.id, exportXLSX: true]">${message(code: 'accessPoint.exportAccessPoint')}</g:link>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
        </ui:controlButtons>
    </g:if>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="/organisation/nav" model="${[orgInstance: accessPoint.org, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

    <h2 class="ui header la-noMargin-top"><g:message code="default.edit.label" args="[entityName]"/></h2>

    <g:link class="ui right floated button" controller="organisation" action="accessPoints"
            id="${orgInstance.id}">
        ${message(code: 'default.button.back')}
    </g:link>
    <br>
    <br>

    <ui:messages data="${flash}"/>


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
            <dl>
                <dt><g:message code="accessMethod.label"/></dt>
                <dd>
                    ${accessPoint.accessMethod.getI10n('value')}
                </dd>
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

<laser:htmlEnd />