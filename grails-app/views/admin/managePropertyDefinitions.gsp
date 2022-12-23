<%@ page import="de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.I10nTranslation; grails.plugin.springsecurity.SpringSecurityUtils" %>

<laser:htmlStart message="menu.admin.managePropertyDefinitions" serviceInjection="true"/>

		<ui:breadcrumbs>
			<ui:crumb message="menu.admin" controller="admin" action="index" />
			<ui:crumb message="menu.admin.managePropertyDefinitions" class="active"/>
		</ui:breadcrumbs>

        <ui:controlButtons>
            <%--<laser:render template="actions"/>--%>
            <%--
            <button class="ui button" value="" data-href="#addPropertyDefinitionModal" data-ui="modal" >${message(code:'propertyDefinition.create_new.label')}</button>
            --%>
            <%-- included in case someone of the admins wishes this export
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:link class="item" action="managePropertyDefinitions" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
                </ui:exportDropdownItem>
            </ui:exportDropdown>--%>
        </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.admin.managePropertyDefinitions" type="admin"/>

        <br />
        <button class="ui button" value="" data-href="#addPropertyDefinitionModal" data-ui="modal" >${message(code:'propertyDefinition.create_new.label')}</button>
        <br />
        <br />

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
                                                <i class="check circle icon green"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                                <i class="redo icon orange"></i>
                                            </span>
                                        </g:if>

                                        <g:if test="${usedPdList?.contains(pd.id)}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                                <i class="info circle icon blue"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.isUsedForLogic}">
                                            <span data-position="top left" class="la-popup-tooltip la-delay" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
                                                <i class="ui icon red cube"></i>
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
                                                    params="${[cmd: 'toggleMandatory', pd: genericOIDService.getOID(pd)]}" class="ui icon yellow button la-modern-button la-popup-tooltip la-delay">
                                                <i class="star icon"></i>
                                            </g:link>
                                        </g:if>
                                        <g:else>
                                            <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMandatory.label')}" data-position="top left"
                                                    params="${[cmd: 'toggleMandatory', pd: genericOIDService.getOID(pd)]}" class="ui icon button blue la-modern-button la-popup-tooltip la-delay">
                                                <i class="la-star slash icon"></i>
                                            </g:link>
                                        </g:else>
                                        <g:if test="${!multiplePdList?.contains(pd.id)}">
                                            <g:if test="${pd.multipleOccurrence}">
                                                <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMultiple.label')}" data-position="top left"
                                                        params="${[cmd: 'toggleMultipleOccurrence', pd: genericOIDService.getOID(pd)]}" class="ui icon orange la-modern-button button la-popup-tooltip la-delay">
                                                    <i class="redo slash icon"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link action="managePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMultiple.label')}" data-position="top left"
                                                        params="${[cmd: 'toggleMultipleOccurrence', pd: genericOIDService.getOID(pd)]}" class="ui icon blue button la-modern-button la-popup-tooltip la-delay">
                                                    <i class="la-redo slash icon"></i>
                                                </g:link>
                                            </g:else>
                                        </g:if>

                                        <g:if test="${(pd.descr == PropertyDefinition.SUB_PROP) && !PropertyDefinition.findByNameAndDescrAndTenant(pd.name, PropertyDefinition.SVY_PROP, null)}">
                                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.copySubPropToSurProp.label')}">
                                                <g:link class="ui icon button blue la-modern-button" action="transferSubPropToSurProp" params="[propertyDefinition: pd.id]">
                                                    <i class="copy icon"></i>
                                                </g:link>
                                            </span>
                                        </g:if>

                                        <sec:ifAnyGranted roles="ROLE_YODA">
                                            <g:if test="${usedPdList?.contains(pd.id)}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="ui icon blue button la-modern-button" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')} (${pd.name})"
                                                    ><i class="exchange icon"></i></button>
                                                </span>
                                            </g:if>
                                        </sec:ifAnyGranted>

                                        <g:if test="${! pd.isHardData && ! usedPdList?.contains(pd.id)}">
                                            <g:link controller="admin" action="managePropertyDefinitions"
                                                    params="${[cmd: 'deletePropertyDefinition', pd: genericOIDService.getOID(pd)]}" class="ui icon negative button la-modern-button"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
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

        <ui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

            <g:form class="ui form" id="create_cust_prop" url="[controller: 'ajax', action: 'addCustomPropertyType']" >
                <input type="hidden" name="reloadReferer" value="/admin/managePropertyDefinitions"/>
                <input type="hidden" name="ownerClass" value="${this.class}"/>

				<div class="field">
                	<label class="property-label">Name</label>
                	<input type="text" name="cust_prop_name"/>
                </div>

                <div class="fields">
                    <div class="field five wide">
                        <label for="cust_prop_desc" class="property-label">Context:</label>
                        <select name="cust_prop_desc" id="cust_prop_desc" class="ui dropdown">
                            <g:each in="${PropertyDefinition.AVAILABLE_CUSTOM_DESCR}" var="pd">
                                <option value="${pd}"><g:message code="propertyDefinition.${pd}.label" /></option>
                            </g:each>
                        </select>
                    </div>
                    <div class="field five wide">
                        <label class="property-label"><g:message code="default.type.label" /></label>
                        <g:select class="ui dropdown"
                            from="${PropertyDefinition.validTypes.entrySet()}"
                            optionKey="key" optionValue="${{PropertyDefinition.getLocalizedValue(it.key)}}"
                            name="cust_prop_type"
                            id="cust_prop_modal_select" />
                    </div>
                    <div class="field five wide">
                        <label class="property-label">${message(code:'propertyDefinition.expl.label')}</label>
                        <textarea name="cust_prop_expl" id="eust_prop_expl" class="ui textarea"></textarea>
                    </div>

                    <div class="field six wide hide" id="cust_prop_ref_data_name">
                        <label class="property-label"><g:message code="refdataCategory.label" /></label>
                        <select id="cust_prop_refdatacatsearch" name="refdatacategory"></select>
                    </div>
                </div>

                <div class="fields">
                    <div class="field five wide">
                        <label class="property-label">${message(code:'default.multipleOccurrence.tooltip')}</label>
                        <g:checkBox type="text" name="cust_prop_multiple_occurence" />
                    </div>
                </div>

            </g:form>

        </ui:modal>

		<laser:script file="${this.getGroovyPageFileName()}">

            if( $( "#cust_prop_modal_select option:selected" ).val() == "${RefdataValue.class.name}") {
                $("#cust_prop_ref_data_name").show();
            } else {
                 $("#cust_prop_ref_data_name").hide();
            }

			$('#cust_prop_modal_select').change(function() {
				var selectedText = $( "#cust_prop_modal_select option:selected" ).val();
				if( selectedText == "${RefdataValue.class.name}") {
					$("#cust_prop_ref_data_name").show();
				}else{
					$("#cust_prop_ref_data_name").hide();
				}
			});

			$("#cust_prop_refdatacatsearch").select2({
				placeholder: "Kategorie eintippen...",
                minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },
                formatNoMatches: function() {
                    return "${message(code:'select2.noMatchesFound')}";
                },
                formatSearching:  function() {
                    return "${message(code:'select2.formatSearching')}";
                },
                allowClear: true,
				ajax: {
					url: '${createLink(controller:'ajaxJson', action:'lookup')}',
					dataType: 'json',
					data: function (p) {
						return {
							q:  p.term || '', // search term
							page_limit: 10,
							baseClass:'${RefdataCategory.class.name}'
						};
					},
					processResults: function (data) {
						return { results: data.values };
					}
				}
			});
		</laser:script>

<laser:htmlEnd />
