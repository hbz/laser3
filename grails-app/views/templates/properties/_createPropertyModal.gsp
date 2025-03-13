<!-- A: templates/properties/_createPropertyModal -->
<%@ page import="de.laser.ui.Icon; de.laser.properties.PropertyDefinitionGroup; de.laser.utils.LocaleUtils; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.RefdataCategory; de.laser.properties.PropertyDefinitionGroupItem; de.laser.properties.PropertyDefinition; de.laser.I10nTranslation; de.laser.FormService;" %>
<laser:serviceInjection/>

<ui:modal id="createPropertyModal" text="${message(code: 'default.add.label', args: [message(code: 'propertyDefinition.plural')])}"
          msgSave="${message(code: 'default.add.label', args: [message(code: 'propertyDefinition.plural')])}" modalSize="large">

    <g:form class="ui form" url="${propertyCreateUrl}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui grid">
            <div class="field" style="width:100%">
                <div class="scrollWrapper">
                    <g:if test="${allPropDefGroups}">
                    <%-- grouped custom properties --%>
                        <g:each in="${allPropDefGroups}" var="pdg">
                            <h3>${message(code: 'propertyDefinition.plural')} (${pdg.name})</h3>
                            <table class="ui celled la-js-responsive-table la-table compact table scrollContent">
                                <tbody>
                                <g:each in="${pdg.items.sort { it.propDef.getI10n('name') }}" var="propertyDefinitionGroupItem" status="i">
                                    <g:set var="pd" value="${propertyDefinitionGroupItem.propDef}"/>
                                    <tr>
                                        <td>${i + 1}</td>
                                        <td>
                                            <g:if test="${pd.mandatory}">
                                                <span data-position="top left" class="la-popup-tooltip"
                                                      data-content="${message(code: 'default.mandatory.tooltip')}">
                                                    <i class="${Icon.PROP.MANDATORY}"></i>
                                                </span>
                                            </g:if>
                                            <g:if test="${pd.multipleOccurrence}">
                                                <span data-position="top right" class="la-popup-tooltip"
                                                      data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                                                    <i class="${Icon.PROP.MULTIPLE}"></i>
                                                </span>
                                            </g:if>
                                        </td>
                                        <td>
                                            ${pd.getI10n('name')}
                                        </td>
                                        <td>
                                            <g:set var="pdExpl" value="${pd.getI10n('expl')}"/>
                                            ${pdExpl != 'null' ? pdExpl : ''}
                                        </td>
                                        <td>
                                            <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                            <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                                <g:set var="refDataCat" value="${pd.refdataCategory ? RefdataCategory.getByDesc(pd.refdataCategory) : null}"/>
                                                <span data-position="top right" class="la-popup-tooltip" data-content="${refDataCat?.getI10n('desc')}">
                                                    <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small> <i class="${Icon.TOOLTIP.INFO}"></i>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${!propertyService.checkPropertyExists(object, pd)}">
                                                <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                            </g:if>
                                        %{--<g:else>
                                            <input type="checkbox" disabled="disabled" name="propertyDefinition" value="${pd.id}"/>
                                        </g:else>--}%
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </g:each>
                    </g:if>

                    <g:if test="${orphanedProperties}">
                    <%-- orphaned properties --%>

                        <h3>${message(code: 'subscription.properties.orphaned')}</h3>
                        <table class="ui celled la-js-responsive-table la-table compact table scrollContent">
                            <tbody>
                            <g:each in="${orphanedProperties.sort { it.getI10n('name') }}" var="pd" status="i">
                                <tr>
                                    <td>${i + 1}</td>
                                    <td>
                                        <g:if test="${pd.mandatory}">
                                            <span data-position="top left" class="la-popup-tooltip"
                                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                                <i class="${Icon.PROP.MANDATORY}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip"
                                                  data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                                                <i class="${Icon.PROP.MULTIPLE}"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${pd.getI10n('name')}
                                    </td>
                                    <td>
                                        <g:set var="pdExpl" value="${pd.getI10n('expl')}"/>
                                        ${pdExpl != 'null' ? pdExpl : ''}
                                    </td>
                                    <td>
                                        <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                        <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                            <g:set var="refDataCat" value="${pd.refdataCategory ? RefdataCategory.getByDesc(pd.refdataCategory) : null}"/>
                                            <span data-position="top right" class="la-popup-tooltip" data-content="${refDataCat?.getI10n('desc')}">
                                                <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small> <i class="${Icon.TOOLTIP.INFO}"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!propertyService.checkPropertyExists(object, pd)}">
                                            <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                        </g:if>
                                    %{-- <g:else>
                                         <input type="checkbox" disabled="disabled" name="propertyDefinition" value="${pd.id}"/>
                                     </g:else>--}%
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </g:if>


                    <g:if test="${privateProperties}">
                        <%-- private properties --%>
                        <h3>${message(code: 'subscription.properties.private')} ${contextService.getOrg().name}</h3>
                        <table class="ui celled la-js-responsive-table la-table compact table scrollContent">
                            <tbody>
                            <g:each in="${privateProperties.sort { it.getI10n('name') }}" var="pd" status="i">
                                <tr>
                                    <td>${i + 1}</td>
                                    <td>
                                        <g:if test="${pd.mandatory}">
                                            <span data-position="top left" class="la-popup-tooltip"
                                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                                <i class="${Icon.PROP.MANDATORY}"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${pd.multipleOccurrence}">
                                            <span data-position="top right" class="la-popup-tooltip"
                                                  data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                                                <i class="${Icon.PROP.MULTIPLE}"></i>
                                            </span>
                                        </g:if>
                                    </td>
                                    <td>
                                        ${pd.getI10n('name')}
                                    </td>
                                    <td>
                                        <g:set var="pdExpl" value="${pd.getI10n('expl')}"/>
                                        ${pdExpl != 'null' ? pdExpl : ''}
                                    </td>
                                    <td>
                                        <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                        <g:if test="${'RefdataValue'.equals(pdRdc)}">
                                            <g:set var="refDataCat" value="${pd.refdataCategory ? RefdataCategory.getByDesc(pd.refdataCategory) : null}"/>
                                            <span data-position="top right" class="la-popup-tooltip" data-content="${refDataCat?.getI10n('desc')}">
                                                <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small> <i class="${Icon.TOOLTIP.INFO}"></i>
                                            </span>
                                        </g:if>
                                        <g:else>
                                            <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                        </g:else>
                                    </td>
                                    <td>
                                        <g:if test="${!propertyService.checkPropertyExists(object, pd)}">
                                            <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                        </g:if>
                                    %{--<g:else>
                                        <input type="checkbox" disabled="disabled" name="propertyDefinition" value="${pd.id}"/>
                                    </g:else>--}%
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </g:if>
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
<!-- O: templates/properties/_createPropertyModal -->