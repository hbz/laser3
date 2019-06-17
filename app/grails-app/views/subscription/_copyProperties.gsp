<%@ page import="com.k_int.kbplus.abstract_domain.PrivateProperty; com.k_int.kbplus.abstract_domain.CustomProperty; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService");
   def contextOrg = contextService.org%>
<semui:form>
    <g:render template="selectSourceAndTargetSubscription" model="[
            sourceSubscription: sourceSubscription,
            targetSubscription: targetSubscription,
            allSubscriptions_readRights: allSubscriptions_readRights,
            allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
            params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">
        <table class="ui celled table" style="table-layout: fixed; width: 100%">
            <tbody>
                <tr>
                    <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}:</b>
                    <g:if test="${sourceSubscription}"><g:link controller="subscription" action="show" id="${sourceSubscription?.id}">${sourceSubscription?.name}</g:link></g:if>
                    </td>
                    <td><b>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}:</b>
                        <g:if test="${targetSubscription}"><g:link controller="subscription" action="show" id="${targetSubscription?.id}">${targetSubscription?.name}</g:link></g:if>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align: top">
                        <g:if test="${sourceSubscription}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                    show_checkboxes: true,
                                    showCopyConflicts: false,
                                    prop_desc: PropertyDefinition.SUB_PROP,
                                    ownobj: sourceSubscription,
                                    showPropClass: CustomProperty.class,
                                    forced_not_editable: true,
                                    custom_props_div: "custom_props_div_${contextOrg.id}",
                                    tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td style="vertical-align: top">
                        <g:if test="${targetSubscription}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                    show_checkboxes: false,
                                    showCopyConflicts: true,
                                    prop_desc: PropertyDefinition.SUB_PROP,
                                    ownobj: targetSubscription,
                                    showPropClass: CustomProperty.class,
                                    forced_not_editable: true,
                                    custom_props_div: "custom_props_div_${contextOrg.id}",
                                    tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align: top">
                        <g:if test="${sourceSubscription}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                show_checkboxes: true,
                                showCopyConflicts: false,
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: sourceSubscription,
                                showPropClass: PrivateProperty.class,
                                forced_not_editable: true,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td style="vertical-align: top">
                        <g:if test="${targetSubscription}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/selectableProperties" model="${[
                                show_checkboxes: false,
                                showCopyConflicts: true,
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: targetSubscription,
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
