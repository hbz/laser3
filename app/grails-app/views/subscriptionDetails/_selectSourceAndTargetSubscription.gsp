<%@ page import="com.k_int.properties.PropertyDefinition; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService");
   def contextOrg = contextService.org%>
<g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}" params="[
            workFlowPart: workFlowPart,
            workFlowPartNext: workFlowPartNext
        ]" method="post" class="ui form newLicence">
    <div class="five wide column">
        <label>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}: </label>
        <g:select class="ui search dropdown"
                  name="sourceSubscriptionId"
                  from="${allSubscriptions_readRights}"
                  optionValue="name"
                  optionKey="id"
                  value="${sourceSubscription?.id}"
                  />
                  %{--disabled="${(subscription)? true : false}"/>--}%
        <label>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}: </label>
        <g:select class="ui search dropdown"
                  name="targetSubscriptionId"
                  from="${allSubscriptions_writeRights}"
                  optionValue="name"
                  optionKey="id"
                  value="${targetSubscription?.id}"
                  noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
        <input type="submit" class="ui button" value="Lizenz(en) auswählen" />
    </div>
</g:form>
