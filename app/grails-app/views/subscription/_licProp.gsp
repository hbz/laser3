<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _licProp -->

<g:set var="derivedPropDefGroups" value="${license.getCalculatedPropDefGroups(contextService.getOrg())}" /> <%-- TODO: getONLYshared --%>

<g:if test="${derivedPropDefGroups.global || derivedPropDefGroups.local || derivedPropDefGroups.propDefInfo}">
    <div class="ui card la-dl-no-table">
        <div class="ui content">
            <h3 class="ui header">
                <g:link controller="license" action="show" id="${license.id}">${license}</g:link>
            </h3>
            <p>
                Die folgenden Merkmale beziehen sich auf den anhängenden Vertrag.
            </p>
        </div>
    </div>
</g:if>

<g:each in="${derivedPropDefGroups.global}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value == 'Yes'}">

        <!-- global -->
        <g:render template="licPropGroupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                prop_desc: PropertyDefinition.LIC_PROP,
                ownobj: license,
                custom_props_div: "derived_lic_props_div_${propDefGroup.id}"
        ]}"/>
    </g:if>
</g:each>

<g:each in="${derivedPropDefGroups.local}" var="propDefInfo">
<%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.visible?.value == 'Yes'}">

        <!-- local -->
        <g:render template="licPropGroupWrapper" model="${[
                propDefGroup: propDefInfo[0],
                propDefGroupBinding: propDefInfo[1],
                prop_desc: PropertyDefinition.LIC_PROP,
                ownobj: license,
                custom_props_div: "derived_lic_props_div_${propDefInfo[0].id}"
        ]}"/>
    </g:if>
</g:each>

<g:each in="${derivedPropDefGroups.member}" var="propDefInfo">
<%-- check binding visibility --%>
    <g:if test="${propDefInfo[1]?.visible?.value == 'Yes'}">
    <%-- check member visibility --%>
        <g:if test="${propDefInfo[1]?.visibleForConsortiaMembers?.value == 'Yes'}">

            <!-- member -->
            <g:render template="licPropGroupWrapper" model="${[
                    propDefGroup: propDefInfo[0],
                    propDefGroupBinding: propDefInfo[1],
                    prop_desc: PropertyDefinition.LIC_PROP,
                    ownobj: license,
                    custom_props_div: "derived_lic_props_div_${propDefInfo[0].id}"
            ]}"/>
        </g:if>
    </g:if>
</g:each>

<!-- _licProp -->