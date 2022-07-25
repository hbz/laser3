<laser:serviceInjection />

<ui:breadcrumbs>

    <ui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />

    <ui:crumb text="${license?.reference}" class="active" />

    <g:if test="${actionName == 'index'}">
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'json']}">JSON</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'xml']}">XML</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" controller="license" action="show" id="${license.id}" params="${params + [format:'csv']}">CSV</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </g:if>
</ui:breadcrumbs>

