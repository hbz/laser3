<%@page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore;com.k_int.kbplus.*; de.laser.AuditConfig" %>
<laser:serviceInjection/>

<g:set var="overwriteEditable" value="${false}"/>
<thead>
    <tr>
        <th class="four wide  aligned">${key}</th>
        <th class="five wide center aligned">
            <div class="la-copyElements-th-flex-container">
                <div class="la-copyElements-th-flex-item">
                    <g:if test="${propBinding && propBinding.get(sourceSubscription)?.isVisibleForConsortiaMembers}">
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.dropdownNamingConvention()}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
                    </g:if>
                    <g:else>
                        <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.dropdownNamingConvention()}</g:link></g:if>
                    </g:else>
                </div>
                <div>
                    <input type="checkbox" class="select-all"  onclick="selectAllTake(this);" checked="${true}" />
                </div>
            </div>
        </th>
        <th class="six wide center aligned">
            <div class="la-copyElements-th-flex-container">
                <div class="la-copyElements-th-flex-item">
                    <g:if test="${propBinding && propBinding.get(targetSubscription)?.isVisibleForConsortiaMembers}">
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.dropdownNamingConvention()}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
                    </g:if>
                    <g:else>
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.dropdownNamingConvention()}</g:link></g:if>
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
<g:each in="${group.sort{it.key}}" var="prop">
    <% PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey()) %>
    <tr>
        <td>
            ${propKey.getI10n("name")}
            <g:if test="${propKey.multipleOccurrence}">
                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
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
                                <div class="ui circular label la-long-tooltip la-popup-tooltip la-delay" data-content="${propValue?.note}">Anm.</div>
                            </g:if>

                            <g:if test="${propValues.get(sourceSubscription)?.size() > 1}"><br></g:if>
                        </div>
                        <g:if test="${propValue instanceof com.k_int.kbplus.SubscriptionCustomProperty}">
                            <div class="la-copyElements-flex-item right aligned wide column">
                                <g:message code="subscription.details.copyElementsIntoSubscription.audit"/>:&nbsp;
                                    <input type="checkbox" name="auditProperties" value="${propValue.id}" ${!AuditConfig.getConfig(propValue) ? '' : 'checked' }/>

                            </div>
                        </g:if>

                        %{--COPY:--}%
                        <g:if test="${propValues.containsKey(sourceSubscription)}">
                            <div class="ui checkbox la-toggle-radio la-replace">
                                <g:checkBox name="subscription.takeProperty" class="bulkcheck" data-action="copy" data-multipleOccurrence="${propKey.multipleOccurrence}" value="${genericOIDService.getOID(propValue)}" checked="${true}" />
                            </div>
                        </g:if>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <div class="la-copyElements-flex-item">
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
                                <div class="ui circular label la-long-tooltip la-popup-tooltip la-delay"  data-content="${propValue?.note}">Anm.</div>
                            </g:if>
                            <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                        </div>
                        <g:if test="${propValue instanceof com.k_int.kbplus.SubscriptionCustomProperty}">
                            <div class="la-copyElements-flex-item">
                                <g:if test="${! AuditConfig.getConfig(propValue)}">
                                    <i class="icon la-thumbtack slash la-js-editmode-icon"></i>
                                </g:if>
                                <g:else>
                                    <i class="thumbtack icon la-js-editmode-icon"></i>
                                </g:else>
                            </div>
                        </g:if>
                        %{--DELETE:--}%
                        <div class="ui checkbox la-toggle-radio la-noChange">
                            <g:checkBox class="bulkcheck"  name="subscription.deleteProperty" data-multipleOccurrence="${propKey.multipleOccurrence}" value="${genericOIDService.getOID(propValue)}" data-action="delete" checked="${false}"/>
                        </div>
                        <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                    </div>
                </g:each>
            </g:elseif>
            <g:else>
                <div class="la-copyElements-flex-item">
                    <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                </div>
            </g:else>
        </td>
    </tr>
</g:each>
</tbody>
