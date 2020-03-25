<%@ page import="de.laser.helper.RDStore; com.k_int.properties.PropertyDefinition;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>



<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'myinst.currentSubscriptions.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>


<semui:messages data="${flash}"/>

<g:if test="${participant}">
    <g:set var="choosenOrg" value="${com.k_int.kbplus.Org.findById(participant.id)}" />
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}" />

    <table class="ui table la-table la-table-small">
        <tbody>
        <tr>
            <td>
                <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                ${choosenOrg?.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}" />
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <g:render template="/templates/cpa/person_details" model="${[person: gcp, tmplHideLinkToAddressbook: true]}" />
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
            </td>
        </tr>
        </tbody>
    </table>
</g:if>

<g:if test="${surveyConfig.type == "Subscription"}">

    <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig: surveyConfig,
                                                    subscriptionInstance: subscriptionInstance,
                                                    visibleOrgRelations: visibleOrgRelations,
                                                    surveyResults: surveyResults]"/>
</g:if>

<g:if test="${surveyConfig.type == "GeneralSurvey"}">

    <g:render template="/templates/survey/generalSurvey" model="[surveyConfig: surveyConfig,
                                               costItemSums: costItemSums,
                                               subscriptionInstance: surveyConfig.subscription,
                                               tasks: tasks,
                                               visibleOrgRelations: visibleOrgRelations,
                                               properties: properties]"/>
</g:if>

</body>
</html>
