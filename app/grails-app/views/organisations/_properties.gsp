<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Org.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" text="Merkmalgruppen konfigurieren" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: orgInstance,
            availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<%-- grouped custom properties --%>

<g:set var="allPropDefGroups" value="${orgInstance.getCalculatedPropDefGroups(contextService.getOrg())}" />

<g:each in="${allPropDefGroups.global}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value == 'Yes'}">

        <g:render template="/templates/properties/groupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                prop_desc: PropertyDefinition.ORG_PROP,
                ownobj: orgInstance,
                custom_props_div: "grouped_custom_props_div_${propDefGroup.id}"
        ]}"/>
    </g:if>
</g:each>

<g:each in="${allPropDefGroups.local}" var="propDefInfo">
<%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.visible?.value == 'Yes'}">

        <g:render template="/templates/properties/groupWrapper" model="${[
                propDefGroup: propDefInfo[0],
                propDefGroupBinding: propDefInfo[1],
                prop_desc: PropertyDefinition.ORG_PROP,
                ownobj: orgInstance,
                custom_props_div: "grouped_custom_props_div_${propDefInfo[0].id}"
        ]}"/>
    </g:if>
</g:each>

<%-- orphaned properties --%>

<g:if test="${! allPropDefGroups.fallback}">
    <g:if test="${allPropDefGroups.orphanedProperties}">

        <div class="ui card la-dl-no-table la-js-hideable">
            <div class="content">
                <h5 class="ui header">
                    ${message(code:'subscription.properties.orphaned')}
                </h5>

                <div id="custom_props_div_props">
                    <g:render template="/templates/properties/orphaned" model="${[
                            prop_desc: PropertyDefinition.ORG_PROP,
                            ownobj: orgInstance,
                            orphanedProperties: allPropDefGroups.orphanedProperties,
                            custom_props_div: "custom_props_div_props" ]}"/>
                </div>
            </div>
        </div>

    </g:if>
</g:if>

<%-- custom properties --%>

<g:else>

    <div class="ui card la-dl-no-table la-js-hideable">
        <div class="content">
            <h5 class="ui header">
                ${message(code:'org.properties')}
            </h5>

            <div id="custom_props_div_props">
                <g:render template="/templates/properties/custom" model="${[
                        prop_desc: PropertyDefinition.ORG_PROP,
                        ownobj: orgInstance,
                        custom_props_div: "custom_props_div_props" ]}"/>
            </div>
        </div>
    </div>

    <r:script language="JavaScript">
        $(document).ready(function(){
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
        });
    </r:script>

</g:else>

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

<%--<r:script>
    $(function(){
        $('#new-dynamic-properties-block a.xEditableValue').each( function(i, elem) {
            $(elem).on('save', function(e, params){
                $target = $(e.target)
                $updates = $('#new-dynamic-properties-block a.xEditableValue[id="' + $target.attr('id') + '"]')
                $updates.attr('data-oldvalue', params.newValue) // TODO BUGGY
                $updates.text(params.response)
            })
        })
        $('#new-dynamic-properties-block a.xEditableManyToOne').each( function(i, elem) {
            $(elem).on('save', function(e, params){
                $target = $(e.target)
                $updates = $('#new-dynamic-properties-block a.xEditableManyToOne[id="' + $target.attr('id') + '"]')
                $updates.attr('data-value', params.newValue) // TODO BUGGY
                $updates.text(params.response.newValue)
            })
        })
    })

</r:script>--%>

<!-- _properties -->