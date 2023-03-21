<%@ page import="de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.Links;de.laser.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; org.grails.web.util.GrailsApplicationAttributes" %>

<laser:serviceInjection />
<ui:actionsDropdown>
    <g:if test="${accessService.checkPermAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC,'INST_EDITOR')}">
        <g:if test="${actionName == 'edit_ip'}">
            <g:if test="${contextService.getUser().is_ROLE_ADMIN_or_hasAffiliation("INST_EDITOR")}">
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> Zugangskonfiguration löschen</g:link>
            </g:if>
        </g:if>
    </g:if>
</ui:actionsDropdown>