<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), License.class.name)}" />

<g:if test="${availPropDefGroups}">

    <div class="ui card la-dl-no-table">
        <div class="content">
            <h5 class="ui header"><i class="icon cogs"></i>Merkmale anzeigen</h5>

            <g:render template="/templates/properties/groupBindings" model="${[
                    propDefGroup: propDefGroup,
                    ownobj: license,
                    availPropDefGroups: availPropDefGroups
            ]}" />
        </div>
    </div>

</g:if>

<%-- grouped custom properties --%>

<g:each in="${availPropDefGroups}" var="propDefGroup">
    <% def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(propDefGroup, license) %>

    <g:if test="${propDefGroup.visible?.value?.equalsIgnoreCase('Yes') || binding?.visible?.value == 'Yes'}">
        <g:if test="${! (binding && binding?.visible?.value == 'No')}">

            <div class="ui card la-dl-no-table">
                <div class="content">
                    <h5 class="ui header">Merkmale: ${propDefGroup.name}</h5>
                    <div id="grouped_custom_props_div_${propDefGroup.id}">

                        <g:render template="/templates/properties/groups" model="${[
                                propDefGroup: propDefGroup,
                                prop_desc: PropertyDefinition.LIC_PROP, // TODO: change
                                ownobj: license,
                                custom_props_div: "grouped_custom_props_div_${propDefGroup.id}"
                        ]}"/>
                    </div>
                </div>
            </div><!--.card-->

            <r:script language="JavaScript">
                $(document).ready(function(){
                    c3po.initGroupedProperties("<g:createLink controller='ajax' action='lookup'/>", "#grouped_custom_props_div_${propDefGroup.id}");
                });
            </r:script>
        </g:if>
    </g:if>
</g:each>

<%-- custom properties --%>

<g:if test="${! availPropDefGroups}">

    <div class="ui card la-dl-no-table">
        <div class="content">
            <h5 class="ui header">
                ${message(code:'license.properties')}
                <% /*
                                        if (license.instanceOf && ! license.instanceOf.isTemplate()) {
                                            if (license.isSlaved?.value?.equalsIgnoreCase('yes')) {
                                                println '&nbsp; <span data-tooltip="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                                            }
                                            else {
                                                println '&nbsp; <span data-tooltip="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                            }
                                        }
                                        else {
                                            println '&nbsp; <span data-tooltip="Wert wird vererbt." data-position="top right"><i class="icon thumbtack blue inverted"></i></span>'
                                        }
                                    */ %>
            </h5>

            <div id="custom_props_div_props">
                <g:render template="/templates/properties/custom" model="${[
                        prop_desc: PropertyDefinition.LIC_PROP,
                        ownobj: license,
                        custom_props_div: "custom_props_div_props" ]}"/>
            </div>
        </div>
    </div><!--.card-->

    <div class="ui card la-dl-no-table">
        <div class="content">

            <h5 class="ui header">
                ${message(code:'license.openaccess.properties')}
            </h5>

            <div id="custom_props_div_oa">
                <g:render template="/templates/properties/custom" model="${[
                        prop_desc: PropertyDefinition.LIC_OA_PROP,
                        ownobj: license,
                        custom_props_div: "custom_props_div_oa" ]}"/>
            </div>
        </div>
    </div><!--.card-->

    <div class="ui card la-dl-no-table">
        <div class="content">

            <h5 class="ui header">
                ${message(code:'license.archive.properties')}
            </h5>

            <div id="custom_props_div_archive">
                <g:render template="/templates/properties/custom" model="${[
                        prop_desc: PropertyDefinition.LIC_ARC_PROP,
                        ownobj: license,
                        custom_props_div: "custom_props_div_archive" ]}"/>
            </div>
        </div>
    </div><!--.card-->

    <r:script language="JavaScript">
        $(document).ready(function(){
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_oa");
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_archive");
        });
    </r:script>

</g:if>

<%-- private properties --%>

<div class="ui card la-dl-no-table">
    <div class="content">
        <g:each in="${authorizedOrgs}" var="authOrg">
            <g:if test="${authOrg.name == contextOrg?.name}">
                <h5 class="ui header">${message(code:'license.properties.private')} ${authOrg.name}</h5>

                <div id="custom_props_div_${authOrg.id}">
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.LIC_PROP,
                            ownobj: license,
                            custom_props_div: "custom_props_div_${authOrg.id}",
                            tenant: authOrg]}"/>

                    <r:script language="JavaScript">
                            $(document).ready(function(){
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                            });
                    </r:script>
                </div>
            </g:if>
        </g:each>
    </div>
</div><!--.card-->

<!-- _properties -->