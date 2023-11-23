<%@ page import="de.laser.survey.SurveyConfig;de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org; de.laser.DocContext; de.laser.survey.SurveyOrg;" %>
<laser:serviceInjection/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <asset:stylesheet src="semantic.css"/>

    <style type="text/css">
    body {
        font-size: 16px;
        font-family: sans-serif;
    }

    h1 > span {
        font-size: 80%;
        color: rgba(0, 0, 0, 0.35);
    }

    table {
        border-spacing: 0;
        border-collapse: collapse;
        border-width: 0;
        width: 100%;
    }

    table thead tr {
        text-align: left;
        color: #FFFFFF;
        background-color: #2471a3;
        border: 1px solid black;
        border-collapse: collapse;
    }

    table thead tr th {
        padding: 0.6em 0.6em;
        border-color: #2471a3;
        border: 1px solid black;
        border-collapse: collapse;
    }

    table tbody tr.even {
        background-color: #F6F7F7;
    }

    table tbody tr td {
        padding: 0.35em 0.6em;
        border: 1px solid black;
        border-collapse: collapse;
    }

    dl dt {
        font-weight: bold;
    }

    div.withBorder{
        padding: 0.35em 0.6em;
    }
    </style>

</head>
<body>
<h1>
    LAS:eR <g:message code="survey.label"/>: ${surveyInfo.name}
    <%
        String date = "("
        if (surveyInfo.startDate) {
            date += g.formatDate(date: surveyInfo.startDate, format: message(code: 'default.date.format.notime'))
        }
        if (surveyInfo.endDate) {
            date += '–'
            date += g.formatDate(date: surveyInfo.endDate, format: message(code: 'default.date.format.notime'))
        }
        date += ")"
        println(date)
    %>
</h1>

<div style="border: 1px solid black;">
    <g:if test="${participant}">
        <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
        <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>
        <table>
            <tbody>
            <tr>
                <td>
                    <p><strong><g:link controller="organisation" action="show" id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            %{-- EXPORT PROBLEM @ laser:render in call stack - ERMS-5437 --}%
                            <g:render template="/templates/cpa/person_details"  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                        </g:each>

                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </g:if>

    <dl>
        <dt>
            <g:message code="surveyInfo.comment.label"/>
        </dt>
        <dd>
            <g:if test="${surveyInfo.comment}">
                ${surveyInfo.comment}
            </g:if>
            <g:else>
                <g:message code="surveyConfigsInfo.comment.noComment"/>
            </g:else>
        </dd>
    </dl>
</div>



<g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

    %{-- EXPORT PROBLEM @ laser:render in call stack - ERMS-5437 --}%
    <g:render template="/templates/survey/export/subscriptionSurveyPDF" model="[surveyConfig       : surveyConfig,
                                                                                costItemSums       : costItemSums,
                                                                                subscription       : subscription,
                                                                                visibleOrgRelations: visibleOrgRelations,
                                                                                surveyResults      : surveyResults,
                                                                                institution: institution,
                                                                                ownerView: ownerView,
                                                                                contextOrg: contextOrg]"/>
</g:if>

<g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

    <g:render template="/templates/survey/export/generalSurveyPDF" model="[surveyConfig       : surveyConfig,
                                                                           costItemSums       : costItemSums,
                                                                           subscription       : surveyConfig.subscription,
                                                                           tasks              : tasks,
                                                                           visibleOrgRelations: visibleOrgRelations,
                                                                           institution: institution,
                                                                           ownerView: ownerView]"/>
</g:if>

<g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

    %{-- EXPORT PROBLEM @ laser:render in call stack - ERMS-5437 --}%
    <g:render template="/templates/survey/export/subscriptionSurveyPDF" model="[surveyConfig       : surveyConfig,
                                                                                costItemSums       : costItemSums,
                                                                                subscription       : subscription,
                                                                                visibleOrgRelations: visibleOrgRelations,
                                                                                surveyResults      : surveyResults,
                                                                                institution: institution,
                                                                                ownerView: ownerView,
                                                                                contextOrg: contextOrg]"/>

    <g:render template="/templates/survey/export/entitlementSurveyPDF" model="[countSelectedIEs: countSelectedIEs,
                                                                               sumListPriceSelectedIEsEUR: sumListPriceSelectedIEsEUR,
                                                                               sumListPriceSelectedIEsUSD: sumListPriceSelectedIEsUSD,
                                                                               sumListPriceSelectedIEsGBP: sumListPriceSelectedIEsGBP,
                                                                               countCurrentPermanentTitles: countCurrentPermanentTitles,
                                                                               surveyConfig       : surveyConfig,
                                                                               subscription       : subscription,
                                                                               subscriber: subscriber]"/>

</g:if>
</body>
</html>

