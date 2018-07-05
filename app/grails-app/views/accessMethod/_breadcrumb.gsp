<semui:breadcrumbs>
        <semui:crumb message="platform.show.all" controller="platform" action="index"/>
        <semui:crumb text="${accessMethod.platform.name}" controller="platform" action="show" id="${accessMethod.platform.id}"/>
        <semui:crumb message="accessMethod.plural" controller="platform" action="accessMethods" id="${accessMethod.platform.id}" class="active"/>
</semui:breadcrumbs>