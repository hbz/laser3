<%@ page import="de.laser.I10nTranslation; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="menu.institutions.prop_defs" />
    <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.institutions.manage_props" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="managePropertyDefinitions" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
        <laser:render template="actions"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.institutions.manage_props" />

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

		<div class="ui styled fluid accordion">
			<g:each in="${propertyDefinitions}" var="entry">
                <div class="title">
                    <i class="dropdown icon"></i>
                    <g:message code="propertyDefinition.${entry.key}.label" default="${entry.key}" />
                </div>
                <div class="content">
                    <table class="ui celled la-js-responsive-table la-table compact table">
                        <thead>
                        <tr>
                            <th></th>
                            <th>${message(code:'default.name.label')}</th>
                            <th>${message(code:'propertyDefinition.expl.label')}</th>
                            <th>${message(code:'default.type.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <g:each in="${entry.value}" var="pd">
                                <tr>
                                    <td>
                                        <g:if test="${pd.isHardData}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="redo icon orange"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${pd.isUsedForLogic}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
                                                <i class="ui icon red cube"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="name_${languageSuffix}" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('name')}
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!pd.isHardData && SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                                            <ui:xEditable owner="${pd}" field="expl_${languageSuffix}" type="textarea" />
                                        </g:if>
                                        <g:else>
                                            ${pd.getI10n('expl')}
                                        </g:else>
                                    </td>
                                    <td>
                                        ${PropertyDefinition.getLocalizedValue(pd.type)}
                                        <g:if test="${pd.isRefdataValueType()}">
                                            <g:set var="refdataValues" value="${[]}"/>
                                            <g:each in="${RefdataCategory.getAllRefdataValues(pd.refdataCategory)}" var="refdataValue">
                                                <g:if test="${refdataValue.getI10n('value')}">
                                                    <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                                </g:if>
                                            </g:each>
                                            <br />
                                            (${refdataValues.join('/')})
                                        </g:if>
                                    </td>
                                </tr>
                            </g:each>

                        </tbody>
                    </table>
                </div>
			</g:each>
        </div>

<laser:htmlEnd />
