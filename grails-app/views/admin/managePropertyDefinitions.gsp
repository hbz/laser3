<%@ page import="de.laser.ui.CSS; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.I10nTranslation; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.finance.CostInformationDefinition" %>

<laser:htmlStart message="menu.admin.managePropertyDefinitions" />

		<ui:breadcrumbs>
			<ui:crumb message="menu.admin" controller="admin" action="index" />
			<ui:crumb message="menu.admin.managePropertyDefinitions" class="active"/>
		</ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="menu.admin.managePropertyDefinitions" type="admin"/>

		<ui:messages data="${flash}" />

		<div class="ui styled fluid accordion">
			<g:each in="${propertyDefinitions}" var="entry">
                <g:if test="${entry.key == CostInformationDefinition.COST_INFORMATION}">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <g:message code="costInformationDefinition.label" /> (${entry.value.size()})
                    </div>
                    <div class="content">
                        <table class="${CSS.ADMIN_TABLE}">
                            <thead>
                                <tr>
                                    <th></th>
                                    <g:sortableColumn property="name" title="${message(code:'propertyDefinition.key.label')}"/>
                                    <g:sortableColumn property="name_de" title="${message(code:'propertyDefinition.name.de.label')}"/>
                                    <g:sortableColumn property="name_en" title="${message(code:'propertyDefinition.name.en.label')}"/>
                                    <th>${message(code:'propertyDefinition.expl.de.label')}</th>
                                    <th>${message(code:'propertyDefinition.expl.en.label')}</th>
                                    <th></th>
                                    <th class="center aligned">
                                        <ui:optionsIcon />
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each in="${entry.value}" var="cif">
                                    <tr>
                                        <td>
                                            <g:if test="${!cif.isHardData}">
                                                <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.hardData.not.tooltip')}">
                                                    <i class="${Icon.PROP.HARDDATA_NOT}"></i>
                                                </span>
                                            </g:if>
                                            <g:elseif test="${usedPdList?.contains(genericOIDService.getOID(cif))}">
                                                <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.dataIsUsed.tooltip', args:[cif.id])}">
                                                    <i class="${Icon.PROP.IN_USE}"></i>
                                                </span>
                                            </g:elseif>
                                        </td>
                                        <td>
                                            ${cif.name}
                                        </td>
                                        <td>
                                            <g:if test="${!cif.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                                <ui:xEditable owner="${cif}" field="name_de" />
                                            </g:if>
                                            <g:else>
                                                ${cif.getI10n('name', 'de')}
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${!cif.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                                <ui:xEditable owner="${cif}" field="name_en" />
                                            </g:if>
                                            <g:else>
                                                ${cif.getI10n('name', 'en')}
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${!cif.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                                <ui:xEditable owner="${cif}" field="expl_de" type="textarea" />
                                            </g:if>
                                            <g:else>
                                                ${cif.getI10n('expl', 'de')}
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${!cif.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                                <ui:xEditable owner="${cif}" field="expl_en" type="textarea" />
                                            </g:if>
                                            <g:else>
                                                ${cif.getI10n('expl', 'en')}
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:set var="pdRdc" value="${cif.type?.split('\\.').last()}"/>
                                            <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${cif.refdataCategory}">
                                                    <small>${cif.type?.split('\\.').last()}</small>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <small>${cif.type?.split('\\.').last()}</small>
                                            </g:else>
                                        </td>
                                        <td class="x">
                                            <%-- TODO later
                                            <sec:ifAnyGranted roles="ROLE_YODA">
                                                <g:if test="${usedPdList?.contains(genericOIDService.getOID(pd))}">
                                                    <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                        <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                                data-xcg-pd="${pd.class.name}:${pd.id}"
                                                                data-xcg-type="${pd.type}"
                                                                data-xcg-rdc="${pd.refdataCategory}"
                                                                data-xcg-debug="${pd.getI10n('name')} (${pd.name})"
                                                        ><i class="${Icon.CMD.REPLACE}"></i></button>
                                                    </span>
                                                </g:if>
                                                <g:else>
                                                    <div class="${Btn.MODERN.SIMPLE} disabled"><icon:placeholder/></div>
                                                </g:else>
                                            </sec:ifAnyGranted>

                                            <g:if test="${! pd.isHardData && ! usedPdList?.contains(genericOIDService.getOID(pd))}">
                                                <g:link controller="admin" action="managePropertyDefinitions"
                                                        params="${[cmd: 'deletePropertyDefinition', pd: pd.id]}" class="${Btn.MODERN.NEGATIVE}"
                                                        role="button"
                                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                </g:link>
                                            </g:if>--%>
                                        </td>

                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                </g:if>
                <g:else>
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <g:message code="propertyDefinition.${entry.key}.label" /> (${entry.value.size()})
                    </div>
                    <div class="content">
                        <table class="${CSS.ADMIN_TABLE}">
                            <thead>
                            <tr>
                                <th></th>
                                <g:sortableColumn property="name" title="${message(code:'propertyDefinition.key.label')}"/>
                                <g:sortableColumn property="name_de" title="${message(code:'propertyDefinition.name.de.label')}"/>
                                <g:sortableColumn property="name_en" title="${message(code:'propertyDefinition.name.en.label')}"/>
                                <th>${message(code:'propertyDefinition.expl.de.label')}</th>
                                <th>${message(code:'propertyDefinition.expl.en.label')}</th>
                                <th></th>
                                <th class="center aligned">
                                    <ui:optionsIcon />
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${entry.value}" var="pd">
                                <g:set var="multipleOccurrenceError" value="${multiplePdList && multiplePdList.contains(pd.id) && !pd.multipleOccurrence}" />

                                <tr>
                                    <td>
                                        <g:if test="${!pd.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.hardData.not.tooltip')}">
                                                <i class="${Icon.PROP.HARDDATA_NOT}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.mandatory}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.mandatory.tooltip')}">
                                                <i class="${Icon.PROP.MANDATORY}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="${Icon.PROP.MULTIPLE}"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${multipleOccurrenceError}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrenceError.tooltip', args:[pd.id])}">
                                                <i class="icon exclamation circle red"></i>
                                            </span>
                                        </g:if>
                                        <g:elseif test="${usedPdList?.contains(genericOIDService.getOID(pd))}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                                <i class="${Icon.PROP.IN_USE}"></i>
                                            </span>
                                        </g:elseif>

                                        <g:if test="${pd.isUsedForLogic}">
                                            <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
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
                                            <span data-position="top right" class="la-popup-tooltip" data-content="${pd.refdataCategory}">
                                                <small>${pd.type?.split('\\.').last()}</small>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <small>${pd.type?.split('\\.').last()}</small>
                                        </g:else>
                                    </td>
                                    <td class="x">
                                        <g:if test="${(pd.descr == PropertyDefinition.SUB_PROP) && !PropertyDefinition.findByNameAndDescrAndTenant(pd.name, PropertyDefinition.SVY_PROP, null)}">
                                            <g:link action="transferSubPropToSurProp" data-content="${message(code:'propertyDefinition.copySubPropToSurProp.label')}" data-position="top left"
                                                    params="[propertyDefinition: pd.id]" class="${Btn.MODERN.SIMPLE_TOOLTIP}" >
                                                <i class="${Icon.CMD.COPY}"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <div class="${Btn.MODERN.SIMPLE} disabled"><icon:placeholder/></div>
                                        </g:else>

                                        <sec:ifAnyGranted roles="ROLE_YODA">
                                            <g:if test="${usedPdList?.contains(genericOIDService.getOID(pd)) && !multipleOccurrenceError}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')} (${pd.name})"
                                                    ><i class="${Icon.CMD.REPLACE}"></i></button>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <div class="${Btn.MODERN.SIMPLE} disabled"><icon:placeholder/></div>
                                            </g:else>
                                        </sec:ifAnyGranted>

                                        <g:if test="${! pd.isHardData && ! usedPdList?.contains(genericOIDService.getOID(pd))}">
                                            <g:link controller="admin" action="managePropertyDefinitions"
                                                    params="${[cmd: 'deletePropertyDefinition', pd: pd.id]}" class="${Btn.MODERN.NEGATIVE}"
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
                </g:else>
			</g:each>
        </div>

        <laser:render template="/myInstitution/replacePropertyDefinition" model="[action: actionName]"/>

<laser:htmlEnd />
