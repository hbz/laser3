<%@page import="de.laser.properties.SubscriptionProperty; de.laser.Subscription; de.laser.License; de.laser.AuditConfig; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinitionGroupBinding" %>
<g:if test="${memberProperties}">
    <section>
        <g:if test="${subscription}">
            <g:set var="memberSubs" value="${Subscription.executeQuery('select s from Subscription s where s.instanceOf = :sub', [sub: subscription])}"/>
        </g:if>
        <header>
            <h3><g:message code="license.properties.consortium"/></h3>
        </header>
        <table>
            <tbody>
                <g:each in="${memberProperties}" var="propType">
                    <tr>
                        <td>
                            ${propType.getI10n("name")}
                        </td>
                        %{--<td>
                            <g:if test="${propType.mandatory}">
                                <span class="yellow"> P </span>
                            </g:if>
                            <g:if test="${propType.multipleOccurrence}">
                                <span class="orange"> M </span>
                            </g:if>
                        </td>--}%
                        <td>
                            %{--<span class="blue"> K </span>--}%
                            <g:if test="${memberSubs}">
                                (${SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null and sp.type = :type', [subscriptionSet: memberSubs, context: institution, type: propType]).size() ?: 0} / ${memberSubs.size() ?: 0})
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </section>
</g:if>
<% List<String> hiddenPropertiesMessages = [] %>
<g:each in="${allPropDefGroups.sorted}" var="entry">
    <%
        String cat                             = entry[0]
        PropertyDefinitionGroup pdg            = entry[1]
        PropertyDefinitionGroupBinding binding = entry[2]
        List numberOfConsortiaProperties       = []
        if(license.getLicensingConsortium() && institution.id != license.getLicensingConsortium().id)
            numberOfConsortiaProperties.addAll(pdg.getCurrentPropertiesOfTenant(license,license.getLicensingConsortium()))
        boolean isVisible = false
        if (cat == 'global') {
            isVisible = pdg.isVisible || numberOfConsortiaProperties.size() > 0
        }
        else if (cat == 'local') {
            isVisible = binding.isVisible
        }
        else if (cat == 'member') {
            isVisible = (binding.isVisible || numberOfConsortiaProperties.size() > 0) && binding.isVisibleForConsortiaMembers
        }
    %>
    <section>
        <g:if test="${isVisible}">
            <header>
                <h3>
                    ${message(code: 'subscription.properties.public')} (${pdg.name})
                    <g:if test="${showConsortiaFunctions}">
                        <g:if test="${pdg.ownerType in [License.class.name, Subscription.class.name]}">
                            <g:if test="${binding?.isVisibleForConsortiaMembers}">
                                <i><g:message code="financials.isVisibleForSubscriber"/></i>
                            </g:if>
                        </g:if>
                    </g:if>
                </h3>
            </header>
            <table>
                <thead>
                    <tr>
                        <th>${message(code:'property.table.property')}</th>
                        <th>${message(code:'default.value.label')}</th>
                        <g:if test="${pdg.ownerType == License.class.name}">
                            <th>${message(code:'property.table.paragraph')}</th>
                        </g:if>
                        <th>${message(code:'property.table.notes')}</th>
                        %{--<th> </th>--}%
                    </tr>
                </thead>
                <tbody>
                    <g:set var="isGroupVisible" value="${pdg.isVisible || binding?.isVisible}"/>
                    <g:if test="${license}">
                        <g:set var="consortium" value="${license.getLicensingConsortium()}"/>
                        <g:set var="atSubscr" value="${license._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION}"/>
                        <g:if test="${isGroupVisible}">
                            <g:set var="propDefGroupItems" value="${pdg.getCurrentProperties(license)}" />
                        </g:if>
                        <g:elseif test="${consortium != null}">
                            <g:set var="propDefGroupItems" value="${propDefGroup.getCurrentPropertiesOfTenant(license,consortium)}" />
                        </g:elseif>
                    </g:if>
                    <g:elseif test="${subscription}">
                        <g:set var="consortium" value="${subscription.getConsortia()}"/>
                        <g:set var="atSubscr" value="${subscription._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION}"/>
                        <g:if test="${isGroupVisible}">
                            <g:set var="propDefGroupItems" value="${pdg.getCurrentProperties(subscription)}" />
                        </g:if>
                        <g:elseif test="${consortium != null}">
                            <g:set var="propDefGroupItems" value="${pdg.getCurrentPropertiesOfTenant(subscription,consortium)}" />
                        </g:elseif>
                    </g:elseif>
                    <g:each in="${propDefGroupItems.sort{a, b -> a.type.getI10n('name') <=> b.type.getI10n('name') ?: a.getValue() <=> b.getValue() ?: a.id <=> b.id }}" var="prop">
                        <g:if test="${(prop.tenant?.id == contextOrg.id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}">
                            <tr>
                                <td>
                                    <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                                        ${prop.type.getI10n('name')}
                                        <g:if test="${prop.type.getI10n('expl')}">
                                            <i>${prop.type.getI10n('expl')}</i>
                                        </g:if>
                                    </g:if>
                                    <g:else>
                                        ${prop.type.getI10n('name')}
                                    </g:else>
                                    %{--<g:if test="${prop.type.multipleOccurrence}">
                                        <span class="redo icon orange"> M </span>
                                    </g:if>--}%
                                </td>
                                <td>
                                    <g:if test="${prop.type.isIntegerType()}">
                                        ${prop.intValue}
                                    </g:if>
                                    <g:elseif test="${prop.type.isStringType()}">
                                        ${prop.stringValue}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isBigDecimalType()}">
                                        ${prop.decValue}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isDateType()}">
                                        <g:formatDate date="${prop.dateValue}" format="${message(code: 'default.date.format.notime')}"/>
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isRefdataValueType()}">
                                        ${prop.refValue?.getI10n("value")}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isURLType()}">
                                        ${prop.urlValue}
                                    </g:elseif>
                                </td>
                                <g:if test="${pdg.ownerType == License.class.name}">
                                    <td>
                                        ${prop.paragraph}
                                    </td>
                                </g:if>
                                <td>
                                    ${prop.note}
                                </td>
                                %{--<td>
                                    <g:if test="${prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf)}">
                                        <span class="blue"> V </span>
                                    </g:if>
                                    <g:elseif test="${prop.tenant?.id == consortium?.id && atSubscr}">
                                        <span class="blue"> K </span>
                                    </g:elseif>
                                </td>--}%
                            </tr>
                        </g:if>
                    </g:each>
                </tbody>
            </table>
            <g:if test="${!binding?.isVisible && !pdg.isVisible}">
                <g:set var="numberOfProperties" value="${pdg.getCurrentProperties(license).size()-numberOfConsortiaProperties.size()}" />
                <g:if test="${numberOfProperties > 0}">
                    <%
                        hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [pdg.name, numberOfProperties])}"
                    %>
                </g:if>
            </g:if>
        </g:if>
        <g:else>
            <g:set var="numberOfProperties" value="${pdg.getCurrentPropertiesOfTenant(license,institution)}" />

            <g:if test="${numberOfProperties.size() > 0}">
                <%
                    hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [pdg.name, numberOfProperties.size()])}"
                %>
            </g:if>
        </g:else>
    </section>
</g:each>
<g:each in="${hiddenPropertiesMessages}" var="hiddenPropertiesMessage">
    <section>
        <header><h3><semui:msg class="info" header="" text="${hiddenPropertiesMessage}" /></h3></header>
    </section>
</g:each>
<section>
    <header>
        <h3>
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
            </g:if>
            <g:else>
                ${message(code:'license.properties')}
            </g:else>
        </h3>
    </header>
    <g:if test="${allPropDefGroups.orphanedProperties}">
        <table>
            <thead>
                <th>${message(code:'property.table.property')}</th>
                <th>${message(code:'default.value.label')}</th>
                <g:if test="${license}">
                    <th>${message(code:'property.table.paragraph')}</th>
                </g:if>
                <th>${message(code:'property.table.notes')}</th>
                %{--<th> </th>--}%
            </thead>
            <tbody>
                <g:each in="${allPropDefGroups.orphanedProperties.sort{a, b -> a.type.getI10n('name') <=> b.type.getI10n('name') ?: a.getValue() <=> b.getValue() ?: a.id <=> b.id }}" var="prop">
                    <g:if test="${(prop.tenant?.id == institution.id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}">
                        <g:if test="${prop.type.descr == prop_desc}">
                            <tr>
                                <td>
                                    <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                                        ${prop.type.getI10n('name')}
                                        <g:if test="${prop.type.getI10n('expl')}">
                                            <i>${prop.type.getI10n('expl')}</i>
                                        </g:if>
                                    </g:if>
                                    <g:else>
                                        ${prop.type.getI10n('name')}
                                    </g:else>
                                    <%--
                                    <g:if test="${prop.type.multipleOccurrence}">
                                        <span class="orange"> M </span>
                                    </g:if>
                                    --%>
                                </td>
                                <td>
                                    <g:if test="${prop.type.isIntegerType()}">
                                        ${prop.intValue}
                                    </g:if>
                                    <g:elseif test="${prop.type.isStringType()}">
                                        ${prop.stringValue}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isBigDecimalType()}">
                                        ${prop.decValue}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isDateType()}">
                                        <g:formatDate date="${prop.dateValue}" format="${message(code: 'default.date.format.notime')}"/>
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isURLType()}">
                                        ${prop.value}
                                    </g:elseif>
                                    <g:elseif test="${prop.type.isRefdataValueType()}">
                                        ${prop.refValue?.getI10n("value")}
                                    </g:elseif>
                                </td>
                                <g:if test="${license}">
                                    <td>
                                        ${prop.paragraph}
                                    </td>
                                </g:if>
                                <td>
                                    ${prop.note}
                                </td>
                                %{--<td>
                                    <g:if test="${license}">
                                        <g:set var="consortium" value="${license.getLicensingConsortium()}"/>
                                    </g:if>
                                    <g:elseif test="${subscription}">
                                        <g:set var="consortium" value="${subscription.getConsortia()}"/>
                                        <g:set var="atSubscr" value="${subscription._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION}"/>
                                    </g:elseif>
                                    %{--<g:if test="${(prop.hasProperty('instanceOf') && prop.instan && AuditConfig.getConfig(prop.instanceOf)) || AuditConfig.getConfig(prop)}">
                                        <span class="blue"> V </span>
                                    </g:if>
                                    <g:elseif test="${prop.tenant?.id == consortium?.id && atSubscr}">
                                        <span class="blue"> V </span>
                                    </g:elseif>
                                    </td>--}%
                            </tr>
                        </g:if>
                    </g:if>
                </g:each>
            </tbody>
        </table>
    </g:if>
</section>
<g:set var="privateProperties" value="${entry.propertySet.findAll { cp -> cp.type.tenant?.id == institution.id && cp.tenant?.id == institution.id }}"/>
<g:if test="${privateProperties}">
<section>
    <header><h3><g:message code="org.properties.private"/> ${institution.name}</h3></header>
        <table>
            <thead>
                <tr>
                    <th>${message(code:'property.table.property')}</th>
                    <th>${message(code:'default.value.label')}</th>
                    <g:if test="${license}">
                        <th>${message(code:'property.table.paragraph')}</th>
                    </g:if>
                    <th>${message(code:'property.table.notes')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${privateProperties.sort{a, b -> a.type.getI10n('name') <=> b.type.getI10n('name') ?: a.getValue() <=> b.getValue() ?: a.id <=> b.id }}" var="prop">
                    <g:if test="${prop.type.tenant?.id == institution?.id}">
                        <tr>
                            <td>
                                <g:if test="${prop.type.getI10n('expl') != null && !prop.type.getI10n('expl').contains(' °')}">
                                    ${prop.type.getI10n('name')}
                                    <g:if test="${prop.type.getI10n('expl')}">
                                        <i>${prop.type.getI10n('expl')}</i>
                                    </g:if>
                                </g:if>
                                <g:else>
                                    ${prop.type.getI10n('name')}
                                </g:else>
                                %{--<g:if test="${prop.type.mandatory}">
                                    <span class="yellow"> P </span>
                                </g:if>
                                <g:if test="${prop.type.multipleOccurrence}">
                                    <span class="orange"> M </span>
                                </g:if>--}%
                            </td>
                            <td>
                                <g:if test="${prop.type.isIntegerType()}">
                                    ${prop.intValue}
                                </g:if>
                                <g:elseif test="${prop.type.isStringType()}">
                                    ${prop.stringValue}
                                </g:elseif>
                                <g:elseif test="${prop.type.isBigDecimalType()}">
                                    ${prop.decValue}
                                </g:elseif>
                                <g:elseif test="${prop.type.isDateType()}">
                                    <g:formatDate date="${prop.dateValue}" format="${message(code: 'default.date.format.notime')}"/>
                                </g:elseif>
                                <g:elseif test="${prop.type.isURLType()}">
                                    ${prop.value}
                                </g:elseif>
                                <g:elseif test="${prop.type.isRefdataValueType()}">
                                    ${prop.refValue?.getI10n("value")}
                                </g:elseif>

                            </td>
                            <g:if test="${license}">
                                <td>
                                    ${prop.paragraph}
                                </td>
                            </g:if>
                            <td>
                                ${prop.note}
                            </td>
                        </tr>
                    </g:if>
                </g:each>
            </tbody>
        </table>
    </section>
</g:if>