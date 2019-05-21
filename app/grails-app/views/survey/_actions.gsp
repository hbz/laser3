<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <g:if test="${actionName == 'currentSurveysConsortia'}">
            <semui:actionsDropdownItem controller="survey" action="createSurvey"
                                       message="createSurvey.label"/>
        </g:if>
        <g:else>
            <g:if test="${true}">
                <semui:actionsDropdownItem controller="survey" action="allSubscriptions" params="[id: params.id]"
                                           message="survey.SurveySub.add.label"/>

                <semui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id, addSurveyConfigs: true]"
                                           message="survey.SurveyProp.add.label"/>

            </g:if>
            <div class="ui divider"></div>
            <g:if test="${surveyInfo && surveyInfo.checkOpenSurvey()}">
                <semui:actionsDropdownItem controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                           message="openSurvey.button"/>
            </g:if>
            <g:else>
                <semui:actionsDropdownItemDisabled controller="survey" action="processOpenSurvey"
                                                   params="[id: params.id]"
                                                   message="openSurvey.button"/>
            </g:else>

            <div class="ui divider"></div>
            <semui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                       message="survey.SurveyProp.all"/>

            <div class="ui divider"></div>
            <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="survey.copyEmailaddresses.participants"/>

            <g:render template="../templates/copyEmailaddresses" model="[orgList: surveyInfo?.surveyConfigs?.orgs.flatten() ? com.k_int.kbplus.Org.findAllByIdInList(surveyInfo?.surveyConfigs?.orgs.id.flatten()) : null]"/>

        </g:else>

    </g:if>
</semui:actionsDropdown>
