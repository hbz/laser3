<semui:breadcrumbs>
    <g:if test="${license?.licensee}">
        <semui:crumb text="${license.licensee.name} - ${message(code:'license.current')}" controller="myInstitutions" action="currentLicenses" params="${[shortcode:license.licensee.shortcode]}" />
    </g:if>
    <semui:crumb message="license.details" id="${params.id}" class="active"/>

    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link controller="licenseDetails" action="index" id="${license.id}" params="${params + [format:'json']}">JSON</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link controller="licenseDetails" action="index" id="${license.id}" params="${params + [format:'xml']}">XML</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link controller="licenseDetails" action="index" id="${license.id}" params="${params + [format:'csv']}">CSV</g:link>
        </semui:exportDropdownItem>
        <g:each in="${transforms}" var="transkey,transval">
            <semui:exportDropdownItem>
                <g:link action="index" id="${params.id}" params="${params + [format:'xml', transformId:transkey]}">${transval.name}</g:link>
            </semui:exportDropdownItem>
        </g:each>
    </semui:exportDropdown>

    <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
    </g:if>

        <li class="pull-right"><g:annotatedLabel owner="${license}" property="detailsPageInfo"></g:annotatedLabel>&nbsp;</li>
</semui:breadcrumbs>
