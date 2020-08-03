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
                </div><br id="element-vor-target-dropdown" />
                <g:select class="ui search dropdown"
                      id="targetSubscriptionId"
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
    // $(document).ready(function(){
    //     alert("Geladen")
    //     $("input[name='show.activeSubscriptions'").prop('checked', true);
    //     $("input[name='show.subscriber'").prop('checked', false);
    //     $("input[name='show.conntectedSubscriptions'").prop('checked', false);
    // })

    function adjustDropdown() {
        var showActiveSubs = $("input[name='show.activeSubscriptions'").prop('checked');
        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedSubs = $("input[name='show.conntectedSubscriptions'").prop('checked');
        var url = '<g:createLink controller="ajax" action="adjustSubscriptionList"/>'+'?showActiveSubs='+showActiveSubs+'&showSubscriber='+showSubscriber+'&showConnectedSubs='+showConnectedSubs+'&format=json'

        $.ajax({
            url: url,
            success: function (data) {
                var select = '';
                for (var index = 0; index < data.length; index++) {
                    var option = data[index];
                    var optionText = option.text;
                    var optionValue = option.value;
                    console.log(optionValue +'-'+optionText)

                    select += '<div class="item"  data-value="' + optionValue + '">' + optionText + '</div>';
                }

                select = ' <div   class="ui fluid search selection dropdown la-filterProp">' +
                        '   <input type="hidden" id="targetSubscriptionId" name="targetSubscriptionId">' +
                        '   <i class="dropdown icon"></i>' +
                        '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
                        '   <div class="menu">'
                        + select +
                        '   </div>' +
                        '</div>';

                $('#element-vor-target-dropdown').next().replaceWith(select);

                $('.la-filterProp').dropdown({
                    duration: 150,
                    transition: 'fade',
                    clearable: true,
                    forceSelection: false,
                    selectOnKeydown: false,
                    onChange: function (value, text, $selectedItem) {
                        value.length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
                    }
                });
            }, async: false
        });
    }
</g:javascript>
