<!-- A: templates/properties/_propertyGroupModal -->
<%@ page import="de.laser.finance.CostInformationDefinitionGroup; de.laser.survey.SurveyConfig; de.laser.survey.SurveyResult; de.laser.survey.SurveyInfo; de.laser.utils.LocaleUtils; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.RefdataCategory; de.laser.properties.PropertyDefinitionGroupItem; de.laser.properties.PropertyDefinition; de.laser.finance.CostInformationDefinition; de.laser.I10nTranslation; de.laser.FormService; de.laser.RefdataValue; de.laser.ui.Icon"%>
<laser:serviceInjection />

<ui:modal id="propDefGroupModal" message="propertyDefinitionGroup.create_new.label" msgSave="${createOrUpdate}">

    <g:form class="ui form" url="${formUrl}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <input type="hidden" name="cmd" value="processing"/>

        <g:if test="${pdGroup}">
            <input type="hidden" name="oid" value="${genericOIDService.getOID(pdGroup)}"/>
        </g:if>
        <g:elseif test="${costInformationsInUse}">
            <input type="hidden" name="costInformationsInUse" value="true"/>
        </g:elseif>

        <g:if test="${!costInformationsInUse}">
            <div class="ui two column grid">
                <div class="column">

                    <div class="field required">
                        <label for="prop_name"><g:message code="default.name.label"/> <g:message code="messageRequiredField" /></label>
                        <input type="text" name="name" id="prop_name" value="${pdGroup?.name}"/>
                    </div>

                    <div class="field required">
                        <label for="prop_descr_selector"><g:message code="propertyDefinitionGroup.editModal.category"/> <g:message code="messageRequiredField" /></label>
                        <select name="prop_descr" id="prop_descr_selector" class="ui dropdown clearable">
                            <g:each in="${PropertyDefinition.AVAILABLE_GROUPS_DESCR}" var="pdDescr">
                            <%-- TODO: REFACTORING: x.class.name with pd.desc --%>
                                <g:if test="${pdDescr == PropertyDefinition.LIC_PROP && pdGroup?.ownerType == License.class.name}">
                                    <option selected="selected" value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" /></option>
                                </g:if>
                                <g:elseif test="${pdDescr == PropertyDefinition.ORG_PROP && pdGroup?.ownerType == Org.class.name}">
                                    <option selected="selected" value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" /></option>
                                </g:elseif>
                                <g:elseif test="${pdDescr == PropertyDefinition.SUB_PROP && pdGroup?.ownerType == Subscription.class.name}">
                                    <option selected="selected" value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" /></option>
                                </g:elseif>
                                <g:elseif test="${pdDescr == PropertyDefinition.SVY_PROP && pdGroup?.ownerType == SurveyConfig.class.name}">
                                    <option selected="selected" value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" /></option>
                                </g:elseif>
                                <g:else>
                                    <option value="${pdDescr}"><g:message code="propertyDefinition.${pdDescr}.label" /></option>
                                </g:else>
                            <%-- TODO: REFACTORING: x.class.name with pd.desc --%>
                            </g:each>
                        </select>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="field">
                        <label><g:message code="propertyDefinitionGroup.editModal.description"/></label>
                        <textarea name="description">${pdGroup?.description}</textarea>
                    </div>
                </div><!-- .column -->
            </div><!-- .grid -->
            <br />
            <br />
        </g:if>
        <div class="ui grid">
            <div class="field" style="width:100%">
                <label><g:message code="propertyDefinitionGroup.editModal.properties"/></label>

                <div class="scrollWrapper">
                    <g:if test="${costInformationsInUse}">
                        <table class="ui table compact scrollContent">
                            <tbody>
                                <g:each in="${CostInformationDefinition.findAllByTenantIsNullOrTenant(contextService.getOrg(), [sort: 'name_' + LocaleUtils.getCurrentLang()])}" var="cif">
                                    <tr>
                                        <td>
                                            ${cif.getI10n('name')}
                                            <g:if test="${cif.tenant == contextService.getOrg()}"><i class="${Icon.PROP.IS_PRIVATE}"></i></g:if>
                                        </td>
                                        <td>
                                            ${cif.getI10n('expl') != 'null' ? cif.getI10n('expl') : ''}
                                        </td>
                                        <td>
                                            <g:if test="${cif.type == RefdataValue.class.name}">
                                                <g:set var="refDataCat" value="${cif.refdataCategory ? RefdataCategory.getByDesc(cif.refdataCategory) : null}" />
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${refDataCat?.getI10n('desc')}">
                                                    <small>${CostInformationDefinition.getLocalizedValue(cif.type)}</small>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <small>${CostInformationDefinition.getLocalizedValue(cif.type)}</small>
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${CostInformationDefinitionGroup.findByCostInformationDefinitionAndTenant(cif, contextService.getOrg())}">
                                                <input type="checkbox" checked="checked" name="costInformationDefinition" value="${cif.id}" />
                                            </g:if>
                                            <g:else>
                                                <input type="checkbox" name="costInformationDefinition" value="${cif.id}" />
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </g:if>
                    <g:else>
                        <g:each in="${PropertyDefinition.AVAILABLE_GROUPS_DESCR}" var="pdDescr">
                            <table class="ui table compact hidden scrollContent" data-propDefTable="${pdDescr}">
                                <tbody>
                                <g:each in="${PropertyDefinition.findAllByTenantIsNullAndDescr(pdDescr, [sort: 'name_' + LocaleUtils.getCurrentLang()])}" var="pd">

                                %{-- <%
                                    List<PropertyDefinition> matches = PropertyDefinition.executeQuery(
                                            'select pd from PropertyDefinition pd where pd.tenant is null and pd.descr = :pdDescr order by :order',
                                            [pdDescr: pdDescr, order: 'name_' + LocaleHelper.getCurrentLang() ])
                                %>
                                <g:each in="${matches}" var="pd"> --}%
                                    <tr>
                                        <td>
                                            ${pd.getI10n('name')}
                                        </td>
                                        <td>
                                            <g:set var="pdExpl" value="${pd.getI10n('expl')}" />
                                            ${pdExpl != 'null' ? pdExpl : ''}
                                        </td>
                                        <td>
                                            <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                            <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                                <g:set var="refDataCat" value="${pd.refdataCategory ? RefdataCategory.getByDesc(pd.refdataCategory) : null}" />
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${refDataCat?.getI10n('desc')}">
                                                    <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${pdGroup && PropertyDefinitionGroupItem.findByPropDefAndPropDefGroup(pd, pdGroup)}">
                                                <input type="checkbox" checked="checked" disabled="disabled" name="propertyDefinition" value="${pd.id}" />
                                            </g:if>
                                            <g:else>
                                                <input type="checkbox" disabled="disabled" name="propertyDefinition" value="${pd.id}" />
                                            </g:else>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </g:each>
                    </g:else>
                </div>
                <style>
                    .scrollWrapper {
                        overflow-y: scroll;
                        max-height: 400px;
                    }
                    .scrollContent {

                    }
                </style>
            </div>
        </div><!-- .grid -->
    </g:form>
</ui:modal>

%{-- <laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.prop_descr_selector_controller = function() {
            init: function () {
                $('#propDefGroupModal #prop_descr_selector').on('change', function () {
                    JSPC.app.prop_descr_selector_controller.changeTable($(this).val())
                })

                $('#propDefGroupModal #prop_descr_selector').trigger('change')
            },
            changeTable: function (target) {
                $('#propDefGroupModal .table').addClass('hidden')
                $('#propDefGroupModal .table input').attr('disabled', 'disabled')

                $('#propDefGroupModal .table[data-propDefTable="' + target + '"]').removeClass('hidden')
                $('#propDefGroupModal .table[data-propDefTable="' + target + '"] input').removeAttr('disabled')
            }
        }
        JSPC.app.prop_descr_selector_controller.init()
        setTimeout( function(){ $(window).trigger('resize')}, 500);
</laser:script> --}%
<!-- O: templates/properties/_propertyGroupModal -->