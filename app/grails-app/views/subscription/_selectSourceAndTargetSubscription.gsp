<%@ page import="com.k_int.kbplus.Subscription" %>
<g:if test="${! (sourceSubscription && targetSubscription)}">
    <% if (params){
        params.remove('sourceSubscriptionId')
        params.remove('targetSubscriptionId')
    } %>
    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}"
            params="${params << [workFlowPart: workFlowPart]}"
            method="post" class="ui form newLicence"  onsubmit="enableSubmit();">
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
            <div class="six wide field">
                <label>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}: </label>
                <div class="ui checkbox">
                    <g:checkBox name="show.activeSubscriptions" value="nur aktive" checked="true" onchange="adjustDropdown()"/>
                    <label for="show.activeSubscriptions">${message(code:'subscription.details.copyElementsIntoSubscription.show.activeSubscriptions.name')}</label>
                </div><br />
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="auch Teilnehmerlizenzen" checked="false" onchange="adjustDropdown()" />
                    <label for="show.subscriber">${message(code:'subscription.details.copyElementsIntoSubscription.show.subscriber.name')}</label>
                </div><br />
                <div class="ui checkbox">
                    <g:checkBox name="show.conntectedSubscriptions" value="auch verknüpfte Lizenzen" checked="false" onchange="adjustDropdown()"/>
                    <label for="show.conntectedSubscriptions">${message(code:'subscription.details.copyElementsIntoSubscription.show.conntectedSubscriptions.name')}</label>
                </div><br />
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
                <input type="submit" class="ui wide button" value="Lizenzen auswählen"/>
            </div>
        </div>

    </g:form>
</g:if>
<g:javascript>
    function adjustDropdown() {
        var isActiveSubs = $("input[name='show.activeSubscriptions'").prop('checked');
        var isSubscriber = $("input[name='show.subscriber'").prop('checked');
        var isConnectedSubs = $("input[name='show.conntectedSubscriptions'").prop('checked');
        var url = '<g:createLink controller="ajax" action="adjustSubscriptionList"/>'+'?isActiveSubs='+isActiveSubs+'&isSubscriber='+isSubscriber+'&isConnectedSubs='+isConnectedSubs;
        $.ajax({
            url: url
        });
    }
</g:javascript>
