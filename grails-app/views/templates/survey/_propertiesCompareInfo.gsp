<%@ page import="de.laser.ui.Icon; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.*; de.laser.AuditConfig;" %>
<laser:serviceInjection/>

<g:set var="overwriteEditable" value="${false}"/>

<g:if test="${customProperties?.size() > 0}">
    <div class="ui card la-js-responsive-table la-dl-no-table">
        <div class="content">

            <table class="ui celled table la-js-responsive-table la-table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription.properties')}</th>
                    <th>${message(code: 'survey.subscription.propertiesChange.now')}</th>
                    <th>${message(code: 'survey.subscription.propertiesChange.future')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${customProperties}" var="prop">
                    <% PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey()) %>
                    <tr>
                        <td>
                            ${propKey.getI10n("name")}
                            <g:if test="${propKey.multipleOccurrence}">
                                <span data-position="top right" class="la-popup-tooltip"
                                      data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                                    <i class="${Icon.PROP.MULTIPLE}"></i>
                                </span>
                            </g:if>
                        </td>
                        <g:set var="propValues" value="${prop.getValue()}"/>
                        <g:if test="${(subscriptionParent && subscriptionParent.id in propValues.owner.id)}">
                            <td class="center aligned">
                                <div class="ui relaxed divided list">
                                    <g:each var="subProperty" in="${propValues}">
                                        <g:if test="${subProperty.owner.id == subscriptionParent.id}">
                                            <div class="item">
                                            %{--SOURCE-SUBSCRIPTION--}%

                                                <g:if test="${subProperty.type.isIntegerType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text" field="intValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:if>
                                                <g:elseif test="${subProperty.type.isStringType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text"
                                                                     field="stringValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isBigDecimalType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text" field="decValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isDateType()}">
                                                    <ui:xEditable owner="${subProperty}" type="date"
                                                                     field="dateValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isURLType()}">
                                                    <ui:xEditable owner="${subProperty}" type="url" field="urlValue"
                                                                     overwriteEditable="${overwriteEditable}"
                                                                     class="la-overflow la-ellipsis"/>
                                                    <g:if test="${subProperty.value}">
                                                        <ui:linkWithIcon href="${subProperty.value}"/>
                                                    </g:if>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isRefdataValueType()}">
                                                    <ui:xEditableRefData owner="${subProperty}" type="text"
                                                                            field="refValue"
                                                                            config="${subProperty.type.refdataCategory}"
                                                                            overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:else>
                                                    <div>
                                                        ${subProperty.value}
                                                    </div>
                                                </g:else>
                                                <g:if test="${subProperty?.note}">
                                                    <div class="ui circular label la-long-tooltip la-popup-tooltip la-delay"
                                                         data-content="${subProperty?.note}">${message(code: 'copyElementsIntoObject.note.short')}</div>
                                                </g:if>
                                            </div>
                                        </g:if>

                                    </g:each>
                                </div>
                            </td>
                        </g:if>
                        <g:if test="${!(subscriptionParent && subscriptionParent.id in propValues.owner.id)}">
                            <td class="center aligned">
                                <a class="ui circular label la-popup-tooltip"
                                   data-content="<g:message
                                           code="default.compare.propertyNotSet"/>"><strong>–</strong>
                                </a>
                            </td>
                        </g:if>
                        <g:if test="${(successorSubscriptionParent && successorSubscriptionParent.id in propValues.owner.id)}">
                            <td class="center aligned">
                                <div class="ui relaxed divided list">
                                    <g:each var="subProperty" in="${propValues}">
                                    %{--TARGET-SUBSCRIPTION--}%
                                        <g:if test="${subProperty.owner.id == successorSubscriptionParent.id}">
                                            <div class="item">
                                                <g:if test="${subProperty.type.isIntegerType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text" field="intValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:if>

                                                <g:elseif test="${subProperty.type.isStringType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text"
                                                                     field="stringValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isBigDecimalType()}">
                                                    <ui:xEditable owner="${subProperty}" type="text" field="decValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isDateType()}">
                                                    <ui:xEditable owner="${subProperty}" type="date"
                                                                     field="dateValue"
                                                                     overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isURLType()}">
                                                    <ui:xEditable owner="${subProperty}" type="url" field="urlValue"
                                                                     overwriteEditable="${overwriteEditable}"
                                                                     class="la-overflow la-ellipsis"/>
                                                    <g:if test="${subProperty.value}">
                                                        <ui:linkWithIcon/>
                                                    </g:if>
                                                </g:elseif>
                                                <g:elseif test="${subProperty.type.isRefdataValueType()}">
                                                    <ui:xEditableRefData owner="${subProperty}" type="text"
                                                                            field="refValue"
                                                                            config="${subProperty.type.refdataCategory}"
                                                                            overwriteEditable="${overwriteEditable}"/>
                                                </g:elseif>
                                                <g:else>
                                                    <div>
                                                        ${subProperty.value}
                                                    </div>
                                                </g:else>
                                                <g:if test="${subProperty?.note}">
                                                    <div class="ui circular label la-long-tooltip la-popup-tooltip la-delay"
                                                         data-content="${subProperty?.note}">${message(code: 'copyElementsIntoObject.note.short')}</div>
                                                </g:if>
                                            </div>

                                        </g:if>
                                    </g:each>
                                </div>
                            </td>
                        </g:if>
                        <g:if test="${!(successorSubscriptionParent && successorSubscriptionParent.id in propValues.owner.id)}">
                            <td class="center aligned">
                                <a class="ui circular label la-popup-tooltip"
                                   data-content="<g:message
                                           code="default.compare.propertyNotSet"/>"><strong>–</strong>
                                </a>
                            </td>
                        </g:if>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</g:if>
