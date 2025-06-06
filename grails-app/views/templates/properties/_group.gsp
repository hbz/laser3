<!-- A: templates/properties/_group -->
<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription; de.laser.License; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection/>

<g:if test="${newProp}">
    <ui:errors bean="${newProp}" />
</g:if>
<table class="ui compact la-js-responsive-table la-table-inCard table">
    <g:if test="${propDefGroupItems && propDefGroup}">
        <colgroup>
            <col class="la-prop-col-1">
            <col class="la-prop-col-2">
            <g:if test="${propDefGroup.ownerType == License.class.name}">
                <col>
                <col class="la-prop-col-3">
            </g:if>
            <col class="la-prop-col-4">
            <col class="la-prop-col-5">
        </colgroup>
        <thead>
            <tr>
                <th>${message(code:'property.table.property')}</th>
                <th>${message(code:'default.value.label')}</th>
                <g:if test="${propDefGroup.ownerType == License.class.name}">
                    <th>${message(code:'property.table.paragraphNumber')}</th>
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${propDefGroupItems}" var="prop">
            <g:set var="overwriteEditable" value="${(prop.tenant?.id == contextService.getOrg().id && editable) || (!prop.tenant && editable)}"/>
            <g:if test="${(prop.tenant?.id == contextService.getOrg().id || !prop.tenant) || prop.isVisibleExternally() || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}">
                <tr>
                    <td>
                        <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                            ${prop.type.getI10n('name')}
                            <g:if test="${prop.type.getI10n('expl')}">
                                <span class="la-popup-tooltip" data-position="right center" data-content="${prop.type.getI10n('expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            ${prop.type.getI10n('name')}
                        </g:else>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="${Icon.PROP.MULTIPLE}"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.isLongType()}">
                            <ui:xEditable owner="${prop}" type="number" field="longValue" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </g:if>
                        <g:elseif test="${prop.type.isStringType()}">
                            <ui:xEditable owner="${prop}" type="text" field="stringValue" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.isBigDecimalType()}">
                            <ui:xEditable owner="${prop}" type="text" field="decValue" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.isDateType()}">
                            <ui:xEditable owner="${prop}" type="date" field="dateValue" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.isRefdataValueType()}">
                            <ui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}" overwriteEditable="${overwriteEditable}" class="la-dont-break-out" constraint="removeValues_processingProvOrVendor"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.isURLType()}">
                            <ui:xEditable owner="${prop}" type="url" field="urlValue" validation="maxlength" maxlength="255" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis" />
                            <g:if test="${prop.value}">
                                <ui:linkWithIcon href="${prop.value}" />
                            </g:if>
                        </g:elseif>
                    </td>
                    <g:if test="${propDefGroup.ownerType == License.class.name}">
                        <td>
                            <ui:xEditable owner="${prop}" type="text" field="paragraphNumber" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </td>
                        <td>
                            <ui:xEditable owner="${prop}" type="textarea" field="paragraph" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </td>
                    </g:if>
                    <td>
                        <ui:xEditable owner="${prop}" type="textarea" field="note" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                    </td>
                    <td class="x">  <%--before="if(!confirm('Merkmal ${prop.type.name} löschen?')) return false" --%>
                        <g:if test="${overwriteEditable && (prop.hasProperty("instanceOf") && !prop.instanceOf)}">
                            <g:if test="${showConsortiaFunctions}">
                                <g:set var="auditMsg" value="${message(code:'property.audit.toggle', args: [prop.type.name])}" />
                                <g:if test="${! AuditConfig.getConfig(prop)}">

                                    <g:if test="${prop.type in memberProperties}">
                                        <ui:remoteLink class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                          controller="ajax"
                                                          action="togglePropertyAuditConfig"
                                                          params='[propClass: prop.getClass(),
                                                                   propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                                   ownerId:"${ownobj.id}",
                                                                   ownerClass:"${ownobj.class}",
                                                                   custom_props_div:"${custom_props_div}",
                                                                   editable:"${editable}",
                                                                   showConsortiaFunctions:true,
                                                                   (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                          ]'
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit2.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="inherit"
                                                          id="${prop.id}"
                                                          data-content="${message(code:'property.audit.off.tooltip')}"
                                                          data-done="c3po.initGroupedProperties('${createLink(controller:'ajaxJson', action:'lookup')}','#${custom_props_div}')"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                        </ui:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <ui:remoteLink class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                          controller="ajax"
                                                          action="togglePropertyAuditConfig"
                                                          params='[propClass: prop.getClass(),
                                                                   propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                                   ownerId:"${ownobj.id}",
                                                                   ownerClass:"${ownobj.class}",
                                                                   custom_props_div:"${custom_props_div}",
                                                                   editable:"${editable}",
                                                                   showConsortiaFunctions:true,
                                                                   (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                          ]'
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="inherit"
                                                          id="${prop.id}"
                                                          data-content="${message(code:'property.audit.off.tooltip')}"
                                                          data-done="c3po.initGroupedProperties('${createLink(controller:'ajaxJson', action:'lookup')}','#${custom_props_div}')"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <ui:remoteLink class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                      controller="ajax"
                                                      action="togglePropertyAuditConfig"
                                                      params='[propClass: prop.getClass(),
                                                               propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                               ownerId:"${ownobj.id}",
                                                               ownerClass:"${ownobj.class}",
                                                               custom_props_div:"${custom_props_div}",
                                                               editable:"${editable}",
                                                               showConsortiaFunctions:true,
                                                               (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                      ]'
                                                      id="${prop.id}"
                                                      data-content="${message(code:'property.audit.on.tooltip')}"
                                                      data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                      data-confirm-term-how="inherit"
                                                      data-done="c3po.initGroupedProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                      data-update="${custom_props_div}"
                                                      role="button"
                                    >
                                        <i class="${Icon.SIG.INHERITANCE}"></i>
                                    </ui:remoteLink>
                                </g:else>
                            </g:if>
                            <g:if test="${! AuditConfig.getConfig(prop)}">
                                <g:if test="${(ownobj.instanceOf && !prop.instanceOf) || !ownobj.hasProperty("instanceOf")}">
                                    <g:if test="${prop.isPublic}">
                                        <ui:remoteLink class="${Btn.ICON.SIMPLE_TOOLTIP} orange" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                          params='[oid: genericOIDService.getOID(prop),
                                                                   editable:"${overwriteEditable}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                                   showConsortiaFunctions: "${showConsortiaFunctions}",
                                                                   (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()]'
                                                          data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                          data-content="${message(code:'property.visible.active.tooltip')}" data-position="left center"
                                                          data-update="${custom_props_div}">
                                            <i class="${Icon.SIG.VISIBLE_ON}"></i>
                                        </ui:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <ui:remoteLink class="${Btn.MODERN.SIMPLE_TOOLTIP}" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                          params='[oid: genericOIDService.getOID(prop),
                                                                   editable:"${overwriteEditable}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                                   showConsortiaFunctions: "${showConsortiaFunctions}",
                                                                   (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()]'
                                                          data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                          data-content="${message(code:'property.visible.inactive.tooltip')}" data-position="left center"
                                                          data-update="${custom_props_div}"
                                        >
                                            <i class="${Icon.SIG.VISIBLE_OFF}"></i>
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>

                                <g:set var="confirmMsg" value="${message(code:'property.delete.confirm', args: [prop.type.name])}" />

                                <ui:remoteLink class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                  controller="ajax"
                                                  action="deleteCustomProperty"
                                                  params='[propClass: prop.getClass(),
                                                           propDefGroup: "${genericOIDService.getOID(propDefGroup)}",
                                                           propDefGroupBinding: "${genericOIDService.getOID(propDefGroupBinding)}",
                                                           ownerId:"${ownobj.id}",
                                                           ownerClass:"${ownobj.class}",
                                                           custom_props_div:"${custom_props_div}",
                                                           editable:"${editable}",
                                                           showConsortiaFunctions:"${showConsortiaFunctions}"
                                                  ]'
                                                  id="${prop.id}"
                                                  data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [prop.type.getI10n('name')])}"
                                                  data-confirm-term-how="delete"
                                                  data-done="c3po.initGroupedProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                  data-update="${custom_props_div}"
                                                  role="button"
                                                  ariaLabel="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </ui:remoteLink>
                            </g:if>
                            <g:else>
                                <!-- Hidden Fake Button To hold the other Botton in Place -->
                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                    <icon:placeholder />
                                </div>

                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)}">
                                <g:if test="${ownobj.instanceOf}">
                                    <ui:auditIcon type="auto" />
                                </g:if>
                                <g:else>
                                    <ui:auditIcon type="default" />
                                </g:else>
                            </g:if>
                            <g:elseif test="${prop.tenant?.id == consortium?.id && atSubscr}">
                                <span class="la-popup-tooltip" data-content="${message(code:'property.notInherited.fromConsortia')}" data-position="top right"><i class="icon cart arrow down grey"></i></span>
                            </g:elseif>
                        </g:else>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>

%{--    <g:if test="${editable && isGroupVisible}">
        <tfoot>
            <tr>
                <g:if test="${propDefGroup}">
                    <g:if test="${propDefGroup.ownerType == License.class.name}">
                        <td colspan="5">
                    </g:if>
                    <g:else>
                        <td colspan="4">
                    </g:else>
                </g:if>
                <g:else>
                    <td>
                </g:else>
                    <ui:remoteForm url="[controller: 'ajax', action: 'addCustomPropertyValue']"
                                  name="cust_prop_add_value_group_${propDefGroup.id}"
                                  class="ui properties form"
                                  data-update="${custom_props_div}"
                                  data-done="c3po.initGroupedProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')">

                        <div class="two fields" style="margin-bottom:0">
                            <div class="field" style="margin-bottom:0">
                                <select class="ui search selection dropdown remotePropertySearch" name="propIdent" data-desc="${prop_desc}" data-oid="${genericOIDService.getOID(propDefGroup)}"></select>
                            </div>
                            <div class="field" style="margin-bottom:0">
                                <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                            </div>
                        </div>

                        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
                        <input type="hidden" name="editable" value="${editable}"/>
                        <input type="hidden" name="showConsortiaFunctions" value="${showConsortiaFunctions}"/>
                        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>
                        <input type="hidden" name="propDefGroup" value="${genericOIDService.getOID(propDefGroup)}"/>
                        <input type="hidden" name="propDefGroupBinding" value="${genericOIDService.getOID(propDefGroupBinding)}"/>
                        <input type="hidden" name="custom_props_div" value="${custom_props_div}"/>
                    </ui:remoteForm>

                </td>
            </tr>
        </tfoot>
    </g:if>--}%

</table>
<g:if test="${error}">
    <ui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${error}"/>
</g:if>



<!-- O: templates/properties/_group -->
