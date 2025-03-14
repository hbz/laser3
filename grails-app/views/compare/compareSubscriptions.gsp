<%@ page import="de.laser.ui.Btn; de.laser.helper.Params; de.laser.CustomerTypeService; de.laser.License; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.RefdataCategory" %>
<laser:htmlStart message="menu.my.comp_sub" />

<ui:breadcrumbs>
    <ui:crumb text="${message(code: 'menu.my.subscriptions')}" controller="myInstitution"
                 action="currentSubscriptions"/>
    <ui:crumb class="active" message="menu.my.comp_sub"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.comp_sub" />

    <ui:form controller="compare" action="${actionName}">
            <div class="field">
                <label for="status">${message(code: 'filter.status')}</label>
                <select id="status" name="status" multiple="" class="ui search selection fluid multiple dropdown" onchange="JSPC.app.adjustDropdown()">
                    <option value=""><g:message code="default.select.choose.label"/></option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }" var="status">
                        <option <%=Params.getLongList(params, 'status').contains(status.id) ? 'selected="selected"' : ''%> value="${status.id}">${status.getI10n('value')}</option>
                    </g:each>
                </select>
            </div>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                <div class="field">
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="true" checked="false" onchange="JSPC.app.adjustDropdown()"/>
                    <label for="show.subscriber">${message(code: 'default.compare.show.subscriber.name')}</label>
                </div>
                </div>
            </g:if>
            <div class="field">
            <div class="ui checkbox">
                <g:checkBox name="show.connectedObjects" value="true" checked="false" onchange="JSPC.app.adjustDropdown()"/>
                <label for="show.connectedObjects">${message(code: 'default.compare.show.connectedObjects.name')}</label>
            </div>
            </div>
        <div class="field">
            <label for="selectedObjects">${message(code: 'default.compare.subscriptions')}</label>
            <select id="selectedObjects" name="selectedObjects" multiple="" class="ui search selection fluid dropdown">
                <option value="">${message(code: 'default.select.choose.label')}</option>
            </select>
        </div>

        <div class="field">
            <g:link controller="compare" action="${actionName}" class="${Btn.SECONDARY}">${message(code: 'default.button.comparereset.label')}</g:link>
            &nbsp;
            <input ${params.selectedObjects ? 'disabled' : ''} type="submit"
                                                               value="${message(code: 'default.button.compare.label')}"
                                                               name="Compare" class="${Btn.SIMPLE}"/>
        </div>

    </ui:form>

<g:if test="${objects}">
    <laser:render template="nav"/>
    <br />
    <br />


    <div style="overflow-x: scroll;">

            <g:if test="${params.tab == 'compareProperties'}">
                <div class="ui padded grid">
                <laser:render template="compareProperties"/>
                </div>
            </g:if>

            <g:if test="${params.tab == 'compareElements'}">
                <laser:render template="compareElements"/>
            </g:if>

            <g:if test="${params.tab == 'compareEntitlements'}">
                <laser:render template="compareEntitlements" model="[showPackage: true, showPlattform: true]"/>
            </g:if>

    </div>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.adjustDropdown = function () {

        var showSubscriber = $("input[name='show.subscriber']").prop('checked');
        var showConnectedObjs = $("input[name='show.connectedObjects']").prop('checked');
        var url = '<g:createLink controller="ajaxJson" action="adjustCompareSubscriptionList"/>?showSubscriber=' + showSubscriber + '&showConnectedObjs=' + showConnectedObjs

        var status = $("select#status").serialize()
        if (status) {
            url = url + '&' + status
        }

        var dropdownSelectedObjects = $('#selectedObjects');
        var selectedObjects = [];
        <g:each in="${objects.id}" var="objId">
            selectedObjects.push(${objId})
        </g:each>

        dropdownSelectedObjects.empty();

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

<laser:htmlEnd />
