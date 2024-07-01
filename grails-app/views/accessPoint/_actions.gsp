<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.Links;de.laser.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; org.grails.web.util.GrailsApplicationAttributes" %>

<laser:serviceInjection />
<ui:actionsDropdown>
    <g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
        <g:if test="${actionName == 'edit_ip'}">
            <g:if test="${contextService.isInstEditor_or_ROLEADMIN()}">
                <g:link class="item" action="delete" id="${params.id}"><i class="${Icons.CMD_DELETE}"></i> Zugangskonfiguration löschen</g:link>
            </g:if>
        </g:if>
    </g:if>
</ui:actionsDropdown>