<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.new"/>
    </ui:actionsDropdown>

    <laser:render template="/templates/tasks/modal_create" model="${[ownobj: tipp, owntp: 'tipp']}"/>
</g:if>

