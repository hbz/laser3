<%@ page import="de.laser.SurveyConfig; de.laser.OrgRole" %>

<h3 class="ui header"><g:message code="surveys.active"/></h3>

<div class="ui divided items">
    <g:each in="${surveys}" var="survey" status="i">

        <g:set var="surveyConfig"
               value="${SurveyConfig.get(survey.key)}"/>

        <g:set var="surveyInfo"
               value="${surveyConfig.surveyInfo}"/>

        <div class="item">

            <div class="content">
                <div class="ui header">
                    <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                        <g:link controller="survey" action="show" params="[surveyConfigID: surveyConfig.id]"
                                id="${surveyInfo.id}">${surveyConfig.getSurveyName()}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:if test="${!surveyConfig.pickAndChoose}">
                        <g:link controller="myInstitution" action="surveyInfos" params="[surveyConfigID: surveyConfig.id]"
                                              id="${surveyInfo.id}">${surveyConfig.getSurveyName()}</g:link>
                        </g:if>
                        <g:if test="${surveyConfig.pickAndChoose}">
                            <g:link controller="myInstitution" action="surveyInfosIssueEntitlements" id="${surveyConfig.id}"
                                    params="${[targetObjectId: surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(institution)?.id]}">
                                ${surveyConfig.getSurveyName()}
                            </g:link>
                        </g:if>
                    </g:else>

                    <span class="ui label survey-${surveyInfo.type.value}">
                        ${surveyInfo.type.getI10n('value')}
                    </span>

                    <g:if test="${surveyInfo.isMandatory}">
                        &nbsp;
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                            <i class="yellow icon exclamation triangle"></i>
                        </span>
                    </g:if>

                </div>

                <div class="description">
                    <p>
                        <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig.id]" class="ui icon">
                                <strong>${message(code: 'surveyParticipants.label')}:</strong>
                                <span class="ui circular ${surveyConfig.configFinish ? "green" : ""} label">
                                    ${surveyConfig.orgs?.size() ?: 0}
                                </span>
                            </g:link>

                            <span class="la-float-right">
                                <g:if test="${surveyConfig && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT && surveyConfig.pickAndChoose}">

                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center"
                                        data-content="${message(code: "surveyTitlesEvaluation.label")} anzeigen">
                                            <g:link controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo.id}"
                                                    params="[surveyConfigID: surveyConfig.id]"
                                                    class="ui icon button">
                                                <i class="icon blue chart pie"></i>
                                            </g:link>
                                    </span>
                                </g:if>
                                <g:else>

                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center"
                                        data-content="${message(code: "surveyEvaluation.label")} anzeigen">
                                            <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                                                    params="[surveyConfigID: surveyConfig.id]"
                                                    class="ui icon button">
                                                <i class="icon blue chart pie"></i>
                                            </g:link>
                                    </span>
                                </g:else>
                            </span>
                        </g:if>
                        <g:else>
                            <strong><g:message code="surveyInfo.owner.label"/>:</strong> ${surveyInfo.owner}
                        </g:else>
                    </p>

                    <g:if test="${surveyInfo.startDate}">
                        <p>
                            <strong><g:message code="surveyInfo.startDate.label"/>:</strong>
                            <g:formatDate date="${surveyInfo.startDate}" formatName="default.date.format.notime"/>
                        </p>
                    </g:if>
                    <g:if test="${surveyInfo.endDate}">
                        <p>
                            <strong><g:message code="surveyInfo.endDate.label"/>:</strong>
                            <g:formatDate date="${surveyInfo.endDate}" formatName="default.date.format.notime"/>
                        </p>
                    </g:if>
                    <g:if test="${surveyInfo.comment}">
                        <p>
                            <strong><g:message code="surveyInfo.comment.label"/>:</strong>
                            ${surveyInfo.comment}
                        </p>
                    </g:if>
                </div>

            </div>
        </div>

    </g:each>

</div>
