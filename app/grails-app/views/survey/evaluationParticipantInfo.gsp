<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.currentSubscriptions.label', default: 'Current Subscriptions')}</title>
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

<br>

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

<semui:form>

    <g:each in="${surveyResult}" var="config" >

        <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.get(config.key)}"/>

        <h4 class="ui left aligned icon header">
            <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(participant)?.id}">
                ${surveyConfig.getConfigNameShort()}
            </g:link>
            <semui:totalNumber total="${config?.value?.size()}"/></h4>


        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <g:each in="${config.value.groupBy {
                    it?.type.id
                }.sort{it?.value?.type?.name}}" var="property">
                    <th>
                        <g:set var="surveyProperty" value="${SurveyProperty.get(property.key)}"/>
                        ${surveyProperty?.getI10n('name')}

                        <g:if test="${surveyProperty?.getI10n('explain')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty?.getI10n('explain')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                    </th>
                </g:each>
            </tr>
            </thead>
            <g:each in="${config.value.groupBy { it?.participant.id }}" var="result" status="i">
                <tr>
                    <g:each in="${result.value.sort{it?.type?.name}}" var="resultProperty">
                        <td>
                            <g:set var="surveyOrg"
                                   value="${com.k_int.kbplus.SurveyOrg.findBySurveyConfigAndOrg(resultProperty?.surveyConfig, participant)}"/>

                            <g:if test="${!surveyOrg?.existsMultiYearTerm()}">

                                <g:if test="${resultProperty?.type?.type == Integer.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="intValue"/>
                                </g:if>
                                <g:elseif test="${resultProperty?.type?.type == String.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="stringValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty?.type?.type == BigDecimal.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="text" field="decValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty?.type?.type == Date.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="date" field="dateValue"/>
                                </g:elseif>
                                <g:elseif test="${resultProperty?.type?.type == URL.toString()}">
                                    <semui:xEditable owner="${resultProperty}" type="url" field="urlValue"
                                                     overwriteEditable="${overwriteEditable}"
                                                     class="la-overflow la-ellipsis"/>
                                    <g:if test="${resultProperty.value}">
                                        <semui:linkIcon/>
                                    </g:if>
                                </g:elseif>
                                <g:elseif test="${resultProperty?.type?.type == RefdataValue.toString()}">
                                    <semui:xEditableRefData owner="${resultProperty}" type="text" field="refValue"
                                                            config="${resultProperty.type?.refdataCategory}"/>
                                </g:elseif>
                                <g:if test="${resultProperty?.comment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${resultProperty?.comment}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </g:if>
                            <g:else>

                                <g:message code="surveyOrg.perennialTerm.available"/>

                                <g:if test="${resultProperty?.comment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${resultProperty?.comment}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>

                            </g:else>
                        </td>
                    </g:each>
                </tr>
            </g:each>
        </table>

    </g:each>

</semui:form>

</body>
</html>
