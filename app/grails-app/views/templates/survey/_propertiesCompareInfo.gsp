<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore;com.k_int.kbplus.*; de.laser.AuditConfig;" %>
<laser:serviceInjection/>

<g:set var="overwriteEditable" value="${false}"/>

<g:if test="${customProperties?.size() > 0}">
    <div class="ui card la-dl-no-table">
        <div class="content">
            <h5 class="ui header">
                ${message(code: 'survey.subscription.propertiesChange')}
            </h5>
        </div>

        <table class="ui celled table la-table">
            <thead>
            <tr>
                <th>${message(code: 'subscription.properties')}</th>
                <th>${message(code: 'renewalWithSurvey.parentSubscription')}</th>
                <th>${message(code: 'renewalWithSurvey.parentSuccessorSubscription')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${customProperties}" var="prop">
                <% PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey()) %>
                <tr>
                    <td>
                        ${propKey.getI10n("name")}
                        <g:if test="${propKey.multipleOccurrence}">
                            <span data-position="top right" class="la-popup-tooltip la-delay"
                                  data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                                <i class="redo icon orange"></i>
                            </span>
                        </g:if>
                    </td>
                    <g:set var="propValues" value="${prop.getValue()}"/>
                    <g:each var="subProperty" in="${propValues}">
                    %{--SOURCE-SUBSCRIPTION--}%
                        <g:if test="${subProperty.owner == subscriptionInstance}">
                            <td class="center aligned">
                                <g:if test="${subProperty.type.type == Integer.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="intValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:if>
                                <g:elseif test="${subProperty.type.type == String.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="stringValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == BigDecimal.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="decValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == Date.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="date" field="dateValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == URL.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="url" field="urlValue"
                                                     overwriteEditable="${overwriteEditable}"
                                                     class="la-overflow la-ellipsis"/>
                                    <g:if test="${subProperty.value}">
                                        <semui:linkIcon href="${subProperty.value}"/>
                                    </g:if>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == RefdataValue.toString()}">
                                    <semui:xEditableRefData owner="${subProperty}" type="text" field="refValue"
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
                                         data-content="${subProperty?.note}">Anm.</div>
                                </g:if>
                            </td>
                        </g:if>

                    </g:each>
                    <g:if test="${!(subscriptionInstance in propValues.owner)}">
                        <td class="center aligned">
                            <a class="ui circular label la-popup-tooltip la-delay"
                               data-content="<g:message
                                       code="default.compare.propertyNotSet"/>"><strong>–</strong>
                            </a>
                        </td>
                    </g:if>


                    <g:each var="subProperty" in="${propValues}">
                    %{--TARGET-SUBSCRIPTION--}%
                        <g:if test="${subProperty.owner == successorSubscription}">
                            <td class="center aligned">
                                <g:if test="${subProperty.type.type == Integer.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="intValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:if>

                                <g:elseif test="${subProperty.type.type == String.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="stringValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == BigDecimal.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="text" field="decValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == Date.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="date" field="dateValue"
                                                     overwriteEditable="${overwriteEditable}"/>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == URL.toString()}">
                                    <semui:xEditable owner="${subProperty}" type="url" field="urlValue"
                                                     overwriteEditable="${overwriteEditable}"
                                                     class="la-overflow la-ellipsis"/>
                                    <g:if test="${subProperty.value}">
                                        <semui:linkIcon/>
                                    </g:if>
                                </g:elseif>
                                <g:elseif test="${subProperty.type.type == RefdataValue.toString()}">
                                    <semui:xEditableRefData owner="${subProperty}" type="text" field="refValue"
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
                                         data-content="${subProperty?.note}">Anm.</div>
                                </g:if>
                            </td>
                        </g:if>
                    </g:each>
                    <g:if test="${!(successorSubscription in propValues.owner)}">
                        <td class="center aligned">
                            <a class="ui circular label la-popup-tooltip la-delay"
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
</g:if>