<%@ page import="de.laser.config.ConfigMapper; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:set var="surveyUrl"
       value="${"/myInstitution/surveyInfos/${survey.id}?surveyConfigID=${survey.surveyConfigs[0].id}"}"/>
<g:set var="renewalSurvey" value="${survey.type == RDStore.SURVEY_TYPE_RENEWAL}"/>
<g:set var="linkToSurvey" value="${ConfigMapper.getConfig('grails.serverURL', String) + raw(surveyUrl)}"/>

<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>

<body>
<g:message code="email.survey.title" locale="${language}"/>
<br />
<br />
<g:if test="${reminder}">
    <g:message code="email.survey.reminder.general.text" locale="${language}"/>
    <br />
    ${escapeService.replaceUmlaute(survey.type.getI10n('value', language))} - ${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.startDate}"/> - <g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.endDate}"/>) ${survey.surveyConfigs[0].subscription ? (survey.surveyConfigs[0].subscription.getProviders() ? escapeService.replaceUmlaute(survey.surveyConfigs[0].subscription.getProviders().name[0]) : '') : ''}
    <br />
    ${linkToSurvey}
    <br />
    <br />
    <g:if test="${survey.isMandatory}">
        <g:if test="${renewalSurvey}">
            <g:message code="email.survey.reminder.renewal.text.isMandatory" locale="${language}"/>
        </g:if>
        <g:else>
            <g:message code="email.survey.reminder.general.text.isMandatory" locale="${language}"/>
        </g:else>
    </g:if>
    <g:else>
        <g:message code="email.survey.reminder.general.text.isNotMandatory" locale="${language}"/>
    </g:else>
</g:if>
<g:else>
    <g:if test="${renewalSurvey}">
        <g:message code="email.survey.renewal.text" locale="${language}"/>
        <br />
        ${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.startDate}"/> - <g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.endDate}"/>) ${survey.surveyConfigs[0].subscription ? (survey.surveyConfigs[0].subscription.getProviders() ? escapeService.replaceUmlaute(survey.surveyConfigs[0].subscription.getProviders().name[0]) : '') : ''}
        <br />
        <br />
        <g:message code="email.survey.renewal.text2" locale="${language}"/>
        <br />
        ${linkToSurvey}
        <br />
        <br />
        <g:message code="email.survey.renewal.text3" locale="${language}"/>
    </g:if>
    <g:else>
        <g:if test="${survey.type == RDStore.SURVEY_TYPE_INTEREST}">
            <g:message code="email.survey.general.text" locale="${language}"/>
            <br />
            ${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.startDate}"/> - <g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.endDate}"/>) ${survey.surveyConfigs[0].subscription ? (survey.surveyConfigs[0].subscription.getProviders() ? escapeService.replaceUmlaute(survey.surveyConfigs[0].subscription.getProviders().name[0]) : '') : ''}
            <br />
            <br />
            <g:message code="email.survey.general.text2" locale="${language}"/>
        </g:if>
        <g:elseif test="${survey.type == RDStore.SURVEY_TYPE_SUBSCRIPTION}">
            <g:message code="email.survey.subscription.text" locale="${language}"/>
            <br />
            ${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.startDate}"/> - <g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.endDate}"/>) ${survey.surveyConfigs[0].subscription ? (survey.surveyConfigs[0].subscription.getProviders() ? escapeService.replaceUmlaute(survey.surveyConfigs[0].subscription.getProviders().name[0]) : '') : ''}
            <br />
            <br />
            <g:message code="email.survey.subscription.text2" locale="${language}"/>
        </g:elseif>
        <g:elseif test="${survey.type == RDStore.SURVEY_TYPE_TITLE_SELECTION}">
            <g:message code="email.survey.selection.text" locale="${language}"/>
            <br />
            ${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.startDate}"/> - <g:formatDate format="${message(code: 'default.date.format.notime', locale: language)}" date="${survey.endDate}"/>) ${survey.surveyConfigs[0].subscription ? (survey.surveyConfigs[0].subscription.getProviders() ? escapeService.replaceUmlaute(survey.surveyConfigs[0].subscription.getProviders().name[0]) : '') : ''}
            <br />
            <br />
            <g:message code="email.survey.selection.text2" locale="${language}"/>
        </g:elseif>
        <br />
        ${linkToSurvey}
        <br />
        <br />
        <g:if test="${survey.isMandatory}">
            <g:if test="${!renewalSurvey}">
                <g:message code="email.survey.reminder.general.text.isMandatory" locale="${language}"/>
            </g:if>
        </g:if>
        <g:else>
            <g:message code="email.survey.reminder.general.text.isNotMandatory" locale="${language}"/>
        </g:else>
    </g:else>
</g:else>
<br />
<br />
<g:message code="email.survey.general.help" locale="${language}"  args="[survey.owner.name]"/>
<br />
<br />
${message(code: 'email.text.end', locale: language)}
<br />
${message(code: 'email.survey.owner', locale: language)}
<br />
${survey.owner.name}
<br />
<br />

</body>
</html>