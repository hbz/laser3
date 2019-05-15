<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <g:if test="${actionName == 'currentSurveysConsortia'}">
            <semui:actionsDropdownItem controller="survey" action="createSurvey"
                                       message="createSurvey.label"/>
        </g:if>

        <g:if test="${true}">
            <semui:actionsDropdownItem controller="survey" action="allSubscriptions" params="[id: params.id]"
                                       message="survey.SurveySub.add.label"/>

            <semui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                       message="survey.SurveyProp.add.label"/>

        </g:if>

        <g:if test="${surveyInfo && surveyInfo.checkOpenSurvey()}">
        <semui:actionsDropdownItem controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                   message="openSurvey.button"/>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                       message="openSurvey.button"/>
        </g:else>
     
    </g:if>
</semui:actionsDropdown>
