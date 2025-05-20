<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.wekb.Platform; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<!-- _properties -->
%{--
deactivated as of ERMS-4837

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Platform.class.name)}" />

<%-- modal --%>

<ui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="true">

    <laser:render template="/templates/properties/groupBindings" model="${[
        propDefGroup: propDefGroup,
        ownobj: platform,
        availPropDefGroups: availPropDefGroups
    ]}" />

</ui:modal>

<div class="ui card la-dl-no-table">

    <g:set var="allPropDefGroups" value="${platform.getCalculatedPropDefGroups(contextService.getOrg())}" />

<%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table">--%>
    <div class="content">
        <h2 class="ui header">
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code: 'subscription.properties.orphanedMajuscule')} ${message(code: 'subscription.propertiesBrackets')}
            </g:if>
            <g:else>
                ${message(code:'license.properties')}
            </g:else>
        </h2>

        <div id="custom_props_div_props">
            <laser:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.PLA_PROP,
                    ownobj: platform,
                    orphanedProperties: allPropDefGroups.orphanedProperties,
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    <%--</div>--%>

    <laser:script file="${this.getGroovyPageFileName()}">
        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_props");
    </laser:script>

</div><!-- .card -->
--}%

<%-- private properties --%>
<g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro()}">

    <div class="ui card la-dl-no-table">
        <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <div class="content">
                <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                    <h2>
                        ${message(code: 'default.properties.my')}
                    </h2>
                    <div class="right aligned four wide column">
                        <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                                data-content="${message(code: 'license.button.addProperty')}"
                                onclick="JSPC.app.createProperty(${platform.id}, '${platform.class.simpleName}', 'true');">
                            <i class="${Icon.CMD.ADD}"></i>
                        </button>
                    </div>
                </div>
            </div>
        </g:if>

        <div class="content">
            <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}"/>
            <div id="${propertyWrapper}">
                <laser:render template="/templates/properties/private" model="${[
                        prop_desc      : PropertyDefinition.PLA_PROP,
                        ownobj         : platform,
                        propertyWrapper: "${propertyWrapper}",
                        tenant         : contextService.getOrg()
                ]}"/>

                <laser:script file="${this.getGroovyPageFileName()}">
                    c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextService.getOrg().id});
                </laser:script>

                </div>
            </div>
        </div><!--.card-->
    <laser:render template="/templates/properties/createProperty_js"/>
</g:if>
<!-- _properties -->