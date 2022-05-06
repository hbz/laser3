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
<g:set var="language" value="${user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value}"/>

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
    ${message(code: 'email.survey.participation.finish.text', locale: language)}
    <br />
    ${message(code: 'email.survey.participation.finish.text2', locale: language)}
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
</g:if>
<br />
<br />
<g:if test="${survey.type.id == RDStore.SURVEY_TYPE_RENEWAL.id}">
    ${message(code: 'email.survey.participation.finish.renewal.text4', locale: language, args: [generalContactsEMails.join(';')])}
</g:if>
<g:elseif test="${survey.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id}">
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text3', locale: language)}
    <br />
    <br />
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text4', locale: language, args: [generalContactsEMails.join(';')])}
</g:elseif>

<br />
<br />
<br />
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
