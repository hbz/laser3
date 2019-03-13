<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>

    <g:if test="${actionName == 'create'}">
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" message="user.create_new.label" />
    </g:if>
    <g:if test="${actionName == 'edit'}">
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" message="user.edit.label" />
    </g:if>
    <g:if test="${actionName == 'show'}">
        <semui:crumb controller="user" action="list" message="user.show_all.label" />
        <semui:crumb class="active" text="${ui.displayName?:'No username'}" />
    </g:if>
    <g:if test="${actionName == 'list'}">
        <semui:crumb class="active" message="user.show_all.label" />
    </g:if>

</semui:breadcrumbs>
