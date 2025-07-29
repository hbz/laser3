<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.CustomerTypeService; de.laser.License; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), License.class.name)}" />

<%-- modal --%>

<%-- removed as of ERMS-6520
<ui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="true">

    <laser:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: license,
            editable: contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC),
            availPropDefGroups: availPropDefGroups
    ]}" />

</ui:modal>
--%>

<g:if test="${memberProperties}">%{-- check for content --}%
    <g:if test="${license._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h2 class="ui header">${message(code:'license.properties.consortium')}</h2>
                <div id="member_props_div">
                    <laser:render template="/templates/properties/members" model="${[
                            prop_desc: PropertyDefinition.LIC_PROP,
                            ownobj: license,
                            custom_props_div: "member_props_div"]}"/>
                </div>
            </div>
        </div>
    </g:if>
</g:if>

<div class="ui card la-dl-no-table">

    <div class="content">
        <div class="ui header la-flexbox la-justifyContent-spaceBetween">
            <h2>
                ${message(code: 'default.properties')}
            </h2>
            <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                <div class="right aligned four wide column">
                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code:'license.button.addProperty')}" onclick="JSPC.app.createProperty(${license.id}, '${license.class.simpleName}','false');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
                </div>
            </g:if>
        </div>
    </div>

<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${license.getCalculatedPropDefGroups(contextService.getOrg())}" />

    <% List<String> hiddenPropertiesMessages = [] %>

    <g:each in="${allPropDefGroups.sorted}" var="entry">
        <%
            String cat                             = entry[0]
            PropertyDefinitionGroup pdg            = entry[1]
            PropertyDefinitionGroupBinding binding = entry[2]
            List numberOfConsortiaProperties       = []
            if(license.getLicensingConsortium() && contextService.getOrg().id != license.getLicensingConsortium().id)
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

            <laser:render template="/templates/properties/groupWrapper" model="${[
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
            <g:set var="numberOfProperties" value="${pdg.getCurrentPropertiesOfTenant(license, contextService.getOrg())}" />

            <g:if test="${numberOfProperties.size() > 0}">
                <%
                    hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [pdg.name, numberOfProperties.size()])}"
                %>
            </g:if>
        </g:else>
    </g:each>

    <g:if test="${hiddenPropertiesMessages.size() > 0}">
        <div class="content">
            <ui:msg class="info" text="${hiddenPropertiesMessages.join('<br />')}" />
        </div>
    </g:if>

    <g:if test="${allPropDefGroups.orphanedProperties}">
    <%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table">--%>
        <div class="content">
            <h3 class="ui header">
                <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                    ${message(code: 'subscription.properties.orphanedMajuscule')} ${message(code: 'subscription.propertiesBrackets')}
                </g:if>
                <g:else>
                    ${message(code: 'license.properties')}
                </g:else>
            </h3>

            <div id="custom_props_div_props">
                <laser:render template="/templates/properties/custom" model="${[
                        prop_desc         : PropertyDefinition.LIC_PROP,
                        ownobj            : license,
                        orphanedProperties: allPropDefGroups.orphanedProperties,
                        editable          : contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC),
                        custom_props_div  : "custom_props_div_props"]}"/>
            </div>
        </div>
    <%--</div>--%>

        <laser:script file="${this.getGroovyPageFileName()}">
            c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'
                                               params='[oid: "${genericOIDService.getOID(license)}"]'/>", "#custom_props_div_props");
        </laser:script>
    </g:if>
</div><!-- .card -->

<%-- private properties --%>

<div class="ui card la-dl-no-table">
    <div class="content">
            <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                <h2>
                    ${message(code: 'default.properties.my')}
                </h2>
                <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                    <div class="right aligned four wide column">
                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-content="${message(code: 'license.button.addProperty')}"
                                onclick="JSPC.app.createProperty(${license.id}, '${license.class.simpleName}', 'true');">
                            <i class="${Icon.CMD.ADD}"></i>
                        </button>
                    </div>
                </g:if>
            </div>
    </div>
    <div class="content">
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}" />
        <div id="${propertyWrapper}">
            <laser:render template="/templates/properties/private" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    propertyWrapper: "${propertyWrapper}",
                    tenant: contextService.getOrg()]}"/>

            <laser:script file="${this.getGroovyPageFileName()}">
                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextService.getOrg().id});
            </laser:script>

        </div>
    </div>
</div><!--.card-->
<laser:render template="/templates/properties/createProperty_js"/>

<!-- _properties -->