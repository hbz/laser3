<g:set var="surveyProperties" value="${surveyConfig.surveyProperties}"/>

<semui:form>

    <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <semui:totalNumber
            total="${surveyProperties.size()}"/></h4>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.name')}</th>
            <th>${message(code: 'surveyProperty.expl.label')}</th>
            <th>${message(code: 'surveyProperty.comment.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th></th>
        </tr>
        </thead>

        <tbody>
        <g:each in="${surveyProperties.sort { it.surveyProperty?.name }}" var="surveyProperty" status="i">
            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    ${surveyProperty?.surveyProperty?.getI10n('name')}

                    <g:if test="${surveyProperty?.surveyProperty?.owner?.id == institution?.id}">
                        <i class='shield alternate icon'></i>
                    </g:if>

                    <g:if test="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <td>
                    <g:if test="${surveyProperty?.surveyProperty?.getI10n('expl')}">
                        ${surveyProperty?.surveyProperty?.getI10n('expl')}
                    </g:if>
                </td>
                <td>
                    <g:if test="${surveyProperty?.surveyProperty?.comment}">
                        ${surveyProperty?.surveyProperty?.comment}
                    </g:if>
                </td>
                <td>

                    ${surveyProperty?.surveyProperty?.getLocalizedType()}

                </td>
                <td>
                    <g:if test="${editable && surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING &&
                            com.k_int.kbplus.SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyProperty?.surveyProperty)
                            && (com.k_int.kbplus.SurveyProperty.findByName('Participation')?.id != surveyProperty?.surveyProperty?.id)}">
                        <g:link class="ui icon negative button"
                                controller="survey" action="deleteSurveyPropFromConfig"
                                id="${surveyProperty?.id}">
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <g:if test="${editable && properties && surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING}">
                <td colspan="6">
                    <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                        <g:hiddenField name="id" value="${surveyInfo?.id}"/>
                        <g:hiddenField name="surveyConfigID" value="${surveyConfig?.id}"/>

                        <div class="field required">
                            <label>${message(code: 'surveyConfigs.property')}</label>
                            <semui:dropdown name="selectedProperty"

                                            class="la-filterPropDef"
                                            from="${properties}"
                                            iconWhich="shield alternate"
                                            optionKey="${{ "${it.id}" }}"
                                            optionValue="${{ it.getI10n('name') }}"
                                            noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                            required=""/>

                        </div>
                        <input type="submit" class="ui button"
                               value="${message(code: 'surveyConfigsInfo.add.button')}"/>

                    </g:form>
                </td>
            </g:if>
        </tr>
        </tfoot>

    </table>

</semui:form>