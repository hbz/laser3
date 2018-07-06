<semui:breadcrumbs>
        <semui:crumb message="platform.show.all" controller="platform" action="index"/>
        <semui:crumb text="${accessMethod.platf.name}" controller="platform" action="show" id="${accessMethod.platf.id}"/>
        <semui:crumb message="accessMethod.plural" controller="platform" action="accessMethods" id="${accessMethod.platf.id}" class="active"/>
</semui:breadcrumbs>