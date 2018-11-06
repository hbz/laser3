<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<!-- _properties -->

<%-- grouped custom properties --%>

<g:each in="${PropertyDefinitionGroup.findAllByTenantAndOwnerType(contextService.getOrg(), Org.class.name)}" var="propDefGroup">
    <g:if test="${propDefGroup.visible?.value?.equalsIgnoreCase('Yes')}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h5 class="ui header"><i class="circle icon red"></i>${propDefGroup.name} (${propDefGroup.tenant})</h5>
                <div id="grouped_custom_props_div_${propDefGroup.id}">
                    <g:render template="/templates/properties/grouped_custom" model="${[
                            propDefGroup: propDefGroup,
                            prop_desc: 'Organisation Property', // TODO: change
                            ownobj: orgInstance,
                            custom_props_div: "grouped_custom_props_div_${propDefGroup.id}"
                    ]}"/>
                </div>
            </div>
        </div><!--.card-->

        <r:script language="JavaScript">
            $(document).ready(function(){
                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#grouped_custom_props_div_${propDefGroup.id}");
            });
        </r:script>
    </g:if>
</g:each>

<%-- custom properties --%>

<div class="ui card la-dl-no-table">
    <div class="content">
        <h5 class="ui header">${message(code:'org.properties')}</h5>

        <div id="custom_props_div_props">
            <g:render template="/templates/properties/custom" model="${[
                    prop_desc: PropertyDefinition.ORG_PROP,
                    ownobj: orgInstance,
                    custom_props_div: "custom_props_div_props"
            ]}"/>
        </div>
    </div>
</div><!--.card-->

<r:script language="JavaScript">
    $(document).ready(function(){
        c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
    });
</r:script>

<%-- private properties --%>

<g:each in="${authorizedOrgs}" var="authOrg">
    <g:if test="${authOrg.name == contextOrg?.name}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h5 class="ui header">${message(code:'org.properties.private')} ${authOrg.name}</h5>

                <div id="custom_props_div_${authOrg.id}">
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.ORG_PROP, // TODO: change
                            ownobj: orgInstance,
                            custom_props_div: "custom_props_div_${authOrg.id}",
                            tenant: authOrg
                    ]}"/>

                    <r:script language="JavaScript">
                            $(document).ready(function(){
                                c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                            });
                    </r:script>
                </div>
            </div>
        </div><!--.card-->
    </g:if>
</g:each>

<!-- _properties -->