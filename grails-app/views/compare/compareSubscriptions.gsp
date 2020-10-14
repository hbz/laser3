<%@ page import="de.laser.License; de.laser.helper.RDConstants; de.laser.helper.RDStore; de.laser.RefdataCategory" %>
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

            <div class="field fieldcontain">
                <label>${message(code: 'filter.status')}</label>
                <laser:select class="ui dropdown" name="status" id="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              multiple="true"
                              value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"
                              onchange="adjustDropdown()"/>
            </div>
            <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="true" checked="false"
                                onchange="adjustDropdown()"/>
                    <label for="show.subscriber">${message(code: 'default.compare.show.subscriber.name')}</label>
                </div><br/>
            </g:if>
            <div class="ui checkbox">
                <g:checkBox name="show.connectedObjects" value="true" checked="false"
                            onchange="adjustDropdown()"/>
                <label for="show.connectedObjects">${message(code: 'default.compare.show.connectedObjects.name')}</label>
            </div>
            <br>
            <br id="element-vor-target-dropdown" />
            <br>
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


    <div style="overflow-y: scroll;">

            <g:if test="${params.tab == 'compareProperties'}">
                <div class="ui padded grid">
                <g:render template="compareProperties"/>
                </div>
            </g:if>

            <g:if test="${params.tab == 'compareElements'}">
                <g:render template="compareElements"/>
            </g:if>

            <g:if test="${params.tab == 'compareEntitlements'}">
                <g:render template="compareEntitlements" model="[showPackage: true, showPlattform: true]"/>
            </g:if>

    </div>
</g:if>

<asset:script type="text/javascript">
    $(document).ready(function(){
       adjustDropdown()
    });
    function adjustDropdown() {
        var status = $( "select#status").val();
        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedObjs = $("input[name='show.connectedObjects'").prop('checked');
        var url = '<g:createLink controller="ajax" action="adjustCompareSubscriptionList"/>'+'?status='+JSON.stringify(status)+'&showSubscriber='+showSubscriber+'&showConnectedObjs='+showConnectedObjs+'&format=json'

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
</asset:script>

</body>
</html>
