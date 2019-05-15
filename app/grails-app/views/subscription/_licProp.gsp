<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _licProp -->

<%-- grouped custom properties --%>

<g:each in="${derivedPropDefGroups.global}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value == 'Yes'}">

        <!-- global -->
        <g:render template="licPropGroupWrapper" model="${[
                propDefGroup: propDefGroup,
                propDefGroupBinding: null,
                ownObj: license
        ]}"/>
    </g:if>
</g:each>

<g:each in="${derivedPropDefGroups.local}" var="propDefGroup">
<%-- check binding visibility --%>
    <g:if test="${propDefGroup[1]?.visible?.value == 'Yes'}">

        <!-- local -->
        <g:render template="licPropGroupWrapper" model="${[
                propDefGroup: propDefGroup[0],
                propDefGroupBinding: propDefGroup[1],
                ownObj: license
        ]}"/>
    </g:if>
</g:each>

<g:each in="${derivedPropDefGroups.member}" var="propDefGroup">
<%-- check binding visibility --%>
    <g:if test="${propDefGroup[1]?.visible?.value == 'Yes'}">
    <%-- check member visibility --%>
        <g:if test="${propDefGroup[1]?.visibleForConsortiaMembers?.value == 'Yes'}">

            <!-- member -->
            <g:render template="licPropGroupWrapper" model="${[
                    propDefGroup: propDefGroup[0],
                    propDefGroupBinding: propDefGroup[1],
                    ownObj: license
            ]}"/>
        </g:if>
    </g:if>
</g:each>

<%-- custom properties --%>

<g:if test="${derivedPropDefGroups.fallback}">

    <div class="ui card la-dl-no-table">
        <div class="content">
            <h5 class="ui header">
                <g:link controller="license" action="show" id="${license.id}"><i class="balance scale icon"></i>${license}</g:link>: ${message(code:'subscription.properties')}
            </h5>

            <g:render template="licPropGroup" model="${[
                    propList: license.customProperties,
                    ownObj: license
            ]}"/>
        </div>
    </div><!--.card-->

</g:if>


<!-- _licProp -->