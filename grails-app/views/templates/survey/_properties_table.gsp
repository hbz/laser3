<%@ page import="de.laser.RefdataValue; de.laser.survey.SurveyOrg; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition; de.laser.RefdataCategory; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties;" %>
<laser:serviceInjection/>

<g:if test="${controllerName == 'survey' && actionName == 'show'}">

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.name')}</th>
            <th>${message(code: 'surveyProperty.expl.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'surveyProperty.mandatoryProperty')}</th>
            <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING && surveyProperties}">
                <th>${message(code: 'default.actions.label')}</th>
            </g:if>
        </tr>
        </thead>

        <tbody>
        <g:each in="${surveyProperties}" var="surveyPropertyConfig" status="i">
            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    ${surveyPropertyConfig.surveyProperty.getI10n('name')}

                    <g:if test="${surveyPropertyConfig.surveyProperty.tenant?.id == contextService.getOrg().id}">
                        <i class='shield alternate icon'></i>
                    </g:if>

                    <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <td>
                    <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                        ${surveyPropertyConfig.surveyProperty.getI10n('expl')}
                    </g:if>
                </td>
                <td>
                    ${PropertyDefinition.getLocalizedValue(surveyPropertyConfig.surveyProperty.type)}
                    <g:if test="${surveyPropertyConfig.surveyProperty.isRefdataValueType()}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${RefdataCategory.getAllRefdataValues(surveyPropertyConfig.surveyProperty.refdataCategory)}"
                                var="refdataValue">
                            <g:set var="refdataValues"
                                   value="${refdataValues + refdataValue?.getI10n('value')}"/>
                        </g:each>
                        <br/>
                        (${refdataValues.join('/')})
                    </g:if>
                </td>

                <td>
                    <g:set var="surveyPropertyMandatoryEditable"
                           value="${(editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                                   (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyPropertyConfig.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                    <g:form action="setSurveyPropertyMandatory" method="post" class="ui form"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyPropertyConfig.id]">

                        <div class="ui checkbox">
                            <input type="checkbox"
                                   onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' : ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                   name="mandatoryProperty" ${surveyPropertyConfig.mandatoryProperty ? 'checked' : ''}>
                        </div>
                    </g:form>
                </td>
                <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                        SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyPropertyConfig.surveyProperty)
                        && ((PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id != surveyPropertyConfig.surveyProperty.id) || surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL)}">
                    <td>
                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyPropertyConfig.surveyProperty.getI10n('name')])}"
                                data-confirm-term-how="delete"
                                controller="survey" action="deleteSurveyPropFromConfig"
                                id="${surveyPropertyConfig.id}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash alternate outline icon"></i>
                        </g:link>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
        <g:if test="${controllerName == 'survey' && actionName == 'show' && editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
            <g:set var="selectableProperties"
                   value="${pdg ? surveyConfig.getSelectablePropertiesByPropDefGroup(pdg) : (selectablePrivateProperties ? surveyConfig.getPrivateSelectableProperties() : surveyConfig.getOrphanedSelectableProperties())}"/>
            <tfoot>
            <tr>
                <td colspan="6">
                    <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                        <g:hiddenField name="id" value="${surveyInfo.id}"/>
                        <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
                        <div class="two fields" style="margin-bottom:0">
                            <div class="field" style="margin-bottom:0">
                                <ui:dropdown name="selectedProperty"
                                             class="la-filterPropDef"
                                             from="${selectableProperties}"
                                             iconWhich="shield alternate"
                                             optionKey="${{ "${it.id}" }}"
                                             optionValue="${{ it.getI10n('name') }}"
                                             noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                             required=""/>
                            </div>

                            <div class="field" style="margin-bottom:0">
                                <input type="submit" value="${message(code: 'default.button.add.label')}"
                                       class="ui button js-wait-wheel"/>
                            </div>
                        </div>
                    </g:form>

                </td>
            </tr>
            </tfoot>
        </g:if>

    </table>
</g:if><g:else>
        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                        ${message(code: 'surveyResult.participantComment')}
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
                <th>
                    <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentOnlyForParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyProperties}" var="surveyResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult.type.getI10n('name')}

                        <g:if test="${surveyResult.type.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${surveyResult.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:set var="surveyConfigProperties"
                               value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                        <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                        <g:if test="${surveyResult.type.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                    var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                            <br/>
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>
                    <g:set var="surveyOrg"
                           value="${SurveyOrg.findBySurveyConfigAndOrg(surveyResult.surveyConfig, institution)}"/>

                    <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyOrg && surveyOrg.existsMultiYearTerm()}">
                        <td>
                            <g:message code="surveyOrg.perennialTerm.available"/>
                        </td>
                        <td>

                        </td>
                        <td>

                        </td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:if test="${surveyResult.type.isIntegerType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                            </g:if>
                            <g:elseif test="${surveyResult.type.isStringType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isDateType()}">
                                <ui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isURLType()}">
                                <ui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                              overwriteEditable="${overwriteEditable}"
                                              class="la-overflow la-ellipsis"/>
                                <g:if test="${surveyResult.urlValue}">
                                    <ui:linkWithIcon href="${surveyResult.urlValue}"/>
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isRefdataValueType()}">

                                <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyResult.type == PropertyStore.SURVEY_PROPERTY_PARTICIPATION && surveyResult.owner?.id != contextService.getOrg().id}">
                                    <ui:xEditableRefData
                                            data_confirm_tokenMsg="${surveyOrg.orgInsertedItself ? message(code: 'survey.participationProperty.confirmation2') : message(code: 'survey.participationProperty.confirmation')}"
                                            data_confirm_term_how="ok"
                                            class="js-open-confirm-modal-xEditableRefData"
                                            data_confirm_value="${RefdataValue.class.name}:${RDStore.YN_NO.id}"
                                            owner="${surveyResult}"
                                            field="refValue" type="text"
                                            id="participation"
                                            config="${surveyResult.type.refdataCategory}"/>
                                </g:if>
                                <g:else>
                                    <ui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                         config="${surveyResult.type.refdataCategory}"/>
                                </g:else>
                            </g:elseif>
                        </td>
                        <td>
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                        </td>
                        <td>
                            <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                                <ui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                            </g:if>
                            <g:else>
                                <ui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                            </g:else>
                        </td>
                    </g:else>

                </tr>
            </g:each>
        </table>
</g:else>