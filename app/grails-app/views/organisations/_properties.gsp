<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Org.class.name)}" />

<g:if test="${availPropDefGroups}">
    <semui:modal id="propDefGroupBindings" text="Merkmalsgruppen anzeigen" hideSubmitButton="hideSubmitButton">

        <g:render template="/templates/properties/groupBindings" model="${[
                propDefGroup: propDefGroup,
                ownobj: orgInstance,
                availPropDefGroups: availPropDefGroups
        ]}" />

    </semui:modal>
</g:if>

<%-- grouped custom properties --%>

<g:each in="${availPropDefGroups}" var="propDefGroup">
    <% def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(propDefGroup, orgInstance) %>

    <g:if test="${propDefGroup.visible?.value?.equalsIgnoreCase('Yes') || binding?.visible?.value == 'Yes'}">
        <g:if test="${! (binding && binding?.visible?.value == 'No')}">

            <div class="ui card la-dl-no-table">
                <div class="content">
                    <h5 class="ui header">Merkmale: ${propDefGroup.name}</h5>
                    <div id="grouped_custom_props_div_${propDefGroup.id}">

                        <g:render template="/templates/properties/group" model="${[
                                propDefGroup: propDefGroup,
                                prop_desc: PropertyDefinition.ORG_PROP, // TODO: change
                                ownobj: orgInstance,
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
            <h5 class="ui header">${message(code:'org.properties')}</h5>

            <div id="custom_props_div_props">

                <g:render template="/templates/properties/custom" model="${[
                        prop_desc: PropertyDefinition.ORG_PROP,
                        ownobj: orgInstance,
                        custom_props_div: "custom_props_div_props"
                ]}"/>
            </div>
        </div>
    </div><!--.card-->

    <r:script language="JavaScript">
        $(document).ready(function(){
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
        });
    </r:script>

</g:if>

<%-- private properties --%>

<g:each in="${authorizedOrgs}" var="authOrg">
    <g:if test="${authOrg.name == contextOrg?.name}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h5 class="ui header">${message(code:'org.properties.private')} ${authOrg.name}</h5>

                <div id="custom_props_div_${authOrg.id}">
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.ORG_PROP, // TODO: change
                            ownobj: orgInstance,
                            custom_props_div: "custom_props_div_${authOrg.id}",
                            tenant: authOrg
                    ]}"/>

                    <r:script language="JavaScript">
                            $(document).ready(function(){
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                            });
                    </r:script>
                </div>
            </div>
        </div><!--.card-->
    </g:if>
</g:each>

<!-- _properties -->