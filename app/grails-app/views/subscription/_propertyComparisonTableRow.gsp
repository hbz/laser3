<%@page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore;com.k_int.kbplus.*" %>
<laser:serviceInjection/>
<thead>
    <tr>
        <th class="four wide  aligned">${key}</th>
        <th class="five wide center aligned">
            <g:if test="${propBinding && propBinding.get(sourceSubscription)?.visibleForConsortiaMembers}">
                <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
            </g:if>
            <g:else>
                <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
            </g:else>
        </th>
        <th class="one wide center aligned">
            <i class="ui icon angle double right"></i>
            <input type="checkbox" data-action="copy" onClick="toggleAllCheckboxes(this)" checked/>
        </th>
        <th class="six wide center aligned">
            <g:if test="${propBinding && propBinding.get(targetSubscription)?.visibleForConsortiaMembers}">
                <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
            </g:if>
            <g:else>
                <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
            </g:else>
        </th>
        <th class="one wide center aligned">
            <i class="ui icon trash alternate outline"></i>
            <g:if test="${targetSubscription}">
                <input type="checkbox" data-action="delete" onClick="toggleAllCheckboxes(this)" />
            </g:if>
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
                        ${propValue.value}
                    </g:else>
                    <g:if test="${propValue?.note}">
                        <div class="ui circular label la-long-tooltip" data-tooltip="${propValue?.note}">Anm.</div>
                    </g:if>
                    <g:if test="${propValues.get(sourceSubscription)?.size() > 1}"><br></g:if>
                </g:each>
            </g:if>
            <g:else>
                <a class="ui red circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>X</strong></a>
            </g:else>
        </td>

        %{--COPY:--}%
        <td class="center aligned">
        <g:if test="${propValues.containsKey(sourceSubscription)}">
            <% Set propValuesForSourceSub_ = propValues.get(sourceSubscription) %>
            <g:each var="propValue" in="${propValuesForSourceSub_}">
                <g:if test="${propValues.containsKey(sourceSubscription)}">
                    <i class="ui icon angle double right"></i>
                    <g:checkBox name="subscription.takeProperty" data-action="copy"  value="${genericOIDService.getOID(propValue)}" checked="${true}" />
                </g:if>
                <br>
            </g:each>
        </g:if>

        %{--TARGET-SUBSCRIPTION--}%
        <td class="center aligned">
            <g:if test="${ ! targetSubscription}">
            </g:if>
            <g:elseif test="${propValues.containsKey(targetSubscription)}">
                <% Set propValuesForTargetSub = propValues.get(targetSubscription) %>
                <g:each var="propValue" in="${propValuesForTargetSub}">
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
                         ${propValue.value}
                     </g:else>
                    <g:if test="${propValue?.note}">
                        <div class="ui circular label la-long-tooltip" data-tooltip="${propValue?.note}">Anm.</div>
                    </g:if>
                    <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                </g:each>
            </g:elseif>
            <g:else>
                <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
            </g:else>
        </td>
        %{--DELETE:--}%
        <td>
            <g:if test="${targetSubscription && propValues?.containsKey(targetSubscription)}">
                <% Set propValuesForTargetSubTrash = propValues.get(targetSubscription) %>
                <g:each var="propValue" in="${propValuesForTargetSubTrash}">
                    <i class="ui icon trash alternate outline"></i><g:checkBox name="subscription.deleteProperty" value="${genericOIDService.getOID(propValue)}" data-action="delete" checked="false"/>
                    <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                </g:each>
            </g:if>
        </td>
    </tr>
</g:each>
</tbody>
<r:script>
$('input[name="subscription.takeProperty"]').change( function(event) {
    if ($(this).prop('checked')){
        $(this).parent().next().addClass('willBeReplaced');
    } else {
        $(this).parent().next().removeClass('willBeReplaced');
    }
})
</r:script>