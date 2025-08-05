<%@ page import="de.laser.UserSetting; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.OrgRole" %>
<laser:serviceInjection/>

    <g:render template="/myInstitution/dashboardTabHelper" model="${[tmplKey: UserSetting.KEYS.DASHBOARD_TAB_TIME_SURVEYS_MANDATORY_ONLY]}" />

<div class="ui two cards">

    <g:each in="${surveys}" var="survey" status="i">
        <g:set var="surveyConfig" value="${SurveyConfig.get(survey.key)}"/>
        <g:set var="surveyInfo" value="${surveyConfig.surveyInfo}"/>

        <div class="ui fluid card">
            <div class="content">
                <div class="ui compact grid">
                    <div class="two column row">
                        <div class="column">
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <g:link controller="survey" action="show" params="[surveyConfigID: surveyConfig.id]" id="${surveyInfo.id}">
                                    <strong>${i+1}. ${surveyConfig.getSurveyName()}</strong>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="myInstitution" action="surveyInfos" params="[surveyConfigID: surveyConfig.id]" id="${surveyInfo.id}">
                                    <strong>${i+1}. ${surveyConfig.getSurveyName()}</strong>
                                </g:link>
                            </g:else>
                        </div>
                        <div class="column">
                            <span class="ui label right floated survey-${surveyInfo.type.value}">
                                ${surveyInfo.type.getI10n('value')}
                            </span>
                        </div>
                    </div>

                    <div class="one column row">
                        <div class="column">
                            <g:if test="${surveyInfo.startDate}">
                                <p class="sc_grey">
%{--                                    <strong><g:message code="surveyInfo.startDate.label"/>:</strong>--}%
                                    <icon:startDate/>
                                    <g:formatDate date="${surveyInfo.startDate}" formatName="default.date.format.notime"/>
                                </p>
                            </g:if>
                            <g:if test="${surveyInfo.endDate}">
                                <p class="sc_grey">
%{--                                    <strong><g:message code="surveyInfo.endDate.label"/>:</strong>--}%
                                    <icon:endDate/>
                                    <g:formatDate date="${surveyInfo.endDate}" formatName="default.date.format.notime"/>
                                </p>
                            </g:if>
                            <g:if test="${surveyInfo.comment}">
                                <p class="sc_grey">
                                    <strong><g:message code="surveyInfo.comment.label"/>:</strong>
                                    ${surveyInfo.comment}
                                </p>
                            </g:if>
                        </div>
                    </div>

                    <div class="three column row">
                        <div class="column">
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]">
                                    <span class="ui circular ${surveyConfig.configFinish ? "green" : ""} label">
                                        ${surveyConfig.orgs.size()}
                                    </span>
                                    <strong>${message(code: 'surveyParticipants.label')}</strong>
                                </g:link>
                            </g:if>
                            <g:else>
                                <strong><g:message code="surveyInfo.owner.label"/>:</strong> ${surveyInfo.owner}
                            </g:else>
                        </div>
                        <div class="column">
                            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                                        class="la-float-right">
                                    <i class="${Icon.SURVEY}"></i> <strong>${message(code: "surveyResult.label")} anzeigen</strong>
                                </g:link>
                            </g:if>
                        </div>
                        <div class="column">
                            <g:if test="${surveyInfo.isMandatory}">
                                <span class="la-long-tooltip la-popup-tooltip right floated" data-position="top right"
                                      data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                                    <i class="${Icon.TOOLTIP.IMPORTANT} red large"></i>
                                </span>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </g:each>

</div>
%{--    <ui:paginate action="dashboard" controller="myInstitution" offset="${surveysOffset}" max="${max ?: contextService.getUser().getPageSizeOrDefault()}" params="${[view:'Surveys']}" total="${surveysCount}"/>--}%

<laser:script file="${this.getGroovyPageFileName()}">
%{--    $("#surveyCount").text("${message(code:'myinst.dash.survey.label', args: [surveysCount])}")--}%
    $("#surveyCount").text("${surveys.size()}${surveysCount > surveys.size() ? '+' : ''}")
</laser:script>