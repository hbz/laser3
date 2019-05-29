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

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>

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

    <div class="la-inline-lists">
        <div class="ui two stackable cards">
            <div class="ui card la-time-card">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.label')}</dt>
                        <dd>
                            <g:link controller="subscription" action="show" id="${subscriptionInstance?.id}">
                            ${subscriptionInstance?.name}
                           </g:link>
                        </dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.startDate.label')}</dt>
                        <dd><g:formatDate formatName="default.date.format.notime" date="${subscriptionInstance?.startDate}"/></dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.endDate.label')}</dt>
                        <dd><g:formatDate formatName="default.date.format.notime" date="${subscriptionInstance?.endDate}"/></dd>
                    </dl>
                </div>
            </div>

            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.details.status')}</dt>
                        <dd>${subscriptionInstance?.status}</dd>
                        <dd><semui:auditButton auditable="[subscriptionInstance, 'status']"/></dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.details.type')}</dt>
                        <dd>
                            ${subscriptionInstance.type?.getI10n('value')}
                        </dd>
                        <dd><semui:auditButton auditable="[subscriptionInstance, 'type']"/></dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                        <dd>${subscriptionInstance?.form?.getI10n('value')}</dd>
                        <dd><semui:auditButton auditable="[subscriptionInstance, 'form']"/></dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                        <dd>${subscriptionInstance?.resource?.getI10n('value')}</dd>
                        <dd><semui:auditButton auditable="[subscriptionInstance, 'resource']"/></dd>
                    </dl>
                </div>
            </div>
        </div>
    </div>

<div class="la-inline-lists">
    <div class="ui two stackable cards">
        <div class="ui card la-time-card">
            <div class="content">

            </div>
        </div>

        <div class="ui card">
            <div class="content">

            </div>
        </div>
    </div>
</div>


<semui:form>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.label')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th>${message(code: 'surveyResult.result')}</th>
            <th>${message(code: 'surveyResult.commentParticipant')}</th>
        </tr>
        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    ${surveyResult?.type?.getI10n('name')}
                </td>
                <td>
                    ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(surveyResult?.type?.type)}
                    <g:if test="${surveyResult?.type?.type == 'class com.k_int.kbplus.RefdataValue'}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyResult?.type?.refdataCategory)}"
                                var="refdataValue">
                            <g:set var="refdataValues"
                                   value="${refdataValues + refdataValue?.getI10n('value')}"/>
                        </g:each>
                        <br>
                        (${refdataValues.join('/')})
                    </g:if>
                </td>

                <td>
                    <g:if test="${surveyResult.type?.type == Integer.toString()}">
                        <semui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                    </g:if>
                    <g:elseif test="${surveyResult.type?.type == String.toString()}">
                        <semui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type?.type == BigDecimal.toString()}">
                        <semui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type?.type == Date.toString()}">
                        <semui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type?.type == URL.toString()}">
                        <semui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                         overwriteEditable="${overwriteEditable}"
                                         class="la-overflow la-ellipsis"/>
                        <g:if test="${surveyResult.value}">
                            <semui:linkIcon/>
                        </g:if>
                    </g:elseif>
                    <g:elseif test="${surveyResult.type?.type == RefdataValue.toString()}">
                        <semui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                config="${surveyResult.type?.refdataCategory}"/>
                    </g:elseif>
                </td>
                <td>
                    <semui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                </td>
            </tr>
        </g:each>
    </table>
</semui:form>


</div>

</body>
</html>
