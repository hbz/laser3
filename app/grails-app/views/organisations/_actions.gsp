<g:if test="${actionName == 'list'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="organisations" action="create" message="org.create_new.label"/>
    </semui:actionsDropdown>
</g:if>
<g:if test="${actionName == 'listProvider'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="organisations" action="findProviderMatches" message="org.create_new_Provider.label"/>
    </semui:actionsDropdown>
</g:if>
<g:if test="${actionName == 'show'}">
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen anzeigen" />
        </semui:actionsDropdown>
    </g:if>
</g:if>

