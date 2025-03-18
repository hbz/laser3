<laser:serviceInjection/>
<g:if test="${contextService.getOrg().id == surveyInfo.owner.id && participant}">
    <div class="ui error message">
        <div class="header">
            <g:message code="myinst.message.attention"/>
            <g:message code="survey.message.participantView"/>
            <span class="ui label"><g:link controller="organisation" action="show" id="${participant.id}">${participant.getDesignation()}</g:link></span>.
        </div>

        <p>

            <g:message code="myinst.subscriptionDetails.message.hereLink"/>
            <g:link controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]">
                <g:message code="survey.message.backToSurvey"/>
            </g:link>


            <g:message code="myinst.subscriptionDetails.message.and"/>

            <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]">
                <g:message code="survey.message.backToParticipants"/>
            </g:link>.
        </p>
    </div>
</g:if>