<laser:serviceInjection />

<semui:breadcrumbs>
    <%-- TODO: breadcrumb refactoring
    <g:if test="${license?.getLicensee() && license?.getLicensee()?.id == contextService.getOrg().id}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${license.getLicensee()?.getDesignation()}" />
    </g:if>
    <g:elseif test="${license?.getLicensingConsortium()?.id == contextService.getOrg().id}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${license.getLicensingConsortium()?.getDesignation()}" />
    </g:elseif>
    --%>

    <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />

    <semui:crumb text="${license?.reference}" class="active" />

    <g:if test="${actionName == 'index'}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'csv']}">CSV</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </g:if>
</semui:breadcrumbs>

