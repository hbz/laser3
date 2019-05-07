<g:if test="${surveyInfo && actionName != 'showSurveyInfo'}">
    <semui:card message="showSurveyInfo.step.first.title" class=" ">
        <div class="content">
            <dl>
                <dt>${message(code: 'surveyInfo.status.label', default: 'Survey Status')}</dt>
                <dd>${surveyInfo.status?.getI10n('value')}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</dt>
                <dd>${surveyInfo.name}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.startDate.label')}</dt>
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.startDate ?: null}"/></dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.endDate.label')}</dt>
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.endDate ?: null}"/></dd>
            </dl>

            <dl>
                <dt>${message(code: 'surveyInfo.type.label')}</dt>
                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.comment.label')}</dt>
                <dd>${surveyInfo?.comment}</dd>
            </dl>

        </div>

    </semui:card>
</g:if>

<g:if test="${surveyConfigs && actionName != 'showSurveyConfig'}">
    <semui:card message="surveyInfo.property" class="">
        <div class="content">
            <g:each in="${surveyConfigs}" var="config" status="i">

                <div class="ui small feed content la-js-dont-hide-this-card">

                    <div class="ui grid summary">
                        <div class="twelve wide column">
                            <g:if test="${config?.type == 'Subscription'}">
                                <g:link controller="subscription" action="show"
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
