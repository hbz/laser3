<h2 class="ui left aligned icon header"><g:message code="surveyEvaluation.surveyConfigs"/><semui:totalNumber
        total="${surveyConfigs?.size()}"/></h2>
<br>




<h3 class="ui left aligned icon header">${message(code: 'subscription.plural')} <semui:totalNumber
        total="${surveyConfigs.findAll { it?.type == 'Subscription' }.size()}"/></h3>

<semui:form>
<table class="ui celled sortable table la-table">
    <thead>
    <tr>
        <th class="center aligned">
            ${message(code: 'surveyConfig.configOrder.label')}
        </th>
        <th>${message(code: 'surveyProperty.subName')}</th>
        <th>${message(code: 'surveyProperty.subProviderAgency')}</th>
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
                    ${config?.configOrder}
                </td>
                <td>
                    <g:link controller="subscription" action="show"
                            id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>

                </td>
                <td>
                    <g:each in="${config?.subscription?.getProviders() + config?.subscription?.getAgencies()}"
                            var="provider">
                        <g:link controller="organisation" action="show"
                                id="${provider?.id}">${provider?.name}</g:link><br>
                    </g:each>
                </td>
                <td class="center aligned">
                    <g:if test="${config?.type == 'Subscription'}">
                        <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo.id}"
                                params="[surveyConfigID: config?.id]" class="ui icon">
                            <div class="ui circular label">${config?.surveyProperties?.size()}</div>
                        </g:link>

                    </g:if>
                </td>
                <td class="center aligned">
                    <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                            params="[surveyConfigID: config?.id]" class="ui icon">
                        <div class="ui circular label"> ${config?.documents?.size()}</div>
                    </g:link>
                </td>
                <td class="center aligned">
                    <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                            params="[surveyConfigID: config?.id]" class="ui icon">
                        <div class="ui circular label">  ${config?.orgs?.size() ?: 0}</div>
                    </g:link>
                </td>
                <td>

                    <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo.id}"
                            params="[surveyConfigID: config?.id]" class="ui icon button"><i
                            class="chart bar icon"></i></g:link>

                </td>
            </tr>
        </g:if>
    </g:each>
</table>
</semui:form>
<br>
<br>

<h3 class="ui left aligned icon header">${message(code: 'surveyConfigs.list.propertys')} <semui:totalNumber
        total="${surveyConfigs.findAll { it?.type == 'SurveyProperty' }.size()}"/></h3>
<semui:form>
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

    <g:each in="${surveyConfigs}" var="config" status="i">
        <g:if test="${config?.type == 'SurveyProperty'}">
            <tr>
                <td class="center aligned">
                    ${config.configOrder}
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
                        <div class="ui circular label"> ${config?.documents?.size()}</div>
                    </g:link>
                </td>
                <td class="center aligned">

                        <div class="ui circular label">  ${config?.orgs?.size() ?: 0}</div>

                </td>
                <td>

                    <g:link controller="survey" action="evaluationConfigResult" id="${surveyInfo.id}"
                            params="[surveyConfigID: config?.id, prop: config?.surveyProperty?.id]" class="ui icon button"><i
                            class="chart bar icon"></i></g:link>

                </td>
            </tr>
        </g:if>
    </g:each>
</table>
</semui:form>

