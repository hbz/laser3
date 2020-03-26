<%@ page import="com.k_int.kbplus.OrgRole" %>

<h3><g:message code="surveys.active"/></h3>


<div class="ui divided items">
    <g:each in="${surveys}" var="survey" status="i">

        <g:set var="surveyConfig"
               value="${com.k_int.kbplus.SurveyConfig.get(survey.key)}"/>

        <g:set var="surveyInfo"
               value="${surveyConfig.surveyInfo}"/>

        <div class="item">

            <div class="content">
                <div class="header">
                    <i class="icon chart pie la-list-icon"></i>
                    <g:if test="${!surveyConfig?.pickAndChoose}">
                    <g:link controller="myInstitution" action="surveyInfos" params="[surveyConfigID: surveyConfig.id]"
                                          id="${surveyInfo.id}">${surveyConfig?.getSurveyName()}</g:link>
                    </g:if>
                    <g:if test="${surveyConfig?.pickAndChoose}">
                        <g:link controller="myInstitution" action="surveyInfosIssueEntitlements" id="${surveyConfig?.id}"
                                params="${[targetSubscriptionId: surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(institution)?.id]}">
                            ${surveyConfig?.getSurveyName()}
                        </g:link>
                    </g:if>
                </div>

                <div class="meta">
                    <span><g:message code="surveyInfo.owner.label"/>: ${surveyInfo?.owner}</span>
                </div>

                <div class="description">
                    <p>
                        <g:if test="${surveyInfo?.startDate}">
                            <g:message code="surveyInfo.startDate.label"/>: <g:formatDate
                                date="${surveyInfo?.startDate}" formatName="default.date.format.notime"/>
                        </g:if>

                        <g:if test="${surveyInfo?.endDate}">
                            <g:message code="surveyInfo.endDate.label"/>: <g:formatDate
                                date="${surveyInfo?.endDate}" formatName="default.date.format.notime"/>
                        </g:if>

                    </p>

                    <p>
                        <g:if test="${surveyInfo?.comment}">
                            <g:message code="surveyInfo.comment.label"/>: ${surveyInfo?.comment}
                        </g:if>
                    </p>
                </div>

                <div class="extra">

                    <div class="ui label">
                        ${surveyInfo?.status.getI10n('value')}
                    </div>

                    <div class="ui label survey-${surveyInfo?.type.value}">
                       ${surveyInfo?.type.getI10n('value')}
                    </div>

                </div>
            </div>
        </div>

    </g:each>

</div>
