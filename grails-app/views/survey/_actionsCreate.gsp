<ui:actionsDropdownItem controller="survey" action="createGeneralSurvey"
                           message="createGeneralSurvey.label"/>

<ui:actionsDropdownItem controller="survey" action="createSubscriptionSurvey"
                           message="createSubscriptionSurvey.label"/>

<ui:actionsDropdownItem controller="survey" action="createIssueEntitlementsSurvey"
                           message="createIssueEntitlementsSurvey.label"/>

%{--<g:if test="${actionName == 'currentSurveysConsortia'}">
    <div class="ui divider"></div>

    <ui:actionsDropdownItem controller="survey" action="workflowsSurveysConsortia"
                               message="workflowsSurveysConsortia.label"/>
</g:if>--}%
