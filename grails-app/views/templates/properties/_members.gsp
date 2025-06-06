<!-- A: templates/properties/_members -->
%{-- To use, add the laser:render custom_props inside a div with id=custom_props_div_xxx --}%
%{-- on head of container page, and on window load execute  --}%
%{-- c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="de.laser.ui.Icon; de.laser.Subscription; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.properties.PropertyDefinition" %>
<laser:serviceInjection />

<%-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${contextService.isInstEditor()}
<g:set var="overwriteEditable" value="${editable || contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO )}" />--%>

<g:if test="${newProp}">
    <ui:errors bean="${newProp}" />
</g:if>
<g:if test="${subscription}">
    <g:set var="memberSubs" value="${Subscription.executeQuery('select s from Subscription s where s.instanceOf = :sub', [sub: subscription])}"/>
</g:if>
<table class="ui compact la-js-responsive-table la-table-inCard table">
    <tbody>
        <g:each in="${memberProperties}" var="propType">
            <tr>
                <td>
                    <g:if test="${editable == true && subscription}">
                        <g:link controller="subscription" action="membersSubscriptionsManagement" params="${[id:subscription.id, propertiesFilterPropDef:genericOIDService.getOID(propType), tab: 'properties']}" >
                            <g:if test="${propType.getI10n('expl') != null && !propType.getI10n('expl').contains(' °')}">
                                ${propType.getI10n('name')}
                                <g:if test="${propType.getI10n('expl')}">
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${propType.getI10n('expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </g:if>
                            </g:if>
                            <g:else>
                                ${propType.getI10n('name')}
                            </g:else>
                            <g:if test="${propType.mandatory}">
                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.mandatory.tooltip')}">
                                    <i class="${Icon.PROP.MANDATORY}"></i>
                                </span>
                            </g:if>
                            <g:if test="${propType.multipleOccurrence}">
                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                    <i class="${Icon.PROP.MULTIPLE}"></i>
                                </span>
                            </g:if>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:if test="${propType.getI10n('expl') != null && !propType.getI10n('expl').contains(' °')}">
                            ${propType.getI10n('name')}
                            <g:if test="${propType.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${propType.getI10n('expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </g:if>
                        </g:if>
                        <g:else>
                            ${propType.getI10n('name')}
                        </g:else>
                        <g:if test="${propType.mandatory}">
                            <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.mandatory.tooltip')}">
                                <i class="${Icon.PROP.MANDATORY}"></i>
                            </span>
                        </g:if>
                        <g:if test="${propType.multipleOccurrence}">
                            <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                <i class="${Icon.PROP.MULTIPLE}"></i>
                            </span>
                        </g:if>
                    </g:else>
                </td>
                <td class="x">
                    <span class="la-popup-tooltip" data-content="${message(code:'property.notInherited.fromConsortia2')}" data-position="top right"><i class="large icon cart arrow down grey"></i></span>
                    <g:if test="${memberSubs}">
                        (<span data-content="${message(code:'property.notInherited.info.propertyCount')}"><i class="icon sticky note grey"></i></span> ${SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null and sp.type = :type', [subscriptionSet: memberSubs, context: contextService.getOrg(), type: propType]).size() ?: 0} / <span data-content="${message(code:'property.notInherited.info.membersCount')}"><i class="${Icon.SUBSCRIPTION} grey"></i></span> ${memberSubs.size() ?: 0})
                    </g:if>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>
<!-- O: templates/properties/_members -->