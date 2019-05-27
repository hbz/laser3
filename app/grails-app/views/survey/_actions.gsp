<semui:actionsDropdown>
    <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <g:if test="${actionName == 'currentSurveysConsortia'}">
            <semui:actionsDropdownItem controller="survey" action="createSurvey"
                                       message="createSurvey.label"/>
        </g:if>
        <g:else>

            <g:if test="${surveyInfo && surveyInfo.status?.id == com.k_int.kbplus.RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])?.id}">

                <semui:actionsDropdownItem controller="survey" action="allSubscriptions" params="[id: params.id]"
                                           message="survey.SurveySub.add.label"/>

                <semui:actionsDropdownItem controller="survey" action="allSurveyProperties"
                                           params="[id: params.id, addSurveyConfigs: true]"
                                           message="survey.SurveyProp.add.label"/>

                <div class="ui divider"></div>
                <g:if test="${surveyInfo && surveyInfo.checkOpenSurvey() && surveyInfo.status?.id == com.k_int.kbplus.RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])?.id}">
                    <semui:actionsDropdownItem controller="survey" action="processOpenSurvey" params="[id: params.id]"
                                               message="openSurvey.button"/>
                </g:if>
                <g:else>
                    <semui:actionsDropdownItemDisabled controller="survey" action="processOpenSurvey"
                                                       params="[id: params.id]"
                                                       message="openSurvey.button"/>
                </g:else>
                <div class="ui divider"></div>
            </g:if>


            <semui:actionsDropdownItem controller="survey" action="allSurveyProperties" params="[id: params.id]"
                                       message="survey.SurveyProp.all"/>

            <div class="ui divider"></div>
            <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal"
                                       message="survey.copyEmailaddresses.participants"/>


            <g:set var="orgs" value="${com.k_int.kbplus.Org.findAllByIdInList(surveyInfo?.surveyConfigs?.orgs?.org?.flatten().unique { a, b -> a?.id <=> b?.id }.id)?.sort {it.sortname}}"/>

            <g:render template="../templates/copyEmailaddresses"
                      model="[orgList: orgs ?: null]"/>

        </g:else>

    </g:if>
</semui:actionsDropdown>
