<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.Subscription; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Subscription.class.name)}"/>

<%-- modal --%>
<%-- removed as of ERMS-6520
<ui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="true">

    <laser:render template="/templates/properties/groupBindings" model="${[
            propDefGroup      : propDefGroup,
            ownobj            : subscription,
            editable          : contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC),
            availPropDefGroups: availPropDefGroups
    ]}"/>

</ui:modal>
--%>

<g:if test="${memberProperties}">%{-- check for content --}%
    <g:if test="${subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h2 class="ui header">${message(code: 'subscription.properties.consortium')}</h2>

                <div id="member_props_div">
                    <laser:render template="/templates/properties/members" model="${[
                            prop_desc       : PropertyDefinition.SUB_PROP,
                            ownobj          : subscription,
                            custom_props_div: "member_props_div"]}"/>
                </div>
            </div>
        </div>
    </g:if>
</g:if>

<!-- TODO div class="ui card la-dl-no-table" -->
<div class="ui card la-dl-no-table">
    <div class="content">
        <div class="ui header la-flexbox la-justifyContent-spaceBetween">
            <h2>
                ${message(code: 'default.properties')}
            </h2>
            <g:if test="${editable || contextService.isInstEditor(CustomerTypeService.ORG_INST_PRO) || contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                <div class="right aligned four wide column">
                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                            data-content="${message(code: 'license.button.addProperty')}"
                            onclick="JSPC.app.createProperty(${subscription.id}, '${subscription.class.simpleName}','false');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
                </div>
            </g:if>
        </div>
    </div>

<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${subscription.getCalculatedPropDefGroups(contextService.getOrg())}"/>

    <% List<String> hiddenPropertiesMessages = [] %>

    <g:each in="${allPropDefGroups.sorted}" var="entry">
        <%
            String cat = entry[0]
            PropertyDefinitionGroup pdg = entry[1]
            PropertyDefinitionGroupBinding binding = entry[2]
            List numberOfConsortiaProperties = []
            if (subscription.getConsortium() && contextService.getOrg().id != subscription.getConsortium().id)
                numberOfConsortiaProperties.addAll(pdg.getCurrentPropertiesOfTenant(subscription, subscription.getConsortium()))

            boolean isVisible = false

            if (cat == 'global') {
                isVisible = pdg.isVisible || numberOfConsortiaProperties.size() > 0
            } else if (cat == 'local') {
                isVisible = binding.isVisible
            } else if (cat == 'member') {
                isVisible = (binding.isVisible || numberOfConsortiaProperties.size() > 0) && binding.isVisibleForConsortiaMembers
            }
        %>

        <g:if test="${isVisible}">

            <laser:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup       : pdg,
                    propDefGroupBinding: binding,
                    prop_desc          : PropertyDefinition.SUB_PROP,
                    ownobj             : subscription,
                    custom_props_div   : "grouped_custom_props_div_${pdg.id}"
            ]}"/>
            <g:if test="${!binding?.isVisible && !pdg.isVisible}">
                <g:set var="numberOfProperties" value="${pdg.getCurrentProperties(subscription).size() - numberOfConsortiaProperties.size()}"/>
                <g:if test="${numberOfProperties > 0}">
                    <%
                        hiddenPropertiesMessages << "${message(code: 'propertyDefinitionGroup.info.existingItems.withInheritance', args: [pdg.name, numberOfProperties])}"
                    %>
                </g:if>
            </g:if>
        </g:if>
        <g:else>
            <g:set var="numberOfProperties" value="${pdg.getCurrentPropertiesOfTenant(subscription, contextService.getOrg())}"/>
            <g:if test="${numberOfProperties.size() > 0}">
                <%
                    hiddenPropertiesMessages << "${message(code: 'propertyDefinitionGroup.info.existingItems', args: [pdg.name, numberOfProperties.size()])}"
                %>
            </g:if>
        </g:else>
    </g:each>

    <g:if test="${hiddenPropertiesMessages.size() > 0}">
        <div class="content">
            <ui:msg class="info" text="${hiddenPropertiesMessages.join('<br />')}"/>
        </div>
    </g:if>

    <g:if test="${allPropDefGroups.orphanedProperties}">
    <%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table"> --%>
        <div class="content">
            <h3 class="ui header">
                <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                    ${message(code: 'subscription.properties.orphanedMajuscule')} ${message(code: 'subscription.propertiesBrackets')}
                </g:if>
                <g:else>
                    ${message(code: 'subscription.properties')}
                </g:else>
            </h3>

            <div id="custom_props_div_props">
                <laser:render template="/templates/properties/custom" model="${[
                        prop_desc         : PropertyDefinition.SUB_PROP,
                        ownobj            : subscription,
                        orphanedProperties: allPropDefGroups.orphanedProperties,
                        editable          : (!calledFromSurvey && contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)),
                        custom_props_div  : "custom_props_div_props"]}"/>
            </div>
        </div>
    <%--</div>--%>

        <laser:script file="${this.getGroovyPageFileName()}">
            c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'
                                               params='[oid: "${genericOIDService.getOID(subscription)}"]'/>", "#custom_props_div_props");
        </laser:script>
    </g:if>

</div><!--.card -->

<%-- private properties --%>

<!-- TODO div class="ui card la-dl-no-table" -->
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
                            onclick="JSPC.app.createProperty(${subscription.id}, '${subscription.class.simpleName}', 'true');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
                </div>
            </g:if>
        </div>
    </div>


    <div class="content">
        <g:set var="propertyWrapper" value="private-property-wrapper-${contextService.getOrg().id}"/>
        <div id="${propertyWrapper}">
            <laser:render template="/templates/properties/private" model="${[
                    prop_desc      : PropertyDefinition.SUB_PROP,
                    ownobj         : subscription,
                    propertyWrapper: "${propertyWrapper}",
                    tenant         : contextService.getOrg()]}"/>

            <laser:script file="${this.getGroovyPageFileName()}">
                c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${propertyWrapper}", ${contextService.getOrg().id});
            </laser:script>
        </div>
    </div>
</div>

<laser:render template="/templates/properties/createProperty_js"/>
<!-- _properties -->
