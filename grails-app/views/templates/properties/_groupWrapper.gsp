<%@ page import="de.laser.Subscription; de.laser.License; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.*" %>
<laser:serviceInjection />
<!-- _groupWrapper -->

<%-- SHOW --%>
<%--<div class="ui card la-dl-no-table">--%>
    <div class="content">
        <h5 class="ui header">
            ${message(code: 'subscription.properties.public')}
            (${propDefGroup.name})

            <g:if test="${showConsortiaFunctions}">
                <g:if test="${propDefGroup.ownerType in [License.class.name, Subscription.class.name]}">
                    <g:if test="${propDefGroupBinding?.isVisibleForConsortiaMembers}">
                        <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="ui icon eye orange"></i>
                        </span>
                    </g:if>
                </g:if>
            </g:if>
        </h5>

        <div id="grouped_custom_props_div_${propDefGroup.id}">
             <%--!!!!Die Editable Prüfung dient dazu, dass für die Umfrag Lizenz-Merkmal nicht editierbar sind !!!!--%>
            <g:render template="/templates/properties/group" model="${[
                    propDefGroup: propDefGroup,
                    propDefGroupBinding: propDefGroupBinding,
                    prop_desc: prop_desc,
                    ownobj: ownobj,
                    editable: (!(controllerName in ['survey', 'myInstitution'] ) && accessService.checkPermAffiliation('ORG_INST, ORG_CONSORTIUM','INST_EDITOR')),
                    custom_props_div: custom_props_div
            ]}"/>
        </div>
    </div>
<%--</div><!--.card-->--%>

<asset:script type="text/javascript">
        $(document).ready(function(){
            c3po.initGroupedProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${custom_props_div}");
        });
</asset:script>

<!-- _groupWrapper -->
