<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.*" %>
<!-- _properties -->

<g:set var="availPropDefGroups" value="${PropertyDefinitionGroup.findAllByTenantAndOwnerType(contextService.getOrg(), Org.class.name)}" />

<div class="ui card la-dl-no-table">
    <div class="content">
        <h5 class="ui header">Merkmalsgruppen anzeigen (lokal vor global)</h5>

        <table class="ui la-table-small la-table-inCard table">
            <thead>
                <tr>
                    <th>Merkmalsgruppe</th>
                    <th>Default</th>
                    <th>Anzeigen</th>
                    <th>Optionen</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${availPropDefGroups}" var="propDefGroup">
                    <tr>
                        <td>
                            <strong>${propDefGroup.name}</strong>

                            <g:if test="${propDefGroup.description}">
                                <p>${propDefGroup.description}</p>
                            </g:if>
                        </td>
                        <td>
                            ${propDefGroup.visible ? propDefGroup.visible.getI10n('value') : 'Nein'}
                        </td>
                        <td>
                            <g:set var="binding" value="${PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(propDefGroup, orgInstance)}" />

                            <g:if test="${binding?.id}">
                                <semui:xEditableRefData owner="${binding}" field="visible" config="YN" />
                            </g:if>
                        </td>
                        <td class="x">
                            <g:if test="${! binding?.id}">
                                <g:if test="${propDefGroup.visible?.value=='Yes'}">
                                    <button class="ui button">Lokal überschreiben</button>
                                </g:if>
                                <g:else>
                                    <button class="ui button">Lokal überschreiben</button>
                                </g:else>
                            </g:if>
                            <g:else>

                            </g:else>
                        </td>
                        <%-- <input type="checkbox" value="${propDefGroup.class.name}:${propDefGroup.id}" /> --%>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</div>

<%-- grouped custom properties --%>

<g:each in="${availPropDefGroups}" var="propDefGroup">
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