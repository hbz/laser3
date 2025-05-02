<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.survey.SurveyOrg; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition; de.laser.RefdataCategory; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties;" %>
<laser:serviceInjection/>

<g:if test="${showSurveyPropertiesForOwer}">

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'surveyProperty.name')}</th>
            <th>${message(code: 'surveyProperty.expl.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'surveyProperty.mandatoryProperty.short')}</th>
            <th>${message(code: 'surveyProperty.propertyOrder')}</th>
            <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING && surveyProperties}">
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
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
                        <i class='${Icon.PROP.IS_PRIVATE}'></i>
                    </g:if>

                    <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
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
                        <g:if test="${surveyPropertyConfig.surveyProperty == PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING}">
                            <g:each in="${[RDStore.INVOICE_PROCESSING_PROVIDER, RDStore.INVOICE_PROCESSING_VENDOR]}" var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                        </g:if>
                        <g:else>
                            <g:each in="${RefdataCategory.getAllRefdataValuesWithOrder(surveyPropertyConfig.surveyProperty.refdataCategory)}" var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                        </g:else>
                        <br/>
                        (${refdataValues.join('/')})
                    </g:if>
                </td>

                <td>
                    <g:set var="surveyPropertyMandatoryEditable"
                           value="${(editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                                   (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyPropertyConfig.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                    <g:form action="actionsForSurveyProperty" method="post" class="ui form"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyPropertyConfig.id, actionForSurveyProperty: 'setSurveyPropertyMandatory']">

                        <div class="ui checkbox">
                            <input type="checkbox"
                                   onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' : ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                   name="mandatoryProperty" ${surveyPropertyConfig.mandatoryProperty ? 'checked' : ''}>
                        </div>
                    </g:form>
                </td>
                <td>
                    <g:if test="${editable}">
                        <g:if test="${i == 1 && surveyProperties.size() == 2}">
                            <ui:remoteLink class="${Btn.MODERN.SIMPLE} compact" action="actionsForSurveyProperty" id="${params.id}" data-update="${props_div}"
                                    params="[actionForSurveyProperty: 'moveUp', surveyPropertyConfigId: surveyPropertyConfig.id, surveyConfigID: surveyConfig.id, surveyPropertiesIDs: surveyProperties.id, props_div: props_div, pdg_id: pdg?.id]"><i class="${Icon.CMD.MOVE_UP}"></i>
                            </ui:remoteLink>
                        </g:if>
                        <g:else>
                            <g:if test="${i > 0}">
                                <ui:remoteLink class="${Btn.MODERN.SIMPLE} compact" action="actionsForSurveyProperty" id="${params.id}" data-update="${props_div}"
                                        params="[actionForSurveyProperty: 'moveUp', surveyPropertyConfigId: surveyPropertyConfig.id, surveyConfigID: surveyConfig.id, surveyPropertiesIDs: surveyProperties.id, props_div: props_div, pdg_id: pdg?.id]"><i class="${Icon.CMD.MOVE_UP}"></i>
                                </ui:remoteLink>
                            </g:if>
                            <g:if test="${i < surveyProperties.size()-1}">
                                <ui:remoteLink class="${Btn.MODERN.SIMPLE} compact" action="actionsForSurveyProperty" id="${params.id}" data-update="${props_div}"
                                        params="[actionForSurveyProperty: 'moveDown', surveyPropertyConfigId: surveyPropertyConfig.id, surveyConfigID: surveyConfig.id, surveyPropertiesIDs: surveyProperties.id, props_div: props_div, pdg_id: pdg?.id]"><i class="${Icon.CMD.MOVE_DOWN}"></i>
                                </ui:remoteLink>
                            </g:if>
                        </g:else>
                    </g:if>

                </td>
                <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                        SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyPropertyConfig.surveyProperty)
                        && ((PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id != surveyPropertyConfig.surveyProperty.id) || surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL)}">
                    <td>
                        <ui:remoteLink class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyPropertyConfig.surveyProperty.getI10n('name')])}"
                                data-confirm-term-how="delete"
                                data-update="${props_div}"
                                controller="survey" action="actionsForSurveyProperty"
                                id="${params.id}"
                                params="[actionForSurveyProperty: 'deleteSurveyPropFromConfig', surveyPropertyConfigId: surveyPropertyConfig.id, surveyConfigID: surveyConfig.id, props_div: props_div, pdg_id: pdg?.id]"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icon.CMD.DELETE}"></i>
                        </ui:remoteLink>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
       %{-- <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
            <g:set var="selectableProperties"
                   value="${pdg ? surveyConfig.getSelectablePropertiesByPropDefGroup(pdg) : (selectablePrivateProperties ? surveyConfig.getPrivateSelectableProperties() : surveyConfig.getOrphanedSelectableProperties())}"/>
            <tfoot>
            <tr>
                <td colspan="6">
                    <ui:remoteForm url="[controller: 'survey', action: 'actionsForSurveyProperty']"
                                   name="survey_prop_add"
                                   class="ui properties form"
                                   data-update="${props_div}">

                        <div class="two fields" style="margin-bottom:0">
                            <div class="field" style="margin-bottom:0">
                                <ui:dropdown name="selectedProperty"
                                             class="la-filterPropDef"
                                             from="${selectableProperties.sort{it.getI10n('name')}}"
                                             iconWhich="${Icon.PROP.IS_PRIVATE}"
                                             optionKey="${{ "${it.id}" }}"
                                             optionValue="${{ it.getI10n('name') }}"
                                             noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                             required=""/>
                            </div>
                            <div class="field" style="margin-bottom:0">
                                <input type="submit" value="${message(code:'default.button.add.label')}" class="${Btn.SIMPLE} js-wait-wheel"/>
                            </div>
                        </div>

                        <g:hiddenField name="id" value="${surveyInfo.id}"/>
                        <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>
                        <g:hiddenField name="actionForSurveyProperty" value="addSurveyPropToConfig"/>
                        <g:hiddenField name="props_div" value="${props_div}"/>
                        <g:if test="${pdg}">
                            <g:hiddenField name="pdg_id" value="${pdg.id}"/>
                        </g:if>
                    </ui:remoteForm>

                </td>
            </tr>
            </tfoot>
        </g:if>--}%

    </table>
</g:if><g:else>
        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'surveyProperty.expl.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyProperty.mandatoryProperty.short')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                        ${message(code: 'surveyResult.participantComment')}
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </g:else>
                </th>
                <th>
                    <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentOnlyForParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </g:else>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyProperties}" var="surveyResult" status="i">
                <g:set var="surveyConfigProperties"
                       value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult.type.getI10n('name')}
                    </td>
                    <td>
                        <g:if test="${surveyResult.type.getI10n('expl')}">
                            ${surveyResult.type.getI10n('expl')}
                        </g:if>
                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                        <g:if test="${surveyResult.type.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:if test="${surveyResult.type == PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING}">
                                <g:each in="${[RDStore.INVOICE_PROCESSING_PROVIDER, RDStore.INVOICE_PROCESSING_VENDOR]}" var="refdataValue">
                                    <g:if test="${refdataValue.getI10n('value')}">
                                        <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                    </g:if>
                                </g:each>
                            </g:if>
                            <g:else>
                                <g:each in="${RefdataCategory.getAllRefdataValuesWithOrder(surveyResult.type.refdataCategory)}" var="refdataValue">
                                    <g:if test="${refdataValue.getI10n('value')}">
                                        <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                    </g:if>
                                </g:each>
                            </g:else>
                            <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                                  data-content="${refdataValues.join('/')}">
                                <i class="${Icon.TOOLTIP.INFO}"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                            <span class="la-popup-tooltip" data-content="${message(code:'surveyProperty.mandatoryProperty.info')}" data-position="bottom center">
                                <i class="${Icon.PROP.MANDATORY} large"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${surveyResult.type.isLongType()}">
                            <ui:xEditable owner="${surveyResult}" type="text" field="longValue"/>
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
                            <g:elseif test="${surveyResult.type == PropertyStore.SURVEY_PROPERTY_INVOICE_PROCESSING}">
                                <ui:xEditableRefData owner="${surveyResult}" type="text" field="refValue" constraint="removeValues_invoiceProcessing"
                                                     config="${surveyResult.type.refdataCategory}"/>
                            </g:elseif>
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
                        <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                        </g:if>
                        <g:else>
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </table>
</g:else>