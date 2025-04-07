<!-- A: templates/properties/_groupWrapper -->
<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.CustomerTypeService; de.laser.Subscription; de.laser.License; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.*" %>
<laser:serviceInjection />
<%-- SHOW --%>
<%--<div class="ui card la-dl-no-table">--%>
    <div class="content">
        <h3 class="ui header">
            <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
            ${propDefGroup.name} ${message(code: 'subscription.propertiesBrackets')}
            <g:if test="${showConsortiaFunctions}">
                <g:if test="${propDefGroup.ownerType in [License.class.name, Subscription.class.name]}">
                    <g:if test="${propDefGroupBinding?.isVisibleForConsortiaMembers}">
                        <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                            <i class="${Icon.SIG.VISIBLE_ON} orange"></i>
                        </span>
                    </g:if>
                </g:if>
            </g:if>
        </h3>
        <div id="grouped_custom_props_div_${propDefGroup.id}">
             <%--!!!!Die Editable Prüfung dient dazu, dass für die Umfrag Lizenz-Merkmal nicht editierbar sind !!!!--%>
            <laser:render template="/templates/properties/group" model="${[
                    propDefGroup: propDefGroup,
                    propDefGroupBinding: propDefGroupBinding,
                    prop_desc: prop_desc,
                    ownobj: ownobj,
                    editable: (!(controllerName in ['survey', 'myInstitution'] ) && contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)),
                    custom_props_div: custom_props_div
            ]}"/>
        </div>
    </div>
<%--</div><!--.card-->--%>

<laser:script file="${this.getGroovyPageFileName()}">
    c3po.initGroupedProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#${custom_props_div}");
</laser:script>
<!-- O: templates/properties/_groupWrapper -->
