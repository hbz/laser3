<%@ page import="de.laser.finance.CostItem; de.laser.Person; de.laser.storage.RDStore; de.laser.interfaces.CalculatedType" %>
<laser:htmlStart message="subscription.details.surveys.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon>
<laser:render template="iconSubscriptionIsChild"/>
<ui:xEditable owner="${subscription}" field="name"/>
<ui:totalNumber total="${surveys.size()}"/>
</ui:h1HeaderWithIcon>
<ui:anualRings object="${subscription}" controller="subscription" action="surveys"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


<laser:render template="nav"/>

<laser:render template="message"/>

<ui:messages data="${flash}"/>

<g:if test="${surveys}">
    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>

            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>

            <g:sortableColumn params="${params}" property="surInfo.name"
                              title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>

            <g:sortableColumn params="${params}" property="surInfo.startDate"
                              title="${message(code: 'default.startDate.label')}"/>
            <g:sortableColumn params="${params}" property="surInfo.endDate"
                              title="${message(code: 'default.endDate.label')}"/>
            <th>${message(code: 'surveyInfo.type.label')}</th>
            <th>${message(code: 'default.status.label')}</th>
            <th>${message(code: 'surveyInfo.finishedDate')}</th>
            <th class="la-action-info">${message(code: 'default.actions.label')}</th>

        </tr>

        </thead>
        <g:each in="${surveys}" var="surveyConfig" status="i">

            <g:set var="surveyInfo"
                   value="${surveyConfig.surveyInfo}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>

                    <div class="la-flexbox">
                        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info2")}">
                                <i class="grey icon pie chart la-list-icon"></i>
                            </span>
                        </g:if>
                        ${surveyConfig.getSurveyName()}
                    </div>
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.startDate}"/>

                </td>
                <td>

                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.endDate}"/>
                </td>
                <td>
                    <div class="ui label survey-${surveyInfo.type.value}">
                        ${surveyInfo.type?.getI10n('value')}
                    </div>

                    <g:if test="${surveyInfo.isMandatory}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                            <i class="yellow icon exclamation triangle"></i>
                        </span>
                    </g:if>
                </td>

                <td class="center aligned">
                    <survey:finishIcon participant="${institution}" surveyConfig="${surveyConfig}"
                                            surveyOwnerView="${false}"/>
                </td>
                <td class="center aligned">
                    <survey:finishDate participant="${institution}" surveyConfig="${surveyConfig}"/>
                </td>
                <td class="x">

                    <g:if test="${(contextOrg.getCustomerType() in ['ORG_CONSORTIUM'])}">
                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: institution.id]"
                                        class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                            </span>
                    </g:if>
                    <g:else>
                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                <g:link controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}"
                                        params="[surveyConfigID: surveyConfig.id]"
                                        class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                            </span>
                    </g:else>

                </td>
            </tr>

        </g:each>
    </table>
</g:if>
<g:else>
    <ui:form>
        <h3 class="ui header">
            <g:message code="survey.notExist.plural"/>
        </h3>
    </ui:form>
</g:else>
<laser:htmlEnd />

