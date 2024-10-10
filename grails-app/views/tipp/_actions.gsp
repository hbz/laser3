<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.reportTitleToProvider"/>
    </ui:actionsDropdown>

%{--    modal @ /templates/tasks/_reportTitleToProvider.gsp --}%
%{--    <laser:render template="/templates/tasks/modal_create" model="${[ownobj: tipp, owntp: 'tipp']}"/>--}%
</g:if>

