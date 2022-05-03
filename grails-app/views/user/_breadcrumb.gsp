<semui:breadcrumbs>
    <semui:crumb message="menu.admin" controller="admin" action="index"/>
    <semui:crumb controller="user" action="list" message="user.show_all.label" />
    <g:if test="${user}">
        <semui:crumb class="active" text="${user.displayName?:'No username'}" />
    </g:if>
</semui:breadcrumbs>
