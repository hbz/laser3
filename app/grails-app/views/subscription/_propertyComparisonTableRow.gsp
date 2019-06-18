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
        <th class="one wide center aligned"><input type="checkbox" class="select-all"  onclick="selectAllTake(this);" checked="${true}" />
        <th class="six wide center aligned">
            <g:if test="${propBinding && propBinding.get(targetSubscription)?.visibleForConsortiaMembers}">
                <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span>
            </g:if>
            <g:else>
                <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
            </g:else>
        </th>
        <th class="one wide center aligned">
            <g:if test="${targetSubscription}">
                <input type="checkbox" data-action="delete" class="select-all" onclick="selectAllDelete(this);" />
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
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="intValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:if>
                    <g:elseif test="${propValue.type.type == String.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == BigDecimal.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == Date.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == URL.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                        <g:if test="${propValue.value}">
                            <semui:linkIcon href="${propValue.value}" />
                        </g:if>
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == RefdataValue.toString()}">
                        <div>
                            <semui:xEditableRefData owner="${propValue}" type="text" field="refValue" config="${propValue.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                        </div>
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
                </g:each>
            </g:if>
            <g:else>
                <div>
                    <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                </div>
            </g:else>
        </td>

        %{--COPY:--}%
        <td class="center aligned">
        <g:if test="${propValues.containsKey(sourceSubscription)}">
            <% Set propValuesForSourceSub_ = propValues.get(sourceSubscription) %>
            <g:each var="propValue" in="${propValuesForSourceSub_}">
                <g:if test="${propValues.containsKey(sourceSubscription)}">
                    <div class="ui checkbox la-toggle-radio la-replace">
                        <g:checkBox name="subscription.takeProperty" class="bulkcheck" data-action="copy"  value="${genericOIDService.getOID(propValue)}" checked="${true}" />
                    </div>
                </g:if>
                <br>
            </g:each>
        </g:if>

        %{--TARGET-SUBSCRIPTION--}%
        <td class="center aligned">
            <div>
                <g:if test="${ ! targetSubscription}">
            </g:if>
            <g:elseif test="${propValues.containsKey(targetSubscription)}">
                <% Set propValuesForTargetSub = propValues.get(targetSubscription) %>
                <g:each var="propValue" in="${propValuesForTargetSub}">

                    <g:if test="${propValue.type.type == Integer.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="intValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:if>

                    <g:elseif test="${propValue.type.type == String.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == BigDecimal.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == Date.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == URL.toString()}">
                        <div>
                            <semui:xEditable owner="${propValue}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                            <g:if test="${propValue.value}">
                                <semui:linkIcon />
                            </g:if>
                        </div>
                    </g:elseif>
                    <g:elseif test="${propValue.type.type == RefdataValue.toString()}">
                        <div>
                            <semui:xEditableRefData owner="${propValue}" type="text" field="refValue" config="${propValue.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                        </div>
                    </g:elseif>
                     <g:else>
                         <div>
                            ${propValue.value}
                         </div>
                     </g:else>
                    <g:if test="${propValue?.note}">
                        <div class="ui circular label la-long-tooltip" data-tooltip="${propValue?.note}">Anm.</div>
                    </g:if>
                    <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                </g:each>
            </g:elseif>
            <g:else>
                <div>
                    <a class="ui circular label la-popup-tooltip la-delay" data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                </div>
            </g:else>
        </td>
        %{--DELETE:--}%
        <td>
            <g:if test="${targetSubscription && propValues?.containsKey(targetSubscription)}">
                <% Set propValuesForTargetSubTrash = propValues.get(targetSubscription) %>
                <g:each var="propValue" in="${propValuesForTargetSubTrash}">
                    <div class="ui checkbox la-toggle-radio la-noChange">
                        <g:checkBox class="bulkcheck"  name="subscription.deleteProperty" value="${genericOIDService.getOID(propValue)}" data-action="delete" checked="${false}"/>
                    </div>
                    <g:if test="${propValues.get(targetSubscription)?.size() > 1}"><br></g:if>
                </g:each>
            </g:if>
        </td>
    </tr>
</g:each>
</tbody>
<r:script>
    var takeProperty = $('input[name="subscription.takeProperty"]');
    var deleteProperty = $('input[name="subscription.deleteProperty"]');

    function selectAllTake(source) {
        var table = $(source).closest('table');
        var thisBulkcheck = $(table).find(takeProperty);
        $( thisBulkcheck ).each(function( index, elem ) {
            elem.checked = source.checked;
            markAffectedTake($(this));
        })
    }
    function selectAllDelete(source) {
        var table = $(source).closest('table');
        var thisBulkcheck = $(table).find(deleteProperty);
        $( thisBulkcheck ).each(function( index, elem ) {
            elem.checked = source.checked;
            markAffectedDelete($(this));
        })
    }

    $(takeProperty).change( function() {
        markAffectedTake($(this));
    });
    $(deleteProperty).change( function() {
        markAffectedDelete($(this));
    });

    markAffectedTake = function (that) {
        if ($(that).is(":checked") ||  $(that).parents('tr').find('input[name="subscription.deleteProperty"]').is(':checked')) {
            $(that).parents('td').next().children('div').addClass('willBeReplaced');
        }
        else {
            $(that).parents('td').next().children('div').removeClass('willBeReplaced');
        }
    }
    markAffectedDelete = function (that) {
        if ($(that).is(":checked") ||  $(that).parents('tr').find('input[name="subscription.takeProperty"]').is(':checked')) {
            $(that).parents('td').prev().children('div').addClass('willBeReplaced');
        }
        else {
            $(that).parents('td').prev().children('div').removeClass('willBeReplaced');
        }
    }

    $(takeProperty).each(function( index, elem ) {
        if (elem.checked){
            markAffectedTake(elem)
        }
    });

</r:script>