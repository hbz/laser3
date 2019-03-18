

<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdownItem controller="survey" action="emptySurvey" message="createSurvey.label" />

    </g:if>
</semui:actionsDropdown>