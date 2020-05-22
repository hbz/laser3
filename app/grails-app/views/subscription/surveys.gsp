<%@ page import="com.k_int.kbplus.CostItem; com.k_int.kbplus.Person; de.laser.helper.RDStore; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.surveys.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon/>
<semui:xEditable owner="${subscriptionInstance}" field="name"/>
<semui:totalNumber total="${surveys.size() ?: 0}"/>
</h1>
<semui:anualRings object="${subscriptionInstance}" controller="subscription" action="surveys"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


<g:render template="nav"/>

<g:render template="message" />

<semui:messages data="${flash}"/>

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
                   value="${surveyConfig?.surveyInfo}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>

                    <div class="la-flexbox">
                        <g:if test="${surveyConfig?.subSurveyUseForTransfer}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyConfig.subSurveyUseForTransfer.label.info2")}">
                                <i class="yellow icon envelope large "></i>
                            </span>
                        </g:if>
                            ${surveyConfig?.getSurveyName()}
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
                <td>
                    ${surveyInfo?.type?.getI10n('value')}
                </td>

                <td class="center aligned">
                    <semui:surveyFinishIcon participant="${institution}" surveyConfig="${surveyConfig}"
                                            surveyOwnerView="${false}"/>
                </td>
                <td class="center aligned">
                    <semui:surveyFinishDate participant="${institution}" surveyConfig="${surveyConfig}"/>
                </td>
                <td class="x">

                    <g:if test="${!surveyConfig.pickAndChoose}">
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="myInstitution" action="surveyInfos" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig.id]"
                                    class="ui icon button">
                                <i class="write icon"></i>
                            </g:link>
                        </span>
                    </g:if>

                    <g:if test="${surveyConfig.pickAndChoose}">
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toIssueEntitlementsSurvey')}">
                            <g:link controller="myInstitution" action="surveyInfosIssueEntitlements" id="${surveyConfig.id}"
                                    class="ui icon button">
                                <i class="write icon"></i>
                            </g:link>
                        </span>
                    </g:if>

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

