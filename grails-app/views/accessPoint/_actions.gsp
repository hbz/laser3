<%@ page import="de.laser.storage.RDStore; de.laser.RefdataValue;de.laser.Links;de.laser.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="org.grails.web.util.GrailsApplicationAttributes" %>

<laser:serviceInjection />
<semui:actionsDropdown>
    <g:if test="${accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <g:if test="${actionName == 'edit_ip'}">
            <g:if test="${contextService.getUser().hasAffiliation("INST_EDITOR")}">
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> Zugangskonfiguration löschen</g:link>
            </g:if>
        </g:if>
    </g:if>
</semui:actionsDropdown>