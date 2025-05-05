<%@ page import="de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.utils.LocaleUtils; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.Org; de.laser.I10nTranslation; de.laser.finance.CostInformationDefinition" %>
<laser:htmlStart message="menu.institutions.private_props" />

    <ui:breadcrumbs>
        <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
        <ui:crumb message="menu.institutions.manage_props" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="${actionName}" params="[cmd: 'exportXLS']">${message(code: 'default.button.export.xls')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
        <laser:render template="actions"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.institutions.manage_props" type="${contextService.getOrg().getCustomerType()}" />

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

    <g:if test="${propertyDefinitions}">

        <div class="ui styled fluid accordion">
            <g:each in="${propertyDefinitions}" var="entry">
                <%
                    String active = ""
                    if (desc == entry.key) { active = "active" }
                %>
                <g:if test="${entry.key == CostInformationDefinition.COST_INFORMATION}">
                    <div class="${active} title">
                        <i class="dropdown icon"></i>
                        <g:message code="costInformationDefinition.label" /> (${entry.value.size()})
                    </div>
                    <div class="${active} content">
                        <g:form class="ui form" action="managePrivatePropertyDefinitions" method="post">
                            <table class="ui celled la-js-responsive-table la-table compact table">
                                <thead>
                                <tr>
                                    <th>${message(code:'default.name.label')}</th>
                                    <th>${message(code:'propertyDefinition.expl.label')}</th>
                                    <th>${message(code:'default.type.label')}</th>
                                    <th>${message(code:'propertyDefinition.count.label')}</th>
                                    <g:if test="${editable || changeProperties}">
                                        <th class="center aligned">
                                            <ui:optionsIcon />
                                        </th>
                                    </g:if>
                                </tr>
                                </thead>
                                <tbody>
                                    <g:each in="${entry.value}" var="cif">
                                        <tr>
                                            <td>
                                                <ui:xEditable owner="${cif}" field="name_${languageSuffix}" />
                                            </td>
                                            <td>
                                                <ui:xEditable owner="${cif}" field="expl_${languageSuffix}" type="textarea" />
                                            </td>
                                            <td>
                                                ${PropertyDefinition.getLocalizedValue(cif.type)}
                                                <g:if test="${cif.type == RefdataValue.class.name}">
                                                    <g:set var="refdataValues" value="${[]}"/>
                                                    <g:each in="${RefdataCategory.getAllRefdataValues(cif.refdataCategory)}"
                                                            var="refdataValue">
                                                        <g:if test="${refdataValue.getI10n('value')}">
                                                            <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                                        </g:if>
                                                    </g:each>
                                                    <br />
                                                    (${refdataValues.join('/')})
                                                </g:if>
                                            </td>
                                            <td>
                                            <%-- TODO once cost item filter is set up, attach it here! --%>
                                                <div class="ui circular label">
                                                    ${cif.countOwnUsages()}
                                                </div>
                                            </td>
                                            <td class="x">
                                                <g:if test="${editable}">
                                                    <g:if test="${cif.countUsages()==0}">
                                                        <g:link action="managePrivatePropertyDefinitions"
                                                                params="[cmd:'delete', deleteIds: cif?.id]"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [fieldValue(bean: cif, field: "name_de")])}"
                                                                data-confirm-term-how="delete"
                                                                class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                                role="button"
                                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                            <i class="${Icon.CMD.DELETE}"></i>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                            <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                                    data-xcg-pd="${cif.class.name}:${cif.id}"
                                                                    data-xcg-type="${cif.type}"
                                                                    data-xcg-rdc="${cif.refdataCategory}"
                                                                    data-xcg-debug="${cif.getI10n('name')}">
                                                                <i class="${Icon.CMD.REPLACE}"></i>
                                                            </button>
                                                        </span>
                                                    </g:else>
                                                </g:if>
                                                <g:elseif test="${changeProperties && cif.countOwnUsages() > 0}">
                                                    <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                        <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                                data-xcg-pd="${cif.class.name}:${cif.id}"
                                                                data-xcg-type="${cif.type}"
                                                                data-xcg-rdc="${cif.refdataCategory}"
                                                                data-xcg-debug="${cif.getI10n('name')}"
                                                        ><i class="${Icon.CMD.REPLACE}"></i></button>
                                                    </span>
                                                </g:elseif>
                                            </td>
                                        </tr>
                                    </g:each>
                                </tbody>
                            </table>
                        </g:form>
                    </div>
                </g:if>
                <g:else>
                    <div class="${active} title">
                        <i class="dropdown icon"></i>
                        <g:message code="propertyDefinition.${entry.key}.label" /> (${entry.value.size()})
                    </div>
                    <div class="${active} content">
                        <g:form class="ui form" action="managePrivatePropertyDefinitions" method="post">
                            <table class="ui celled la-js-responsive-table la-table compact table">
                                <thead>
                                <tr>
                                    <th></th>
                                    <th>${message(code:'default.name.label')}</th>
                                    <th>${message(code:'propertyDefinition.expl.label')}</th>
                                    <th>${message(code:'default.type.label')}</th>
                                    <th>${message(code:'propertyDefinition.count.label')}</th>
                                    <g:if test="${editable || changeProperties}">
                                        <th class="center aligned">
                                            <ui:optionsIcon />
                                        </th>
                                    </g:if>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${entry.value}" var="pd">
                                    <g:set var="multipleOccurrenceError" value="${multiplePdList && multiplePdList.contains(pd.id) && !pd.multipleOccurrence}" />

                                    <tr>
                                        <td>
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
                                            <g:if test="${pd.isUsedForLogic}">
                                                <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
                                                    <i class="${Icon.PROP.LOGIC}"></i>
                                                </span>
                                            </g:if>
                                            <g:if test="${multipleOccurrenceError}">
                                                <span data-position="top left" class="la-popup-tooltip" data-content="${message(code:'default.multipleOccurrenceError.tooltip', args:[pd.id])}">
                                                    <i class="icon exclamation circle red"></i>
                                                </span>
                                            </g:if>
                                        </td>
                                        <td>
                                            <ui:xEditable owner="${pd}" field="name_${languageSuffix}" />
                                        </td>
                                        <td>
                                            <ui:xEditable owner="${pd}" field="expl_${languageSuffix}" type="textarea" />
                                        </td>
                                        <td>
                                            ${PropertyDefinition.getLocalizedValue(pd.type)}
                                            <g:if test="${pd.isRefdataValueType()}">
                                                <g:set var="refdataValues" value="${[]}"/>
                                                <g:each in="${RefdataCategory.getAllRefdataValues(pd.refdataCategory)}"
                                                        var="refdataValue">
                                                    <g:if test="${refdataValue.getI10n('value')}">
                                                        <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                                    </g:if>
                                                </g:each>
                                                <br />
                                                (${refdataValues.join('/')})
                                            </g:if>
                                        </td>
                                        <td>

                                                <g:if test="${pd.descr == PropertyDefinition.LIC_PROP}">
                                                    <g:link controller="myInstitution" action="currentLicenses" params="${[filterPropDef:genericOIDService.getOID(pd),filterSubmit:true]}">
                                                        <ui:bubble count="${pd.countOwnUsages()}" />
                                                    </g:link>
                                                </g:if>
                                                <g:elseif test="${pd.descr == PropertyDefinition.SUB_PROP}">
                                                    <g:link controller="myInstitution" action="currentSubscriptions" params="${[filterPropDef:genericOIDService.getOID(pd),status:'FETCH_ALL']}">
                                                        <ui:bubble count="${pd.countOwnUsages()}" />
                                                    </g:link>
                                                </g:elseif>
                                                <%-- TODO platforms and orgs do not have property filters yet, they must be built! --%>
                                                <g:else>
                                                    <div class="ui circular label">
                                                        ${pd.countOwnUsages()}
                                                    </div>
                                                </g:else>

                                        </td>
                                        <td class="x">
                                            <g:if test="${editable}">
                                                <g:if test="${pd.mandatory}">
                                                    <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMandatory.label')}" data-position="left center"
                                                            params="${[cmd: 'toggleMandatory', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">
                                                        <i class="${Icon.PROP.MANDATORY_NOT.replaceAll('yellow', '')}"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMandatory.label')}" data-position="left center"
                                                            params="${[cmd: 'toggleMandatory', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">
                                                        <i class="${Icon.PROP.MANDATORY.replaceAll('yellow', '')}"></i>
                                                    </g:link>
                                                </g:else>
                                                <g:if test="${pd.multipleOccurrence}">
                                                    <g:if test="${!multiplePdList?.contains(pd.id)}">
                                                        <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMultiple.label')}" data-position="left center"
                                                                params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">
                                                            <i class="${Icon.PROP.MULTIPLE_NOT.replaceAll('teal', '')}"></i>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                            <icon:placeholder /><%-- Hidden Fake Button --%>
                                                        </div>
                                                    </g:else>
                                                </g:if>
                                                <g:else>
                                                    <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMultiple.label')}"  data-position="left center"
                                                            params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">
                                                        <i class="${Icon.PROP.MULTIPLE.replaceAll('teal', '')}"></i>
                                                    </g:link>
                                                </g:else>
%{--                                                <g:if test="${!multiplePdList?.contains(pd.id)}">--}%
%{--                                                    <g:if test="${pd.multipleOccurrence}">--}%
%{--                                                        <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMultiple.label')}" data-position="left center"--}%
%{--                                                                params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">--}%
%{--                                                            <i class="${Icon.PROP.MULTIPLE_NOT.replaceAll('teal', '')}"></i>--}%
%{--                                                        </g:link>--}%
%{--                                                    </g:if>--}%
%{--                                                    <g:else>--}%
%{--                                                        <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMultiple.label')}"  data-position="left center"--}%
%{--                                                                params="${[cmd: 'toggleMultipleOccurrence', pd: pd.id]}" class="${Btn.MODERN.SIMPLE_TOOLTIP}">--}%
%{--                                                            <i class="${Icon.PROP.MULTIPLE.replaceAll('teal', '')}"></i>--}%
%{--                                                        </g:link>--}%
%{--                                                    </g:else>--}%
%{--                                                </g:if>--}%
                                                <g:if test="${pd.countUsages()==0}">
                                                    <g:link action="managePrivatePropertyDefinitions"
                                                            params="[cmd:'delete', deleteIds: pd?.id]"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [fieldValue(bean: pd, field: "name_de")])}"
                                                            data-confirm-term-how="delete"
                                                            class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                        <i class="${Icon.CMD.DELETE}"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                        <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                                data-xcg-pd="${pd.class.name}:${pd.id}"
                                                                data-xcg-type="${pd.type}"
                                                                data-xcg-rdc="${pd.refdataCategory}"
                                                                data-xcg-debug="${pd.getI10n('name')}">
                                                            <i class="${Icon.CMD.REPLACE}"></i>
                                                        </button>
                                                    </span>
                                                </g:else>
                                            </g:if>
                                            <g:elseif test="${changeProperties && pd.countOwnUsages() > 0}">
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="${Btn.MODERN.SIMPLE}" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')}"
                                                    ><i class="${Icon.CMD.REPLACE}"></i></button>
                                                </span>
                                            </g:elseif>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </g:form>
                    </div>
                </g:else>
            </g:each>
        </div>
     </g:if>

    <laser:render template="/myInstitution/replacePropertyDefinition" model="[action: actionName]"/>

    <ui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

        <g:form class="ui form" action="managePrivatePropertyDefinitions" >
            <input type="hidden" name="cmd" value="add"/>

            <div class="field required">
                <label for="pd_name">Name <g:message code="messageRequiredField" /></label>
                <input type="text" name="pd_name" id="pd_name" />
            </div>

            <div class="field">
                <label for="pd_expl">${message(code:'propertyDefinition.expl.label')}</label>
                <textarea name="pd_expl" id="pd_expl" class="ui textarea" rows="2"></textarea>
            </div>

            <div class="fields">

                <div class="field six wide required">
                    <label for="pd_descr">${message(code:'propertyDefinition.descr.label')} <g:message code="messageRequiredField" /></label>
                    <%
                        Map<String,Object> availablePrivateDescr = [:]
                        Set<String> availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR
                        if (contextService.getOrg().isCustomerType_Inst_Pro()) {
                            availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR - PropertyDefinition.SVY_PROP
                        }
                        availablePrivDescs.each { String pd ->
                            availablePrivateDescr[pd] = message(code:"propertyDefinition.${pd}.label")
                        }
                        availablePrivateDescr[CostInformationDefinition.COST_INFORMATION] = message(code:"costInformationDefinition.label")
                    %>
                    <g:select name="pd_descr" id="pd_descr" class="ui dropdown clearable" optionKey="key" optionValue="value"
                        from="${availablePrivateDescr.entrySet()}" noSelection="${[null:message(code:'default.select.choose.label')]}"/>
                </div>

                <div class="field five wide required propertyDefinitionField">
                    <label for="pd_type"><g:message code="propertyDefinition.type.label" /> <g:message code="messageRequiredField" /></label>
                    <g:select class="ui dropdown clearable"
                        from="${PropertyDefinition.validTypes.entrySet()}"
                        optionKey="key" optionValue="${{PropertyDefinition.getLocalizedValue(it.key)}}"
                        noSelection="${[null:message(code:'default.select.choose.label')]}"
                        name="pd_type"
                        id="pd_type" />
                </div>

                <div class="field four wide propertyDefinitionField">
                    <label>Optionen</label>

                    <g:checkBox type="text" name="pd_mandatory" /> ${message(code:'default.mandatory.tooltip')}
                    <br />
                    <g:checkBox type="text" name="pd_multiple_occurrence" /> ${message(code:'default.multipleOccurrence.tooltip')}
                </div>

            </div>

            <div id="refdataFormWrapper" class="propertyDefinitionField">
                <div class="field">
                    <label for="rdCatSelector"><g:message code="refdataCategory.label" /></label>
                    <select class="ui search selection dropdown la-not-clearable" id="rdCatSelector" name="refdatacategory">
                        <g:each in="${RefdataCategory.executeQuery('from RefdataCategory order by desc_' + LocaleUtils.getCurrentLang())}" var="rdc">
%{--                            <option value="">${message(code:'default.select.choose.label')}</option>--}%
                            <option value="${rdc.id}"${rdc.desc == RDConstants.Y_N_U ? ' selected' : ''}>${rdc.getI10n('desc')}</option>
                        </g:each>
                    </select>
                </div>
                <div class="field">
                    %{-- TODO: AJAX --}%
                    <label><g:message code="refdataValue.plural" /></label>
                    <g:each in="${RefdataCategory.executeQuery('from RefdataCategory order by desc_' + LocaleUtils.getCurrentLang())}" var="rdc">
                        <g:set var="h1ag352df" value="${RefdataValue.executeQuery('select value_' + LocaleUtils.getCurrentLang() + ' from RefdataValue where owner.id = ' + rdc.id + ' order by value_' + LocaleUtils.getCurrentLang())}"/>
                        <textarea style="display: none" class="rdCatValues" id="rdCatValues_${rdc.id}">${h1ag352df.join(', ')}</textarea>
                    </g:each>
                </div>
            </div>

        </g:form>
    </ui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">

    $('#pd_descr').change(function() {
        $('#pd_type').trigger('change');
        if($(this).val() == '${CostInformationDefinition.COST_INFORMATION}') {
            $('.propertyDefinitionField').hide();
        }
        else {
            $('.propertyDefinitionField').show();
            $('#pd_type').trigger('change');
        }
    });

    $('#pd_type').change(function() {
        var selectedText = $( "#pd_type option:selected" ).val();
        if( selectedText == "${RefdataValue.name}") {
            $("#refdataFormWrapper").show();
            $('#rdCatSelector').trigger('change');
        }
        else {
            $("#refdataFormWrapper").hide();
        }
    });

    $('#pd_type').trigger('change');
    $('#pd_descr').trigger('change');

    $('#rdCatSelector').on('change', function() {
        let rdc = $('#rdCatSelector').dropdown('get value')
        $('.rdCatValues').hide()
        $('#rdCatValues_' + rdc).show()
    });


    </laser:script>

<laser:htmlEnd />
