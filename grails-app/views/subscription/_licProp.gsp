<%@ page import="de.laser.License; de.laser.Subscription; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.*; de.laser.AuditConfig" %>
<laser:serviceInjection />
<!-- _licProp -->

<%-- grouped custom properties --%>

<% List<String> hiddenPropertiesMessages = [] %>
    <g:if test="${derivedPropDefGroups.global || derivedPropDefGroups.local || derivedPropDefGroups.member || derivedPropDefGroups.orphanedProperties}">
        <div id="derived-license-properties${linkId}" class="hidden">

            <g:each in="${derivedPropDefGroups.global}" var="propDefGroup">
                <g:if test="${propDefGroup.isVisible}">

                    <!-- global -->
                    <laser:render template="/subscription/licPropGroupWrapper" model="${[
                            propDefGroup: propDefGroup,
                            propDefGroupBinding: null,
                            ownObj: license
                    ]}"/>
                </g:if>
                <g:else>
                    <g:set var="numberOfProperties" value="${propDefGroup.getCurrentProperties(license)}" />
                    <g:if test="${numberOfProperties.size() > 0}">
                        <%
                            hiddenPropertiesMessages << "Die Merkmalsgruppe ${propDefGroup.name} beinhaltet <strong>${numberOfProperties.size()}</strong> Merkmale, ist aber ausgeblendet."
                        %>
                    </g:if>
                </g:else>
            </g:each>

            <g:each in="${derivedPropDefGroups.local}" var="propDefGroup">
            <%-- check binding visibility --%>
                <g:if test="${propDefGroup[1]?.isVisible}">

                    <!-- local -->
                    <laser:render template="/subscription/licPropGroupWrapper" model="${[
                            propDefGroup: propDefGroup[0],
                            propDefGroupBinding: propDefGroup[1],
                            ownObj: license
                    ]}"/>
                </g:if>
                <g:else>
                    <g:set var="numberOfProperties" value="${propDefGroup[0].getCurrentProperties(license)}" />
                    <g:if test="${numberOfProperties.size() > 0}">
                        <%
                            hiddenPropertiesMessages << "Die Merkmalsgruppe <strong>${propDefGroup[0].name}</strong> beinhaltet ${numberOfProperties.size()} Merkmale, ist aber ausgeblendet."
                        %>
                    </g:if>
                </g:else>
            </g:each>

            <g:each in="${derivedPropDefGroups.member}" var="propDefGroup">
            <%-- check binding visibility --%>
                <g:if test="${propDefGroup[1]?.isVisible}">
                <%-- check member visibility --%>
                    <g:if test="${propDefGroup[1]?.isVisibleForConsortiaMembers}">

                        <!-- member -->
                        <laser:render template="/subscription/licPropGroupWrapper" model="${[
                                propDefGroup: propDefGroup[0],
                                propDefGroupBinding: propDefGroup[1],
                                ownObj: license
                        ]}"/>
                    </g:if>
                </g:if>
                <g:else>
                    <g:set var="numberOfProperties" value="${propDefGroup[0].getCurrentProperties(license)}" />
                    <g:if test="${numberOfProperties.size() > 0}">
                        <%
                            hiddenPropertiesMessages << "Die Merkmalsgruppe <strong>${propDefGroup[0].name}</strong> beinhaltet ${numberOfProperties.size()} Merkmale, ist aber ausgeblendet."
                        %>
                    </g:if>
                </g:else>
            </g:each>

            <g:if test="${hiddenPropertiesMessages.size() > 0}">
                    <semui:msg class="info" header="" text="${hiddenPropertiesMessages.join('<br />')}" />
            </g:if>

        <%-- orphaned properties --%>

        <%-- custom properties --%>

            <g:if test="${derivedPropDefGroups.orphanedProperties}">
                <g:set var="filteredOrphanedProperties" value="${derivedPropDefGroups.orphanedProperties.findAll { prop -> (prop.tenant?.id == contextService.getOrg().id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}}"/>
            </g:if>
            <g:if test="${filteredOrphanedProperties}">
                <div>
                    <h5 class="ui header">
                        <g:link controller="license" action="show" id="${license.id}"><i class="balance scale icon"></i>${license}</g:link>
                        (${message(code:'subscription.properties')})
                    </h5>

                    <laser:render template="/subscription/licPropGroup" model="${[
                            propList: derivedPropDefGroups.orphanedProperties,
                            ownObj: license
                    ]}"/>
                </div>

            </g:if>
        </div>
    </g:if>

<!-- _licProp -->
