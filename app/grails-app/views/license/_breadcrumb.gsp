<laser:serviceInjection />

<semui:breadcrumbs>
    <g:if test="${license?.getLicensee() && license?.getLicensee()?.id == contextService.getOrg()?.id}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${license.getLicensee()?.getDesignation()}" />
        <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
    </g:if>
    <g:elseif test="${license?.getLicensingConsortium()?.id == contextService.getOrg()?.id}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${license.getLicensingConsortium()?.getDesignation()}" />
        <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
    </g:elseif>

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
            <g:each in="${transforms}" var="transkey,transval">
                <semui:exportDropdownItem>
                    <g:link class="item" action="show" id="${params.id}" params="${params + [format:'xml', transformId:transkey]}">${transval.name}</g:link>
                </semui:exportDropdownItem>
            </g:each>
        </semui:exportDropdown>
    </g:if>
</semui:breadcrumbs>

