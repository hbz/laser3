<%@ page import="com.k_int.kbplus.CostItem; com.k_int.kbplus.Person; de.laser.helper.RDStore; de.laser.interfaces.TemplateSupport" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.surveys.label')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui icon header"><semui:headerIcon />
        <semui:xEditable owner="${subscriptionInstance}" field="name" />
        <semui:totalNumber total="${surveys.size() ?: 0}"/>
    </h1>
    <semui:anualRings object="${subscriptionInstance}" controller="subscription" action="surveys" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <g:render template="nav" />


    <semui:messages data="${flash}" />

    <g:if test="${surveys}">
        <table class="ui celled sortable table la-table">
            <thead>
            <tr>

                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>

                <g:sortableColumn params="${params}" property="surInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>

                <g:sortableColumn params="${params}" property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <g:sortableColumn params="${params}" property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>

                <th>${message(code: 'surveyInfo.finished')}</th>
                <th>${message(code: 'surveyInfo.finishedDate')}</th>
                <th class="la-action-info">${message(code:'default.actions')}</th>

            </tr>

            </thead>
            <g:each in="${surveys}" var="surveyConfig" status="i">

                <g:set var="surveyInfo"
                       value="${surveyConfig?.surveyInfo}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                            <g:if test="${surveyConfig?.type == 'Subscription'}">
                                <i class="icon clipboard outline la-list-icon"></i>
                                ${surveyConfig?.subscription?.name}
                            </g:if>
                            <g:else>
                                <i class="icon chart pie la-list-icon"></i>
                                ${surveyConfig?.getConfigNameShort()}
                            </g:else>

                        <div class="la-flexbox">
                            <g:if test="${surveyConfig?.isSubscriptionSurveyFix}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: "surveyConfig.isSubscriptionSurveyFix.label.info2")}">
                                    <i class="yellow icon envelope large "></i>
                                </span>
                            </g:if>

                            <i class="icon chart pie la-list-icon"></i>
                            <g:if test="${surveyConfig?.isSubscriptionSurveyFix}">
                                <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}"
                                        class="ui ">
                                    ${surveyConfig?.getSurveyName()}
                                </g:link>
                            </g:if>
                            <g:else>
                                ${surveyConfig?.getSurveyName()}
                            </g:else>
                        </div>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.startDate}"/>

                    </td>
                    <td>

                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.endDate}"/>
                    </td>

                    <td class="center aligned">

                        <g:set var="surveyResults"
                               value="${com.k_int.kbplus.SurveyResult.findAllByParticipantAndSurveyConfig(institution, surveyConfig)}"/>

                        <g:if test="${surveyResults}">

                                <g:if test="${surveyResults?.finishDate?.contains(null)}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                          data-variation="tiny"
                                          data-content="Nicht abgeschlossen">
                                        <i class="circle red icon"></i>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                          data-variation="tiny"
                                          data-content="${message(code: 'surveyResult.finish.info.consortia')}">
                                        <i class="check big green icon"></i>
                                    </span>
                                </g:else>
                        </g:if>
                    </td>
                    <td class="center aligned">
                        <g:if test="${surveyResults}">
                            <g:if test="${!surveyResults?.finishDate?.contains(null)}">
                                <g:formatDate formatName="default.date.format.notime" date="${surveyResults?.finishDate[0]}"/>
                            </g:if>
                        </g:if>
                    </td>
                    <td class="x">


                            <span class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                                <g:link controller="myInstitution" action="surveyConfigsInfo" id="${surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]"
                                        class="ui icon button">
                                    <i class="write icon"></i>
                                </g:link>
                            </span>

                    </td>
                </tr>

            </g:each>
        </table>
    </g:if>
    <g:else>
        <semui:form>
            <h3>
                <g:message code="survey.notExist.plural"/>
            </h3>
        </semui:form>
    </g:else>
        </body>
        </html>

