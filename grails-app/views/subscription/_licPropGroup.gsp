<!-- _licPropGroup.gsp -->
<%@ page import="de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.License; de.laser.AuditConfig" %>

<table class="ui table compact la-table-inCard la-ignore-fixed">
    <g:if test="${propList}">
        <thead><tr>
            <th>${message(code:'property.table.property')}</th>
            <th>${message(code:'default.value.label')}</th>
            <th>${message(code:'property.table.paragraph')}</th>
            <th>${message(code:'property.table.notes')}</th>
        </tr></thead>
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
                            if (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) {
                                if (ownObj.isSlaved) {
                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
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
                        <g:if test="${prop.type.isIntegerType()}">
                            ${prop.intValue}
                        </g:if>
                        <g:elseif test="${prop.type.isStringType()}">
                            ${prop.stringValue}
                        </g:elseif>
                        <g:elseif test="${prop.type.isBigDecimalType()}">
                            ${prop.decValue}
                        </g:elseif>
                        <g:elseif test="${prop.type.isDateType()}">
                            <g:formatDate date="${prop.dateValue}" format="${message(code:'default.date.format.notime')}"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.isRefdataValueType()}">
                            ${prop.refValue?.getI10n('value')}
                        </g:elseif>
                        <g:elseif test="${prop.type.isURLType()}">
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