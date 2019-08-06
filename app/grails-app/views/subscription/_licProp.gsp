<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _licProp -->

<%-- grouped custom properties --%>

<% List<String> hiddenPropertiesMessages = [] %>

<div class="ui card la-dl-no-table">

<g:each in="${derivedPropDefGroups.global}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value == 'Yes'}">

        <!-- global -->
        <g:render template="/subscription/licPropGroupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                ownObj: license
        ]}"/>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefGroup.getCurrentProperties(license)}" />
        <g:if test="${numberOfProperties.size() > 0}">
            <%
                hiddenPropertiesMessages << "Die Merkmalsgruppe ${propDefGroup.name} beinhaltet <strong>${numberOfProperties.size()}</strong> Merkmale, ist aber ausgeblendet."
            %>
        </g:if>
    </g:else>
</g:each>

<g:each in="${derivedPropDefGroups.local}" var="propDefGroup">
<%-- check binding visibility --%>
    <g:if test="${propDefGroup[1]?.visible?.value == 'Yes'}">

        <!-- local -->
        <g:render template="/subscription/licPropGroupWrapper" model="${[
                propDefGroup: propDefGroup[0],
                propDefGroupBinding: propDefGroup[1],
                ownObj: license
        ]}"/>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefGroup[0].getCurrentProperties(license)}" />
        <g:if test="${numberOfProperties.size() > 0}">
            <%
                hiddenPropertiesMessages << "Die Merkmalsgruppe <strong>${propDefGroup[0].name}</strong> beinhaltet ${numberOfProperties.size()} Merkmale, ist aber ausgeblendet."
            %>
        </g:if>
    </g:else>
</g:each>

<g:each in="${derivedPropDefGroups.member}" var="propDefGroup">
<%-- check binding visibility --%>
    <g:if test="${propDefGroup[1]?.visible?.value == 'Yes'}">
    <%-- check member visibility --%>
        <g:if test="${propDefGroup[1]?.visibleForConsortiaMembers?.value == 'Yes'}">

            <!-- member -->
            <g:render template="/subscription/licPropGroupWrapper" model="${[
                    propDefGroup: propDefGroup[0],
                    propDefGroupBinding: propDefGroup[1],
                    ownObj: license
            ]}"/>
        </g:if>
    </g:if>
    <g:else>
        <g:set var="numberOfProperties" value="${propDefGroup[0].getCurrentProperties(license)}" />
        <g:if test="${numberOfProperties.size() > 0}">
            <%
                hiddenPropertiesMessages << "Die Merkmalsgruppe <strong>${propDefGroup[0].name}</strong> beinhaltet ${numberOfProperties.size()} Merkmale, ist aber ausgeblendet."
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

    <%--
    <div class="content">
        <h5 class="ui header">
            <g:if test="${derivedPropDefGroups.global || derivedPropDefGroups.local || derivedPropDefGroups.member}">
                ${message(code:'subscription.properties.orphaned')}
            </g:if>
            <g:else>
                ${message(code:'license.properties')}
            </g:else>
        </h5>

        <div id="custom_props_div_props">
            <g:render template="/templates/properties/orphaned" model="${[
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    orphanedProperties: derivedPropDefGroups.orphanedProperties,
                    custom_props_div: "custom_props_div_props" ]}"/>
        </div>
    </div>
    --%>

<%-- custom properties --%>

<g:if test="${derivedPropDefGroups.orphanedProperties}">

    <div class="content">
        <h5 class="ui header">
            <g:link controller="license" action="show" id="${license.id}"><i class="balance scale icon"></i>${license}</g:link>: ${message(code:'subscription.properties')}
        </h5>

        <g:render template="/subscription/licPropGroup" model="${[
                propList: derivedPropDefGroups.orphanedProperties,
                ownObj: license
        ]}"/>
    </div>

</g:if>



</div><!--.card-->

<!-- _licProp -->