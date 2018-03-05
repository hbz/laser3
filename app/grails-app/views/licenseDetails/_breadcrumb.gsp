<semui:breadcrumbs>
    <g:if test="${license?.licensee}">
        <semui:crumb controller="myInstitution" action="dashboard" text="${license.licensee?.getDesignation()}" />
        <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
    </g:if>

    <semui:crumb text="${license.reference}" class="active" />

    <g:if test="${actionName == 'index'}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" controller="licenseDetails" action="show" id="${license.id}" params="${params + [format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" controller="licenseDetails" action="show" id="${license.id}" params="${params + [format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" controller="licenseDetails" action="show" id="${license.id}" params="${params + [format:'csv']}">CSV</g:link>
            </semui:exportDropdownItem>
            <g:each in="${transforms}" var="transkey,transval">
                <semui:exportDropdownItem>
                    <g:link class="item" action="show" id="${params.id}" params="${params + [format:'xml', transformId:transkey]}">${transval.name}</g:link>
                </semui:exportDropdownItem>
            </g:each>
        </semui:exportDropdown>
    </g:if>
    <g:annotatedLabel owner="${license}" property="detailsPageInfo"></g:annotatedLabel>
</semui:breadcrumbs>

