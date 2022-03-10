<%@ page import="de.laser.License; de.laser.properties.SubscriptionProperty; de.laser.properties.LicenseProperty; de.laser.SurveyConfig; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.interfaces.CalculatedType; de.laser.helper.RDStore; de.laser.AuditConfig; de.laser.Subscription" %>
<laser:serviceInjection/>

<g:set var="overwriteEditable" value="${false}"/>
<thead>
<tr>
    <th class="four wide  aligned">${key}</th>
    <th class="five wide center aligned">
        <div class="la-copyElements-th-flex-container">
            <div class="la-copyElements-th-flex-item">
                <g:if test="${sourceObject && propBinding && propBinding.get(sourceObject)?.isVisibleForConsortiaMembers}">
                    <g:if test="${sourceObject}"><g:link
                            controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="show"
                            id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if><span
                        class="ui blue tag label">${message(code: 'financials.isVisibleForSubscriber')}</span>
                </g:if>
                <g:else>
                    <g:if test="${sourceObject}"><g:link
                            controller="${sourceObject.getClass().getSimpleName().toLowerCase()}" action="show"
                            id="${sourceObject.id}">${sourceObject.dropdownNamingConvention()}</g:link></g:if>
                </g:else>
            </div>
        </div>
    </th>
    <g:if test="${isConsortialObjects}">
    %{--th HEREDITY--}%
        <th class="center aligned">
            <g:message code="copyElementsIntoObject.audit"/>
        </th>
    </g:if>
%{--th ACTION--}%
    <th class="center aligned">
        <input type="checkbox" class="select-all" onclick="JSPC.app.selectAllTake(this);" checked="${true}"/>
    </th>
    <g:if test="${!copyObject}">
        <th class="six wide center aligned">
            <div class="la-copyElements-th-flex-container">
                <div class="la-copyElements-th-flex-item">
                    <g:if test="${targetObject && propBinding && propBinding.get(targetObject)?.isVisibleForConsortiaMembers}">
                        <g:if test="${targetObject}"><g:link
                                controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="show"
                                id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if><span
                            class="ui blue tag label">${message(code: 'financials.isVisibleForSubscriber')}</span>
                    </g:if>
                    <g:else>
                        <g:if test="${targetObject}"><g:link
                                controller="${targetObject.getClass().getSimpleName().toLowerCase()}" action="show"
                                id="${targetObject.id}">${targetObject.dropdownNamingConvention()}</g:link></g:if>
                    </g:else>
                </div>
            </div>
        </th>
    %{--th DELETE:--}%
        <th>
            <g:if test="${targetObject}">
                <input class="setDeletionConfirm" type="checkbox" data-action="delete" class="select-all" onclick="JSPC.app.selectAllDelete(this);"/>
            </g:if>
        </th>
    </g:if>
</tr>
</thead>
<tbody>
<g:each in="${group}" var="prop">
    <% PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey()) %>
    <g:set var="propValues" value="${prop.getValue()}"/>
    <% List propValuesForSourceSub = propValues.get(sourceObject) %>
    <% List propValuesForTargetSub = propValues.get(targetObject) %>
    <%

        boolean showProp = false
        if (accessService.checkPerm('ORG_INST')) {
            if (((propValuesForSourceSub?.size() > 0) && (propValuesForSourceSub[0].tenant?.id == contextOrg.id || (sourceObject._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_LOCAL && (!propValuesForSourceSub[0].tenant || propValuesForSourceSub[0].isPublic)))) ||
                    ((propValuesForTargetSub?.size() > 0) && (propValuesForTargetSub[0].tenant?.id == contextOrg.id || (targetObject._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_LOCAL && (!propValuesForTargetSub[0].tenant || propValuesForTargetSub[0].isPublic))))) {
                showProp = true
            }
        }

        if (accessService.checkPerm('ORG_CONSORTIUM')) {
            if (((propValuesForSourceSub?.size() > 0) && (propValuesForSourceSub[0].tenant?.id == contextOrg.id || !propValuesForSourceSub[0].tenant || propValuesForSourceSub[0].isPublic || (propValuesForSourceSub[0].hasProperty('instanceOf') && propValuesForSourceSub[0].instanceOf && AuditConfig.getConfig(propValuesForSourceSub[0].instanceOf)))) ||
                    ((propValuesForTargetSub?.size() > 0) && (propValuesForTargetSub[0].tenant?.id == contextOrg.id || !propValuesForTargetSub[0].tenant) || propValuesForTargetSub[0].isPublic || (propValuesForTargetSub[0].hasProperty('instanceOf') && propValuesForTargetSub[0].instanceOf && AuditConfig.getConfig(propValuesForTargetSub[0].instanceOf)))) {
                showProp = true
            }
        }
    %>


    <g:if test="${showProp}">
        <tr>
            <td>
                <g:if test="${propKey.getI10n('expl') != null && !propKey.getI10n('expl').contains(' °')}">
                    ${propKey.getI10n('name')}
                    <g:if test="${propKey.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${propKey.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </g:if>
                <g:else>
                    ${propKey.getI10n('name')}
                </g:else>
                <g:if test="${propKey.multipleOccurrence}">
                    <span data-position="top right" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                        <i class="redo icon orange"></i>
                    </span>
                </g:if>
            </td>

        %{--SOURCE-SUBSCRIPTION--}%
            <g:if test="${(sourceObject instanceof Subscription || sourceObject instanceof License)}">
                <td class="center aligned">
                    <g:if test="${propValues.containsKey(sourceObject)}">
                        <g:each var="propValue" in="${propValuesForSourceSub}">
                            <div class="la-copyElements-flex-container la-multi-sources la-colorCode-source">
                                <div class="la-copyElements-flex-item">
                                    <g:if test="${propValue.getValue() != "" && propValue.getValue() != null}">
                                        <g:if test="${propValue.type.isIntegerType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="intValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:if>
                                        <g:elseif test="${propValue.type.isStringType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="stringValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="decValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isDateType()}">
                                            <semui:xEditable owner="${propValue}" type="date" field="dateValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isURLType()}">
                                            <semui:xEditable owner="${propValue}" type="url" field="urlValue"
                                                             overwriteEditable="${overwriteEditable}"
                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${propValue.value}">
                                                <semui:linkIcon href="${propValue.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${propValue}" type="text" field="refValue"
                                                                    config="${propValue.type.refdataCategory}"
                                                                    overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:else>
                                            <div>
                                                ${propValue.value}
                                            </div>
                                        </g:else>
                                        &nbsp;
                                        <g:if test="${propValue.note}">
                                            <div class="ui circular large label la-long-tooltip la-popup-tooltip la-delay"
                                                 data-content="${propValue.note}">${message(code: 'copyElementsIntoObject.note.short')}</div>
                                        </g:if>

                                        <g:if test="${sourceObject instanceof License && propValue.paragraph}">
                                            <div class="ui circular large label la-long-tooltip la-popup-tooltip la-delay"
                                                 data-content="${propValue.paragraph}">§</div><br />
                                        </g:if>

                                        <g:if test="${propValues.get(sourceObject)?.size() > 1}"><br /></g:if>
                                    </g:if>
                                    <g:else>
                                        <span data-position="top left" class="la-popup-tooltip la-delay"
                                              data-content="${message(code: "default.compare.propertyValueNotSet")}"/><i
                                            class="close icon"></i></span>
                                    </g:else>
                                </div>
                            </div>

                        </g:each>
                    </g:if>
                    <g:else>
                        <div class="la-copyElements-flex-item">
                            <a class="ui circular label la-popup-tooltip la-delay"
                               data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                        </div>
                    </g:else>
                </td>
                <g:if test="${isConsortialObjects}">
                %{--HEREDITY--}%
                    <td class="center aligned">
                        <g:if test="${propValues.containsKey(sourceObject)}">
                            <g:each var="propValue" in="${propValuesForSourceSub}">
                                <g:if test="${propValue instanceof SubscriptionProperty || propValue instanceof LicenseProperty}">
                                    <div class="ui checkbox la-toggle-radio la-inherit">
                                        <input type="checkbox" name="auditProperties"
                                               value="${propValue.id}" ${!AuditConfig.getConfig(propValue) ? '' : 'checked'}/>
                                    </div>
                                    <br />
                                </g:if>
                            </g:each>
                        </g:if>
                    </td>
                </g:if>
            </g:if>
            <g:elseif test="${(targetObject instanceof SurveyConfig)}">
                <td class="center aligned">
                    <g:if test="${propValues.containsKey(sourceObject)}">
                        <g:each var="propValue" in="${propValuesForSourceSub}">
                            <div class="la-copyElements-flex-container la-multi-sources la-colorCode-source">
                                <div class="la-copyElements-flex-item">
                                    ${PropertyDefinition.getLocalizedValue(propValue.type)}
                                    <g:if test="${propValue.isRefdataValueType()}">
                                        <g:set var="refdataValues" value="${[]}"/>
                                        <g:each in="${RefdataCategory.getAllRefdataValues(propValue.refdataCategory)}"
                                                var="refdataValue">
                                            <g:if test="${refdataValue.getI10n('value')}">
                                                <g:set var="refdataValues"
                                                       value="${refdataValues + refdataValue.getI10n('value')}"/>
                                            </g:if>
                                        </g:each>
                                        <br />
                                        (${refdataValues.join('/')})
                                    </g:if>
                                </div>
                            </div>
                        </g:each>
                    </g:if>
                    <g:else>
                        <div class="la-copyElements-flex-item">
                            <a class="ui circular label la-popup-tooltip la-delay"
                               data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                        </div>
                    </g:else>
                </td>
            </g:elseif>
        %{--ACTION--}%
            <td class="center aligned">
                <g:if test="${propValues.containsKey(sourceObject)}">
                    <g:each var="propValue" in="${propValuesForSourceSub}">
                    %{--COPY:--}%
                        <g:if test="${propValues.containsKey(sourceObject)}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="copyObject.takeProperty" class="bulkcheck" data-action="copy"
                                            data-multipleOccurrence="${propKey.multipleOccurrence}"
                                            value="${genericOIDService.getOID(propValue)}" checked="${true}"/>
                            </div>
                            <br />
                        </g:if>
                    </g:each>
                </g:if>
            </td>
            <g:if test="${!copyObject}">
            %{--TARGET-SUBSCRIPTION--}%
                <g:if test="${(targetObject instanceof Subscription || targetObject instanceof License)}">
                    <td>
                        <g:if test="${targetObject && propValues.containsKey(targetObject)}">
                            <g:each var="propValue" in="${propValuesForTargetSub}">
                                <div class="la-copyElements-flex-container la-colorCode-target la-multi-sources">
                                    <div class="la-copyElements-flex-item">
                                        <g:if test="${propValue.getValue() != "" && propValue.getValue() != null}">
                                        <g:if test="${propValue.type.isIntegerType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="intValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:if>

                                        <g:elseif test="${propValue.type.isStringType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="stringValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${propValue}" type="text" field="decValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isDateType()}">
                                            <semui:xEditable owner="${propValue}" type="date" field="dateValue"
                                                             overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isURLType()}">
                                            <semui:xEditable owner="${propValue}" type="url" field="urlValue"
                                                             overwriteEditable="${overwriteEditable}"
                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${propValue.value}">
                                                <semui:linkIcon href="${propValue.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${propValue.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${propValue}" type="text" field="refValue"
                                                                    config="${propValue.type.refdataCategory}"
                                                                    overwriteEditable="${overwriteEditable}"/>
                                        </g:elseif>
                                        <g:else>
                                            <div>
                                                ${propValue.value}
                                            </div>
                                        </g:else>
                                        &nbsp;
                                        <g:if test="${propValue.note}">
                                            <div class="ui circular large label la-long-tooltip la-popup-tooltip la-delay"
                                                 data-content="${propValue.note}">${message(code: 'copyElementsIntoObject.note.short')}</div>
                                        </g:if>

                                            <g:if test="${targetObject instanceof License && propValue.paragraph}">
                                                <div class="ui circular large label la-long-tooltip la-popup-tooltip la-delay"
                                                     data-content="${propValue.paragraph}">§</div><br />
                                            </g:if>
                                            <g:if test="${propValues.get(targetObject)?.size() > 1}"><br /></g:if>
                                        </g:if>
                                        <g:else>
                                            <span data-position="top left" class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: "default.compare.propertyValueNotSet")}"/><i
                                                class="close icon"></i></span>
                                        </g:else>
                                    </div>


                                    <g:if test="${isConsortialObjects}">
                                        <g:if test="${propValue instanceof SubscriptionProperty || propValue instanceof LicenseProperty}">
                                            <g:if test="${targetObject instanceof License}">
                                                <g:set var="consortium"
                                                       value="${targetObject.getLicensingConsortium()}"/>
                                            </g:if>
                                            <g:elseif test="${targetObject instanceof Subscription}">
                                                <g:set var="consortium" value="${targetObject.getConsortia()}"/>
                                                <g:set var="atSubscr"
                                                       value="${targetObject._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION}"/>
                                            </g:elseif>
                                            <g:if test="${(propValue.hasProperty('instanceOf') && propValue.instanceOf && AuditConfig.getConfig(propValue.instanceOf)) || AuditConfig.getConfig(propValue)}">
                                                <g:if test="${targetObject.isSlaved}">
                                                    <span class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'property.audit.target.inherit.auto')}"
                                                          data-position="top right"><i class="icon grey la-thumbtack-regular"></i>
                                                    </span>
                                                </g:if>
                                                <g:else>
                                                    <span class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'property.audit.target.inherit')}"
                                                          data-position="top right"><i class="icon thumbtack grey"></i>
                                                    </span>
                                                </g:else>
                                            </g:if>
                                            <g:elseif test="${propValue.tenant?.id == consortium?.id && atSubscr}">
                                                <span class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'property.notInherited.fromConsortia')}"
                                                      data-position="top right"><i
                                                        class="large icon cart arrow down grey"></i></span>
                                            </g:elseif>

                                        </g:if>
                                    </g:if>
                                    <g:if test="${propValues.get(targetObject)?.size() > 1}"><br /></g:if>
                                </div>
                            </g:each>
                        </g:if>
                        <g:else>
                            <div class="la-copyElements-flex-item">
                                <a class="ui circular label la-popup-tooltip la-delay"
                                   data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong>
                                </a>
                            </div>
                        </g:else>
                    </td>
                </g:if>
                <g:elseif test="${(targetObject instanceof SurveyConfig)}">
                    <td class="center aligned">
                        <g:if test="${targetObject && propValues.containsKey(targetObject)}">
                            <g:each var="propValue" in="${propValuesForTargetSub}">
                                <div class="la-copyElements-flex-container la-colorCode-target la-multi-sources">
                                    <div class="la-copyElements-flex-item">
                                        ${PropertyDefinition.getLocalizedValue(propValue.type)}
                                        <g:if test="${propValue.isRefdataValueType()}">
                                            <g:set var="refdataValues" value="${[]}"/>
                                            <g:each in="${RefdataCategory.getAllRefdataValues(propValue.refdataCategory)}"
                                                    var="refdataValue">
                                                <g:if test="${refdataValue.getI10n('value')}">
                                                    <g:set var="refdataValues"
                                                           value="${refdataValues + refdataValue.getI10n('value')}"/>
                                                </g:if>
                                            </g:each>
                                            <br />
                                            (${refdataValues.join('/')})
                                        </g:if>
                                    </div>
                                </div>
                            </g:each>
                        </g:if>
                        <g:else>
                            <div class="la-copyElements-flex-item">
                                <a class="ui circular label la-popup-tooltip la-delay"
                                   data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong>
                                </a>
                            </div>
                        </g:else>
                    </td>
                </g:elseif>
            %{--DELETE:--}%
                <td>
                    <g:if test="${targetObject && propValues.containsKey(targetObject)}">
                        <g:each var="propValue" in="${propValuesForTargetSub}">
                            <div class="ui checkbox la-toggle-radio la-noChange setDeletionConfirm">
                                <g:checkBox class="bulkcheck" name="copyObject.deleteProperty"
                                            data-multipleOccurrence="${propKey.multipleOccurrence}"
                                            value="${genericOIDService.getOID(propValue)}" data-action="delete"
                                            checked="${false}"/>
                            </div>
                            <br />
                        </g:each>
                    </g:if>
                </td>
            </g:if>
        </tr>
    </g:if>
</g:each>
</tbody>
