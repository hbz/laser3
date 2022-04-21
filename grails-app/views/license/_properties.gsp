<%@ page import="de.laser.http.AjaxUtils; de.laser.License; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), License.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: license,
            editable: accessService.checkPermAffiliation('ORG_INST, ORG_CONSORTIUM','INST_EDITOR'),
            availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<g:if test="${memberProperties}">%{-- check for content --}%
    <g:if test="${license._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL}">
        <div class="ui card la-dl-no-table ">
            <div class="content">
                <h2 class="ui header">${message(code:'license.properties.consortium')}</h2>
                <div id="member_props_div">
                    <g:render template="/templates/properties/members" model="${[
                            prop_desc: PropertyDefinition.LIC_PROP,
                            ownobj: license,
                            custom_props_div: "member_props_div"]}"/>
                </div>
            </div>
        </div>
    </g:if>
</g:if>

<div class="ui card la-dl-no-table la-js-hideable">

<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${license.getCalculatedPropDefGroups(institution)}" />

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

        <g:if test="${isVisible}">

            <g:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup: pdg,
                    propDefGroupBinding: binding,
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    custom_props_div: "grouped_custom_props_div_${pdg.id}"
            ]}"/>
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
    </g:each>

    <g:if test="${hiddenPropertiesMessages.size() > 0}">
        <div class="content">
            <semui:msg class="info" header="" text="${hiddenPropertiesMessages.join('<br />')}" />
        </div>
    </g:if>

<%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table la-js-hideable">--%>
    <div class="content">
        <h5 class="ui header">
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
            </g:if>
            <g:else>
                ${message(code:'license.properties')}
            </g:else>
        </h5>

        <div id="custom_props_div_props">
            <g:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    orphanedProperties: allPropDefGroups.orphanedProperties,
                    editable: accessService.checkPermAffiliation('ORG_INST, ORG_CONSORTIUM','INST_EDITOR'),
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    <%--</div>--%>

    <laser:script file="${this.getGroovyPageFileName()}">
        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup' params='[oid:"${genericOIDService.getOID(license)}"]'/>", "#custom_props_div_props");
    </laser:script>

</div><!-- .card -->

<%-- private properties --%>

<div class="ui card la-dl-no-table la-js-hideable">
    <div class="content">
        <h2 class="ui header">${message(code:'license.properties.private')} ${contextOrg.name}</h2>
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextOrg.id}" />
        <div id="${propertyWrapper}">
            <g:render template="/templates/properties/private" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    propertyWrapper: "${propertyWrapper}",
                    tenant: contextOrg]}"/>

            <laser:script file="${this.getGroovyPageFileName()}">
                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextOrg.id});
            </laser:script>

        </div>
    </div>
</div><!--.card-->

<!-- _properties -->