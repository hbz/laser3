<semui:breadcrumbs>

    <g:if test="${actionName == 'create'}">
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" message="user.create_new.label" />
    </g:if>
    <g:if test="${actionName in ['edit','userEdit']}">
        <g:if test="${controllerName == 'myInstitution'}">
            <semui:crumb controller="myInstitution" action="userList" message="menu.institutions.users"/>
        </g:if>
        <g:elseif test="${controllerName == 'organisation'}">
            <semui:crumb controller="organisation" action="users" message="menu.institutions.users"/>
        </g:elseif>
        <g:elseif test="${controllerName == 'user'}">
            <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
            <semui:crumb controller="user" action="list" message="user.show_all.label" />
        </g:elseif>
        <semui:crumb class="active" message="user.edit.label" />
    </g:if>
    <g:if test="${actionName == 'show'}">
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" text="${user.displayName?:'No username'}" />
    </g:if>
    <g:if test="${actionName == 'list'}">
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb class="active" message="user.show_all.label" />
    </g:if>

</semui:breadcrumbs>
