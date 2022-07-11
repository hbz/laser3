<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb controller="user" action="list" message="user.show_all.label" />
    <g:if test="${user}">
        <ui:crumb class="active" text="${user.displayName?:'No username'}" />
    </g:if>
</ui:breadcrumbs>
