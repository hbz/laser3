<%@ page import="com.k_int.kbplus.SubscriptionCustomProperty; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
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

<!-- TODO div class="ui card la-dl-no-table la-js-hideable" -->
<div class="ui card la-dl-no-table">
<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${subscriptionInstance.getCalculatedPropDefGroups(contextService.getOrg())}" />

    <% List<String> hiddenPropertiesMessages = [] %>

<g:each in="${allPropDefGroups.global}" var="propDefGroup">
    <%-- check visibility --%>
    <g:if test="${propDefGroup.isVisible}">

        <g:render template="/templates/properties/groupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                prop_desc: PropertyDefinition.SUB_PROP,
                ownobj: subscriptionInstance,
                custom_props_div: "grouped_custom_props_div_${propDefGroup.id}"
        ]}"/>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefGroup.getCurrentProperties(subscriptionInstance)}" />
        <g:if test="${numberOfProperties.size() > 0}">
           <%
               hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [propDefGroup.name, numberOfProperties.size()])}"
           %>
        </g:if>
    </g:else>
</g:each>

<g:each in="${allPropDefGroups.local}" var="propDefInfo">
    <%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.isVisible}">

        <g:render template="/templates/properties/groupWrapper" model="${[
                propDefGroup: propDefInfo[0],
                propDefGroupBinding: propDefInfo[1],
                prop_desc: PropertyDefinition.SUB_PROP,
                ownobj: subscriptionInstance,
                custom_props_div: "grouped_custom_props_div_${propDefInfo[0].id}"
        ]}"/>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefInfo[0].getCurrentProperties(subscriptionInstance)}" />
        <g:if test="${numberOfProperties.size() > 0}">
            <%
                hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [propDefInfo[0].name, numberOfProperties.size()])}"
            %>
        </g:if>
    </g:else>
</g:each>

<g:each in="${allPropDefGroups.member}" var="propDefInfo">
    <%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.isVisible}">
        <%-- check member visibility --%>
        <g:if test="${propDefInfo[1]?.isVisibleForConsortiaMembers}">

            <g:render template="/templates/properties/groupWrapper" model="${[
                    propDefGroup: propDefInfo[0],
                    propDefGroupBinding: propDefInfo[1],
                    prop_desc: PropertyDefinition.SUB_PROP,
                    ownobj: subscriptionInstance,
                    custom_props_div: "grouped_custom_props_div_${propDefInfo[0].id}"
            ]}"/>
        </g:if>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefInfo[0].getCurrentProperties(subscriptionInstance)}" />
        <g:if test="${numberOfProperties.size() > 0}">
            <%
                hiddenPropertiesMessages << "${message(code:'propertyDefinitionGroup.info.existingItems', args: [propDefInfo[0].name, numberOfProperties.size()])}"
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

<g:each in="${authorizedOrgs}" var="authOrg">
    <g:if test="${authOrg.name == contextOrg?.name && accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_USER','ROLE_ADMIN')}">
        <!-- TODO div class="ui card la-dl-no-table la-js-hideable" -->
        <div class="ui card la-dl-no-table ">
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

<!-- _properties -->