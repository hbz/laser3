<%@ page import="de.laser.CustomerTypeService; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection />
<!-- _properties -->

<%-- private properties --%>
<div class="ui card la-dl-no-table">
    <div class="content">
        <h2 class="ui header">${message(code:'org.properties.private')} ${contextService.getOrg().name}</h2>
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}" />
        <div id="${propertyWrapper}">
            <laser:render template="/templates/properties/private" model="${[
                prop_desc: PropertyDefinition.PRV_PROP,
                ownobj: provider,
                propertyWrapper: "${propertyWrapper}",
                tenant: contextService.getOrg()
            ]}"/>
            <laser:script file="${this.getGroovyPageFileName()}">
                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextService.getOrg().id});
            </laser:script>
        </div>
    </div>
</div><!--.card-->

<!-- _properties -->