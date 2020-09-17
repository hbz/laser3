<%@ page import="com.k_int.kbplus.License" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.comp_sub')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb text="${message(code: 'menu.my.subscriptions')}" controller="myInstitution"
                 action="currentSubscriptions"/>
    <semui:crumb class="active" message="menu.my.comp_sub"/>
</semui:breadcrumbs>
<br>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'menu.my.comp_sub')}</h1>

<semui:form>
    <g:form class="ui form" action="${actionName}" method="post">
        <div class="ui field">
            <label for="selectedSubscriptions">${message(code: 'default.compare.subscriptions')}</label>

            <div class="ui checkbox">
                <g:checkBox name="show.activeSubscriptions" value="nur aktive" checked="true"
                            onchange="adjustDropdown()"/>
                <label for="show.activeSubscriptions">${message(code: 'default.compare.show.activeSubscriptions.name')}</label>
            </div><br/>

            <div class="ui checkbox">
                <g:checkBox name="show.intendedSubscriptions" value="intended" checked="false"
                            onchange="adjustDropdown()"/>
                <label for="show.intendedSubscriptions">${message(code: 'default.compare.show.intendedSubscriptions.name')}</label>
            </div><br/>
            <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="auch Teilnehmerlizenzen" checked="false"
                                onchange="adjustDropdown()"/>
                    <label for="show.subscriber">${message(code: 'default.compare.show.subscriber.name')}</label>
                </div><br/>
            </g:if>
            <div class="ui checkbox">
                <g:checkBox name="show.conntectedSubscriptions" value="auch verknüpfte Lizenzen" checked="false"
                            onchange="adjustDropdown()"/>
                <label for="show.conntectedSubscriptions">${message(code: 'default.compare.show.conntectedSubscriptions.name')}</label>
            </div>
            <br id="element-vor-target-dropdown"/>

            <select id="selectedSubscriptions" name="selectedObjects" multiple=""
                    class="ui search selection multiple dropdown">
                <option value="">${message(code: 'default.select.choose.label')}</option>

                <g:each in="${availableSubscriptions.sort { it.dropdownNamingConvention() }}" var="sub">
                    <option <%=(sub in objects) ? 'selected="selected"' : ''%>
                    value="${sub.id}" ">
                    ${sub.dropdownNamingConvention()}
                    </option>
                </g:each>
            </select>
        </div>

        <div class="field">
            <g:link controller="compare" action="${actionName}"
                    class="ui button">${message(code: 'default.button.comparereset.label')}</g:link>
            &nbsp;
            <input ${params.selectedObjects ? 'disabled' : ''} type="submit"
                                                               value="${message(code: 'default.button.compare.label')}"
                                                               name="Compare" class="ui button"/>
        </div>

    </g:form>
</semui:form>

<g:if test="${objects}">
    <g:render template="nav"/>
    <br>
    <br>

    <g:if test="${params.tab == 'compareProperties'}">
        <g:render template="compareProperties"/>
    </g:if>

    <g:if test="${params.tab == 'compareElements'}">
        <g:render template="compareElements"/>
    </g:if>
</g:if>

<g:javascript>
    function adjustDropdown() {
        var showActiveSubs = $("input[name='show.activeSubscriptions'").prop('checked');
        var showIntendedSubs = $("input[name='show.intendedSubscriptions'").prop('checked');
        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedSubs = $("input[name='show.conntectedSubscriptions'").prop('checked');
        var url = '<g:createLink controller="ajax" action="adjustCompareSubscriptionList"/>'+'?showActiveSubs='+showActiveSubs+'&showIntendedSubs='+showIntendedSubs+'&showSubscriber='+showSubscriber+'&showConnectedSubs='+showConnectedSubs+'&format=json'

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

                    select += '<div class="item" data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
                }

                select = ' <div class="ui fluid search selection dropdown la-filterProp">' +
'   <i class="dropdown icon"></i>' +
'   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
'   <div class="menu">'
+ select +
'</div>' +
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

</body>
</html>
