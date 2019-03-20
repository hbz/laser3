<%@ page import="com.k_int.kbplus.Subscription" %>
<g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}" params="[
            workFlowPart: workFlowPart,
            workFlowPartNext: workFlowPartNext
        ]" method="post" class="ui form newLicence">
    <div class="three fields">
        <div class="field">
            <label>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}: </label>
            <g:select class="ui search dropdown"
                  name="sourceSubscriptionId"
                  from="${allSubscriptions_readRights}"
                  optionValue="name"
                  optionKey="id"
                  value="${sourceSubscription?.id}"
                  />
        </div>
                  %{--disabled="${(subscription)? true : false}"/>--}%
        <div class="field">
            <label>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}: </label>
            <g:select class="ui search dropdown"
                  name="targetSubscriptionId"
                  from="${allSubscriptions_writeRights}"
                  optionValue="name"
                  optionKey="id"
                  value="${targetSubscription?.id}"
                  noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
        </div>
        <div class="field la-field-right-aligned">
            <input type="submit" class="ui button" value="Lizenzen auswählen" />
        </div>
    </div>
</g:form>
