<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Links;com.k_int.kbplus.Subscription" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes" %>

<laser:serviceInjection />
<semui:actionsDropdown>
    <g:if test="${accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <g:if test="${actionName == 'edit_ip'}">
            <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> Zugangskonfiguration löschen</g:link>
            </g:if>
        </g:if>
    </g:if>
</semui:actionsDropdown>