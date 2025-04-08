<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Org.class.name)}" />

<%-- modal --%>
<g:if test="${false}"> %{-- erms-4798 --}%
<ui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="true">

    <laser:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: orgInstance,
            availPropDefGroups: availPropDefGroups
    ]}" />

</ui:modal>

<div class="ui card la-dl-no-table">

    <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <div class="content">
            <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                <h2>
                    ${message(code: 'default.properties')}
                </h2>
                <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                    <div class="right aligned four wide column">
                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code:'org.button.addProperty')}"
                                onclick="JSPC.app.createProperty(${orgInstance.id}, '${orgInstance.class.simpleName}', 'false');">
                            <i class="${Icon.CMD.ADD}"></i>
                        </button>
                    </div>
                </g:if>
            </div>
        </div>
    </g:if>

<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${orgInstance.getCalculatedPropDefGroups(contextService.getOrg())}" />

    <% List<String> hiddenPropertiesMessages = [] %>

    <g:each in="${allPropDefGroups.sorted}" var="entry">
        <%
            String cat                             = entry[0]
            PropertyDefinitionGroup pdg            = entry[1]
            PropertyDefinitionGroupBinding binding = entry[2]

            boolean isVisible = false

            if (cat == 'global') {
                isVisible = pdg.isVisible
            }
            else if (cat == 'local') {
                isVisible = binding.isVisible
            }
        %>

        <g:if test="${isVisible}">

            <laser:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup: pdg,
                    propDefGroupBinding: binding,
                    prop_desc: PropertyDefinition.ORG_PROP,
                    ownobj: orgInstance,
                    custom_props_div: "grouped_custom_props_div_${pdg.id}"
            ]}"/>
        </g:if>
        <g:else>
            <g:set var="numberOfProperties" value="${pdg.getCurrentProperties(orgInstance)}" />

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
                    ${message(code: 'org.properties')}
                </g:else>
            </h3>

            <div id="custom_props_div_props">
                <laser:render template="/templates/properties/custom" model="${[
                        prop_desc         : PropertyDefinition.ORG_PROP,
                        ownobj            : orgInstance,
                        orphanedProperties: allPropDefGroups.orphanedProperties,
                        custom_props_div  : "custom_props_div_props"]}"/>
            </div>
        </div>
    <%--</div>--%>

        <laser:script file="${this.getGroovyPageFileName()}">
            c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'
                                               params='[oid: "${orgInstance.class.simpleName}:${orgInstance.id}"]'/>", "#custom_props_div_props");
        </laser:script>
    </g:if>

</div><!-- .card -->

</g:if> %{-- erms-4798 --}%

<%-- private properties --%>
<g:if test="${authOrg && (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro())}">

    <g:if test="${authOrg.name == contextService.getOrg()?.name}">%{-- ERMS-6070 org/show --}%
        <div class="ui card la-dl-no-table">
            <div class="content">
                <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                    <h2>
                        ${message(code: 'default.properties.my')}
                    </h2>
                    <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                        <div class="right aligned four wide column">
                            <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'license.button.addProperty')}"
                                    onclick="JSPC.app.createProperty(${orgInstance.id}, '${orgInstance.class.simpleName}', 'true');">
                                <i class="${Icon.CMD.ADD}"></i>
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
            <div class="content">
                <g:set var="propertyWrapper" value="private-property-wrapper-${authOrg.id}" />
                <div id="${propertyWrapper}">
                    <laser:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.ORG_PROP, // TODO: change
                            ownobj: orgInstance,
                            propertyWrapper: "${propertyWrapper}",
                            tenant: authOrg
                    ]}"/>

                    <laser:script file="${this.getGroovyPageFileName()}">
                        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${authOrg.id});
                    </laser:script>

                </div>
            </div>
        </div><!--.card-->
    </g:if>

</g:if>
<laser:render template="/templates/properties/createProperty_js"/>

<!-- _properties -->