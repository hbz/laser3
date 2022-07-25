<ui:breadcrumbs>
        <ui:crumb message="platform.show.all" controller="platform" action="index"/>
        <ui:crumb text="${accessMethod.platf.name}" controller="platform" action="show" id="${accessMethod.platf.id}"/>
        <ui:crumb message="accessMethod.plural" controller="platform" action="accessMethods" id="${accessMethod.platf.id}" class="active"/>
</ui:breadcrumbs>