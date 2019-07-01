<!-- _licPropGroupWrapper -->
<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />

<g:set var="propList" value="${propDefGroup.getCurrentProperties(ownObj)}" />

<g:if test="${propList}">
    <div class="content">
        <h5 class="ui header">
            <g:link controller="license" action="show" id="${ownObj.id}"><i class="balance scale icon"></i>${ownObj}</g:link>: ${propDefGroup.name}

            <g:if test="${showConsortiaFunctions}">
                <g:if test="${propDefGroup.ownerType == License.class.name}">
                    <g:if test="${! propDefGroupBinding || propDefGroupBinding?.visibleForConsortiaMembers?.value == 'Yes'}">
                        <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </g:if>
            </g:if>
        </h5>

        <div id="grouped_derived_custom_props_div_${propDefGroup.id}">

            <g:render template="/subscription/licPropGroup" model="${[
                    propList: propList,
                    ownObj: ownObj
            ]}"/>
        </div>
    </div>
</g:if>

<!-- _licPropGroupWrapper -->