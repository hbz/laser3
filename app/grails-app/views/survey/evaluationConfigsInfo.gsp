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

                                <b>${message(code: 'surveyProperty.type.label')}: ${surveyConfig?.surveyProperty?.getLocalizedType()}

                                </b>
                            </g:if>

                        </dd>

                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.orgs.label')}</dt>
                        <dd>
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular label">${surveyConfig?.orgs?.size() ?: 0}</div>
                            </g:link>
                        </dd>

                    </dl>

                    <dl>
                        <dt class="control-label">${message(code: 'surveyConfig.documents.label')}</dt>
                        <dd>
                            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular label">${surveyConfig?.documents?.size()}</div>
                            </g:link>
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
<g:if test="${surveyConfig?.type == 'Subscription'}">

    <div>
        <semui:form>

            <h3 class="ui left aligned icon header">${message(code: 'surveyProperty.label')} <semui:totalNumber
                    total="${surveyResult?.size()}"/></h3>
            <table class="ui celled sortable table la-table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'surveyProperty.name')}</th>
                    <th>${message(code: 'surveyProperty.type.label')}</th>
                    <th>${message(code: 'surveyParticipants.label')}</th>
                    <th></th>
                </tr>
                </thead>
                <g:each in="${surveyResult}" var="prop" status="i">

                    <g:set var="surveyProperty" value="${com.k_int.kbplus.SurveyProperty.get(prop.key)}"/>

                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <td>
                            ${surveyProperty?.getI10n('name')}

                            <g:if test="${surveyProperty?.getI10n('explain')}">
                                <span class="la-long-tooltip" data-position="right center" data-variation="tiny"
                                      data-tooltip="${surveyProperty?.getI10n('explain')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>

                        </td>
                        <td>
                            ${surveyProperty?.getLocalizedType()}

                        </td>
                        <td>
                            ${prop?.value?.size()}
                        </td>
                        <td>
                            <g:link controller="survey" action="evaluationConfigResult" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig?.id, prop: prop.key]"
                                    class="ui icon button"><i
                                    class="chart bar icon"></i></g:link>

                        </td>
                    </tr>
                </g:each>
            </table>
        </semui:form>
    </div>
</g:if>

</body>
</html>
