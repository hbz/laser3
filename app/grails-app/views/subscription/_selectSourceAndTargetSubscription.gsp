<%@ page import="com.k_int.kbplus.Subscription" %>
<g:if test="${! (sourceSubscription && targetSubscription)}">
    <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id}" params="[
                workFlowPart: workFlowPart,
                workFlowPartNext: workFlowPartNext
            ]" method="post" class="ui form newLicence">
        <div class="fields" style="justify-content: flex-end;">
            <div class="six wide field">
                <label>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}: </label>
                <g:select class="ui search dropdown"
                      name="sourceSubscriptionId"
                      from="${((List<Subscription>)allSubscriptions_readRights)?.sort {it.dropdownNamingConvention()}}"
                      optionValue="${{it?.dropdownNamingConvention()}}"
                      optionKey="id"
                      value="${sourceSubscription?.id}"
                      />
            </div>
                      %{--disabled="${(subscription)? true : false}"/>--}%
            <div class="six wide field">
                <label>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}: </label>
                <g:select class="ui search dropdown"
                      name="targetSubscriptionId"
                      from="${((List<Subscription>)allSubscriptions_writeRights)?.sort {it.dropdownNamingConvention()}}"
                      optionValue="${{it?.dropdownNamingConvention()}}"
                      optionKey="id"
                      value="${targetSubscription?.id}"
                      noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="fields" style="justify-content: flex-end;">
            <div class="six wide field" style="text-align: right;">
                <input type="submit" class="ui wide button" value="Lizenzen auswählen" />
            </div>
        </div>

    </g:form>
</g:if>