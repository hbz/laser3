%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.GenericOIDService; com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.License; de.laser.AuditConfig" %>

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<table class="ui la-table-small la-table-inCard table">
    <g:if test="${orphanedProperties}">
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
                <th class="la-js-dont-hide-this-card" >${message(code:'property.table.property')}</th>
                <th>${message(code:'property.table.value')}</th>
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${orphanedProperties.sort{a, b -> a.type.getI10n('name').compareToIgnoreCase b.type.getI10n('name')}}" var="prop">
            <g:set var="overwriteEditable" value="${(prop.tenant?.id == contextOrg.id && editable) || (!prop.tenant && editable)}"/>
            <g:if test="${(prop.tenant?.id == contextOrg.id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}">
                <g:if test="${prop.type.descr == prop_desc}">
                    <tr>
                        <td class="la-js-dont-hide-this-card">
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
                            <g:if test="${prop.type.multipleOccurrence}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                    <i class="redo icon orange"></i>
                                </span>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${prop.type.type == Integer.toString()}">
                                <semui:xEditable owner="${prop}" type="number" field="intValue" overwriteEditable="${overwriteEditable}"/>
                            </g:if>
                            <g:elseif test="${prop.type.type == String.toString()}">
                                <semui:xEditable owner="${prop}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}"/>
                            </g:elseif>
                            <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                                <semui:xEditable owner="${prop}" type="text" field="decValue" overwriteEditable="${overwriteEditable}"/>
                            </g:elseif>
                            <g:elseif test="${prop.type.type == Date.toString()}">
                                <semui:xEditable owner="${prop}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}"/>
                            </g:elseif>
                            <g:elseif test="${prop.type.type == URL.toString()}">
                                <semui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis" />
                                <g:if test="${prop.value}">
                                    <semui:linkIcon href="${prop.value}" />
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                                <semui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}" overwriteEditable="${overwriteEditable}"/>
                            </g:elseif>
                        </td>
                        <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                            <td>
                                <semui:xEditable owner="${prop}" type="textarea" field="paragraph" overwriteEditable="${overwriteEditable}"/>
                            </td>
                        </g:if>
                        <td>
                            <semui:xEditable owner="${prop}" type="textarea" field="note" overwriteEditable="${overwriteEditable}"/>
                        </td>
                        <td class="x la-js-editmode-container">  <%--before="if(!confirm('Merkmal ${prop.type.name} löschen?')) return false" --%>
                            <g:if test="${overwriteEditable}">
                                <g:if test="${ownobj.hasProperty('instanceOf') && showConsortiaFunctions && !ownobj.instanceOf}">
                                    <g:set var="auditMsg" value="${message(code:'property.audit.toggle', args: [prop.type.name])}" />

                                    <g:if test="${! AuditConfig.getConfig(prop)}">
                                        <laser:remoteLink class="ui icon button la-popup-tooltip la-delay js-open-confirm-modal"
                                                          controller="ajax"
                                                          action="togglePropertyAuditConfig"
                                                          params='[propClass: prop.getClass(),
                                                                   ownerId: "${ownobj.id}",
                                                                   ownerClass: "${ownobj.class}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   editable: "${overwriteEditable}",
                                                                   showConsortiaFunctions: true
                                                          ]'
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="inherit"
                                                          id="${prop.id}"
                                                          data-content="${message(code:'property.audit.off.tooltip')}"
                                                          data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                          data-always="c3po.loadJsAfterAjax()"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="icon la-thumbtack slash la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                    </g:if>
                                    <g:else>

                                        <laser:remoteLink class="ui icon green button la-popup-tooltip la-delay js-open-confirm-modal"
                                                          controller="ajax" action="togglePropertyAuditConfig"
                                                          params='[propClass: prop.getClass(),
                                                                   ownerId: "${ownobj.id}",
                                                                   ownerClass: "${ownobj.class}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   editable: "${overwriteEditable}",
                                                                   showConsortiaFunctions: true
                                                          ]'
                                                          id="${prop.id}"
                                                          data-content="${message(code:'property.audit.on.tooltip')}"
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="inherit"
                                                          data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                          data-always="c3po.loadJsAfterAjax()"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="thumbtack icon la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:elseif test="${ownobj.hasProperty('instanceOf') && !ownobj.instanceOf}">
                                    <g:if test="${prop.isPublic}">
                                        <laser:remoteLink class="ui orange icon button la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                          params='[oid: GenericOIDService.getOID(prop),editable:"${overwriteEditable}",custom_props_div: "${custom_props_div}",showConsortiaFunctions: "${showConsortiaFunctions}"]'
                                                          data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                          data-always="c3po.loadJsAfterAjax()"
                                                          data-update="${custom_props_div}">
                                            <i class="icon eye la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <laser:remoteLink class="ui icon button la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                          params='[oid: GenericOIDService.getOID(prop),editable:"${overwriteEditable}",custom_props_div: "${custom_props_div}",showConsortiaFunctions: "${showConsortiaFunctions}"]'
                                                          data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                          data-always="c3po.loadJsAfterAjax()"
                                                          data-update="${custom_props_div}">
                                            <i class="icon eye slash la-js-editmode-icon"></i>
                                        </laser:remoteLink>
                                    </g:else>
                                </g:elseif>

                                <g:if test="${! AuditConfig.getConfig(prop)}">
                                    <g:set var="confirmMsg" value="${message(code:'property.delete.confirm', args: [prop.type.name])}" />

                                    <g:if test="${! AuditConfig.getConfig(prop)}">
                                        <laser:remoteLink class="ui icon negative button js-open-confirm-modal"
                                                          controller="ajax"
                                                          action="deleteCustomProperty"
                                                          params='[propClass: prop.getClass(),
                                                                   ownerId: "${ownobj.id}",
                                                                   ownerClass: "${ownobj.class}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   editable: "${overwriteEditable}",
                                                                   showConsortiaFunctions: "${showConsortiaFunctions}"
                                                          ]'
                                                          id="${prop.id}"
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="delete"
                                                          data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                                          data-always="c3po.loadJsAfterAjax()"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="trash alternate icon"></i>
                                        </laser:remoteLink>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    <!-- Hidden Fake Button To hold the other Botton in Place -->
                                    <div class="ui icon button la-hidden">
                                        <i class="coffee icon"></i>
                                    </div>
                                </g:else>
                            </g:if>
                            <g:else>
                                <g:if test="${prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)}">
                                    <g:if test="${ownobj.isSlaved}">
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon thumbtack blue"></i></span>
                                    </g:if>
                                    <g:else>
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit')}" data-position="top right"><i class="icon thumbtack grey"></i></span>
                                    </g:else>
                                </g:if>
                            </g:else>
                        </td>
                    </tr>
                </g:if>
            </g:if>
        </g:each>
    </tbody>

    <g:if test="${editable}">
        <tfoot>
            <tr>
            <g:if test="${orphanedProperties}">
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <td colspan="5">
                </g:if>
                <g:else>
                    <td colspan="4">
                </g:else>
            </g:if>
            <g:else>
                <td>
            </g:else>
                    <laser:remoteForm url="[controller: 'ajax', action: 'addCustomPropertyValue']"
                                  name="cust_prop_add_value_custom"
                                  class="ui form"
                                  data-update="${custom_props_div}"
                                  data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                  data-always="c3po.loadJsAfterAjax()"
                    >

                        <input type="hidden" name="propIdent" data-desc="${prop_desc}" class="customPropSelect"/>
                        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
                        <input type="hidden" name="editable" value="${editable}"/>
                        <input type="hidden" name="showConsortiaFunctions" value="${showConsortiaFunctions}"/>
                        <input type="hidden" name="showCollectiveFunctions" value="${showCollectiveFunctions}"/>
                        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>
                        <input type="hidden" name="custom_props_div" value="${custom_props_div}"/>

                        <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                    </laser:remoteForm>
                </td>
            </tr>
        </tfoot>
    </g:if>
</table>
<g:if test="${error}">
    <semui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${error}"/>
</g:if>