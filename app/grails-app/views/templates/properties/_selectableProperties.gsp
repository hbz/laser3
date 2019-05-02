%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.abstract_domain.PrivateProperty; com.k_int.kbplus.abstract_domain.CustomProperty; com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; java.net.URL; com.k_int.kbplus.License; de.laser.AuditConfig; com.k_int.kbplus.GenericOIDService" %>
<laser:serviceInjection />

<!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')} -->
<g:set var="overwriteEditable" value="${editable || accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}" />
<g:set var="overwriteEditable" value="${ ! forced_not_editable}" />

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<g:if test="${error}">
    <bootstrap:alert class="alert-danger">${error}</bootstrap:alert>
</g:if>

<table class="ui la-table-small la-table-inCard table">
    <g:set var="properties" value="${showPropClass == PrivateProperty.class? ownobj.privateProperties : ownobj.customProperties}" />
    <g:if test="${properties}">
        <colgroup>
            <g:if test="${show_checkboxes}">
                <col style="width: 5px;">
            </g:if>
            <col style="width: 129px;">
            <col style="width: 96px;">
            <g:if test="${ownobj instanceof License}">
                <col style="width: 359px;">
            </g:if>
            <col style="width: 148px;">
        </colgroup>
        <thead>
            <tr>
                <g:if test="${show_checkboxes}">
                    <th></th>
                </g:if>
                <th class="la-js-dont-hide-this-card">${message(code:'property.table.property')}</th>
                <th>${message(code:'property.table.value')}</th>
                <g:if test="${ownobj instanceof License}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${properties.sort{a, b -> a.type.getI10n('name').compareToIgnoreCase b.type.getI10n('name')}}" var="prop">
            <g:if test="${showPropClass == CustomProperty.class || ( showPropClass == PrivateProperty.class && prop.type?.tenant?.id == tenant?.id)}">
                <g:if test="${showCopyConflicts}">
                    <tr data-prop-type="${prop.class.simpleName + prop.type.name}">
                </g:if><g:else>
                    <tr>
                </g:else>
                    <g:if test="${show_checkboxes}">
                        <g:if test="${prop.type.multipleOccurrence}">
                            <td><g:checkBox name="subscription.takeProperty" value="${genericOIDService.getOID(prop)}" checked="false"/></td>
                        </g:if><g:else>
                            <td><g:checkBox name="subscription.takeProperty" value="${genericOIDService.getOID(prop)}" data-prop-type="${prop.class.simpleName + prop.type.name}" checked="false"/></td>
                        </g:else>
                    </g:if>
                    <td>
                        ${prop.type.getI10n('name')}
                        <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                            <span class="la-long-tooltip" data-position="right center" data-variation="tiny" data-tooltip="${prop.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                        <%
                            if (showPropClass == CustomProperty.class) {
                                if (AuditConfig.getConfig(prop)) {
                                    println '&nbsp; <span data-tooltip="Wert wird vererbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                                }

                                if (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) {
                                    if (ownobj.isSlaved?.value?.equalsIgnoreCase('yes')) {
                                        println '&nbsp; <span data-tooltip="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                                    }
                                    else {
                                        println '&nbsp; <span data-tooltip="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                    }
                                }
                            }
                        %>

                        <g:if test="${showPropClass == CustomProperty.class && prop.type.mandatory}">
                            <span data-position="top right" data-tooltip="${message(code:'default.mandatory.tooltip')}">
                                <i class="star icon yellow"></i>
                            </span>
                        </g:if>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="redo icon orange"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.type == Integer.toString()}">
                            <semui:xEditable owner="${prop}" type="number" field="intValue" overwriteEditable="${overwriteEditable}" />
                        </g:if>
                        <g:elseif test="${prop.type.type == String.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                        <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                        <g:elseif test="${prop.type.type == Date.toString()}">
                            <semui:xEditable owner="${prop}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                        <g:elseif test="${prop.type.type == URL.toString()}">
                            <semui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                    </td>
                    <g:if test="${ownobj instanceof License}">
                        <td>
                            <semui:xEditable owner="${prop}" type="textarea" field="paragraph"/>
                        </td>
                    </g:if>
                    <td>
                        <semui:xEditable owner="${prop}" type="textarea" field="note" overwriteEditable="${overwriteEditable}" />
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>
</table>
<r:script>
    $('input:checkbox').change( function(event) {
        if (this.checked) {
            var dPropType = $(this).attr('data-prop-type');
            $('.table tr[data-prop-type="' + dPropType + '"]').addClass('trWarning')
        } else {
            var dPropType = $(this).attr('data-prop-type');
            $('.table tr[data-prop-type="' + dPropType + '"]').removeClass('trWarning')
        }
    })
</r:script>
<style>
table tr.trWarning td {
    background-color:tomato !important;
    text-decoration: line-through;
}
/*table tr.trWarning td[data-uri]:hover,*/
/*table tr.trWarning td[data-context]:hover {*/
    /*cursor: pointer;*/
/*}*/
</style>
