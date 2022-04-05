<%@ page import="de.laser.License; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.RefdataCategory" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.comp_sub')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb text="${message(code: 'menu.my.subscriptions')}" controller="myInstitution"
                 action="currentSubscriptions"/>
    <semui:crumb class="active" message="menu.my.comp_sub"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'menu.my.comp_sub')}</h1>

<semui:form>
    <g:form class="ui form" action="${actionName}" method="post">
        <div class="ui field">
            <label for="selectedSubscriptions">${message(code: 'default.compare.subscriptions')}</label>

            <div class="field">
                <label>${message(code: 'filter.status')}</label>
                <select id="status" name="status" multiple="" class="ui search selection fluid multiple dropdown" onchange="JSPC.app.adjustDropdown()">
                    <option value=""><g:message code="default.select.choose.label"/></option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }" var="status">
                        <option <%=(status.id.toString() in params.list('status')) ? 'selected="selected"' : ''%> value="${status.id}">${status.getI10n('value')}</option>
                    </g:each>
                </select>

            </div>
            <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="true" checked="false"
                                onchange="JSPC.app.adjustDropdown()"/>
                    <label for="show.subscriber">${message(code: 'default.compare.show.subscriber.name')}</label>
                </div><br />
            </g:if>
            <div class="ui checkbox">
                <g:checkBox name="show.connectedObjects" value="true" checked="false"
                            onchange="JSPC.app.adjustDropdown()"/>
                <label for="show.connectedObjects">${message(code: 'default.compare.show.connectedObjects.name')}</label>
            </div>
            <br />
            <select id="selectedObjects" name="selectedObjects" multiple="" class="ui search selection fluid dropdown">
                <option value="">${message(code: 'default.select.choose.label')}</option>
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
    <br />
    <br />


    <div style="overflow-x: scroll;">

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

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.adjustDropdown = function () {

        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedObjs = $("input[name='show.connectedObjects'").prop('checked');
        var url = '<g:createLink controller="ajaxJson" action="adjustCompareSubscriptionList"/>?showSubscriber=' + showSubscriber + '&showConnectedObjs=' + showConnectedObjs

        var status = $("select#status").serialize()
        if (status) {
            url = url + '&' + status
        }

        var dropdownSelectedObjects = $('#selectedObjects');
        var selectedObjects = ${raw(objects?.id as String)};

        dropdownSelectedObjects.empty();
        dropdownSelectedObjects.append('<option selected="true" disabled>${message(code: 'default.select.choose.label')}</option>');
        dropdownSelectedObjects.prop('selectedIndex', 0);

        $.ajax({
                url: url,
                success: function (data) {
                    $.each(data, function (key, entry) {
                        if(jQuery.inArray(entry.value, selectedObjects) >=0 ){
                            dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).attr('selected', 'selected').text(entry.text));
                        }else{
                            dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).text(entry.text));
                        }
                     });
                }
        });
    }

    JSPC.app.adjustDropdown();
</laser:script>

</body>
</html>
