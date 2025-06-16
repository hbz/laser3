<%@ page import="de.laser.survey.SurveySubscriptionResult; de.laser.storage.RDConstants; de.laser.survey.SurveyPackageResult; de.laser.survey.SurveyVendorResult; de.laser.survey.SurveyOrg; de.laser.addressbook.Contact; de.laser.survey.SurveyResult; de.laser.survey.SurveyPersonResult; de.laser.survey.SurveyConfigProperties; de.laser.storage.PropertyStore; de.laser.storage.RDStore; de.laser.config.ConfigMapper; de.laser.properties.PropertyDefinition; de.laser.UserSetting; de.laser.*; de.laser.base.AbstractPropertyWithCalculatedLastUpdated;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style>
    table {
        border-collapse: collapse;
        border-spacing: 0;
    }

    td {
        padding: 10px 5px;
        border-style: solid;
        border-width: 1px;
        overflow: hidden;
        word-break: normal;
    }

    th {;
        padding: 10px 5px;
        border-style: solid;
        border-width: 1px;
        overflow: hidden;
        word-break: normal;
    }
    </style>
</head>

<body>

<g:set var="userName" value="${user ? raw(user.getDisplayName()) : 'User'}"/>
<g:set var="orgName" value="${raw(org.name)}"/>
<g:set var="language"
       value="${user && user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)) ? user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString() : 'de'}"/>
<g:set var="grailsApplication" bean="grailsApplication"/>
<g:set var="surveyUrl" value="${"/myInstitution/surveyInfos/${survey.id}?surveyConfigID=${survey.surveyConfigs[0].id}"}"/>

<g:set var="surveyOrg" value="${SurveyOrg.findBySurveyConfigAndOrg(survey.surveyConfigs[0], org)}"/>

<g:set var="surveyPersons"
       value="${SurveyPersonResult.executeQuery('select spr.person from SurveyPersonResult spr where spr.surveyPerson = true and spr.participant = :participant and spr.surveyConfig = :surveyConfig', [participant: org, surveyConfig: survey.surveyConfigs[0]])}"/>
<g:set var="surveyContactMails" value="${Contact.executeQuery("select c.content from Person p " +
        "join p.contacts c where p in (:persons) and c.contentType = :type",
        [persons: surveyPersons, type: RDStore.CCT_EMAIL])}"/>

<g:message code="email.text.title" locale="${language}"/> ${userName},
<br/>
<br/>
<g:if test="${survey.type.id == RDStore.SURVEY_TYPE_RENEWAL.id}">
    ${message(code: 'email.survey.participation.finish.renewal.text', locale: language, args: [survey.name, formatDate(format: message(code: 'default.date.format.notime'), date: survey.startDate), formatDate(format: message(code: 'default.date.format.notime'), date: survey.endDate)])}
    <br/>
    ${message(code: 'email.survey.participation.finish.renewal.text2', locale: language)}
    <br/>
</g:if>
<g:elseif test="${survey.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id}">
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text', locale: language, args: [survey.name, formatDate(format: message(code: 'default.date.format.notime'), date: survey.startDate), formatDate(format: message(code: 'default.date.format.notime'), date: survey.endDate)])}
    <br/>
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text2', locale: language)}
    <br/>
    <br/>
</g:elseif>
<g:else>
    ${message(code: 'email.survey.participation.finish.text', locale: language, args: [survey.name, formatDate(format: message(code: 'default.date.format.notime'), date: survey.startDate), formatDate(format: message(code: 'default.date.format.notime'), date: survey.endDate)])}
    <br/>
    <br/>
</g:else>

<g:if test="${surveyResults}">
    <strong><g:message code="email.surveyResult.text" locale="${language}"/>:</strong><br/><br/>
    <table>
        <thead>
        <tr>
            <th><g:message code="sidewide.number" locale="${language}"/></th>
            <th><g:message code="surveyProperty.label" locale="${language}"/></th>
            <th><g:message code="surveyResult.result" locale="${language}"/></th>
            <th>
                <g:message code="surveyResult.participantComment" locale="${language}"/>
            </th>
            <th>
                <g:message code="surveyResult.commentOnlyForOwner" locale="${language}"/>
            </th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">
            <tr>
                <td>
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult.type.getI10n('name', language)}
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
    <br/>
    <br/>
</g:if>

<g:if test="${survey.surveyConfigs[0].pickAndChoose}">
    <strong>${survey.type.getI10n('value', language)}</strong>:<br/>
    <g:set var="subscriberSub" value="${survey.surveyConfigs[0].subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)}"/>
    <g:set var="sumListPriceSelectedIEsEUR"
           value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_EUR)}"/>
    <g:set var="sumListPriceSelectedIEsUSD"
           value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_USD)}"/>
    <g:set var="sumListPriceSelectedIEsGBP"
           value="${surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0], RDStore.CURRENCY_GBP)}"/>

    <g:message code="email.survey.finish.selection.text"
               locale="${language}"/> ${surveyService.countIssueEntitlementsByIEGroup(subscriberSub, survey.surveyConfigs[0])}:
    <br/>
    <br/>
    <g:message code="tipp.price.listPrice" locale="${language}"/>:
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

    <br/>
    <br/>
</g:if>


<g:if test="${survey.surveyConfigs[0].subscriptionSurvey}">
    <g:set var="subs"
           value="${SurveySubscriptionResult.executeQuery('select ssr.subscription from SurveySubscriptionResult ssr where ssr.participant = :participant and ssr.surveyConfig = :surveyConfig', [participant: org, surveyConfig: survey.surveyConfigs[0]])}"/>
    <strong><g:message code="surveySubscriptions.selectedSubscriptions" locale="${language}"/>:</strong><br/>
    <g:each in="${subs}" var="sub">${sub.name}</g:each>
    <br/>
    <br/>
</g:if>
<g:if test="${survey.surveyConfigs[0].vendorSurvey}">
    <g:set var="vendors"
           value="${SurveyVendorResult.executeQuery('select svr.vendor from SurveyVendorResult svr where svr.participant = :participant and svr.surveyConfig = :surveyConfig', [participant: org, surveyConfig: survey.surveyConfigs[0]])}"/>
    <strong><g:message code="surveyVendors.selectedVendor" locale="${language}"/>:</strong><br/>
    <g:each in="${vendors}" var="vendor">${vendor.name}</g:each>
    <br/>
    <br/>
</g:if>
<g:if test="${survey.surveyConfigs[0].packageSurvey}">
    <g:set var="pkgs"
           value="${SurveyPackageResult.executeQuery('select spr.pkg from SurveyPackageResult spr where spr.participant = :participant and spr.surveyConfig = :surveyConfig', [participant: org, surveyConfig: survey.surveyConfigs[0]])}"/>
    <strong><g:message code="surveyPackages.selectedPackages" locale="${language}"/>:</strong><br/>
    <g:each in="${pkgs}" var="pkg"> - ${pkg.name}  <br/></g:each>
    <br/>
    <br/>
</g:if>

<strong>${RDStore.PRS_FUNC_SURVEY_CONTACT.getI10n('value', language)}:</strong><br/>
<g:if test="${surveyContactMails}">
    E-Mails: ${surveyContactMails.join('; ')}<br/>
    <br/>
</g:if>
<g:each in="${surveyPersons}" var="person">
    Name: ${person.title} ${person.first_name} ${person.middle_name} ${person.last_name} <br/>
    <g:each in="${person.contacts}" var="contact">
        <g:message code="contact" locale="${language}"/> (${contact.contentType ? contact.contentType.getI10n('value', language) :''}): ${contact.content}
        <br/>
    </g:each>
    <br/>
</g:each>
<br/>
<br/>

<g:if test="${survey.surveyConfigs[0].invoicingInformation}">
    <g:set var="billingPersons"
           value="${SurveyPersonResult.executeQuery('select spr.person from SurveyPersonResult spr where spr.billingPerson = true and spr.participant = :participant and spr.surveyConfig = :surveyConfig', [participant: org, surveyConfig: survey.surveyConfigs[0]])}"/>
    %{--<g:set var="billingContactEmails" value="${Contact.executeQuery("select c.content from Person p " +
            "join p.contacts c where p in (:persons) and c.contentType = :type",
            [persons: billingPersons, type: RDStore.CCT_EMAIL])}"/>--}%
    <strong>${RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value', language)}:</strong><br/>
   %{-- <g:if test="${billingContactEmails}">
        E-Mail: ${billingContactEmails.join('; ')}<br/>
        <br/>
    </g:if>--}%
    <g:each in="${billingPersons}" var="person">
        Name: ${person.title} ${person.first_name} ${person.middle_name} ${person.last_name} <br/>
        <g:each in="${person.contacts}" var="contact">
            <g:message code="contact" locale="${language}"/> (${contact.contentType ? contact.contentType.getI10n('value', language) :''}): ${contact.content}
            <br/>
        </g:each>
        <br/>
    </g:each>
    <br/>
    <br/>
    <strong>${RDStore.ADDRESS_TYPE_BILLING.getI10n('value', language)}:</strong><br/>
    <g:if test="${surveyOrg.address}">
        ${surveyOrg.address.getAddressForExport()}
    </g:if>
    <br/>
    <br/>
    <strong><g:message code="surveyOrg.eInvoice.label" locale="${language}"/>:</strong><br/>
    <g:message code="surveyOrg.eInvoicePortal.label" locale="${language}"/>: ${surveyOrg.eInvoicePortal ? surveyOrg.eInvoicePortal.getI10n('value', language) : ''}<br/>
    <g:message code="surveyOrg.eInvoiceLeitwegId.label" locale="${language}"/>: ${surveyOrg.eInvoiceLeitwegId}<br/>
    <g:message code="surveyOrg.eInvoiceLeitkriterium.label" locale="${language}"/>: ${surveyOrg.eInvoiceLeitkriterium}<br/>
    <g:message code="surveyOrg.peppolReceiverId.label" locale="${language}"/>: ${surveyOrg.peppolReceiverId}<br/>
    <br/>
    <br/>
</g:if>

<g:if test="${survey.type.id == RDStore.SURVEY_TYPE_RENEWAL.id}">
    ${message(code: 'email.survey.participation.finish.renewal.text3', locale: language)}
    <br/>
    <br/>
    ${message(code: 'email.survey.participation.finish.renewal.text4', locale: language, args: [generalContactsEMails.join(';')])}
    <br/>
    <br/>
</g:if>
<g:elseif test="${survey.type.id == RDStore.SURVEY_TYPE_SUBSCRIPTION.id}">
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text3', locale: language)}
    <br/>
    <br/>
    ${message(code: 'email.survey.participation.finish.subscriptionSurvey.text4', locale: language, args: [generalContactsEMails.join(';')])}
    <br/>
    <br/>
</g:elseif>
<br/>
<br/>
<g:message code="email.survey.finish.url" locale="${language}"/><br/>
${ConfigMapper.getConfig('grails.serverURL', String) + surveyUrl}
<br/>
<br/>
${message(code: 'email.text.end', locale: language)}
<br/>
${message(code: 'email.survey.owner', locale: language)}
<br/>
${survey.owner.name}
<br/>
<br/>
<g:message code="email.profile.settings" locale="${language}"/>
<g:render template="/mailTemplates/html/signature"/>
</body>
</html>
