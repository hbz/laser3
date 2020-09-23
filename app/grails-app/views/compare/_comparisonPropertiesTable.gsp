<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; com.k_int.kbplus.License; com.k_int.kbplus.Subscription; de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.LicenseProperty; com.k_int.kbplus.SubscriptionProperty; de.laser.AuditConfig; de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection/>
<thead>
<tr>
    <th>${key}</th>
    <g:each in="${objects}" var="object">
        <th>
            <g:if test="${object && propBinding && propBinding.get(object)?.isVisibleForConsortiaMembers}">
                <g:if test="${object}"><g:link
                        controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                        id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if><span
                    class="ui blue tag label">${message(code: 'financials.isVisibleForSubscriber')}</span>
            </g:if>
            <g:else>
                <g:if test="${object}"><g:link
                        controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                        id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if>
            </g:else>
        </th>
    </g:each>
</tr>
</thead>
<g:each in="${group}" var="prop">
    <%
        PropertyDefinition propKey = (PropertyDefinition) genericOIDService.resolveOID(prop.getKey())
    %>
    <tr>
        <td>
            <g:if test="${propKey.getI10n('expl') != null && !propKey.getI10n('expl').contains(' °')}">
                ${propKey.getI10n('name')}
                <g:if test="${propKey.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                          data-content="${propKey.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </g:if>
            <g:else>
                ${propKey.getI10n('name')}
            </g:else>

            <g:if test="${propKey.multipleOccurrence}">
                <span data-position="top right" class="la-popup-tooltip la-delay"
                      data-content="${message(code: 'default.multipleOccurrence.tooltip')}">
                    <i class="redo icon orange"></i>
                </span>
            </g:if>
        </td>
        <g:each in="${objects}" var="object">
            <g:set var="propValues" value="${prop.getValue()}"/>
            <g:if test="${propValues.containsKey(object)}">
                <td>
                    <g:each var="propValue" in="${propValues.get(object)}">
                        <div class="la-copyElements-flex-container la-colorCode-target la-multi-sources">
                        <div class="la-copyElements-flex-item">
                            <g:if test="${propValue.getValue() != "" && propValue.getValue() != null}">
                                <g:if test="${propValue.type.type == Integer.toString()}">
                                    <semui:xEditable owner="${propValue}" type="text" field="intValue"
                                                     overwriteEditable="${false}"/>
                                </g:if>

                                <g:elseif test="${propValue.type.type == String.toString()}">
                                    <semui:xEditable owner="${propValue}" type="text" field="stringValue"
                                                     overwriteEditable="${false}"/>
                                </g:elseif>
                                <g:elseif test="${propValue.type.type == BigDecimal.toString()}">
                                    <semui:xEditable owner="${propValue}" type="text" field="decValue"
                                                     overwriteEditable="${false}"/>
                                </g:elseif>
                                <g:elseif test="${propValue.type.type == Date.toString()}">
                                    <semui:xEditable owner="${propValue}" type="date" field="dateValue"
                                                     overwriteEditable="${false}"/>
                                </g:elseif>
                                <g:elseif test="${propValue.type.type == URL.toString()}">
                                    <semui:xEditable owner="${propValue}" type="url" field="urlValue"
                                                     overwriteEditable="${false}"
                                                     class="la-overflow la-ellipsis"/>
                                    <g:if test="${propValue.value}">
                                        <semui:linkIcon href="${propValue.value}"/>
                                    </g:if>
                                </g:elseif>
                                <g:elseif test="${propValue.type.type == RefdataValue.toString()}">
                                    <span data-position="top left" class="la-popup-tooltip la-delay"
                                          data-content="${propValue.refValue.getI10n("value")}">
                                        <g:if test="${object instanceof License}">
                                            <%
                                                String value
                                                switch (propValue.refValue.owner) {
                                                    case RefdataCategory.getByDesc(RDConstants.Y_N):
                                                    case RefdataCategory.getByDesc(RDConstants.Y_N_O):
                                                        switch (propValue.refValue) {
                                                            case RDStore.YN_YES:
                                                            case RDStore.YNO_YES: value = raw('<i class="green thumbs up icon huge"></i>')
                                                                break
                                                            case RDStore.YN_NO:
                                                            case RDStore.YNO_NO: value = raw('<i class="red thumbs down icon huge"></i>')
                                                                break
                                                            case RDStore.YNO_OTHER: value = raw('<i class="yellow dot circle icon huge"></i>')
                                                                break
                                                        }
                                                        break
                                                    case RefdataCategory.getByDesc(RDConstants.PERMISSIONS):
                                                        switch (propValue.refValue) {
                                                            case RDStore.PERM_PERM_EXPL: value = raw('<i class="green check circle icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_PERM_INTERP: value = raw('<i class="green check circle outline icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_PROH_EXPL: value = raw('<i class="red times circle icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_PROH_INTERP: value = raw('<i class="red times circle outline icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_SILENT: value = raw('<i class="hand point up icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_NOT_APPLICABLE: value = raw('<i class="exclamation icon huge"></i>')
                                                                break
                                                            case RDStore.PERM_UNKNOWN: value = raw('<i class="question circle icon huge"></i>')
                                                                break
                                                        }
                                                        break
                                                    default: value = propValue.refValue.getI10n("value")
                                                        break
                                                }

                                            %>
                                            ${value ?: propValue.refValue.getI10n("value")}
                                        </g:if>
                                        <g:else>
                                            ${propValue.refValue.getI10n("value")}
                                        </g:else>
                                    </span>
                                </g:elseif>
                                <g:else>
                                    <div>
                                        ${propValue.value}
                                    </div>
                                </g:else>
                                </div>

                                <g:if test="${propValues.get(object)?.size() > 1}"><br></g:if>
                            </g:if>
                            <g:else>
                                <div class="la-copyElements-flex-item">
                                    <span data-position="top left" class="ui circular label la-popup-tooltip la-delay"
                                      data-content="${message(code: "default.compare.propertyValueNotSet")}">x</span>
                                </div>
                            </g:else>

                            <g:if test="${propValue.note}">
                                    <span data-position="top left" class="ui circular label la-long-tooltip la-popup-tooltip la-delay"
                                         data-content="${propValue.note}">Anm.</span>

                            </g:if>
                            <g:if test="${object instanceof License && propValue.paragraph}">
                                    <span data-position="top left" class="ui circular huge label la-long-tooltip la-popup-tooltip la-delay"
                                         data-content="${propValue.paragraph}">§</span><br>
                            </g:if>

                            <g:if test="${object._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_PARTICIPATION]}">
                                <g:if test="${propValue instanceof SubscriptionProperty || propValue instanceof LicenseProperty}">

                                    <g:if test="${object instanceof License}">
                                        <g:set var="consortium" value="${object.getLicensingConsortium()}"/>
                                    </g:if>
                                    <g:elseif test="${object instanceof Subscription}">
                                        <g:set var="consortium" value="${object.getConsortia()}"/>
                                        <g:set var="atSubscr"
                                               value="${object._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}"/>
                                    </g:elseif>
                                    <g:if test="${(propValue.hasProperty('instanceOf') && propValue.instanceOf && AuditConfig.getConfig(propValue.instanceOf)) || AuditConfig.getConfig(propValue)}">
                                        <g:if test="${object.isSlaved}">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'property.audit.target.inherit.auto')}"
                                                  data-position="top right"><i class="icon thumbtack blue"></i></span>
                                        </g:if>
                                        <g:else>
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'property.audit.target.inherit')}"
                                                  data-position="top right"><i class="icon thumbtack grey"></i></span>
                                        </g:else>
                                    </g:if>
                                    <g:elseif test="${propValue.tenant?.id == consortium?.id && atSubscr}">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'property.notInherited.fromConsortia')}"
                                              data-position="top right"><i class="large icon cart arrow down blue"></i>
                                        </span>
                                    </g:elseif>

                                </g:if>
                            </g:if>
                            <g:if test="${propValues.get(object)?.size() > 1}"><br></g:if>
                        </div>
                    </g:each>
                </td>
            </g:if>
            <g:else>
                <td>
                    <div class="la-copyElements-flex-item">
                        <a class="ui circular label la-popup-tooltip la-delay"
                           data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                    </div>
                </td>
            </g:else>
        </g:each>
    </tr>
</g:each>
