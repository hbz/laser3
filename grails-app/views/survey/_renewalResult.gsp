<%@ page import="de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg" %>
<laser:serviceInjection/>

<table class="ui celled sortable table la-js-responsive-table la-table">
    <thead>
    <tr>
        <th class="center aligned">${message(code: 'sidewide.number')}</th>
        <th>${message(code: 'default.name.label')}</th>

        <th>
            ${participationProperty?.getI10n('name')}

            <g:if test="${participationProperty?.getI10n('expl')}">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${participationProperty?.getI10n('expl')}">
                    <i class="question circle icon"></i>
                </span>
            </g:if>
        </th>
        <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey || multiYearTermFourSurvey || multiYearTermFiveSurvey}">
            <th>
                <g:message code="renewalEvaluation.period"/>
            </th>
        </g:if>


        <g:each in="${properties}" var="surveyProperty">
            <th>
                ${surveyProperty.getI10n('name')}

                <g:if test="${surveyProperty?.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${surveyProperty.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </th>
        </g:each>
        <th>${message(code: 'renewalEvaluation.costItem.label')}</th>
        <th>${message(code: 'default.actions.label')}</th>
    </tr>
    </thead>
    <g:each in="${participantResults}" var="participantResult" status="i">
        <g:set var="surveyOrg"
               value="${SurveyOrg.findBySurveyConfigAndOrg(participantResult.surveyConfig, participantResult.participant)}"/>

        <tr>
            <td class="center aligned">
                ${i + 1}
            </td>
            <td>
                <g:link controller="myInstitution" action="manageParticipantSurveys"
                        id="${participantResult.participant.id}">
                    ${participantResult.participant.sortname}
                </g:link>
                <br/>
                <g:link controller="organisation" action="show"
                        id="${participantResult.participant.id}">(${fieldValue(bean: participantResult.participant, field: "name")})</g:link>


                <g:if test="${!surveyConfig.hasOrgSubscription(participantResult.participant)}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyResult.newOrg')}">
                        <i class="star black large  icon"></i>
                    </span>
                </g:if>

                <g:if test="${surveyConfig.checkResultsEditByOrg(participantResult.participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyResult.processedOrg')}">
                        <i class="edit green icon"></i>
                    </span>
                </g:if>
                <g:else>
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                        <i class="edit red icon"></i>
                    </span>
                </g:else>

                <g:if test="${surveyConfig.isResultsSetFinishByOrg(participantResult.participant)}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyResult.finishOrg')}">
                        <i class="check green icon"></i>
                    </span>
                </g:if>
                <g:else>
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyResult.notfinishOrg')}">
                        <i class="x red icon"></i>
                    </span>
                </g:else>

                <g:if test="${propertiesChangedByParticipant && participantResult.participant.id in propertiesChangedByParticipant.id}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                        <i class="exclamation triangle yellow large icon"></i>
                    </span>
                </g:if>
                <g:if test="${surveyOrg.orgInsertedItself}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyLinks.newParticipate')}">
                        <i class="paper plane outline large icon"></i>
                    </span>
                </g:if>

            </td>
            <td>
                ${participantResult.resultOfParticipation.getResult()}

                <g:if test="${participantResult.resultOfParticipation.comment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${participantResult.resultOfParticipation.comment}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>

                <g:if test="${surveyOrg && surveyOrg.ownerComment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${surveyOrg.ownerComment}">
                        <i class="info circle icon"></i>
                    </span>
                </g:if>

            </td>

            <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey || multiYearTermFourSurvey || multiYearTermFiveSurvey}">
                <td>
            </g:if>

            <g:if test="${multiYearTermTwoSurvey}">
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodTwoStartDate}"/>
                <br/>
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodTwoEndDate}"/>

                <g:if test="${participantResult.participantPropertyTwoComment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${participantResult.participantPropertyTwoComment}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>

            </g:if>
            <g:if test="${multiYearTermThreeSurvey}">
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodThreeStartDate}"/>
                <br/>
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodThreeEndDate}"/>

                <g:if test="${participantResult.participantPropertyThreeComment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${participantResult.participantPropertyThreeComment}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </g:if>

            <g:if test="${multiYearTermFourSurvey}">
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodFourStartDate}"/>
                <br/>
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodFourEndDate}"/>

                <g:if test="${participantResult.participantPropertyFourComment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${participantResult.participantPropertyFourComment}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </g:if>

            <g:if test="${multiYearTermFiveSurvey}">
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodFiveStartDate}"/>
                <br/>
                <g:formatDate formatName="default.date.format.notime"
                              date="${participantResult.newSubPeriodFiveEndDate}"/>

                <g:if test="${participantResult.participantPropertyFiveComment}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${participantResult.participantPropertyFiveComment}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </g:if>

            <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey || multiYearTermFourSurvey || multiYearTermFiveSurvey}">
                </td>
            </g:if>

            <g:each in="${participantResult.properties}" var="participantResultProperty">
                <td>
                    ${participantResultProperty.getResult()}

                    <g:if test="${participantResultProperty.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResultProperty.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                    <g:set var="surveyOrg"
                           value="${SurveyOrg.findBySurveyConfigAndOrg(participantResultProperty.surveyConfig, participantResultProperty.participant)}"/>

                    <g:if test="${surveyOrg && surveyOrg.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyOrg.ownerComment}">
                            <i class="info circle icon"></i>
                        </span>
                    </g:if>

                </td>
            </g:each>

            <td>

                <g:set var="costItem" value="${participantResult.resultOfParticipation.getCostItem()}"/>

                <g:if test="${costItem}">
                    <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                            maxFractionDigits="2" type="number"/></strong>

                    (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)

                    ${(costItem.billingCurrency?.getI10n('value')?.split('-'))?.first()}
                </g:if>
            </td>
            <td class="x">
                <g:link controller="survey" action="evaluationParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participantResult.participant.id]"
                        class="ui icon button blue la-modern-button"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                    <i aria-hidden="true" class="write icon"></i>
                </g:link>

                <g:if test="${participantResult.sub}">
                    <br/>
                    <g:link controller="subscription" action="show" id="${participantResult.sub?.id}"
                            class="ui button icon"><i class="icon clipboard"></i></g:link>
                </g:if>
            </td>

        </tr>
    </g:each>
</table>