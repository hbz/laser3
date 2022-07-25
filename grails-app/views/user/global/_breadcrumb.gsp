<ui:breadcrumbs>

    <g:if test="${controllerName == 'myInstitution'}">
        <ui:crumb controller="org" action="show" text="${orgInstance.name}" id="${orgInstance.id}" />
        <ui:crumb controller="myInstitution" action="users" message="menu.institutions.users"/>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">
        <ui:crumb controller="org" action="show" text="${orgInstance.sortname}" id="${orgInstance.id}" />
        <ui:crumb controller="organisation" action="users" message="menu.institutions.users" id="${orgInstance.id}"/>
    </g:elseif>
    <g:elseif test="${controllerName == 'user'}">
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb controller="user" action="list" message="user.show_all.label" />
    </g:elseif>

    <g:if test="${actionName in ['create', 'createUser']}">
        <ui:crumb class="active" message="user.create_new.label" />
    </g:if>
    <g:if test="${actionName in ['delete', 'deleteUser']}">
        <ui:crumb class="active" message="user.delete.label" />
    </g:if>
    <g:if test="${actionName in ['edit', 'editUser']}">
        <ui:crumb class="active" message="user.edit.label" />
    </g:if>
    <g:if test="${actionName == 'show'}">
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb controller="user" action="list" message="user.show_all.label" />
        <ui:crumb class="active" text="${user.displayName?:'No username'}" />
    </g:if>
    <g:if test="${actionName == 'list'}">
        ????
    </g:if>

</ui:breadcrumbs>
