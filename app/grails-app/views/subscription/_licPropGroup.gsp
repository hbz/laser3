<!-- _licPropGroup.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.License; de.laser.AuditConfig" %>

<table class="ui table la-table-small la-table-inCard">
    <g:if test="${propList}">
        <thead>
            <th>${message(code:'property.table.property')}</th>
            <th>${message(code:'property.table.value')}</th>
            <th>${message(code:'property.table.paragraph')}</th>
            <th>${message(code:'property.table.notes')}</th>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${propList}" var="prop">
                <tr>
                    <td>
                        <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                            ${prop.type.getI10n('name')}
                            <g:if test="${prop.type.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${prop.type.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            ${prop.type.getI10n('name')}
                        </g:else>
                        <%
                            /*
                            if (AuditConfig.getConfig(prop)) {
                                println '&nbsp; <span data-content="Wert wird vererbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                            }
                            */
                            if (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) {
                                if (ownObj.isSlaved) {
                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                }
                                else {
                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                }
                            }
                        %>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="redo icon orange"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.type == Integer.toString()}">
                            ${prop.intValue}
                        </g:if>
                        <g:elseif test="${prop.type.type == String.toString()}">
                            ${prop.stringValue}
                        </g:elseif>
                        <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                            ${prop.decValue}
                        </g:elseif>
                        <g:elseif test="${prop.type.type == Date.toString()}">
                            <g:formatDate date="${prop.dateValue}" format="${message(code:'default.date.format.notime')}"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            ${prop.refValue?.getI10n('value')}
                        </g:elseif>
                        <g:elseif test="${prop.type.type == URL.toString()}">
                            <span class="la-overflow la-ellipsis">
                                ${prop.urlValue}
                                <g:if test="${prop.value}">
                                    <semui:linkIcon href="${prop.value}" />
                                </g:if>
                            </span>
                        </g:elseif>
                    </td>
                    <td>
                        ${prop.paragraph}
                    </td>
                    <td>
                        ${prop.note}
                    </td>
                </tr>

        </g:each>
    </tbody>
</table>
<!-- _licPropGroup.gsp -->