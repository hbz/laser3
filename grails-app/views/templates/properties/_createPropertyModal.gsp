<!-- A: templates/properties/_createPropertyModal -->
<%@ page import="de.laser.ui.Icon; de.laser.properties.PropertyDefinitionGroup; de.laser.utils.LocaleUtils; de.laser.Subscription; de.laser.License; de.laser.Org; de.laser.RefdataCategory; de.laser.properties.PropertyDefinitionGroupItem; de.laser.properties.PropertyDefinition; de.laser.I10nTranslation; de.laser.FormService;" %>
<laser:serviceInjection/>

<ui:modal id="createPropertyModal" text="${message(code: 'default.add.label', args: [message(code: 'propertyDefinition.plural')])}"
          msgSave="${message(code: 'default.add.label', args: [message(code: 'propertyDefinition.plural')])}" modalSize="large" contentClass="scrolling">

    <g:form class="ui form" url="${propertyCreateUrl}" method="POST">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        <div class="ui grid">
            <div class="field" style="width:100%">
                <div class="scrollWrapper">
                    <g:if test="${allPropDefGroups}">
                    <%-- grouped custom properties --%>
                        <g:each in="${allPropDefGroups}" var="pdg">
                            <div class="la-checkAllArea">
                                <div class="ui grid">
                                    <div class="row">
                                        <div class="fourteen wide column">
                                            <h3 class="ui header">${message(code: 'propertyDefinition.plural')} (${pdg.name})</h3>
                                        </div>

                                        <div class="two wide column">
                                            <div class="ui compact inverted segment right floated" style="margin-right: 0;">
                                                <div class="ui checkbox la-js-checkAll right aligned">
                                                    <input type="checkbox" id="checkAllInput_plural" name="checkAllInput_plural">
                                                    <label for="checkAllInput_plural">${message(code: 'menu.public')}</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <input type="text" class="propDefFilter" data-forTable="pdg${pdg.id}" placeholder="Merkmale einschränken ...">
                                    </div>
                                </div>
                                <table id="pdg${pdg.id}" class="ui celled la-js-responsive-table la-table compact table scrollContent">
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
                                            <td class="pdName">
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
                                                    <div class="ui checkbox">
                                                        <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                                        <label></label>
                                                    </div>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </g:each>
                    </g:if>

                    <g:if test="${orphanedProperties}">
                    <%-- orphaned properties --%>
                        <div class="la-checkAllArea">
                            <div class="ui grid">
                                <div class="row">
                                    <div class="fourteen wide column">
                                        <h3 class="ui header">${message(code: 'subscription.properties.orphanedMajuscule')} ${message(code: 'subscription.propertiesBrackets')}</h3>
                                    </div>
                                    <div class="two wide column">
                                        <div class="ui compact inverted segment right floated" style="margin-right: 0;">
                                            <div class="ui checkbox la-js-checkAll right aligned">
                                                <input type="checkbox" id="checkAllInput_orphaned" name="checkAllInput_orphaned">
                                                <label for="checkAllInput_orphaned">${message(code: 'menu.public')}</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <input type="text" class="propDefFilter" data-forTable="orphaned" placeholder="Merkmale einschränken ...">
                                </div>
                            </div>
                            <table id="orphaned" class="ui celled la-js-responsive-table la-table compact table scrollContent">
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
                                        <td class="pdName">
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
                                                <div class="ui checkbox">
                                                    <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                                    <label></label>
                                                </div>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div>
                    </g:if>


                    <g:if test="${privateProperties}">
                        <%-- private properties --%>
                        <div class="la-checkAllArea">
                            <div class="ui grid">
                                <div class="row">
                                    <div class="fourteen wide column">
                                        <h3 class="ui header">${message(code: 'subscription.properties.private')} ${contextService.getOrg().name}</h3>
                                    </div>

                                    <div class="two wide column">
                                        <div class="ui compact inverted segment right floated" style="margin-right: 0;">
                                            <div class="ui checkbox la-js-checkAll right aligned">
                                                <input type="checkbox" id="checkAllInput_private"
                                                       name="checkAllInput_private">
                                                <label
                                                    for="checkAllInput_private">${message(code: 'menu.public')}</label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <input type="text" class="propDefFilter" data-forTable="private" placeholder="Merkmale einschränken ...">
                                    </div>
                                </div>
                            </div>
                            <table id="private" class="ui celled la-js-responsive-table la-table compact table scrollContent">
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
                                        <td class="pdName">
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
                                                <div class="ui checkbox">
                                                    <input type="checkbox" name="propertyDefinition" value="${pd.id}"/>
                                                    <label></label>
                                                </div>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div>
                    </g:if>
                </div>
            </div>

        </div><!-- .grid -->

    </g:form>
</ui:modal>

<!-- O: templates/properties/_createPropertyModal -->