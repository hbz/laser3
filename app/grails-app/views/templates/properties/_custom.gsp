%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.License; de.laser.AuditConfig" %>

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<g:if test="${error}">
    <bootstrap:alert class="alert-danger">${error}</bootstrap:alert>
</g:if>
<table class="ui la-table-small la-table-inCard table">
    <g:if test="${ownobj.customProperties}">
        <colgroup>
            <col style="width: 129px;">
            <col style="width: 96px;">
            <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                <col style="width: 359px;">
            </g:if>
            <col style="width: 148px;">
            <col style="width: 76px;">
        </colgroup>
        <thead>
            <tr>
                <th class="la-column-nowrap la-js-dont-hide-this-card" >${message(code:'property.table.property')}</th>
                <th>${message(code:'property.table.value')}</th>
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                <th></th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${ownobj.customProperties.sort{a, b -> a.type.getI10n('name').compareToIgnoreCase b.type.getI10n('name')}}" var="prop">
            <g:if test="${prop.type.descr == prop_desc}">
                <tr>
                    <td class="la-js-dont-hide-this-card">
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
                        %>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="redo icon orange"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.type == Integer.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${prop.type.type == String.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == Date.toString()}">
                            <semui:xEditable owner="${prop}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == URL.toString()}">
                            <semui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                        <td>
                            <semui:xEditable owner="${prop}" type="textarea" field="paragraph"/>
                        </td>
                    </g:if>
                    <td>
                        <semui:xEditable owner="${prop}" type="textarea" field="note"/>
                    </td>
                    <td class="x">  <%--before="if(!confirm('Merkmal ${prop.type.name} löschen?')) return false" --%>
                        <g:if test="${prop.type.type == URL.toString()}">
                            <semui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" />
                            <span data-position="top right" data-tooltip="Diese URL aufrufen ..">
                                <a href="${prop.value}" target="_blank" class="ui icon blue button">
                                    <i class="share square icon"></i>
                                </a>
                            </span>
                        </g:if>
                        <g:if test="${editable == true}">
                            <g:if test="${ownobj.hasProperty('instanceOf') && showConsortiaFunctions}">
                                <g:set var="auditMsg" value="${message(code:'property.audit.toggle', args: [prop.type.name])}" />

                                <span data-position="top right" data-tooltip="${message(code:'property.audit.tooltip')}">
                                    <button class="ui icon negative button js-open-confirm-modal-copycat">
                                        <i class="trash alternate icon"></i>
                                    </button>
                                    <g:remoteLink class="js-gost"
                                                  controller="ajax" action="togglePropertyAuditConfig"
                                                  params='[propClass: prop.getClass(), ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", custom_props_div:"${custom_props_div}", editable:"${editable}", showConsortiaFunctions:true]' id="${prop.id}"
                                                  data-confirm-term-what="property"
                                                  data-confirm-term-what-detail="${prop.type.name}"
                                                  data-confirm-term-how="delete"
                                                  onSuccess="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                  onComplete="c3po.loadJsAfterAjax()"
                                                  update="${custom_props_div}">
                                    </g:remoteLink>
                                </span>
                            </g:if>

                            <g:if test="${! AuditConfig.getConfig(prop)}">
                                <g:set var="confirmMsg" value="${message(code:'property.delete.confirm', args: [prop.type.name])}" />
                                <button class="ui icon negative button js-open-confirm-modal-copycat">
                                    <i class="trash alternate icon"></i>
                                </button>
                                <g:remoteLink class="js-gost"
                                              controller="ajax" action="deleteCustomProperty"
                                              params='[propClass: prop.getClass(), ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", custom_props_div:"${custom_props_div}", editable:"${editable}", showConsortiaFunctions:"${showConsortiaFunctions}"]' id="${prop.id}"
                                              data-confirm-term-what="property"
                                              data-confirm-term-what-detail="${prop.type.name}"
                                              data-confirm-term-how="delete"
                                              onSuccess="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                              onComplete="c3po.loadJsAfterAjax()"
                                              update="${custom_props_div}">
                                </g:remoteLink>
                            </g:if>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>

    <g:if test="${editable}">
        <tfoot>
            <tr>
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <td colspan="5">
                </g:if>
                <g:else>
                    <td colspan="4">
                </g:else>
                    <g:formRemote url="[controller: 'ajax', action: 'addCustomPropertyValue']" method="post"
                                  name="cust_prop_add_value"
                                  class="ui form"
                                  update="${custom_props_div}"
                                  onSuccess="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                  onComplete="c3po.loadJsAfterAjax()"
                    >

                        <input type="hidden" name="propIdent" data-desc="${prop_desc}" class="customPropSelect"/>
                        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
                        <input type="hidden" name="editable" value="${editable}"/>
                        <input type="hidden" name="showConsortiaFunctions" value="${showConsortiaFunctions}"/>
                        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>

                        <input type="hidden" name="custom_props_div" value="${custom_props_div}"/>

                        <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button"/>
                    </g:formRemote>
                </td>
            </tr>
        </tfoot>
    </g:if>

</table>