<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractProperty; com.k_int.kbplus.UserSettings;" %><laser:serviceInjection /><%@ page Content-type: text/plain; charset=utf-8; %><g:set var="userName" value="${raw(user.getDisplayName())}"/><g:set var="orgName" value="${raw(org.name)}"/><g:set var="language" value="${user.getSetting(UserSettings.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de','Language')).value}"/><g:set var="grailsApplication" bean="grailsApplication" /><g:set var="surveyUrl" value="${survey?.surveyConfigs[0]?.pickAndChoose ? "/myInstitution/surveyInfosIssueEntitlements/" : "/myInstitution/surveyInfos/"}"/>
${message(code: 'email.text.title', locale: language)} ${userName},

${message(code: 'email.survey.text', locale: language)}
${survey?.name} (<g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${survey?.startDate}"/> - <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${survey?.endDate}"/>)

${message(code: 'email.survey.text2', locale: language)}
${grailsApplication.config.grails.serverURL+surveyUrl+survey?.id}


${message(code: 'email.text.end', locale: language)}
${message(code: 'email.survey.owner', locale: language)}
${survey?.owner?.name}

${message(code: 'email.text.autogeneric', locale: language)}