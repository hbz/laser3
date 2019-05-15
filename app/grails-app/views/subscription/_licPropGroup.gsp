<!-- _licPropGroup.gsp -->
<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.License; de.laser.AuditConfig" %>

<table class="ui la-table-small la-table-inCard table">
    <g:if test="${propList}">
        <colgroup>
            <col style="width: 129px;">
            <col style="width: 96px;">
            <col style="width: 359px;">
            <col style="width: 148px;">
            <col style="width: 76px;">
        </colgroup>
        <thead>
            <tr>
                <th>${message(code:'property.table.property')}</th>
                <th>${message(code:'property.table.value')}</th>
                <th>${message(code:'property.table.paragraph')}</th>
                <th>${message(code:'property.table.notes')}</th>
                <th></th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${propList.sort{a, b -> a.type.getI10n('name').compareToIgnoreCase b.type.getI10n('name')}}" var="prop">
                <tr>
                    <td>
                        <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                            ${prop.type.getI10n('name')}
                            <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${prop.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                        <g:else>
                            ${prop.type.getI10n('name')}
                        </g:else>
                        <%
                            /*
                            if (AuditConfig.getConfig(prop)) {
                                println '&nbsp; <span data-tooltip="Wert wird vererbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                            }
                            */
                            if (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) {
                                if (ownObj.isSlaved?.value?.equalsIgnoreCase('yes')) {
                                    println '&nbsp; <span data-tooltip="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                                }
                                else {
                                    println '&nbsp; <span data-tooltip="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                }
                            }
                        %>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
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
                            ${prop.dateValue}
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            ${prop.refValue?.getI10n('value')}
                        </g:elseif>
                        <g:elseif test="${prop.type.type == URL.toString()}">
                            ${prop.urlValue}
                            <g:if test="${prop.value}">
                                <semui:linkIcon href="${prop.value}" />
                            </g:if>
                        </g:elseif>
                    </td>
                    <td>
                        ${prop.paragraph}
                    </td>
                    <td>
                        ${prop.note}
                    </td>
                    <td class="x"></td>
                </tr>

        </g:each>
    </tbody>
</table>
<!-- _licPropGroup.gsp -->