%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; java.net.URL" %>
<laser:serviceInjection />

<!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')} -->
<g:set var="overwriteEditable" value="${editable || accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}" />

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<g:if test="${error}">
    <bootstrap:alert class="alert-danger">${error}</bootstrap:alert>
</g:if>

<table class="ui la-table-small la-table-inCard table">
    <g:if test="${ownobj.privateProperties}">
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
                <th class="la-js-dont-hide-this-card">${message(code:'property.table.property')}</th>
                <th>${message(code:'property.table.value')}</th>
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                <th>${message(code:'default.actions')}</th>
            </tr>
        </thead>
    </g:if>
    <tbody>
        <g:each in="${ownobj.privateProperties.sort{a, b -> a.type.getI10n('name').compareToIgnoreCase b.type.getI10n('name')}}" var="prop">
            <g:if test="${prop.type?.tenant?.id == tenant?.id}">
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
                        <g:if test="${prop.type.mandatory}">
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
                            <semui:xEditable owner="${prop}" type="url" field="urlValue" overwriteEditable="${overwriteEditable}" class="la-overflow la-ellipsis"/>
                            <g:if test="${prop.value}">
                                <semui:linkIcon href="${prop.value}" />
                            </g:if>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}" overwriteEditable="${overwriteEditable}" />
                        </g:elseif>

                    </td>
                    <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                        <td>
                            <semui:xEditable owner="${prop}" type="textarea" field="paragraph"/>
                        </td>
                    </g:if>
                    <td>
                        <semui:xEditable owner="${prop}" type="textarea" field="note" overwriteEditable="${overwriteEditable}" />
                    </td>
                    <td class="x">
                        <g:if test="${overwriteEditable == true}">
                            <laser:remoteLink class="ui icon negative button js-open-confirm-modal"
                                              controller="ajax"
                                              action="deletePrivateProperty"
                                              params='[propClass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]'
                                              id="${prop.id}"
                                              data-confirm-term-what="property"
                                              data-confirm-term-what-detail="${prop.type.getI10n('name')}"
                                              data-confirm-term-how="delete"
                                              data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}')"
                                              data-always="c3po.loadJsAfterAjax()"
                                              data-update="${custom_props_div}"
                            >
                                <i class="trash alternate icon"></i>
                            </laser:remoteLink>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>

    <g:if test="${overwriteEditable}">
        <tfoot>
            <tr>
                <td colspan="4">
                    <g:formRemote url="[controller: 'ajax', action: 'addPrivatePropertyValue']" method="post"
                                  name="cust_prop_add_value"
                                  class="ui form"
                                  update="${custom_props_div}"
                                  onSuccess="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})"
                                  onComplete="c3po.loadJsAfterAjax()"
                    >

                    <input type="hidden" name="propIdent"  data-desc="${prop_desc}" class="customPropSelect"/>
                    <input type="hidden" name="ownerId"    value="${ownobj?.id}"/>
                    <input type="hidden" name="tenantId"   value="${tenant?.id}"/>
                    <input type="hidden" name="editable"   value="${editable}"/>
                    <input type="hidden" name="ownerClass" value="${ownobj?.class}"/>

                    <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                </g:formRemote>

            </td>
        </tr>
    </tfoot>
</g:if>
</table>