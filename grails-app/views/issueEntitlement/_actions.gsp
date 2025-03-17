<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor(CustomerTypeService.PERMS_PRO)}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateRttpTask" message="task.create.reportTitleToProvider"/>
    </ui:actionsDropdown>

%{--    modal @ /templates/reportTitleToProvider/_flyoutAndTippTask.gsp --}%
</g:if>

