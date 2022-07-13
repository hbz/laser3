<%@ page import="de.laser.SurveyLinks; de.laser.SurveyConfig; de.laser.SurveyOrg; de.laser.Subscription; de.laser.helper.RDStore;" %>
<semui:card message="${message(code: 'surveyLinks.label')}" class="la-js-hideable" href="#surveyLinks" editable="${editable && controllerName == 'survey' && actionName == 'show'}">
    <div class="ui small feed content la-js-dont-hide-this-card">
        <div class="ui grid summary">
            <div class="sixteen wide column">
                <g:if test="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                    <g:set var="surveyLinks" value="${SurveyLinks.findAllBySourceSurvey(surveyConfig.surveyInfo)}"/>
                    <g:if test="${surveyLinks.size() > 0}">
                        <table class="ui table">
                            <tr>
                                <th>${message(code: 'surveyInfo.slash.name')}</th>
                                <th>${message(code: 'surveyInfo.type.label')}</th>
                                <th>${message(code: 'subscription.periodOfValidity.label')}</th>
                                <th>${message(code: 'surveyInfo.status.label')}</th>
                                <th>${message(code: 'surveyLinks.bothDirection')}</th>
                                <th></th>
                            </tr>
                            <g:each in="${surveyLinks.sort{it.targetSurvey.name}}" var="surveyLink">
                                <tr>
                                    <td>
                                        <g:link controller="survey" action="show"
                                                id="${surveyLink.targetSurvey.id}">${surveyLink.targetSurvey.name}</g:link>
                                    </td>
                                    <td>
                                        ${surveyLink.targetSurvey.type.getI10n('value')}
                                    </td>
                                    <td>
                                        <g:if test="${surveyLink.targetSurvey.startDate}"><g:formatDate
                                            date="${surveyLink.targetSurvey.startDate}"
                                            format="${message(code: 'default.date.format.notime')}"/></g:if><g:if
                                            test="${surveyLink.targetSurvey.endDate}"> - <g:formatDate date="${surveyLink.targetSurvey.endDate}"
                                                                                        format="${message(code: 'default.date.format.notime')}"/></g:if>
                                    </td>
                                    <td>
                                        ${surveyLink.targetSurvey.status.getI10n('value')}
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
                </g:if>
                <g:else>
                    <g:set var="surveyLinks" value="${SurveyLinks.findAllBySourceSurvey(surveyConfig.surveyInfo)}"/>
                    <g:if test="${surveyLinks.size() > 0}">
                        <table class="ui table">
                            <g:each in="${surveyLinks.sort{it.targetSurvey.name}}" var="surveyLink">
                                <tr>
                                    <th><g:message code="survey.label"/></th>
                                    <th>${message(code: 'surveyInfo.type.label')}</th>
                                    <th>${message(code: 'subscription.periodOfValidity.label')}</th>
                                    <th><g:message code="surveyLinks.possible.participation"/></th>
                                </tr>
                                <g:if test="${surveyLink.targetSurvey.status != RDStore.SURVEY_IN_PROCESSING}">
                                    <g:if test="${SurveyOrg.findAllByOrgAndSurveyConfigInList(institution, surveyLink.targetSurvey.surveyConfigs).size() == 0}">
                                        <%
                                            boolean existsMultiYearTerm = false
                                            SurveyConfig targetSurveyConfig = surveyLink.targetSurvey.surveyConfigs[0]
                                            Subscription sub = targetSurveyConfig.subscription
                                            if (sub && !targetSurveyConfig.pickAndChoose && targetSurveyConfig.subSurveyUseForTransfer) {
                                                Subscription subChild = sub.getDerivedSubscriptionBySubscribers(institution)

                                                if (subChild && subChild.isCurrentMultiYearSubscriptionNew()) {
                                                    existsMultiYearTerm = true
                                                }

                                            } %>
                                        <g:if test="${!existsMultiYearTerm}">
                                            <tr>
                                                <td>
                                                    ${surveyLink.targetSurvey.name}
                                                </td>

                                                <td>
                                                    ${surveyLink.targetSurvey.type.getI10n('value')}
                                                </td>
                                                <td>
                                                    <g:if
                                                            test="${surveyLink.targetSurvey.startDate}"><g:formatDate
                                                            date="${surveyLink.targetSurvey.startDate}"
                                                            format="${message(code: 'default.date.format.notime')}"/></g:if><g:if
                                                            test="${surveyLink.targetSurvey.endDate}">- <g:formatDate
                                                                date="${surveyLink.targetSurvey.endDate}"
                                                                format="${message(code: 'default.date.format.notime')}"/></g:if>
                                                </td>

                                                <td>
                                                    <g:if test="${editable && surveyLink.targetSurvey.status == RDStore.SURVEY_SURVEY_STARTED}">
                                                        <span class="la-popup-tooltip la-delay"
                                                              data-content="${message(code: 'surveyLinks.participateToSurvey')}">
                                                            <g:link class="ui button la-modern-button js-open-confirm-modal"
                                                                    controller="myInstitution"
                                                                    action="surveyLinkOpenNewSurvey"
                                                                    params="${[surveyLink: surveyLink.id]}"
                                                                    role="button"
                                                                    aria-label="${message(code: 'surveyLinks.participateToSurvey')}">
                                                                <g:message code="surveyLinks.participateToSurvey"/>
                                                            </g:link>
                                                        </span>
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:if>
                                </g:if>
                            </g:each>
                        </table>
                    </g:if>
                </g:else>
            </div>
        </div>
    </div>
    <g:if test="${editable && controllerName == 'survey' && actionName == 'show'}">
        <g:render template="/survey/surveyLinksModal"/>
    </g:if>

</semui:card>

