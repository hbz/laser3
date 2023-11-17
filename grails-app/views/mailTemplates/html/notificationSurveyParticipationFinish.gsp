<%@ page import="de.laser.properties.PropertyDefinition; de.laser.UserSetting; de.laser.base.AbstractPropertyWithCalculatedLastUpdated; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.RefdataCategory;" %>
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

<g:set var="userName" value="${raw(user.getDisplayName())}"/>
<g:set var="language" value="${user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RDStore.LANGUAGE_DE).value}"/>

${message(code: 'email.text.title', locale: language)} ${userName},
<br />
<br />
<g:if test="${survey.type.id == RDStore.SURVEY_TYPE_RENEWAL.id}">
    ${message(code: 'email.survey.participation.finish.renewal.text', locale: language, args: [survey.name])}
    <br />
    ${message(code: 'email.survey.participation.finish.renewal.text2', locale: language)}
    <br />
    ${message(code: 'email.survey.participation.finish.renewal.text3', locale: language)}
</g:if>
<g:elseif test="${survey.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id}">
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text', locale: language, args: [survey.name])}
    <br />
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text2', locale: language)}

</g:elseif>
<g:else>
    ${message(code: 'email.survey.participation.finish.text', locale: language, args: [survey.name])}
    <br />
    <g:if test="${survey.surveyConfigs[0].pickAndChoose}">
        <g:set var="subscriberSub" value="${survey.surveyConfigs[0].subscription.getDerivedSubscriptionBySubscribers(org)}"/>
        <g:set var="sumListPriceSelectedIEsEUR" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_EUR)}"/>
        <g:set var="sumListPriceSelectedIEsUSD" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_USD)}"/>
        <g:set var="sumListPriceSelectedIEsGBP" value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_GBP)}"/>

        ${message(code: 'email.survey.finish.selection.text', locale: language)} ${surveyService.countIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0])}
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
        <br />
    </g:if>
    <g:if test="${surveyResults}">
    ${message(code: 'email.survey.participation.finish.text2', locale: language)}
    </g:if>
</g:else>
<br />
<br />
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
                ${message(code: 'surveyResult.commentOnlyForParticipant')}
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
                    ${surveyResult.participantComment}
                </td>
            </tr>
        </g:each>
    </table>
    <br />
    <br />
</g:if>
<g:if test="${survey.type.id == RDStore.SURVEY_TYPE_RENEWAL.id}">
    ${message(code: 'email.survey.participation.finish.renewal.text4', locale: language, args: [generalContactsEMails.join(';')])}
    <br />
    <br />
    <br />
</g:if>
<g:elseif test="${survey.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id}">
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text3', locale: language)}
    <br />
    <br />
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text4', locale: language, args: [generalContactsEMails.join(';')])}
    <br />
    <br />
    <br />
</g:elseif>


${message(code: 'email.text.end', locale: language)}
<br />
${message(code: 'email.survey.owner', locale: language)}
<br />
${survey.owner.name}
<br />
<br />
${message(code: 'email.profile.settings', locale: language)}
${message(code: 'email.text.autogeneric', locale: language)}
</body>
</html>
