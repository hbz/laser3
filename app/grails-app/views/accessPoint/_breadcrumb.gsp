<semui:breadcrumbs>
        %{--<semui:crumb message="menu.public.all_orgs" controller="organisation" action="index"/>--}%
        <semui:crumb text="${accessPoint.org.getDesignation()}" controller="organisation" action="show" id="${accessPoint.org.id}"/>
        <semui:crumb message="accessPoint.plural" controller="organisation" action="accessPoints" id="${accessPoint.org.id}"/>
        <g:if test="${actionName == 'edit_ip'}">
                <semui:crumb class="active" message="accessPoint.edit.label" />
        </g:if>
</semui:breadcrumbs>