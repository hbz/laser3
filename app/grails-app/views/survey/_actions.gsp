

<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdownItem controller="survey" action="showSurveyInfo" message="createSurvey.label" />

    </g:if>
</semui:actionsDropdown>