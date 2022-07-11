<ui:breadcrumbs>
    <ui:crumb text="${accessPoint.org.getDesignation()}" controller="organisation" action="show" id="${accessPoint.org.id}"/>
    <ui:crumb message="accessPoint.plural" controller="organisation" action="accessPoints" id="${accessPoint.org.id}"/>
    <g:if test="${actionName == 'edit_ip' || actionName == 'edit_shibboleth' || actionName == 'edit_proxy' || actionName == 'edit_vpn' || actionName == 'edit_ezproxy'}">
        <ui:crumb class="active" message="accessPoint.edit.label" />
    </g:if>
</ui:breadcrumbs>