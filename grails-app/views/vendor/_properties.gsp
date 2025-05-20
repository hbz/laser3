<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection />
<!-- _properties -->

<%-- private properties --%>
<div class="ui card la-dl-no-table">


    <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <div class="content">
            <div class="ui header la-flexbox la-justifyContent-spaceBetween">
                <h2>
                    ${message(code: 'default.properties.my')}
                </h2>
                <div class="right aligned four wide column">
                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code:'license.button.addProperty')}"
                            onclick="JSPC.app.createProperty(${vendor.id}, '${vendor.class.simpleName}','true');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
                </div>
            </div>
        </div>
    </g:if>

    <div class="content">
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}" />
        <div id="${propertyWrapper}">
            <laser:render template="/templates/properties/private" model="${[
                prop_desc: PropertyDefinition.VEN_PROP,
                ownobj: vendor,
                propertyWrapper: "${propertyWrapper}",
                tenant: contextService.getOrg()
            ]}"/>
            <laser:script file="${this.getGroovyPageFileName()}">
                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextService.getOrg().id});
            </laser:script>
        </div>
    </div>
</div><!--.card-->
<laser:render template="/templates/properties/createProperty_js"/>
<!-- _properties -->