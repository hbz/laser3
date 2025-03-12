<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>
<!-- _properties -->

<%-- private properties --%>
<div class="ui card la-dl-no-table">

    <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <div class="right aligned four wide column">
            <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'license.button.addProperty')}"
                    onclick="JSPC.app.createProperty(${provider.id}, '${provider.class.simpleName}');">
                <i class="${Icon.CMD.ADD}"></i>
            </button>
        </div>
    </g:if>

    <div class="content">
        <h2 class="ui header">${message(code: 'org.properties.private')} ${contextService.getOrg().name}</h2>
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}"/>
        <div id="${propertyWrapper}">
            <laser:render template="/templates/properties/private" model="${[
                    prop_desc      : PropertyDefinition.PRV_PROP,
                    ownobj         : provider,
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
<!-- _properties -->