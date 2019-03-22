<g:if test="${surveyInfo}">
    <semui:card message="showSurveyInfo.step.first.title" class=" ">
        <div class="content">
            <dl>
                ${message(code: 'surveyInfo.status.label', default: 'Survey Status')}
                <dd>${surveyInfo.status?.getI10n('value')}</dd>
            </dl>
            <dl>
                ${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}
                <dd>${surveyInfo.name}</dd>
            </dl>
            <dl>
                ${message(code: 'surveyInfo.startDate.label')}
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.startDate ?: null}"/></dd>
            </dl>
            <dl>
                ${message(code: 'surveyInfo.endDate.label')}
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.endDate ?: null}"/></dd>
            </dl>

            <dl>
                ${message(code: 'surveyInfo.type.label')}
                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
            </dl>

        </div>

    </semui:card>
</g:if>

<g:if test="${surveyConfigs}">
    <semui:card message="surveyInfo.property" class="">
        <div class="content">
            <g:each in="${surveyConfigs}" var="config" status="i">

                <div class="ui small feed content la-js-dont-hide-this-card">
                    <!--<div class="event">-->

                    <div class="ui grid summary">
                        <div class="twelve wide column">
                            <g:if test="${config?.type == 'Subscription'}">
                                <g:link controller="subscriptionDetails" action="show"
                                        id="${config?.subscription?.id}">${config?.subscription?.name}</g:link>
                                <br>${config?.subscription?.startDate ? '(' : ''}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${config?.subscription?.startDate}"/>
                                ${config?.subscription?.endDate ? '-' : ''}
                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                              date="${config?.subscription?.endDate}"/>
                                ${config?.subscription?.startDate ? ')' : ''}
                            </g:if>

                            <g:if test="${config?.type == 'SurveyProperty'}">
                                ${config?.surveyProperty?.getI10n('name')}
                            </g:if>
                        </div>

                        <div class="center aligned four wide column">
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}
                        </div>
                    </div>
                </div>
            </g:each>
        </div>

    </semui:card>
</g:if>
