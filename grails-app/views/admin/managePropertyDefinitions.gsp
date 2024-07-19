<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.I10nTranslation; grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="menu.admin.managePropertyDefinitions" serviceInjection="true"/>

		<ui:breadcrumbs>
			<ui:crumb message="menu.admin" controller="admin" action="index" />
			<ui:crumb message="menu.admin.managePropertyDefinitions" class="active"/>
		</ui:breadcrumbs>

%{--
        <ui:controlButtons>
            <laser:render template="actions"/>
            <button class="${Btn.SIMPLE}" value="" data-href="#addPropertyDefinitionModal" data-ui="modal" >${message(code:'propertyDefinition.create_new.label')}</button>
            <%-- included in case someone of the admins wishes this export
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:link class="item" action="managePropertyDefinitions" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
                </ui:exportDropdownItem>
            </ui:exportDropdown>--%>
        </ui:controlButtons>
--}%

        <ui:h1HeaderWithIcon message="menu.admin.managePropertyDefinitions" type="admin"/>

		<ui:messages data="${flash}" />

		<div class="ui styled fluid accordion">
			<g:each in="${propertyDefinitions}" var="entry">
                <div class="title">
                    <i class="dropdown icon"></i>
                    <g:message code="propertyDefinition.${entry.key}.label" />
                </div>
                <div class="content">
                    <table class="ui celled la-js-responsive-table la-table compact table">
                        <thead>
                        <tr>
                            <th></th>
                            <th>${message(code:'propertyDefinition.key.label')}</th>
                            <th>${message(code:'propertyDefinition.name.de.label')}</th>
                            <th>${message(code:'propertyDefinition.name.en.label')}</th>
                            <th>${message(code:'propertyDefinition.expl.de.label')}</th>
                            <th>${message(code:'propertyDefinition.expl.en.label')}</th>
                            <th></th>
                            <th class="la-action-info">${message(code:'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <g:each in="${entry.value}" var="pd">
                                <tr>
                                    <td>
                                        <g:if test="${pd.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                <i class="${Icon.PROP.HARDDATA}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="${Icon.PROP.MULTIPLE}"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${usedPdList?.contains(pd.id)}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                                <i class="${Icon.PROP.IN_USE}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.isUsedForLogic}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
                                                <i class="${Icon.PROP.LOGIC}"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${pd.isUsedForLogic}">
                                            <span class="sc_red">${fieldValue(bean: pd, field: "name")}</span>
                                        </g:if>
                                        <g:else>
                                            ${fieldValue(bean: pd, field: "name")}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="name_de" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('name', 'de')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="name_en" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('name', 'en')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="expl_de" type="textarea" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('expl', 'de')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="expl_en" type="textarea" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('expl', 'en')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                        <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${pd.refdataCategory}">
                                                <small>${pd.type?.split('\\.').last()}</small>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <small>${pd.type?.split('\\.').last()}</small>
                                        </g:else>
                                    </td>
                                    <td class="x">

                                        <g:if test="${pd.mandatory}">
                                            <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMandatory.label')}" data-position="top left"
                                                    params="${[cmd: 'toggleMandatory', pd: pd.id]}" class="${Btn.MODERN.BASIC_ICON} yellow la-popup-tooltip la-delay">
                                                <i class="${Icon.PROP.MANDATORY}"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMandatory.label')}" data-position="top left"
                                                    params="${[cmd: 'toggleMandatory', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_ICON_TOOLTIP} la-delay">
                                                <i class="la-star slash icon"></i>
                                            </g:link>
                                        </g:else>
                                        <g:if test="${!multiplePdList?.contains(pd.id)}">
                                            <g:if test="${pd.multipleOccurrence}">
                                                <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMultiple.label')}" data-position="top left"
                                                        params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.BASIC_ICON} orange la-popup-tooltip la-delay">
                                                    <i class="redo slash icon"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMultiple.label')}" data-position="top left"
                                                        params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_ICON_TOOLTIP} la-delay">
                                                    <i class="la-redo slash icon"></i>
                                                </g:link>
                                            </g:else>
                                        </g:if>

                                        <g:if test="${(pd.descr == PropertyDefinition.SUB_PROP) && !PropertyDefinition.findByNameAndDescrAndTenant(pd.name, PropertyDefinition.SVY_PROP, null)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.copySubPropToSurProp.label')}">
                                                <g:link class="${Btn.MODERN.SIMPLE_ICON}" action="transferSubPropToSurProp" params="[propertyDefinition: pd.id]">
                                                    <i class="${Icon.CMD.COPY}"></i>
                                                </g:link>
                                            </span>
                                        </g:if>

                                        <sec:ifAnyGranted roles="ROLE_YODA">
                                            <g:if test="${usedPdList?.contains(pd.id)}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="${Btn.MODERN.SIMPLE_ICON}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')} (${pd.name})"
                                                    ><i class="${Icon.CMD.REPLACE}"></i></button>
                                                </span>
                                            </g:if>
                                        </sec:ifAnyGranted>

                                        <g:if test="${! pd.isHardData && ! usedPdList?.contains(pd.id)}">
                                            <g:link controller="admin" action="managePropertyDefinitions"
                                                    params="${[cmd: 'deletePropertyDefinition', pd: pd.id]}" class="${Btn.MODERN.NEGATIVE_ICON}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="${Icon.CMD.DELETE}"></i>
                                            </g:link>
                                        </g:if>
                                    </td>

                                </tr>
                            </g:each>

                        </tbody>
                    </table>
                </div>
			</g:each>
        </div>

        <laser:render template="/myInstitution/replacePropertyDefinition" model="[action: actionName]"/>

<laser:htmlEnd />
