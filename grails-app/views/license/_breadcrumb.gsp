<laser:serviceInjection />

<semui:breadcrumbs>

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

