<%@ page import="de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.Org; de.laser.I10nTranslation" %>

<laser:htmlStart message="menu.institutions.private_props" serviceInjection="true"/>

        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
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

    <ui:h1HeaderWithIcon message="menu.institutions.manage_props" />

    <laser:render template="nav" />

    <ui:messages data="${flash}" />

    <g:if test="${propertyDefinitions}">

        <div class="ui styled fluid accordion">
            <g:each in="${propertyDefinitions}" var="entry">
                <%
                    String active = ""
                    if(desc == entry.key)
                        active = "active"
                %>
                <div class="${active} title">
                    <i class="dropdown icon"></i>
                    <g:message code="propertyDefinition.${entry.key}.label" />
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
                                        <th class="la-action-info">${message(code:'default.actions.label')}</th>
                                    </g:if>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each in="${entry.value}" var="pd">
                                    <tr>
                                        <td>
                                            <g:if test="${pd.isHardData}">
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.hardData.tooltip')}">
                                                    <i class="check circle icon green"></i>
                                                </span>
                                            </g:if>
                                            <g:if test="${pd.multipleOccurrence}">
                                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
                                                    <i class="redo icon orange"></i>
                                                </span>
                                            </g:if>

                                            <g:if test="${pd.isUsedForLogic}">
                                                <span data-position="top left"  class="la-popup-tooltip la-delay" data-content="${message(code:'default.isUsedForLogic.tooltip')}">
                                                    <i class="ui icon orange cube"></i>
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
                                                        <div class="ui blue circular label">
                                                            ${pd.countOwnUsages()}
                                                        </div>
                                                    </g:link>
                                                </g:if>
                                                <g:elseif test="${pd.descr == PropertyDefinition.SUB_PROP}">
                                                    <g:link controller="myInstitution" action="currentSubscriptions" params="${[filterPropDef:genericOIDService.getOID(pd),status:'FETCH_ALL']}">
                                                        <div class="ui blue circular label">
                                                            ${pd.countOwnUsages()}
                                                        </div>
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
                                                            params="${[cmd: 'toggleMandatory', pd: genericOIDService.getOID(pd)]}" class="ui icon yellow button la-modern-button la-popup-tooltip la-delay">
                                                        <i class="star icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMandatory.label')}" data-position="left center"
                                                            params="${[cmd: 'toggleMandatory', pd: genericOIDService.getOID(pd)]}" class="ui icon blue button la-modern-button la-popup-tooltip la-delay">
                                                        <i class="la-star slash icon"></i>
                                                    </g:link>
                                                </g:else>
                                                <g:if test="${!multiplePdList?.contains(pd.id)}">
                                                    <g:if test="${pd.multipleOccurrence}">
                                                        <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.unsetMultiple.label')}" data-position="left center"
                                                                params="${[cmd: 'toggleMultipleOccurrence', pd: genericOIDService.getOID(pd)]}" class="ui icon orange button la-modern-button la-popup-tooltip la-delay">
                                                            <i class="redo slash icon"></i>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <g:link action="managePrivatePropertyDefinitions" data-content="${message(code:'propertyDefinition.setMultiple.label')}" data-position="left center" params="${[cmd: 'toggleMultipleOccurrence', pd: genericOIDService.getOID(pd)]}" class="ui icon blue button la-modern-button la-popup-tooltip la-delay">
                                                            <i class="la-redo slash icon"></i>
                                                        </g:link>
                                                    </g:else>
                                                </g:if>
                                                <g:if test="${pd.countUsages()==0}">
                                                    <g:link action="managePrivatePropertyDefinitions"
                                                            params="[cmd:'delete', deleteIds: pd?.id]"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [fieldValue(bean: pd, field: "name_de")])}"
                                                            data-confirm-term-how="delete"
                                                            class="ui icon negative button la-modern-button js-open-confirm-modal"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                        <i class="trash alternate outline icon"></i>
                                                    </g:link>
                                                </g:if>
                                                <g:else>
                                                    <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                        <button class="ui icon blue button la-modern-button" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                                data-xcg-pd="${pd.class.name}:${pd.id}"
                                                                data-xcg-type="${pd.type}"
                                                                data-xcg-rdc="${pd.refdataCategory}"
                                                                data-xcg-debug="${pd.getI10n('name')}">
                                                            <i class="exchange icon"></i>
                                                        </button>
                                                    </span>
                                                </g:else>
                                            </g:if>
                                            <g:elseif test="${changeProperties && pd.countOwnUsages() > 0}">
                                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'propertyDefinition.exchange.label')}">
                                                    <button class="ui icon blue button la-modern-button" data-href="#replacePropertyDefinitionModal" data-ui="modal"
                                                            data-xcg-pd="${pd.class.name}:${pd.id}"
                                                            data-xcg-type="${pd.type}"
                                                            data-xcg-rdc="${pd.refdataCategory}"
                                                            data-xcg-debug="${pd.getI10n('name')}"
                                                    ><i class="exchange icon"></i></button>
                                                </span>
                                            </g:elseif>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </g:form>
                </div>
            </g:each>
        </div>
     </g:if>

    <laser:render template="/myInstitution/replacePropertyDefinition" model="[action: actionName]"/>

    <ui:modal id="addPropertyDefinitionModal" message="propertyDefinition.create_new.label">

        <g:form class="ui form" action="managePrivatePropertyDefinitions" >
            <g:field type="hidden" name="cmd" value="add" />

            <div class="field required">
                <label class="property-label" for="pd_name">Name <g:message code="messageRequiredField" /></label>
                <input type="text" name="pd_name" id="pd_name" />
            </div>

            <div class="field">
                <label class="property-label" for="pd_expl">${message(code:'propertyDefinition.expl.label')}</label>
                <textarea name="pd_expl" id="pd_expl" class="ui textarea" rows="2"></textarea>
            </div>

            <div class="fields">

                <div class="field six wide required">
                    <label class="property-label" for="pd_descr">${message(code:'default.description.label')} <g:message code="messageRequiredField" /></label>
                    <%
                        Map<String,Object> availablePrivateDescr = [:]
                        Set<String> availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR
                        if(institution.getCustomerType() == "ORG_INST")
                            availablePrivDescs = PropertyDefinition.AVAILABLE_PRIVATE_DESCR-PropertyDefinition.SVY_PROP
                        availablePrivDescs.each { String pd ->
                            availablePrivateDescr[pd] = message(code:"propertyDefinition.${pd}.label")
                        }
                    %>
                    <g:select name="pd_descr" id="pd_descr" class="ui dropdown" optionKey="key" optionValue="value"
                        from="${availablePrivateDescr.entrySet()}" noSelection="${[null:message(code:'default.select.choose.label')]}"/>
                </div>

                <div class="field five wide required">
                    <label class="property-label" for="cust_prop_modal_select"><g:message code="default.type.label" /> <g:message code="messageRequiredField" /></label>
                    <g:select class="ui dropdown"
                        from="${PropertyDefinition.validTypes.entrySet()}"
                        optionKey="key" optionValue="${{PropertyDefinition.getLocalizedValue(it.key)}}"
                        noSelection="${[null:message(code:'default.select.choose.label')]}"
                        name="pd_type"
                        id="cust_prop_modal_select" />
                </div>

                <div class="field four wide">
                    <label class="property-label">Optionen</label>

                    <g:checkBox type="text" name="pd_mandatory" /> ${message(code:'default.mandatory.tooltip')}
                    <br />
                    <g:checkBox type="text" name="pd_multiple_occurrence" /> ${message(code:'default.multipleOccurrence.tooltip')}
                </div>

            </div>

            <div class="fields">
                <div class="field hide" id="cust_prop_ref_data_name" style="width: 100%">
                    <label class="property-label"><g:message code="refdataCategory.label" /></label>
                    <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>

                    <div class="ui grid" style="margin-top:1em">
                        <div class="ten wide column">
                            <g:each in="${propertyService.getRefdataCategoryUsage()}" var="cat">

                                <p class="hidden" data-prop-def-desc="${cat.key}">
                                    Häufig verwendete Kategorien: <br />

                                    <%
                                        List catList =  cat.value?.take(3)
                                        catList = catList.collect { entry ->
                                            '&nbsp; - ' + (RefdataCategory.getByDesc(entry[0]))?.getI10n('desc')
                                        }
                                        println catList.join('<br />')
                                    %>

                                </p>
                            </g:each>
                        </div>
                        <div class="six wide column">
                            <br />
                            <a href="<g:createLink controller="profile" action="properties" />" target="_blank">
                                <i class="icon external alternate"></i>
                                Alle Kategorien und Referenzwerte<br />als Übersicht öffnen
                            </a>
                        </div>
                    </div><!-- .grid -->
                </div>
            </div>

        </g:form>
    </ui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">

    $('#pd_descr').change(function() {
        $('#cust_prop_modal_select').trigger('change');
    });

    $('#cust_prop_modal_select').change(function() {
        var selectedText = $( "#cust_prop_modal_select option:selected" ).val();
        if( selectedText == "${RefdataValue.name}") {
            $("#cust_prop_ref_data_name").show();

            var $pMatch = $( "p[data-prop-def-desc='" + $( "#pd_descr option:selected" ).val() + "']" )
            if ($pMatch) {
                $( "p[data-prop-def-desc]" ).addClass('hidden')
                $pMatch.removeClass('hidden')
            }
        }
        else {
            $("#cust_prop_ref_data_name").hide();
        }
    });

    $('#cust_prop_modal_select').trigger('change');

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
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
            url: '${createLink(controller:'ajaxJson', action:'lookup')}',
            dataType: 'json',
            data: function (term, page) {
                return {
                    q: term, // search term
                    page_limit: 10,
                    baseClass:'${RefdataCategory.class.name}'
                };
            },
            results: function (data, page) {
                return {results: data.values};
            }
        }
    });

    </laser:script>

<laser:htmlEnd />
