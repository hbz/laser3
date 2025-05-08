<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg" %>
<laser:serviceInjection/>

<table class="ui celled sortable table la-js-responsive-table la-table">
    <thead>
    <tr>
        <th class="center aligned">${message(code: 'sidewide.number')}</th>
        <th>${message(code: 'default.name.label')}</th>

        <th>
            ${participationProperty?.getI10n('name')}

            <g:if test="${participationProperty?.getI10n('expl')}">
                <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                      data-content="${participationProperty?.getI10n('expl')}">
                    <i class="${Icon.TOOLTIP.HELP}"></i>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                          data-content="${surveyProperty.getI10n('expl')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </g:if>
            </th>
        </g:each>
        <th>${message(code: 'renewalEvaluation.costItem.label')}</th>
        <th class="center aligned">
            <ui:optionsIcon />
        </th>
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
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyResult.newOrg')}">
                        <i class="${Icon.SIG.NEW_OBJECT} large"></i>
                    </span>
                </g:if>

                <g:if test="${surveyConfig.checkResultsEditByOrg(participantResult.participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyResult.processedOrg')}">
                        <i class="${Icon.ATTR.SURVEY_RESULTS_PROCESSED}"></i>
                    </span>
                </g:if>
                <g:else>
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                        <i class="${Icon.ATTR.SURVEY_RESULTS_NOT_PROCESSED}"></i>
                    </span>
                </g:else>

                <g:if test="${surveyConfig.isResultsSetFinishByOrg(participantResult.participant)}">
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyResult.finishOrg')}">
                        <i class="${Icon.SYM.YES} green"></i>
                    </span>
                </g:if>
                <g:else>
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyResult.notfinishOrg')}">
                        <i class="${Icon.SYM.NO} red"></i>
                    </span>
                </g:else>

                <g:if test="${surveyConfig.checkOrgTransferred(participantResult.participant)}">
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyTransfer.transferred')}: ${surveyConfig.getSubscriptionWhereOrgTransferred(participantResult.participant).collect {it.getLabel()}.join(', ')}">
                        <i class="${Icon.ATTR.SURVEY_ORG_TRANSFERRED}"></i>
                    </span>
                </g:if>

                <g:if test="${propertiesChangedByParticipant && participantResult.participant.id in propertiesChangedByParticipant.id}">
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                        <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                    </span>
                </g:if>
                <g:if test="${surveyOrg.orgInsertedItself}">
                    <span data-position="top right" class="la-popup-tooltip"
                          data-content="${message(code: 'surveyLinks.newParticipate')}">
                        <i class="paper plane outline large icon"></i>
                    </span>
                </g:if>

                <g:if test="${participantResult.sub}">
                    <ui:xEditableAsIcon owner="${participantResult.sub}" class="ui icon center aligned" iconClass="info circular inverted" field="comment" type="textarea" overwriteEditable="${false}"/>
                </g:if>

            </td>
            <td>
                ${participantResult.resultOfParticipation.getResult()}

                <g:if test="${participantResult.resultOfParticipation.comment}">
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${participantResult.resultOfParticipation.comment}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </g:if>

                <g:if test="${surveyOrg && surveyOrg.ownerComment}">
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${surveyOrg.ownerComment}">
                        <i class="${Icon.TOOLTIP.INFO}"></i>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${participantResult.participantPropertyTwoComment}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${participantResult.participantPropertyThreeComment}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${participantResult.participantPropertyFourComment}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                          data-content="${participantResult.participantPropertyFiveComment}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${participantResultProperty.comment}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </g:if>

                    <g:set var="surveyOrg"
                           value="${SurveyOrg.findBySurveyConfigAndOrg(participantResultProperty.surveyConfig, participantResultProperty.participant)}"/>

                    <g:if test="${surveyOrg && surveyOrg.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${surveyOrg.ownerComment}">
                            <i class="${Icon.TOOLTIP.INFO}"></i>
                        </span>
                    </g:if>

                </td>
            </g:each>

            <td>

                <g:set var="costItems" value="${participantResult.resultOfParticipation.getCostItems()}"/>

                <g:if test="${costItems}">
                    <g:each in="${costItems}" var="costItem">
                        <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/></strong>

                        (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${costItem.billingCurrency?.getI10n('value')}
                        <br>
                    </g:each>
                </g:if>
            </td>
            <td class="x">
                <g:link controller="survey" action="evaluationParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participantResult.participant.id]"
                        class="${Btn.MODERN.SIMPLE}"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                    <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                </g:link>

                <g:if test="${participantResult.sub}">
                    <br/>
                    <g:link controller="subscription" action="show" id="${participantResult.sub?.id}"
                            class="${Btn.MODERN.SIMPLE} orange"><i class="${Icon.SUBSCRIPTION} icon"></i></g:link>
                </g:if>
            </td>

        </tr>
    </g:each>
</table>