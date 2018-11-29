<%@ page import="com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Subscription.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" text="Merkmalsgruppen konfigurieren" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: subscriptionInstance,
            availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<%-- grouped custom properties --%>

<g:set var="allPropDefGroups" value="${subscriptionInstance.getCaculatedPropDefGroups()}" />

<g:each in="${allPropDefGroups.global}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value == 'Yes'}">

        <g:render template="/templates/properties/groupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                prop_desc: PropertyDefinition.SUB_PROP,
                ownobj: subscriptionInstance,
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
                prop_desc: PropertyDefinition.SUB_PROP,
                ownobj: subscriptionInstance,
                custom_props_div: "grouped_custom_props_div_${propDefInfo[0].id}"
        ]}"/>
    </g:if>
</g:each>

<g:each in="${allPropDefGroups.member}" var="propDefInfo">
<%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.visible?.value == 'Yes'}">
    <%-- check member visibility --%>
        <g:if test="${propDefInfo[1]?.visibleForConsortiaMembers?.value == 'Yes'}">

            <g:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup: propDefInfo[0],
                    propDefGroupBinding: propDefInfo[1],
                    prop_desc: PropertyDefinition.SUB_PROP,
                    ownobj: subscriptionInstance,
                    custom_props_div: "grouped_custom_props_div_${propDefInfo[0].id}"
            ]}"/>
        </g:if>
    </g:if>
</g:each>

<%-- custom properties --%>

<g:if test="${allPropDefGroups.fallback}">

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