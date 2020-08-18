<%@ page import="com.k_int.kbplus.Subscription; com.k_int.kbplus.GenericOIDService;" %>
<g:if test="${! (sourceObject && targetObject)}">
    <% if (params){
        params.remove('sourceObjectId')
        params.remove('targetObjectId')
    } %>
    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}"
            params="${params << [workFlowPart: workFlowPart]}"
            method="post" class="ui form newLicence">
        <div class="fields" style="justify-content: flex-end;">
            <div class="eight wide field">
                <label>${message(code: 'copyElementsIntoObject.sourceObject.name')}: </label>
                <g:select class="ui search selection dropdown"
                      name="sourceObjectId"
                      from="${((List<Object>)allObjects_readRights)?.sort {it.dropdownNamingConvention()}}"
                      optionValue="${{it.dropdownNamingConvention()}}"
                      optionKey="${{GenericOIDService.getOID(it)}}"
                      value="${sourceObject?.id}"
                      />
            </div>
            <div class="eight wide field">
                <g:if test="${sourceObject instanceof com.k_int.kbplus.Subscription}">
                    <label>${message(code: 'copyElementsIntoObject.targetObject.name')}: </label>
                    <div class="ui checkbox">
                        <g:checkBox name="show.activeSubscriptions" value="nur aktive" checked="true" onchange="adjustDropdown()"/>
                        <label for="show.activeSubscriptions">${message(code:'copyElementsIntoObject.show.activeSubscriptions.name')}</label>
                    </div><br />
                    <div class="ui checkbox">
                        <g:checkBox name="show.intendedSubscriptions" value="intended" checked="false" onchange="adjustDropdown()"/>
                        <label for="show.intendedSubscriptions">${message(code:'copyElementsIntoObject.show.intendedSubscriptions.name')}</label>
                    </div><br />
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                    <div class="ui checkbox">
                        <g:checkBox name="show.subscriber" value="auch Teilnehmerlizenzen" checked="false" onchange="adjustDropdown()" />
                        <label for="show.subscriber">${message(code:'copyElementsIntoObject.show.subscriber.name')}</label>
                    </div><br />
                    </g:if>
                    <div class="ui checkbox">
                        <g:checkBox name="show.conntectedSubscriptions" value="auch verknüpfte Lizenzen" checked="false" onchange="adjustDropdown()"/>
                        <label for="show.conntectedSubscriptions">${message(code:'copyElementsIntoObject.show.conntectedSubscriptions.name')}</label>
                    </div><br id="element-vor-target-dropdown" />
                </g:if>
                <g:select class="ui search selection dropdown"
                      id="targetObjectId"
                      name="targetObjectId"
                      from="${allObjects_writeRights}"
                      optionValue="${{it.dropdownNamingConvention()}}"
                      optionKey="${{GenericOIDService.getOID(it)}}"
                      value="${targetObject?.id}"
                      noSelection="${['': message(code: 'default.select.choose.label')]}"/>
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
        var showIntendedSubs = $("input[name='show.intendedSubscriptions'").prop('checked');
        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedSubs = $("input[name='show.conntectedSubscriptions'").prop('checked');
        var url = '<g:createLink controller="ajax" action="adjustSubscriptionList"/>'+'?showActiveSubs='+showActiveSubs+'&showIntendedSubs='+showIntendedSubs+'&showSubscriber='+showSubscriber+'&showConnectedSubs='+showConnectedSubs+'&format=json'

        $.ajax({
            url: url,
            success: function (data) {
                var select = '';
                for (var index = 0; index < data.length; index++) {
                    var option = data[index];
                    var optionText = option.text;
                    var optionValue = option.value;
                    var count = index + 1
                    // console.log(optionValue +'-'+optionText)

                    select += '<div class="item"  data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
                }

                select = ' <div   class="ui fluid search selection dropdown la-filterProp">' +
                        '   <input type="hidden" id="targetObjectId" name="targetObjectId">' +
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
