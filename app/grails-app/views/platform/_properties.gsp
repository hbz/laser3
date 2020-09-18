<%@ page import="de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; com.k_int.kbplus.Platform; com.k_int.kbplus.RefdataValue; de.laser.RefdataCategory" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextOrg, Platform.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
        propDefGroup: propDefGroup,
        ownobj: platform,
        availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<div class="ui card la-dl-no-table la-js-hideable">

    <g:set var="allPropDefGroups" value="${platform._getCalculatedPropDefGroups(contextOrg)}" />

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
                    prop_desc: PropertyDefinition.PLA_PROP,
                    ownobj: platform,
                    orphanedProperties: allPropDefGroups.orphanedProperties,
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    <%--</div>--%>

    <r:script>
        $(document).ready(function(){
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
        });
    </r:script>

</div><!-- .card -->
<!-- _properties -->