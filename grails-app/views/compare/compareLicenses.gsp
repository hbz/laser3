<%@ page import="de.laser.ui.Btn; de.laser.helper.Params; de.laser.CustomerTypeService; de.laser.License; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.RefdataCategory" %>
<laser:htmlStart message="menu.my.comp_lic" />

<ui:breadcrumbs>
    <ui:crumb text="${message(code: 'menu.my.licenses')}" controller="myInstitution" action="currentLicenses"/>
    <ui:crumb class="active" message="menu.my.comp_lic"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.my.comp_lic" />

<ui:form controller="compare" action="${actionName}">
        <div class="ui field">
        <label for="status">${message(code: 'filter.status')}</label>
        <select id="status" name="status" multiple="" class="ui search selection fluid dropdown" onchange="JSPC.app.adjustDropdown()">
            <option value=""><g:message code="default.select.choose.label"/></option>
            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS) }" var="status">
                <option <%=Params.getLongList(params, 'status').contains(status.id) ? 'selected="selected"' : ''%> value="${status.id}">${status.getI10n('value')}</option>
            </g:each>
        </select>
        </div>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                <div class="ui field">
                <div class="ui checkbox">
                    <g:checkBox name="show.subscriber" value="true" checked="false" onchange="JSPC.app.adjustDropdown()"/>
                    <label for="show.subscriber">${message(code: 'default.compare.show.licensee.name')}</label>
                </div>
                </div>
            </g:if>
        %{--<div class="ui checkbox">
            <g:checkBox name="show.connectedLicenses" value="true" checked="false" onchange="JSPC.app.adjustDropdown()"/>
            <label for="show.connectedLicenses">${message(code:'default.compare.show.connectedLicenses.name')}</label>
        </div>--}%
        <div class="ui field">
            <label for="selectedObjects">${message(code: 'default.compare.licenses')}</label>
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

    <g:if test="${params.tab == 'compareProperties'}">
        <laser:render template="compareProperties"/>
    </g:if>

    <g:if test="${params.tab == 'compareElements'}">
        <laser:render template="compareElements"/>
    </g:if>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.adjustDropdown = function () {

        var showSubscriber = $("input[name='show.subscriber']").prop('checked');
        var showConnectedLics = $("input[name='show.connectedLicenses']").prop('checked');
        var url = '<g:createLink controller="ajaxJson" action="adjustCompareLicenseList"/>?showSubscriber=' + showSubscriber + '&showConnectedLics=' + showConnectedLics

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

    JSPC.app.adjustDropdown()
</laser:script>


<laser:htmlEnd />
