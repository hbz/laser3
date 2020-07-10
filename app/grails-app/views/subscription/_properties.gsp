<%@ page import="com.k_int.kbplus.SubscriptionProperty; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), Subscription.class.name)}" />

<%-- modal --%>

<semui:modal id="propDefGroupBindings" message="propertyDefinitionGroup.config.label" hideSubmitButton="hideSubmitButton">

    <g:render template="/templates/properties/groupBindings" model="${[
            propDefGroup: propDefGroup,
            ownobj: subscriptionInstance,
            availPropDefGroups: availPropDefGroups
    ]}" />

</semui:modal>

<g:if test="${subscriptionInstance.getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL,CalculatedType.TYPE_ADMINISTRATIVE]}">
    <div class="ui card la-dl-no-table ">
        <div class="content">
            <h5 class="ui header">${message(code:'license.properties.consortium')}</h5>
            <div id="member_props_div">
                <g:render template="/templates/properties/members" model="${[
                        prop_desc: PropertyDefinition.SUB_PROP,
                        ownobj: subscriptionInstance,
                        custom_props_div: "member_props_div"]}"/>

                <%--<r:script language="JavaScript">
                    $(document).ready(function(){
                           c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${institution.id}", ${institution.id});
                    });
                </r:script>--%>
            </div>
        </div>
    </div>
</g:if>

<!-- TODO div class="ui card la-dl-no-table la-js-hideable" -->
<div class="ui card la-dl-no-table">
<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${subscriptionInstance.getCalculatedPropDefGroups(contextService.getOrg())}" />

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
            else if (cat == 'member') {
                isVisible = binding.isVisible && binding.isVisibleForConsortiaMembers
            }
        %>

        <g:if test="${isVisible}">

            <g:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup: pdg,
                    propDefGroupBinding: binding,
                    prop_desc: PropertyDefinition.SUB_PROP,
                    ownobj: subscriptionInstance,
                    custom_props_div: "grouped_custom_props_div_${pdg.id}"
            ]}"/>
        </g:if>
        <g:else>
            <g:set var="numberOfProperties" value="${pdg.getCurrentProperties(subscriptionInstance)}" />

            <g:if test="${numberOfProperties.size() > 0}">
                <%
                    hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [pdg.name, numberOfProperties.size()])}"
                %>
            </g:if>
        </g:else>
    </g:each>

    <g:if test="${hiddenPropertiesMessages.size() > 0}">
        <div class="content">
            <semui:msg class="info" header="" text="${hiddenPropertiesMessages.join('<br/>')}" />
        </div>
    </g:if>

<%-- orphaned properties --%>

    <%--<div class="ui card la-dl-no-table la-js-hideable"> --%>
    <div class="content">
        <h5 class="ui header">
            <g:if test="${allPropDefGroups.global || allPropDefGroups.local || allPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
            </g:if>
            <g:else>
                ${message(code:'subscription.properties')}
            </g:else>
        </h5>

        <div id="custom_props_div_props">
            <g:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.SUB_PROP,
                    ownobj: subscriptionInstance,
                    orphanedProperties: allPropDefGroups.orphanedProperties,
                    editable: accessService.checkPermAffiliation('ORG_INST, ORG_CONSORTIUM','INST_EDITOR'),
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    <%--</div>--%>

    <r:script language="JavaScript">
    $(document).ready(function(){
        c3po.initProperties("<g:createLink controller='ajax' action='lookup' params='[oid:"${subscriptionInstance.class.simpleName}:${subscriptionInstance.id}"]'/>", "#custom_props_div_props");
    });
    </r:script>

</div><!--.card -->

<%-- private properties --%>

<!-- TODO div class="ui card la-dl-no-table la-js-hideable" -->
<div class="ui card la-dl-no-table ">
    <div class="content">
        <h5 class="ui header">${message(code:'subscription.properties.private')} ${institution.name}</h5>
        <div id="custom_props_div_${institution.id}">
            <g:render template="/templates/properties/private" model="${[
                    prop_desc: PropertyDefinition.SUB_PROP,
                    ownobj: subscriptionInstance,
                    custom_props_div: "custom_props_div_${institution.id}",
                    tenant: institution]}"/>

            <r:script language="JavaScript">
                    $(document).ready(function(){
                           c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${institution.id}", ${institution.id});
                    });
            </r:script>
        </div>
    </div>
</div>

<!-- _properties -->