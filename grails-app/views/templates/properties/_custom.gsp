<!-- A: templates/properties/_custom -->
%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="de.laser.Subscription; de.laser.License; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection/>
<g:if test="${newProp}">
    <ui:errors bean="${newProp}" />
</g:if>

<table class="ui compact la-js-responsive-table la-table-inCard table">
    <g:if test="${orphanedProperties}">
        <colgroup>
            <col class="la-prop-col-1">
            <col class="la-prop-col-2">
            <g:if test="${ownobj instanceof License}">
                <col class="la-prop-col-3">
            </g:if>
            <col class="la-prop-col-4">
            <col class="la-prop-col-5">
        </colgroup>
        <thead>
            <tr>
                <th class="la-js-dont-hide-this-card" >${message(code:'property.table.property')}</th>
                <th>${message(code:'default.value.label')}</th>
                <g:if test="${ownobj instanceof License}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                <th class="la-action-info">${message(code:'default.actions.label')}</th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${orphanedProperties.sort{a, b -> a.type.getI10n('name') <=> b.type.getI10n('name') ?: a.getValue() <=> b.getValue() ?: a.id <=> b.id }}" var="prop">
            %{-- TODO : ERMS-3058 - check if $calledFromSurvey@subscription/_properties.gsp must be applied here --}%
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
                            <g:if test="${prop.type.isIntegerType()}">
                                <ui:xEditable owner="${prop}" type="number" field="intValue" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
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
                            <g:elseif test="${prop.type.isURLType()}">
                                <ui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis" />
                                <g:if test="${prop.value}">
                                    <ui:linkIcon href="${prop.value}" />
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${prop.type.isRefdataValueType()}">
                                <ui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                            </g:elseif>
                        </td>
                        <g:if test="${ownobj instanceof License}">
                            <td>
                                <ui:xEditable owner="${prop}" type="textarea" field="paragraph" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                            </td>
                        </g:if>
                        <td>
                            <ui:xEditable owner="${prop}" type="textarea" field="note" overwriteEditable="${overwriteEditable}" class="la-dont-break-out"/>
                        </td>
                        <td class="x la-js-editmode-container">  <%--before="if(!confirm('Merkmal ${prop.type.name} löschen?')) return false" --%>
                            <g:if test="${overwriteEditable && (prop.hasProperty("instanceOf") && !prop.instanceOf)}">
                                <g:if test="${showConsortiaFunctions}">
                                    <g:set var="auditMsg" value="${message(code:'property.audit.toggle', args: [prop.type.name])}" />

                                    <g:if test="${! AuditConfig.getConfig(prop)}">
                                        <g:if test="${prop.type in memberProperties}">
                                            <ui:remoteLink class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                              controller="ajax"
                                                              action="togglePropertyAuditConfig"
                                                              params='[propClass: prop.getClass(),
                                                                       ownerId: "${ownobj.id}",
                                                                       ownerClass: "${ownobj.class}",
                                                                       custom_props_div: "${custom_props_div}",
                                                                       editable: "${overwriteEditable}",
                                                                       showConsortiaFunctions: true,
                                                                       (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                              ]'
                                                              data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit2.property", args: [prop.type.getI10n('name')])}"
                                                              data-confirm-term-how="inherit"
                                                              id="${prop.id}"
                                                              data-content="${message(code:'property.audit.off.tooltip')}"
                                                              data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                              data-update="${custom_props_div}"
                                                              role="button"
                                            >
                                                <i class="icon la-thumbtack slash la-js-editmode-icon"></i>
                                            </ui:remoteLink>
                                        </g:if>
                                        <g:else>
                                            <ui:remoteLink class="ui icon button blue la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                              controller="ajax"
                                                              action="togglePropertyAuditConfig"
                                                              params='[propClass: prop.getClass(),
                                                                       ownerId: "${ownobj.id}",
                                                                       ownerClass: "${ownobj.class}",
                                                                       custom_props_div: "${custom_props_div}",
                                                                       editable: "${overwriteEditable}",
                                                                       showConsortiaFunctions: true,
                                                                       (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                              ]'
                                                              data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                              data-confirm-term-how="inherit"
                                                              id="${prop.id}"
                                                              data-content="${message(code:'property.audit.off.tooltip')}"
                                                              data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                              data-update="${custom_props_div}"
                                                              role="button"
                                            >
                                                <i class="icon la-thumbtack slash la-js-editmode-icon"></i>
                                            </ui:remoteLink>
                                        </g:else>
                                    </g:if>
                                    <g:else>

                                        <ui:remoteLink class="ui icon green button la-modern-button la-popup-tooltip la-delay js-open-confirm-modal"
                                                          controller="ajax" action="togglePropertyAuditConfig"
                                                          params='[propClass: prop.getClass(),
                                                                   ownerId: "${ownobj.id}",
                                                                   ownerClass: "${ownobj.class}",
                                                                   custom_props_div: "${custom_props_div}",
                                                                   editable: "${overwriteEditable}",
                                                                   showConsortiaFunctions: true,
                                                                   (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                          ]'
                                                          id="${prop.id}"
                                                          data-content="${message(code:'property.audit.on.tooltip')}"
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [prop.type.getI10n('name')])}"
                                                          data-confirm-term-how="inherit"
                                                          data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                          data-update="${custom_props_div}"
                                                          role="button"
                                        >
                                            <i class="thumbtack icon la-js-editmode-icon"></i>
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:if test="${! AuditConfig.getConfig(prop)}">
                                    <g:if test="${(ownobj.instanceOf && !prop.instanceOf) || !ownobj.hasProperty("instanceOf")}">
                                        <g:if test="${prop.isPublic}">
                                            <ui:remoteLink class="ui orange icon button la-popup-tooltip la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                              params='[oid: genericOIDService.getOID(prop), editable:"${overwriteEditable}", custom_props_div: "${custom_props_div}", showConsortiaFunctions: "${showConsortiaFunctions}", (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()]'
                                                              data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                              data-content="${message(code:'property.visible.active.tooltip')}" data-position="left center"
                                                              data-update="${custom_props_div}"
                                            >
                                                <i class="icon eye la-js-editmode-icon"></i>
                                            </ui:remoteLink>
                                        </g:if>
                                        <g:else>
                                            <ui:remoteLink class="ui icon blue button la-modern-button la-popup-tooltip la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                              params='[oid: genericOIDService.getOID(prop), editable:"${overwriteEditable}", custom_props_div: "${custom_props_div}", showConsortiaFunctions: "${showConsortiaFunctions}", (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()]'
                                                              data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                              data-content="${message(code:'property.visible.inactive.tooltip')}" data-position="left center"
                                                              data-update="${custom_props_div}">
                                                <i class="icon eye slash la-js-editmode-icon"></i>
                                            </ui:remoteLink>
                                        </g:else>
                                    </g:if>

                                    <g:set var="confirmMsg" value="${message(code:'property.delete.confirm', args: [prop.type.name])}" />

                                    <ui:remoteLink class="ui icon negative button la-modern-button js-open-confirm-modal"
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
                                                      data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                      data-update="${custom_props_div}"
                                                      role="button"
                                                      ariaLabel="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </ui:remoteLink>
                                </g:if>
                                <g:else>
                                    <!-- Hidden Fake Button To hold the other Botton in Place -->
                                    <div class="ui icon button la-hidden">
                                        <i class="coffee icon"></i>
                                    </div>
                                </g:else>
                            </g:if>
                            <g:elseif test="${overwriteEditable && !prop.hasProperty("instanceOf")}">
                                <g:if test="${prop.isPublic}">
                                    <ui:remoteLink class="ui orange icon button la-popup-tooltip la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                      params='[oid: genericOIDService.getOID(prop), editable:"${overwriteEditable}", custom_props_div: "${custom_props_div}", showConsortiaFunctions: "${showConsortiaFunctions}"]'
                                                      data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                      data-content="${message(code:'property.visible.active.tooltip')}" data-position="left center"
                                                      data-update="${custom_props_div}">
                                        <i class="icon eye la-js-editmode-icon"></i>
                                    </ui:remoteLink>
                                </g:if>
                                <g:else>
                                    <ui:remoteLink class="ui icon button blue la-modern-button la-popup-tooltip la-delay" controller="ajax" action="togglePropertyIsPublic" role="button"
                                                      params='[oid: genericOIDService.getOID(prop), editable:"${overwriteEditable}", custom_props_div: "${custom_props_div}", showConsortiaFunctions: "${showConsortiaFunctions}"]'
                                                      data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                      data-content="${message(code:'property.visible.inactive.tooltip')}" data-position="left center"
                                                      data-update="${custom_props_div}">
                                        <i class="icon eye slash la-js-editmode-icon"></i>
                                    </ui:remoteLink>
                                </g:else>
                                <g:set var="confirmMsg" value="${message(code:'property.delete.confirm', args: [prop.type.name])}" />
                                <ui:remoteLink class="ui icon negative button la-modern-button js-open-confirm-modal"
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
                                                  data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')"
                                                  data-update="${custom_props_div}"
                                                  role="button"
                                                  ariaLabel="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </ui:remoteLink>
                            </g:elseif>
                            <g:else>
                                <g:if test="${ownobj instanceof License}">
                                    <g:set var="consortium" value="${ownobj.getLicensingConsortium()}"/>
                                </g:if>
                                <g:elseif test="${ownobj instanceof Subscription}">
                                    <g:set var="consortium" value="${ownobj.getConsortia()}"/>
                                    <g:set var="atSubscr" value="${ownobj._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION}"/>
                                </g:elseif>
                                <g:if test="${(prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)) || AuditConfig.getConfig(prop)}">
                                    <g:if test="${ownobj.isSlaved}">
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>
                                    </g:if>
                                    <g:else>
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit')}" data-position="top right"><i class="icon thumbtack grey"></i></span>
                                    </g:else>
                                </g:if>
                                <g:elseif test="${prop.tenant?.id == consortium?.id && atSubscr}">
                                    <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.notInherited.fromConsortia')}" data-position="top right"><i class="large icon cart arrow down grey"></i></span>
                                </g:elseif>
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
                <g:if test="${ownobj instanceof License}">
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
                                  name="cust_prop_add_value_custom"
                                  class="ui properties form"
                                  data-update="${custom_props_div}"
                                  data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${custom_props_div}')">
                        <input type="hidden" name="propIdent" data-desc="${prop_desc}" class="customPropSelect"/>
                        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
                        <input type="hidden" name="editable" value="${editable}"/>
                        <input type="hidden" name="showConsortiaFunctions" value="${showConsortiaFunctions}"/>
                        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>
                        <input type="hidden" name="custom_props_div" value="${custom_props_div}"/>

                        <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                    </ui:remoteForm>
                </td>
            </tr>
        </tfoot>
    </g:if>
</table>
<g:if test="${error}">
    <ui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${error}"/>
</g:if>
<!-- O: templates/properties/_custom -->