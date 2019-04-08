<%@page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore;com.k_int.kbplus.*" %>
<%
    String unknownString = raw("<span data-tooltip=\"${RDStore.PERM_UNKNOWN.getI10n("value")}\"><i class=\"question circle icon  \"></i></span>")
%>
<laser:serviceInjection/>
<tr>
    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-append"><input type="radio" name="subscription.takeProperty" value="${COPY}" /></div></td>
    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-replace"><input type="radio" name="subscription.takeProperty" value="${REPLACE}" /></div></td>
    <td class="center aligned"><div class="ui checkbox la-toggle-radio la-noChange"><input type="radio" name="subscription.takeProperty" value="${DO_NOTHING}" checked /></div></td>
    <th>${key}</th>
    <g:each in="${subscriptions}" var="s">
        <g:if test="${propBinding && propBinding.get(s)?.visibleForConsortiaMembers}">
            <th><g:if test="${s}"><g:link controller="subscription" action="show" id="${s?.id}">${s?.name}</g:link></g:if><span class="ui blue tag label">${message(code:'financials.isVisibleForSubscriber')}</span></th>
        </g:if>
        <g:else>
            <th><g:if test="${s}"><g:link controller="subscription" action="show" id="${s?.id}">${s?.name}</g:link></g:if></th>
        </g:else>
    </g:each>
</tr>
<g:each in="${group}" var="prop">
    <%-- leave it for debugging
    <tr>
        <td colspan="999">${prop.getValue()}</td>
    </tr>--%>
    <%
        PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey())
    %>
    <tr>
        <td></td>
        <td></td>
        <td></td>

        <td>
            ${propKey.getI10n("name")}
            <g:if test="${propKey.multipleOccurrence}">
                <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                    <i class="redo icon orange"></i>
                </span>
            </g:if>
        </td>
        <g:each in="${subscriptions}" var="s">
            <g:set var="propValues" value="${prop.getValue()}" />
            <g:if test="${propValues.containsKey(s)}">
                <td>
                    <g:each var="propValue" in="${propValues.get(s)}">
                        <g:if test="${propValue.type.multipleOccurrence && propValues.get(s).size() > 1}">
                            <g:checkBox name="subscription.takePropertyIds" value="${propValue.id}" checked="${false}" />
                        </g:if>
                        <g:if test="${propValue.value}">
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
                        </g:if>
                        <g:else>
                            ${unknownString}
                        </g:else>
                        <g:if test="${propValues.get(s)?.size() > 1}"><br></g:if>
                        <g:if test="${propValue?.note}">
                            <div class="ui circular label la-long-tooltip" data-tooltip="${propValue?.note}">Anm.</div><br>
                        </g:if>
                    </g:each>
                </td>
            </g:if>
            <g:else>
                <td>
                    <i class="window minimize icon"></i>
                    %{--<g:message code="default.compare.propertyNotSet"/>--}%
                </td>
            </g:else>
        </g:each>
    </tr>
</g:each>