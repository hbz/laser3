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
</h1>

<g:render template="nav"/>


<semui:messages data="${flash}"/>


<h2><g:message code="surveyEvaluation.surveyConfig.info" args="[surveyConfig?.getConfigNameShort()]"/></h2>

<g:if test="${surveyConfig}">

    <div class="la-inline-lists">
        <div class="ui two stackable cards">

            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.type.label')}</dt>
                        <dd>
                            ${surveyConfig.getTypeInLocaleI10n()}

                            <g:if test="${surveyConfig?.surveyProperty}">

                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(surveyConfig?.surveyProperty?.type)}

                                <g:if test="${surveyConfig?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyConfig?.surveyProperty?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    (${refdataValues.join('/')})
                                </g:if>
                                </b>
                            </g:if>

                        </dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.orgs.label')}</dt>
                        <dd>
                            ${surveyConfig?.orgs?.size() ?: 0}
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.documents.label')}</dt>
                        <dd>
                            ${surveyConfig?.documents?.size()}
                        </dd>

                    </dl>
                </div>
            </div>

            <div class="ui card ">
                <div class="content">
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.header.label')}</dt>
                        <dd>
                            ${surveyConfig?.header}
                        </dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.comment.label')}</dt>
                        <dd>
                            ${surveyConfig?.comment}
                        </dd>

                    </dl>

                </div>
            </div>

        </div>
    </div>
</g:if>

<br>

<div>

    <h3 class="ui left aligned icon header">

        <g:message code="surveyProperty.label"/>:
        ${surveyProperty?.getI10n('name')}

        ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(surveyProperty?.type)}

        <g:if test="${surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
            <g:set var="refdataValues" value="${[]}"/>
            <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyProperty?.refdataCategory)}"
                    var="refdataValue">
                <g:set var="refdataValues"
                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
            </g:each>
            (${refdataValues.join('/')})
        </g:if>
    </h3>


    <semui:form>
        <h4 class="ui left aligned icon header">${message(code: 'surveyParticipants.label')} <semui:totalNumber
                total="${surveyResult?.size()}"/></h4>
        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'org.sortname.label')}</th>
                <th>${message(code: 'org.name.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>${message(code: 'surveyResult.commentParticipant')}</th>
            </tr>
            </thead>
            <g:each in="${surveyResult}" var="result" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${result?.participant?.sortname}
                    </td>
                    <td>
                        <g:link controller="organisation" action="show" id="${result?.participant.id}">
                            ${fieldValue(bean: result?.participant, field: "name")}
                        </g:link>
                    </td>

                    <td>
                        <g:if test="${result.type?.type == Integer.toString()}">
                            <semui:xEditable owner="${result}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${result.type?.type == String.toString()}">
                            <semui:xEditable owner="${result}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${result.type?.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${result}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${result.type?.type == Date.toString()}">
                            <semui:xEditable owner="${result}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${result.type?.type == URL.toString()}">
                            <semui:xEditable owner="${result}" type="url" field="urlValue"
                                             overwriteEditable="${overwriteEditable}"
                                             class="la-overflow la-ellipsis"/>
                            <g:if test="${result.value}">
                                <semui:linkIcon/>
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${result.type?.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${result}" type="text" field="refValue"
                                                    config="${result.type?.refdataCategory}"/>
                        </g:elseif>
                    </td>

                    <td>
                        ${result?.comment}
                    </td>
                </tr>
            </g:each>
        </table>
    </semui:form>
</div>

</body>
</html>
