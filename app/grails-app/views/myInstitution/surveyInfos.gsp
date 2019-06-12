<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="survey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'survey.label')} - ${surveyInfo.name}

<g:if test="${surveyInfo.startDate || surveyInfo.endDate}"></g:if>
(<g:formatDate formatName="default.date.format.notime"
               date="${surveyInfo.startDate}"/>
-
<g:formatDate formatName="default.date.format.notime"
              date="${surveyInfo.endDate}"/>)
</h1>

<br>

<semui:messages data="${flash}"/>

<br>

<g:if test="${ownerId}">
    <g:set var="choosenOrg" value="${com.k_int.kbplus.Org.findById(ownerId)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <semui:form>
        <h3><g:message code="surveyInfo.owner.label"/>:</h3>

        <table class="ui table la-table la-table-small">
            <tbody>
            <tr>
                <td>
                    <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${choosenOrgCPAs}">
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <g:render template="/templates/cpa/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </semui:form>
</g:if>

<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfig.label')} <semui:totalNumber
        total="${surveyResults?.size()}"/></h2>
<br>

<semui:form>

    <h3 class="ui left aligned icon header">${message(code: 'subscription.plural')} <semui:totalNumber
            total="${com.k_int.kbplus.SurveyConfig.findAllByIdInListAndType(surveyResults.collect {
                it.key
            }, 'Subscription').size()}"/></h3>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <th>${message(code: 'surveyProperty.subName')}</th>
            <th>${message(code: 'surveyProperty.subProvider')}</th>
            <th>${message(code: 'surveyProperty.plural.label')}</th>
            <th></th>

        </tr>

        </thead>

        <g:each in="${surveyResults}" var="config" status="i">

            <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.get(config.key)}"/>
            <g:if test="${surveyConfig?.type == 'Subscription'}">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        <g:link controller="subscription" action="show"
                                id="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution)?.id}">${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution)?.name}</g:link>

                    </td>
                    <td>
                        <g:each in="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution).providers}"
                                var="org">
                            <g:link controller="organisation" action="show" id="${org.id}">${org.name}</g:link><br/>
                        </g:each>
                        <g:each in="${surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution).agencies}"
                                var="org">
                            <g:link controller="organisation" action="show"
                                    id="${org.id}">${org.name} (${message(code: 'default.agency.label', default: 'Agency')})</g:link><br/>
                        </g:each>

                    </td>
                    <td>
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <g:link action="surveyConfigsInfo" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular label">${surveyConfig?.surveyProperties?.size()}</div>
                            </g:link>
                        </g:if>
                    </td>
                    <td>

                        <g:link action="surveyConfigsInfo" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon button"><i
                                class="tasks icon"></i></g:link>

                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>
</semui:form>
<br>
<br>

<semui:form>
    <h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.propertys')} <semui:totalNumber
            total="${com.k_int.kbplus.SurveyConfig.findAllByIdInListAndType(surveyResults.collect {
                it.key
            }, 'SurveyProperty').size()}"/></h3>
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>
            <th>${message(code: 'surveyProperty.name')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>${message(code: 'surveyResult.commentParticipant')}</th>

        </tr>

        </thead>

        <g:each in="${surveyResults}" var="config" status="j">

            <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.get(config?.key)}"/>

            <g:if test="${surveyConfig?.type == 'SurveyProperty'}">
                <tr>
                    <td class="center aligned">
                        ${j + 1}
                    </td>
                    <td>
                        <g:if test="${surveyConfig?.type == 'SurveyProperty'}">
                            ${surveyConfig?.surveyProperty?.getI10n('name')}

                            <g:if test="${surveyConfig?.surveyProperty?.getI10n('explain')}">
                                <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${surveyConfig?.surveyProperty?.getI10n('explain')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>

                        </g:if>

                    </td>
                    <td>
                        ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyConfig?.type)}

                        <g:if test="${surveyConfig?.surveyProperty}">
                            <br>
                            <b>${message(code: 'surveyProperty.type.label')}: ${surveyConfig?.surveyProperty?.getLocalizedType()}

                        </g:if>

                    </td>
                    <td>
                        <g:if test="${config.value[0]?.type?.type == Integer.toString()}">
                            <semui:xEditable owner="${config.value[0]}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${config.value[0]?.type?.type == String.toString()}">
                            <semui:xEditable owner="${config.value[0]}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${config.value[0]?.type?.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${config.value[0]}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${config.value[0]?.type?.type == Date.toString()}">
                            <semui:xEditable owner="${config.value[0]}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${config.value[0]?.type?.type == URL.toString()}">
                            <semui:xEditable owner="${config.value[0]}" type="url" field="urlValue"
                                             overwriteEditable="${overwriteEditable}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${config.value[0].value}">
                                <semui:linkIcon/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${config.value[0]?.type?.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${config.value[0]}" type="text" field="refValue"
                                                    config="${config.value[0].type?.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <td>
                        ${config.value[0]?.comment}
                    </td>
                </tr>
            </g:if>
        </g:each>
    </table>
</semui:form>

<g:link class="ui button" controller="myInstitution" action="surveyInfoFinish" id="${surveyInfo.id}">
    <g:message code="surveyResult.finish"/>
</g:link>
<br>
<br>
</body>
</html>
