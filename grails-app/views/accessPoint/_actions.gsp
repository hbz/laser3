<%@ page import="de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.Links;de.laser.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; org.grails.web.util.GrailsApplicationAttributes" %>

<laser:serviceInjection />
<ui:actionsDropdown>
    <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
        <g:if test="${actionName == 'edit_ip'}">
            <g:if test="${contextService.getUser().hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR')}">
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> Zugangskonfiguration löschen</g:link>
            </g:if>
        </g:if>
    </g:if>
</ui:actionsDropdown>