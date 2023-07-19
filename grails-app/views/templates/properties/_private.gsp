<!-- A: templates/properties/_private -->
%{-- To use, add the laser:render custom_props inside a div with id=private-property-wrapper-xxx --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#private-property-wrapper-xxx"); --}%

<%@ page import="de.laser.CustomerTypeService; de.laser.License; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; java.net.URL" %>
<laser:serviceInjection />


<%-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${userService.hasFormalAffiliation(user, contextService.getOrg(), 'INST_EDITOR')} --%>
<g:set var="overwriteEditable" value="${editable || contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_PRO )}" />

<g:if test="${newProp}">
    <ui:errors bean="${newProp}" />
</g:if>

<table class="ui compact la-js-responsive-table la-table-inCard table">
    <g:set var="privateProperties" value="${ownobj.propertySet.findAll { cp -> cp.type.tenant?.id == contextOrg.id && cp.tenant?.id == contextOrg.id }}"/>
    <g:if test="${privateProperties}">
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
                <th class="la-js-dont-hide-this-card">${message(code:'property.table.property')}</th>
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
        <g:each in="${privateProperties.sort{a, b -> a.type.getI10n('name').toLowerCase() <=> b.type.getI10n('name').toLowerCase() ?: a.getValue() <=> b.getValue() ?: a.id <=> b.id }}" var="prop">
            <g:if test="${prop.type.tenant?.id == tenant?.id}">
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
                        <g:if test="${prop.type.mandatory}">
                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'default.mandatory.tooltip')}">
                                <i class="star icon yellow"></i>
                            </span>
                        </g:if>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
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
                            <ui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                            <g:if test="${prop.value}">
                                <ui:linkWithIcon href="${prop.value}" />
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
                    <td class="x la-js-editmode-container">
                        <g:if test="${overwriteEditable == true}">
                            <ui:remoteLink class="ui icon negative button la-modern-button js-open-confirm-modal"
                                              controller="ajax"
                                              action="deletePrivateProperty"
                                              params='[propClass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]'
                                              id="${prop.id}"
                                              data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [prop.type.getI10n('name')])}"
                                              data-confirm-term-how="delete"
                                              data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${propertyWrapper}', ${tenant?.id})"
                                              data-update="${propertyWrapper}"
                                              role="button"
                                              ariaLabel="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </ui:remoteLink>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>

    <g:if test="${overwriteEditable}">
        <tfoot>
            <tr>
                <g:if test="${privateProperties}">
                    <td colspan="4">
                </g:if>
                <g:else>
                    <td>
                </g:else>
                    <ui:remoteForm url="[controller: 'ajax', action: 'addPrivatePropertyValue']"
                                      name="cust_prop_add_value_private"
                                      class="ui properties form"
                                      data-update="${propertyWrapper}"
                                      data-done="c3po.initProperties('${createLink(controller:'ajaxJson', action:'lookup')}', '#${propertyWrapper}', ${tenant?.id})">

                        <!-- The actionName? and controllerName? need for PDF generation! In PDF generation actionName and controllerName not set! -->
                        <g:if test="${!(actionName?.contains('survey') || controllerName?.contains('survey'))}">
                            <div class="two fields" style="margin-bottom:0">
                                <div class="field" style="margin-bottom:0">
                                    <select class="ui search selection dropdown remotePropertySearch" name="propIdent" data-desc="${prop_desc}"></select>
                                </div>
                                <div class="field" style="margin-bottom:0">
                                    <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                                </div>
                            </div>

                            <input type="hidden" name="ownerId"    value="${ownobj?.id}"/>
                            <input type="hidden" name="tenantId"   value="${tenant?.id}"/>
                            <input type="hidden" name="editable"   value="${editable}"/>
                            <input type="hidden" name="ownerClass" value="${ownobj?.class?.name}"/>
                            <input type="hidden" name="withoutRender" value="${withoutRender}"/>
                        </g:if>
                    </ui:remoteForm>

                    </td>
        </tr>
    </tfoot>
</g:if>
</table>
<g:if test="${error}">
    <ui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${error}"/>
</g:if>

<!-- O: templates/properties/_private -->
