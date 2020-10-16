<%@ page import="de.laser.SurveyConfig; de.laser.Subscription; de.laser.helper.RDConstants; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.License" %>
<laser:serviceInjection/>
<g:if test="${!(sourceObject && targetObject)}">
    <% if (params) {
        params.remove('sourceObjectId')
        params.remove('targetObjectId')
    } %>
    <g:form action="${actionName}" controller="${controllerName}" id="${params.id}"
            params="${params << [workFlowPart: workFlowPart]}"
            method="post" class="ui form newLicence">
        <div class="fields" style="justify-content: flex-end;">
            <div class="eight wide field">
                <label>${message(code: 'copyElementsIntoObject.sourceObject.name', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}:</label>
                <g:select class="ui search selection dropdown"
                          name="sourceObjectId"
                          from="${((List<Object>) allObjects_readRights)?.sort { it.dropdownNamingConvention() }}"
                          optionValue="${{ it.dropdownNamingConvention() }}"
                          optionKey="${{ genericOIDService.getOID(it) }}"
                          value="${genericOIDService.getOID(sourceObject)}"/>
            </div>

            <div class="eight wide field">
                <label>${message(code: 'copyElementsIntoObject.targetObject.name', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}:</label>
                <g:if test="${sourceObject instanceof Subscription}">
                    <div class="field fieldcontain">
                        <label>${message(code: 'filter.status')}</label>
                        <laser:select class="ui dropdown" name="status" id="status"
                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                      optionKey="id"
                                      optionValue="value"
                                      multiple="true"
                                      value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                      noSelection="${['': message(code: 'default.select.choose.label')]}"
                                      onchange="adjustDropdown()"/>
                    </div><br/>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <div class="ui checkbox">
                            <g:checkBox name="show.subscriber" value="true" checked="false"
                                        onchange="adjustDropdown()"/>
                            <label for="show.subscriber">${message(code: 'copyElementsIntoObject.show.subscriber.sub')}</label>
                        </div><br/>
                    </g:if>
                    <div class="ui checkbox">
                        <g:checkBox name="show.connectedObjects" value="true" checked="false"
                                    onchange="adjustDropdown()"/>
                        <label for="show.connectedObjects">${message(code: 'copyElementsIntoObject.show.connectedObjects.sub')}</label>
                    </div>
                    <br>
                    <br id="element-vor-target-dropdown"/>
                    <br>
                </g:if>
                <g:if test="${sourceObject instanceof License}">
                    <div class="field fieldcontain">
                        <label>${message(code: 'filter.status')}</label>
                        <laser:select class="ui dropdown" name="status" id="status"
                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS)}"
                                      optionKey="id"
                                      optionValue="value"
                                      multiple="true"
                                      value="${RDStore.LICENSE_CURRENT.id}"
                                      noSelection="${['': message(code: 'default.select.choose.label')]}"
                                      onchange="adjustDropdown()"/>
                    </div><br/>
                    <g:if test="${accessService.checkPerm("ORG_CONSORTIUM")}">
                        <div class="ui checkbox">
                            <g:checkBox name="show.subscriber" value="true" checked="false"
                                        onchange="adjustDropdown()"/>
                            <label for="show.subscriber">${message(code: 'copyElementsIntoObject.show.subscriber.lic')}</label>
                        </div><br/>
                    </g:if>
                    <div class="ui checkbox">
                        <g:checkBox name="show.connectedObjects" value="true" checked="false"
                                    onchange="adjustDropdown()"/>
                        <label for="show.connectedObjects">${message(code: 'copyElementsIntoObject.show.connectedObjects.lic')}</label>
                    </div>
                    <br>
                    <br id="element-vor-target-dropdown"/>
                    <br>
                </g:if>
                <g:if test="${sourceObject instanceof SurveyConfig}">
                    <g:select class="ui search selection dropdown"
                              name="targetObjectId"
                              from="${((List<Object>) allObjects_writeRights)?.sort { it.dropdownNamingConvention() }}"
                              optionValue="${{ it.dropdownNamingConvention() }}"
                              optionKey="${{ genericOIDService.getOID(it) }}"
                              value="${genericOIDService.getOID(sourceObject)}"/>
                </g:if>

            </div>
        </div>

        <div class="fields" style="justify-content: flex-end;">
            <div class="six wide field" style="text-align: right;">
                <input type="submit" class="ui wide button"
                       value="${message(code: 'default.select2.label', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"/>
            </div>
        </div>

    </g:form>
</g:if>
<g:if test="${sourceObject instanceof Subscription || sourceObject instanceof License}">
    <g:javascript>
    // $(document).ready(function(){
    //     alert("Geladen")
    //     $("input[name='show.activeSubscriptions'").prop('checked', true);
    //     $("input[name='show.subscriber'").prop('checked', false);
    //     $("input[name='show.connectedObjects'").prop('checked', false);
    // })
     $(document).ready(function(){
       adjustDropdown()
    });

    function adjustDropdown() {
        var status = $("#status").val();
        var showSubscriber = $("input[name='show.subscriber'").prop('checked');
        var showConnectedObjs = $("input[name='show.connectedObjects'").prop('checked');
        var url = '<g:createLink controller="ajax"
                                 action="${sourceObject instanceof License ? 'adjustLicenseList' : 'adjustSubscriptionList'}"/>'+'?valueAsOID=true&status='+JSON.stringify(status)+'&showSubscriber='+showSubscriber+'&showConnectedObjs='+showConnectedObjs+'&format=json'

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
    '   <input type="hidden" id="targetObjectId" name="targetObjectId">' +
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
                        value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                    }
                });
            }, async: false
        });
    }
    </g:javascript>
</g:if>
