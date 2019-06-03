<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyConfigs.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>


<br>


<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<div>

    <semui:form>

        <h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.subscriptions')} <semui:totalNumber
                total="${surveyConfigs.findAll { it?.type == 'Subscription' }.size()}"/></h3>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">
                    ${message(code: 'surveyConfig.configOrder.label')}
                </th>
                <th>${message(code: 'surveyProperty.subName')}</th>
                <th>${message(code: 'surveyProperty.subStatus')}</th>
                <th>${message(code: 'surveyProperty.subDate')}</th>
                <th>${message(code: 'surveyProperty.type.label')}</th>
                <th>${message(code: 'surveyProperty.plural.label')}</th>
                <th>${message(code: 'surveyConfig.documents.label')}</th>
                <th>${message(code: 'surveyConfig.orgs.label')}</th>
                <th></th>

            </tr>

            </thead>

            <g:each in="${surveyConfigs}" var="config" status="i">
                <g:if test="${config?.type == 'Subscription'}">
                    <tr>
                        <td class="center aligned">
                            <semui:xEditable owner="${config}" field="configOrder"/>
                        </td>
                        <td>
                            <g:link controller="subscription" action="show"
                                    id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>

                        </td>
                        <td>
                            ${config?.subscription?.status?.getI10n('value')}
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime"
                                          date="${config?.subscription?.startDate}"/><br>
                            <g:formatDate formatName="default.date.format.notime"
                                          date="${config?.subscription?.endDate}"/>
                        </td>
                        <td>
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}

                            <g:if test="${config?.surveyProperty}">
                                <br>
                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(config?.surveyProperty?.type)}</b>

                                <g:if test="${config?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(config?.surveyProperty?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    <br>
                                    (${refdataValues.join('/')})
                                </g:if>
                            </g:if>

                        </td>
                        <td>
                            <g:if test="${config?.type == 'Subscription'}">
                                <div class="ui circular label">${config?.surveyProperties?.size()}</div>
                            </g:if>

                        </td>
                        <td>
                            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="">
                                ${config?.documents?.size()}
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="">
                                ${config?.orgs?.size() ?: 0}
                            </g:link>
                        </td>
                        <td>

                            <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon button"><i
                                    class="write icon"></i></g:link>

                            <g:if test="${editable && config?.getCurrentDocs()}">
                                <span data-position="top right"
                                      data-tooltip="${message(code: 'surveyConfigs.delete.existingDocs')}">
                                    <button class="ui icon button negative" disabled="disabled">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </span>
                            </g:if>
                            <g:elseif test="${editable}">
                                <g:link class="ui icon negative button js-open-confirm-modal"
                                        data-confirm-term-what="Abfrage-Elemente"
                                        data-confirm-term-what-detail="${config?.getConfigNameShort()}"
                                        data-confirm-term-how="delete"
                                        controller="survey" action="deleteSurveyConfig"
                                        id="${config?.id}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:elseif>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </table>
    </semui:form>

    <br>
    <br>


    <semui:form>

        <h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.selecetedPropertys')} <semui:totalNumber
                total="${surveyConfigs.findAll { it?.type == 'SurveyProperty' }.size()}"/></h3>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">
                    ${message(code: 'surveyConfig.configOrder.label')}
                </th>
                <th>${message(code: 'surveyProperty.name.label')}</th>
                <th>${message(code: 'surveyProperty.type.label')}</th>
                <th>${message(code: 'surveyConfig.documents.label')}</th>
                <th>${message(code: 'surveyConfig.orgs.label')}</th>
                <th></th>

            </tr>

            </thead>

            <g:each in="${surveyConfigs}" var="config" status="i">
                <g:if test="${config?.type == 'SurveyProperty'}">
                    <tr>
                        <td class="center aligned">
                            <semui:xEditable owner="${config}" field="configOrder"/>
                        </td>
                        <td>

                            <g:if test="${config?.type == 'SurveyProperty'}">
                                ${config?.surveyProperty?.getI10n('name')}

                                <g:if test="${config?.surveyProperty?.getI10n('explain')}">
                                    <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${config?.surveyProperty??.getI10n('explain')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </g:if>

                        </td>
                        <td>
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}

                            <g:if test="${config?.surveyProperty}">
                                <br>
                                <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(config?.surveyProperty?.type)}</b>

                                <g:if test="${config?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(config?.surveyProperty?.refdataCategory)}"
                                            var="refdataValue">
                                        <g:set var="refdataValues"
                                               value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>
                                    <br>
                                    (${refdataValues.join('/')})
                                </g:if>
                            </g:if>

                        </td>
                        <td>
                            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="">
                                ${config?.documents?.size()}
                            </g:link>
                        </td>
                        <td>
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="">
                                ${config?.orgs?.size() ?: 0}
                            </g:link>
                        </td>
                        <td>

                            <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon button"><i
                                    class="write icon"></i></g:link>

                            <g:if test="${config?.getCurrentDocs()}">
                                <span data-position="top right"
                                      data-tooltip="${message(code: 'surveyConfigs.delete.existingDocs')}">
                                    <button class="ui icon button negative" disabled="disabled">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </span>
                            </g:if>
                            <g:elseif test="${editable}">
                                <g:link class="ui icon negative button js-open-confirm-modal"
                                        data-confirm-term-what="Abfrage-Elemente"
                                        data-confirm-term-what-detail="${config?.getConfigNameShort()}"
                                        data-confirm-term-how="delete"
                                        controller="survey" action="deleteSurveyConfig"
                                        id="${config?.id}">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:elseif>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </table>
    </semui:form>

</div>

</body>
</html>
