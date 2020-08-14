<%@ page import="com.k_int.kbplus.SubscriptionController; com.k_int.kbplus.abstract_domain.PrivateProperty; com.k_int.kbplus.abstract_domain.CustomProperty; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<%@ page import="static com.k_int.kbplus.Subscription.WORKFLOW_END" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService");
   def contextOrg = contextService.org%>
<semui:form>
    <g:render template="/templates/subscription/selectSourceAndTargetObject" model="[
            sourceObject: sourceObject,
            targetObject: targetObject,
            allObjects_readRights: allObjects_readRights,
            allObjects_writeRights: allObjects_writeRights]"/>
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceObjectId}"
            params="[workFlowPart: WORKFLOW_END, sourceObjectId: sourceObjectId, targetObjectId: targetObjectId]"
            method="post" class="ui form newLicence">
        <table class="ui celled table" style="table-layout: fixed; width: 100%">
            <tbody>
                <tr>
                    <td><b>${message(code: 'copyElementsIntoObject.sourceObject.name')}:</b>
                    <g:if test="${sourceObject}"><g:link controller="subscription" action="show" id="${sourceObject?.id}">${sourceObject?.dropdownNamingConvention()}</g:link></g:if>
                    </td>
                    <td><b>${message(code: 'copyElementsIntoObject.targetObject.name')}:</b>
                        <g:if test="${targetObject}"><g:link controller="subscription" action="show" id="${targetObject?.id}">${targetObject?.dropdownNamingConvention()}</g:link></g:if>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align: top">
                        <g:if test="${sourceObject}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                    show_checkboxes: true,
                                    showCopyConflicts: false,
                                    prop_desc: PropertyDefinition.SUB_PROP,
                                    ownobj: sourceObject,
                                    showPropClass: CustomProperty.class,
                                    forced_not_editable: true,
                                    custom_props_div: "custom_props_div_${contextOrg.id}",
                                    tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td style="vertical-align: top">
                        <g:if test="${targetObject}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                    show_checkboxes: false,
                                    showCopyConflicts: true,
                                    prop_desc: PropertyDefinition.SUB_PROP,
                                    ownobj: targetObject,
                                    showPropClass: CustomProperty.class,
                                    forced_not_editable: true,
                                    custom_props_div: "custom_props_div_${contextOrg.id}",
                                    tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align: top">
                        <g:if test="${sourceObject}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                show_checkboxes: true,
                                showCopyConflicts: false,
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: sourceObject,
                                showPropClass: PrivateProperty.class,
                                forced_not_editable: true,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td style="vertical-align: top">
                        <g:if test="${targetObject}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                show_checkboxes: false,
                                showCopyConflicts: true,
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: targetObject,
                                showPropClass: PrivateProperty.class,
                                forced_not_editable: true,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                </tr>
            </tbody>
        </table>
        <input type="submit" class="ui button js-click-control" value="Ausgewählte Merkmale in Ziellizenz kopieren" />
    </g:form>
</semui:form>
<style>
/*table  {*/
    /*table-layout: fixed;*/
    /*width: 100%;*/
/*}*/
/*table td {*/
    /*vertical-align: top;*/
/*}*/
</style>
