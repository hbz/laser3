<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated; com.k_int.kbplus.UserSettings;" %><laser:serviceInjection /><%@ page Content-type: text/plain; charset=utf-8; %><g:set var="userName" value="${raw(user.getDisplayName())}"/><g:set var="orgName" value="${raw(org.name)}"/><g:set var="language" value="${user.getSetting(UserSettings.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de',de.laser.helper.RDConstants.LANGUAGE)).value}"/><g:set var="grailsApplication" bean="grailsApplication" /><g:set var="surveyUrl" value="${survey.surveyConfigs[0].pickAndChoose ? "/myInstitution/surveyInfosIssueEntitlements/${survey.surveyConfigs[0].id}" : "/myInstitution/surveyInfos/${survey.id}?surveyConfigID=${survey.surveyConfigs[0].id}"}"/>
${message(code: 'email.text.title', locale: language)} ${userName},

${message(code: 'email.survey.text', locale: language)}
${escapeService.replaceUmlaute(survey.name)} (<g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${survey.startDate}"/> - <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${survey.endDate}"/>)

${message(code: 'email.survey.text2', locale: language)}
${grailsApplication.config.grails.serverURL+raw(surveyUrl)}


${message(code: 'email.text.end', locale: language)}
${message(code: 'email.survey.owner', locale: language)}
${survey.owner.name}

${message(code: 'email.text.autogeneric', locale: language)}