<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.Subscription; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<laser:serviceInjection />
<!-- _groupWrapper -->

<%-- SHOW --%>
<%--<div class="ui card la-dl-no-table">--%>
    <div class="content">
        <h5 class="ui header">
            ${message(code: 'subscription.properties.public')} ${propDefGroup.name}

            <g:if test="${showConsortiaFunctions}">
                <g:if test="${propDefGroup.ownerType in [License.class.name, Subscription.class.name]}">
                    <g:if test="${! propDefGroupBinding || propDefGroupBinding?.visibleForConsortiaMembers?.value == 'Yes'}">
                        <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </g:if>
            </g:if>
        </h5>

        <div id="grouped_custom_props_div_${propDefGroup.id}">

            <g:render template="/templates/properties/group" model="${[
                    propDefGroup: propDefGroup,
                    prop_desc: prop_desc,
                    ownobj: ownobj,
                    custom_props_div: custom_props_div
            ]}"/>
        </div>
    </div>
<%--</div><!--.card-->--%>

<r:script language="JavaScript">
        $(document).ready(function(){
            c3po.initGroupedProperties("<g:createLink controller='ajax' action='lookup'/>", "#${custom_props_div}");
        });
</r:script>

<!-- _groupWrapper -->