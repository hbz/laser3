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
<semui:surveyStatus object="${surveyInfo}"/>
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
                <th>${message(code: 'surveyProperty.subProviderAgency')}</th>
                <th>${message(code: 'surveyProperty.subStatus')}</th>

                <th>${message(code: 'surveyProperty.plural.label')}</th>
                <th>${message(code: 'surveyConfig.documents.label')}</th>
                <th>${message(code: 'surveyConfig.orgs.label')}</th>
                <th></th>

            </tr>

            </thead>
        <g:set var="surveySubConfigs" value="${surveyConfigs.findAll{it?.type == 'Subscription'}}"/>
            <g:each in="${surveySubConfigs}" var="config" status="i">
                    <tr style="${config?.configFinish ? 'background-color: Lime' : ''}">
                        <td class="center aligned" >
                            <div class="ui label large la-annual-rings">
                                <g:if test="${config?.configOrder > 1}">
                                <g:link action="changeConfigOrder" id="${surveyInfo.id}"
                                        params="[surveyConfigID: config?.id, change: 'up']"><i class="angle up icon"></i></g:link>
                                </g:if>
                                <g:else>
                                    <i class="icon"></i>
                                </g:else>
                                <br>
                                ${config?.configOrder}<br>
                                <g:if test="${config?.configOrder <= surveySubConfigs?.size()-1}">
                                <g:link action="changeConfigOrder" id="${surveyInfo.id}"
                                        params="[surveyConfigID: config?.id, change: 'down']"><i class="angle down icon"></i></g:link>
                                </g:if>
                                <g:else>
                                    <i class="icon"></i>
                                </g:else>
                            </div>
                        </td>
                        <td>
                            <g:link controller="subscription" action="show"
                                    id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>

                        </td>
                        <td>
                            <g:each in="${config?.subscription?.getProviders()+config?.subscription?.getAgencies()}" var="provider">
                                <g:link controller="organisation" action="show" id="${provider?.id}">${provider?.name}</g:link><br>
                            </g:each>
                        </td>
                        <td>
                            ${config?.subscription?.status?.getI10n('value')}
                        </td>
                        <td class="center aligned">
                            <g:if test="${config?.type == 'Subscription'}">
                                <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}"
                                        params="[surveyConfigID: config?.id]" class="ui icon">
                                    <div class="ui circular label">${config?.surveyProperties?.size()}</div>
                                </g:link>
                            </g:if>

                        </td>
                        <td class="center aligned">
                            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon">
                                <div class="ui circular label">${config?.getCurrentDocs()?.size()}</div>
                            </g:link>
                        </td>
                        <td class="center aligned">
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon">
                                <div class="ui circular label">${config?.orgs?.size() ?: 0}</div>
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
                            <g:elseif test="${editable && config?.surveyProperties?.size() > 0}">
                                <span data-position="top right"
                                      data-tooltip="${message(code: 'surveyConfigs.delete.existingProperties')}">
                                    <button class="ui icon button negative" disabled="disabled">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                </span>
                            </g:elseif>
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
                <th>${message(code: 'surveyProperty.name')}</th>
                <th>${message(code: 'surveyProperty.type.label')}</th>
                <th>${message(code: 'surveyConfig.documents.label')}</th>
                <th>${message(code: 'surveyConfig.orgs.label')}</th>
                <th></th>

            </tr>

            </thead>

            <g:set var="surveyPropertyConfigs" value="${surveyConfigs.findAll{it?.type == 'SurveyProperty'}}"/>
            <g:each in="${surveyPropertyConfigs}" var="config" status="i">
                    <tr>
                        <td class="center aligned">
                            <div class="ui label large la-annual-rings">
                                <g:if test="${config?.configOrder-surveySubConfigs.size() > 1}">
                                    <g:link action="changeConfigOrder" id="${surveyInfo.id}"
                                            params="[surveyConfigID: config?.id, change: 'up']"><i class="angle up icon"></i></g:link>
                                </g:if>
                                <g:else>
                                    <i class="icon"></i>
                                </g:else>
                                <br>
                                ${config?.configOrder}<br>
                                <g:if test="${config?.configOrder <= surveySubConfigs?.size()-1}">
                                    <g:link action="changeConfigOrder" id="${surveyInfo.id}"
                                            params="[surveyConfigID: config?.id, change: 'down']"><i class="angle down icon"></i></g:link>
                                </g:if>
                                <g:else>
                                    <i class="icon"></i>
                                </g:else>
                            </div>
                        </td>
                        <td>

                            <g:if test="${config?.type == 'SurveyProperty'}">
                                ${config?.surveyProperty?.getI10n('name')}

                                <g:if test="${config?.surveyProperty?.getI10n('explain')}">
                                    <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${config?.surveyProperty?.getI10n('explain')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </g:if>

                        </td>
                        <td>
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}

                            <g:if test="${config?.surveyProperty}">
                                <br>
                                <b>${message(code: 'surveyProperty.type.label')}: ${config?.surveyProperty?.getLocalizedType()}</b>

                            </g:if>

                        </td>
                        <td class="center aligned">
                            <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon">
                                <div class="ui circular label">${config?.documents?.size()}</div>
                            </g:link>
                        </td>
                        <td class="center aligned">
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                    params="[surveyConfigID: config?.id]" class="ui icon">
                                <div class="ui circular label">${config?.orgs?.size() ?: 0}</div>
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
            </g:each>
        </table>
    </semui:form>

</div>

</body>
</html>
