<%@ page import="de.laser.survey.SurveyLinks; de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.Subscription; de.laser.storage.RDStore;" %>

<g:if test="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
    <ui:card message="${message(code: 'surveyLinks.label')}" class="la-js-hideable" href="#surveyLinks"
                editable="${editable && controllerName == 'survey' && actionName == 'show'}">
        <div class="ui small feed content la-js-dont-hide-this-card">
            <div class="ui grid summary">
                <div class="sixteen wide column">
                    <g:set var="surveyLinks" value="${SurveyLinks.findAllBySourceSurvey(surveyConfig.surveyInfo)}"/>
                    <g:if test="${surveyLinks.size() > 0}">
                        <table class="ui table">
                            <tr>
                                <th>${message(code: 'surveyInfo.slash.name')}</th>
                                <th>${message(code: 'subscription.periodOfValidity.label')}</th>
                                <th>${message(code: 'surveyLinks.bothDirection')}</th>
                                <th></th>
                            </tr>
                            <g:each in="${surveyLinks.sort { it.targetSurvey.name }}" var="surveyLink">
                                <tr>
                                    <td>
                                        <g:link controller="survey" action="show"
                                                id="${surveyLink.targetSurvey.id}">${surveyLink.targetSurvey.name}</g:link>

                                        (<b><g:message
                                            code="default.type.label"/></b>: ${surveyLink.targetSurvey.type.getI10n('value')},
                                        <b><g:message
                                                code="default.status.label"/></b>: ${surveyLink.targetSurvey.status.getI10n('value')})
                                    </td>
                                    <td>
                                        <g:if test="${surveyLink.targetSurvey.startDate}"><g:formatDate
                                                date="${surveyLink.targetSurvey.startDate}"
                                                format="${message(code: 'default.date.format.notime')}"/></g:if><g:if
                                                test="${surveyLink.targetSurvey.endDate}">- <g:formatDate
                                                    date="${surveyLink.targetSurvey.endDate}"
                                                    format="${message(code: 'default.date.format.notime')}"/></g:if>
                                    </td>
                                    <td>${surveyLink.bothDirection ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</td>
                                    <td class="right aligned">
                                        <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'default.button.unlink.label')}">
                                                <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                        data-confirm-tokenMsg="${surveyLink.bothDirection ? message(code: "surveyLinks.bothDirection.unlink.confirm.dialog") : message(code: "surveyLinks.unlink.confirm.dialog")}"
                                                        data-confirm-term-how="unlink"
                                                        controller="survey" action="setSurveyLink"
                                                        params="${[unlinkSurveyLink: surveyLink.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                    <i class="unlink icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>
                </div>
            </div>
        </div>
        <g:if test="${editable}">
            <g:render template="/survey/surveyLinksModal"/>
        </g:if>
    </ui:card>
</g:if>
<g:else>
    <g:set var="surveyLinks" value="${SurveyLinks.findAllBySourceSurvey(surveyConfig.surveyInfo)}"/>
    <g:if test="${surveyLinks.size() > 0}">
        <ui:card message="${message(code: 'surveyLinks.label')}" class="la-js-hideable">
            <div class="ui small feed content la-js-dont-hide-this-card">
                <div class="ui grid summary">
                    <div class="sixteen wide column">
                        <table class="ui table">
                            <tr>
                                <th><g:message code="survey.label"/></th>
                                <th><g:message code="default.button.show.label"/></th>
                            </tr>
                            <g:each in="${surveyLinks.sort { it.targetSurvey.name }}" var="surveyLink">
                                <g:if test="${surveyLink.targetSurvey.status != RDStore.SURVEY_IN_PROCESSING}">
                                    <%
                                        boolean surveyOrgFound = SurveyOrg.findAllByOrgAndSurveyConfigInList(institution, surveyLink.targetSurvey.surveyConfigs).size() > 0
                                        boolean existsMultiYearTerm = false
                                        SurveyConfig targetSurveyConfig = surveyLink.targetSurvey.surveyConfigs[0]
                                        Subscription sub = targetSurveyConfig.subscription
                                        if (sub && !targetSurveyConfig.pickAndChoose && targetSurveyConfig.subSurveyUseForTransfer) {
                                            Subscription subChild = sub.getDerivedSubscriptionBySubscribers(institution)

                                            if (subChild && subChild.isCurrentMultiYearSubscriptionNew()) {
                                                existsMultiYearTerm = true
                                            }

                                        }

                                        String newControllerName = "myInstitution"
                                        String newActionName = "surveyInfos"
                                        Map newParams = [id: surveyLink.targetSurvey.id]

                                        if(controllerName == 'survey' && contextOrg?.id == surveyConfig.surveyInfo.owner.id){
                                            newControllerName = "survey"
                                            newActionName = "evaluationParticipant"
                                            newParams=[id: surveyLink.targetSurvey.id, participant: participant.id]
                                        }
                                    %>

                                    <g:if test="${!existsMultiYearTerm}">
                                        <tr>
                                            <td>
                                                <g:if test="${surveyOrgFound}">
                                                    <g:link controller="${newControllerName}" action="${newActionName}"
                                                            params="${newParams}">${surveyLink.targetSurvey.name}</g:link>
                                                </g:if>
                                                <g:else>
                                                    ${surveyLink.targetSurvey.name}
                                                </g:else>
                                            </td>
                                            <td>
                                                <g:if test="${surveyOrgFound}">
                                                    <g:link class="ui button small la-modern-button" controller="${newControllerName}" action="${newActionName}" target="_blank"
                                                            params="${newParams}"><g:message code="default.button.show.label"/></g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${editable && surveyLink.targetSurvey.status == RDStore.SURVEY_SURVEY_STARTED}">
                                                        <span class="la-popup-tooltip la-delay"
                                                              data-content="${message(code: 'surveyLinks.participateToSurvey')}">
                                                            <g:link class="ui button small la-modern-button js-open-confirm-modal"
                                                                    data-confirm-tokenMsg = "${message(code: 'surveyLinks.participateToSurvey.confirm.dialog')}"
                                                                    data-confirm-term-how="ok"
                                                                    controller="myInstitution" target="_blank"
                                                                    action="surveyLinkOpenNewSurvey"
                                                                    params="${[surveyLink: surveyLink.id, participant: participant?.id]}"
                                                                    role="button"
                                                                    aria-label="${message(code: 'surveyLinks.participateToSurvey')}">
                                                                <g:message code="surveyLinks.participateToSurvey"/>
                                                            </g:link>
                                                        </span>
                                                    </g:if>
                                                </g:else>
                                            </td>
                                        </tr>
                                    </g:if>
                                </g:if>
                            </g:each>
                        </table>
                    </div>
                </div>
            </div>
        </ui:card>
    </g:if>
</g:else>




