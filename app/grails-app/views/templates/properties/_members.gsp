%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.GenericOIDService" %>
<laser:serviceInjection />

<%-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}
<g:set var="overwriteEditable" value="${editable || accessService.checkPermAffiliationX('ORG_INST','INST_EDITOR','ROLE_ADMIN')}" />--%>

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<table class="ui la-table-small la-table-inCard table">
    <tbody>
        <g:each in="${memberProperties}" var="propType">
            <tr>
                <td>
                    <g:if test="${editable == true && subscription}">
                        <g:link action="propertiesMembers" params="${[id:subscription.id,filterPropDef:GenericOIDService.getOID(propType)]}" >
                            <g:if test="${propType.getI10n('expl') != null && !propType.getI10n('expl').contains(' °')}">
                                ${propType.getI10n('name')}
                                <g:if test="${propType.getI10n('expl')}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${propType.getI10n('expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </g:if>
                            </g:if>
                            <g:else>
                                ${propType.getI10n('name')}
                            </g:else>
                            <g:if test="${propType.mandatory}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.mandatory.tooltip')}">
                                    <i class="star icon yellow"></i>
                                </span>
                            </g:if>
                            <g:if test="${propType.multipleOccurrence}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                    <i class="redo icon orange"></i>
                                </span>
                            </g:if>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:if test="${propType.getI10n('expl') != null && !propType.getI10n('expl').contains(' °')}">
                            ${propType.getI10n('name')}
                            <g:if test="${propType.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${propType.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            ${propType.getI10n('name')}
                        </g:else>
                        <g:if test="${propType.mandatory}">
                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.mandatory.tooltip')}">
                                <i class="star icon yellow"></i>
                            </span>
                        </g:if>
                        <g:if test="${propType.multipleOccurrence}">
                            <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="redo icon orange"></i>
                            </span>
                        </g:if>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>

    <%--<g:if test="${editable}">
        <tfoot>
            <tr>
                <g:if test="${ownobj.privateProperties}">
                    <td colspan="4">
                </g:if>
                <g:else>
                    <td>
                </g:else>
                        <laser:remoteForm url="[controller: 'ajax', action: 'addPrivatePropertyValue']"
                                      name="cust_prop_add_value_private"
                                      class="ui form"
                                      data-update="${custom_props_div}"
                                      data-done="c3po.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})"
                                      data-always="c3po.loadJsAfterAjax()"
                        >
                        <g:if test="${!(actionName.contains('survey') || controllerName.contains('survey'))}">
                            <input type="hidden" name="propIdent"  data-desc="${prop_desc}" class="customPropSelect"/>
                            <input type="hidden" name="ownerId"    value="${ownobj?.id}"/>
                            <input type="hidden" name="tenantId"   value="${tenant?.id}"/>
                            <input type="hidden" name="editable"   value="${editable}"/>
                            <input type="hidden" name="ownerClass" value="${ownobj?.class}"/>

                            <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button js-wait-wheel"/>
                        </g:if>
                    </laser:remoteForm>

                    </td>
            </tr>
        </tfoot>
    </g:if>--%>
</table>
<g:if test="${error}">
    <semui:msg class="negative" header="${message(code: 'myinst.message.attention')}" text="${error}"/>
</g:if>