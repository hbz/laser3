<%@page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore;com.k_int.kbplus.*" %>
<laser:serviceInjection/>
<thead>
    <tr>
        <th class="four wide  aligned">${key}</th>
        <th class="five wide center aligned">
            <div class="la-copyElements-th-flex-container">
                <div class="la-copyElements-th-flex-item"
                    <g:if test="${propBinding && propBinding.get(sourceSubscription)?.visibleForConsortiaMembers}">
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
                    </g:if>
                    <g:else>
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </g:else>
                </div>
                <div>
                    <input type="checkbox" class="select-all"  onclick="selectAllTake(this);" checked="${true}" />
                </div>
            </th>
        <th class="six wide center aligned">
            <div class="la-copyElements-th-flex-container">
                <div class="la-copyElements-th-flex-item"
                    <g:if test="${propBinding && propBinding.get(targetSubscription)?.visibleForConsortiaMembers}">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
                    </g:if>
                    <g:else>
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </g:else>
                </div>
                <div>
                    <g:if test="${targetSubscription}">
                        <input type="checkbox" data-action="delete" class="select-all" onclick="selectAllDelete(this);" />
                    </g:if>
                </div>
            </div>
        </th>
    </tr>
</thead>
<tbody>
<g:each in="${group}" var="prop">
    <% PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey()) %>
    <tr>
        <td>
            ${propKey.getI10n("name")}
            <g:if test="${propKey.multipleOccurrence}">
                <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                    <i class="redo icon orange"></i>
                </span>
            </g:if>
        </td>
        <g:set var="propValues" value="${prop.getValue()}" />

        %{--SOURCE-SUBSCRIPTION--}%
        <td class="center aligned">
            <g:if test="${propValues.containsKey(sourceSubscription)}">
                <% Set propValuesForSourceSub = propValues.get(sourceSubscription) %>
                <g:each var="propValue" in="${propValuesForSourceSub}">
                    <div class="la-copyElements-flex-container">
                        <div class="la-copyElements-flex-item">
                            <g:if test="${propValue.type.type == Integer.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="intValue" overwriteEditable="${overwriteEditable}" />
                            </g:if>
                            <g:elseif test="${propValue.type.type == String.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == BigDecimal.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == Date.toString()}">
                                <semui:xEditable owner="${propValue}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == URL.toString()}">
                                <semui:xEditable owner="${propValue}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                                <g:if test="${propValue.value}">
                                    <semui:linkIcon href="${propValue.value}" />
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == RefdataValue.toString()}">
                                <semui:xEditableRefData owner="${propValue}" type="text" field="refValue" config="${propValue.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:else>
                                <div>
                                    ${propValue.value}
                                </div>
                            </g:else>
                            <g:if test="${propValue?.note}">
                                <div class="ui circular label la-long-tooltip" data-tooltip="${propValue?.note}">Anm.</div>
                            </g:if>
                            <g:if test="${propValues.get(sourceSubscription)?.size() > 1}"><br></g:if>
                        </div>

                        %{--COPY:--}%
                        <g:if test="${propValues.containsKey(sourceSubscription)}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeProperty" class="bulkcheck" data-action="copy"  value="${genericOIDService.getOID(propValue)}" checked="${true}" />
                            </div>
                        </g:if>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <div>
                    <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                </div>
            </g:else>

        </td>



        %{--TARGET-SUBSCRIPTION--}%
        <td>
            <g:if test="${ ! targetSubscription}">
            </g:if>
            <g:elseif test="${propValues.containsKey(targetSubscription)}">
                <% Set propValuesForTargetSub = propValues.get(targetSubscription) %>
                <g:each var="propValue" in="${propValuesForTargetSub}">
                    <div class="la-copyElements-flex-container">
                        <div  class="la-copyElements-flex-item">
                            <g:if test="${propValue.type.type == Integer.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="intValue" overwriteEditable="${overwriteEditable}" />
                            </g:if>

                            <g:elseif test="${propValue.type.type == String.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == BigDecimal.toString()}">
                                <semui:xEditable owner="${propValue}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == Date.toString()}">
                                <semui:xEditable owner="${propValue}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == URL.toString()}">
                                <semui:xEditable owner="${propValue}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                                <g:if test="${propValue.value}">
                                    <semui:linkIcon />
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${propValue.type.type == RefdataValue.toString()}">
                                <semui:xEditableRefData owner="${propValue}" type="text" field="refValue" config="${propValue.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                            </g:elseif>
                             <g:else>
                                 <div>
                                    ${propValue.value}
                                 </div>
                             </g:else>
                            <g:if test="${propValue?.note}">
                                <div class="ui circular label la-long-tooltip"  data-tooltip="${propValue?.note}">Anm.</div>
                            </g:if>
                            <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                        </div>
                        %{--DELETE:--}%
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox class="bulkcheck"  name="subscription.deleteProperty" value="${genericOIDService.getOID(propValue)}" data-action="delete" checked="${false}"/>
                        </div>
                        <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                    </div>
                </g:each>
            </g:elseif>
            <g:else>
                <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
            </g:else>
        </td>


    </tr>
</g:each>
</tbody>
