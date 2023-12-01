<laser:serviceInjection />

<ui:breadcrumbs>
    <ui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
    <ui:crumb text="${license?.reference}" class="active" />
</ui:breadcrumbs>

