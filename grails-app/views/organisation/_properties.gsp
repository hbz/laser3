<%@ page import="de.laser.helper.AjaxUtils; de.laser.Org; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Org.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: orgInstance,
            availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<div class="ui card la-dl-no-table la-js-hideable">

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

            <g:render template="/templates/properties/groupWrapper" model="${[
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
            <semui:msg class="info" header="" text="${hiddenPropertiesMessages.join('<br />')}" />
        </div>
    </g:if>

<%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table la-js-hideable">--%>
    <div class="content">
        <h2 class="ui header">
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
            </g:if>
            <g:else>
                ${message(code:'org.properties')}
            </g:else>
        </h2>

        <div id="custom_props_div_props">
            <g:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.ORG_PROP,
                    ownobj: orgInstance,
                    orphanedProperties: allPropDefGroups.orphanedProperties,
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    <%--</div>--%>

    <laser:script file="${this.getGroovyPageFileName()}">
        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup' params='[oid:"${orgInstance.class.simpleName}:${orgInstance.id}"]'/>", "#custom_props_div_props");
    </laser:script>

</div><!-- .card -->

<%-- private properties --%>
<g:if test="${accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')}">

<g:each in="${authorizedOrgs}" var="authOrg">
    <g:if test="${authOrg.name == contextOrg?.name}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h2 class="ui header">${message(code:'org.properties.private')} ${authOrg.name}</h2>
                <g:set var="propertyWrapper" value="private-property-wrapper-${authOrg.id}" />
                <div id="${propertyWrapper}">
                    <g:render template="/templates/properties/private" model="${[
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
</g:each>

</g:if>

<!-- _properties -->