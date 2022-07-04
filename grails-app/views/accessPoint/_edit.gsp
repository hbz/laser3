<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>
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

    <laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

    <g:if test="${(accessService.checkPermAffiliation('ORG_BASIC_MEMBER','INST_EDITOR') && inContextOrg)
            || (accessService.checkPermAffiliation('ORG_CONSORTIUM','INST_EDITOR'))}">
        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="edit_${accessPoint.accessMethod}"
                            params="[id: accessPoint.id, exportXLSX: true]">${message(code: 'accessPoint.exportAccessPoint')}</g:link>
                </semui:exportDropdownItem>
            </semui:exportDropdown>
        </semui:controlButtons>
    </g:if>

    <semui:headerWithIcon text="${orgInstance.name}" />

    <laser:render template="/organisation/nav" model="${[orgInstance: accessPoint.org, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

    <h2 class="ui header la-noMargin-top"><g:message code="default.edit.label" args="[entityName]"/></h2>
    <semui:messages data="${flash}"/>

    <g:form class="ui form" url="[controller: 'accessPoint', action: 'edit_' + accessPoint.accessMethod]" id="${accessPoint.id}" method="GET">
        <g:hiddenField id="accessPoint_id_${accessPoint.id}" name="id" value="${accessPoint.id}"/>
        <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt><g:message code="default.name.label" /></dt>
        <dd><semui:xEditable owner="${accessPoint}" field="name"/></dd>
        </dl>
        <dl>
            <dt><g:message code="accessMethod.label" /></dt>
            <dd>
                ${accessPoint.accessMethod.getI10n('value')}
                <g:hiddenField id="accessMethod_id_${accessPoint.accessMethod.id}" name="accessMethod" value="${accessPoint.accessMethod.id}"/>
            </dd>
        </dl>
        <g:if test="${accessPoint.hasProperty('url')}">
            <dl>
                <dt><g:message code="accessPoint.url" /></dt>
                <dd><semui:xEditable owner="${accessPoint}" field="url"/></dd>
            </dl>
        </g:if>

        <g:if test="${accessPoint.hasProperty('entityId')}">
            <dl>
                <dt><g:message code="accessPoint.entitiyId.label" /></dt>
                <dd><semui:xEditable owner="${accessPoint}" field="entityId"/></dd>
            </dl>
        </g:if>

        </div>

    </g:form>


    <laser:render template="link"
              model="${[accessPoint: accessPoint, params: params, linkedPlatforms: linkedPlatforms, linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages]}"/>

</body>
</html>