<semui:breadcrumbs>

    <g:if test="${controllerName == 'myInstitution'}">
        <semui:crumb controller="org" action="show" text="${orgInstance.name}" id="${orgInstance.id}" />
        <semui:crumb controller="myInstitution" action="users" message="menu.institutions.users"/>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">
        <semui:crumb controller="org" action="show" text="${orgInstance.sortname}" id="${orgInstance.id}" />
        <semui:crumb controller="organisation" action="users" message="menu.institutions.users" id="${orgInstance.id}"/>
    </g:elseif>
    <g:elseif test="${controllerName == 'user'}">
        <semui:crumb message="menu.admin" controller="admin" action="index"/>
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
    </g:elseif>

    <g:if test="${actionName in ['create', 'createUser']}">
        <semui:crumb class="active" message="user.create_new.label" />
    </g:if>
    <g:if test="${actionName in ['delete', 'deleteUser']}">
        <semui:crumb class="active" message="user.delete.label" />
    </g:if>
    <g:if test="${actionName in ['edit', 'editUser']}">
        <semui:crumb class="active" message="user.edit.label" />
    </g:if>
    <g:if test="${actionName == 'show'}">
        <semui:crumb message="menu.admin" controller="admin" action="index"/>
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" text="${user.displayName?:'No username'}" />
    </g:if>
    <g:if test="${actionName == 'list'}">
        ????
    </g:if>

</semui:breadcrumbs>
