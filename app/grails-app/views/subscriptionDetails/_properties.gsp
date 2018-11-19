<%@ page import="com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Subscription.class.name)}" />

<%-- modal --%>

<g:if test="${availPropDefGroups.context}">
    <semui:modal id="propDefGroupBindings" text="Merkmalsgruppen anzeigen" hideSubmitButton="hideSubmitButton">

        <g:render template="/templates/properties/groupBindings" model="${[
                propDefGroup: propDefGroup,
                ownobj: subscriptionInstance,
                availPropDefGroups: availPropDefGroups
        ]}" />

    </semui:modal>
</g:if>

<%-- grouped custom properties --%>

<g:each in="${availPropDefGroups.all}" var="propDefGroup">
    <% def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndSub(propDefGroup, subscriptionInstance) %>

    <g:if test="${propDefGroup.visible?.value?.equalsIgnoreCase('Yes') || binding?.visible?.value == 'Yes'}">
        <g:if test="${! (binding && binding?.visible?.value == 'No')}">

            <div class="ui card la-dl-no-table">
                <div class="content">
                    <h5 class="ui header">Merkmale: ${propDefGroup.name}</h5>
                    <div id="grouped_custom_props_div_${propDefGroup.id}">

                        <g:render template="/templates/properties/group" model="${[
                                propDefGroup: propDefGroup,
                                prop_desc: PropertyDefinition.SUB_PROP,  // TODO: change
                                ownobj: subscriptionInstance,
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

<g:if test="${! availPropDefGroups.all}">

    <div class="ui card la-dl-no-table la-js-hideable">
        <div class="content">
            <h5 class="ui header">
                ${message(code:'subscription.properties')}
                <% /*
                                    if (subscriptionInstance.instanceOf && ! subscriptionInstance.instanceOf.isTemplate()) {
                                        if (subscriptionInstance.isSlaved?.value?.equalsIgnoreCase('yes')) {
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
                        prop_desc: PropertyDefinition.SUB_PROP,
                        ownobj: subscriptionInstance,
                        custom_props_div: "custom_props_div_props" ]}"/>
            </div>
        </div>
    </div>

    <r:script language="JavaScript">
        $(document).ready(function(){
            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
        });
    </r:script>

</g:if>

<%-- private properties --%>

<g:each in="${authorizedOrgs}" var="authOrg">
    <g:if test="${authOrg.name == contextOrg?.name}">
        <div class="ui card la-dl-no-table la-js-hideable">
            <div class="content">

                <h5 class="ui header">${message(code:'subscription.properties.private')} ${authOrg.name}</h5>

                <div id="custom_props_div_${authOrg.id}">
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: subscriptionInstance,
                            custom_props_div: "custom_props_div_${authOrg.id}",
                            tenant: authOrg]}"/>

                    <r:script language="JavaScript">
                        $(document).ready(function(){
                            c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                        });
                    </r:script>
                </div>
            </div>
        </div>
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