<%@ page import="de.laser.Platform; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.CustomerTypeService" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextOrg, Platform.class.name)}" />

<%-- modal --%>

<ui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="true">

    <laser:render template="/templates/properties/groupBindings" model="${[
        propDefGroup: propDefGroup,
        ownobj: platform,
        availPropDefGroups: availPropDefGroups
    ]}" />

</ui:modal>

<div class="ui card la-dl-no-table la-js-hideable">

    <g:set var="allPropDefGroups" value="${platform.getCalculatedPropDefGroups(contextOrg)}" />

<%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table la-js-hideable">--%>
    <div class="content">
        <h2 class="ui header">
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
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

<%-- private properties --%>
<g:if test="${contextService.hasPerm(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">

        <div class="ui card la-dl-no-table">
            <div class="content">
                <h2 class="ui header">${message(code:'subscription.properties.private')} ${contextOrg.name}</h2>
                <g:set var="propertyWrapper" value="private-property-wrapper-${contextOrg.id}" />
                <div id="${propertyWrapper}">
                    <laser:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.PLA_PROP,
                            ownobj: platform,
                            propertyWrapper: "${propertyWrapper}",
                            tenant: contextOrg
                    ]}"/>

                    <laser:script file="${this.getGroovyPageFileName()}">
                        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextOrg.id});
                    </laser:script>

                </div>
            </div>
        </div><!--.card-->

</g:if>
<!-- _properties -->