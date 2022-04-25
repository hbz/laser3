<%@ page import="de.laser.helper.ConfigMapper; de.laser.properties.PropertyDefinition; de.laser.UserSetting; com.k_int.kbplus.*; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated;" %>
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
<g:set var="orgName" value="${raw(org.name)}"/>
<g:set var="language" value="${user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', de.laser.storage.RDConstants.LANGUAGE)).value}"/>
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
    ${message(code: 'email.survey.finish.selection.text', locale: language)} ${subscriptionService.getIssueEntitlementsUnderNegotiation(survey.surveyConfigs[0].subscription.getDerivedSubscriptionBySubscribers(org)).size()}
    <br />
    <br />
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
        <g:each in="${surveyResults.sort { it.type.getI10n('name') }}" var="surveyResult" status="i">
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
${ConfigMapper.getConfig('grails.serverURL') + surveyUrl}
<br />
<br />
${message(code: 'email.profile.settings', locale: language)}
<laser:render template="/mailTemplates/html/signature" />
</body>
</html>
