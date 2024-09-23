<%@ page import="de.laser.survey.SurveyResult; de.laser.survey.SurveyConfigProperties; de.laser.storage.PropertyStore; de.laser.storage.RDStore; de.laser.config.ConfigMapper; de.laser.properties.PropertyDefinition; de.laser.UserSetting; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style>
    table{border-collapse:collapse;border-spacing:0;}
    td{padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
    th{;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
    </style>
</head>
<body>

<g:set var="userName" value="${user ? raw(user.getDisplayName()) : 'User'}"/>
<g:set var="orgName" value="${raw(org.name)}"/>
<g:set var="language" value="${user ? user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', de.laser.storage.RDConstants.LANGUAGE)).value : 'de'}"/>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:set var="surveyUrl" value="${"/survey/evaluationParticipant/${survey.id}?surveyConfigID=${survey.surveyConfigs[0].id}&participant=${org.id}"}"/>

${message(code: 'email.text.title', locale: language)} ${userName},
<br />
<br />
${message(code: 'email.survey.finish.text', locale: language)}
<br />
${message(code: 'surveyInfo.name.label', locale: language)}: <strong>${survey.name} </strong>
<br />
(${message(code: 'email.survey.date.from.to', args: [formatDate(format: message(code: 'default.date.format.notime'), date: survey.startDate), formatDate(format: message(code: 'default.date.format.notime'), date: survey.endDate)])})
<br />
<br />
${message(code: 'surveyconfig.orgs.label', locale: language)}: ${orgName}
<br />
<br />

<g:if test="${survey.surveyConfigs[0].pickAndChoose}">
    <g:set var="subscriberSub" value="${survey.surveyConfigs[0].subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)}"/>
    <g:set var="sumListPriceSelectedIEsEUR" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_EUR)}"/>
    <g:set var="sumListPriceSelectedIEsUSD" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_USD)}"/>
    <g:set var="sumListPriceSelectedIEsGBP" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_GBP)}"/>

    ${message(code: 'email.survey.finish.selection.text', locale: language)} ${surveyService.countIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0])}
    <br />
    <br />
    ${message(code: 'tipp.price.listPrice')}:
    <g:if test="${sumListPriceSelectedIEsEUR > 0}">
        <br>
        <g:formatNumber
                number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/>
    </g:if>
    <g:if test="${sumListPriceSelectedIEsUSD > 0}">
        <br>
        <g:formatNumber
                number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/>
    </g:if>
    <g:if test="${sumListPriceSelectedIEsGBP > 0}">
        <br>
        <g:formatNumber
                number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/>
    </g:if>
</g:if>

<g:if test="${surveyResults}">
    <table>
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>
                ${message(code: 'surveyResult.participantComment')}
            </th>
            <th>
                ${message(code: 'surveyResult.commentOnlyForOwner')}
            </th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">
            <tr>
                <td>
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult.type?.getI10n('name')}
                </td>
                <td>
                    ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                    <g:if test="${surveyResult.type.isRefdataValueType()}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                var="refdataValue">
                            <g:if test="${refdataValue.getI10n('value')}">
                                <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                            </g:if>
                        </g:each>
                        <br />
                        (${refdataValues.join('/')})
                    </g:if>
                </td>
                <td>
                    ${surveyResult.getResult()}
                </td>
                <td>
                    ${surveyResult.comment}
                </td>
                <td>
                    ${surveyResult.ownerComment}
                </td>
            </tr>
        </g:each>
    </table>
</g:if>
<br />
<br />
${message(code: 'email.survey.finish.url', locale: language)}
<br />
${ConfigMapper.getConfig('grails.serverURL', String) + surveyUrl}
<br />
<g:if test="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(survey.surveyConfigs[0], PropertyStore.SURVEY_PROPERTY_TEST)}">
    <g:set var="participantTestProperty" value="${SurveyResult.findBySurveyConfigAndParticipantAndType(survey.surveyConfigs[0], org, PropertyStore.SURVEY_PROPERTY_TEST)}"/>
    <g:if test="${participantTestProperty && participantTestProperty.refValue == RDStore.YN_YES }">
        <g:set var="mailInfos" value="${"organisation/mailInfos/${org.id}?subscription=${survey.surveyConfigs[0].subscription?.id}"}"/>
        ${ConfigMapper.getConfig('grails.serverURL', String) + mailInfos}
    </g:if>
</g:if>
<br />
${message(code: 'email.profile.settings', locale: language)}
<g:render template="/mailTemplates/html/signature" />
</body>
</html>
