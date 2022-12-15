<!-- A: templates/properties/_genericFilter -->
<%@ page import="de.laser.properties.PropertyDefinition; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<%--params.filterProp: ${params.filterProp}--%>
<div class="field">
    <label for="filterPropDef">${label}
        <i class="question circle icon la-popup"></i>
        <span class="ui popup">
            <i class="shield alternate icon"></i> = ${message(code: 'subscription.properties.my')}
        </span>
    </label>
    <%-- value="${params.filterPropDef}" --%>
    <ui:dropdown id="filterPropDef" name="${newfilterPropDefName ?: 'filterPropDef'}"
                 class="la-filterPropDef"
                 from="${propList}"
                 iconWhich="shield alternate"
                 optionKey="${{
                     it.refdataCategory ?
                             "${it}\" data-rdc=\"${it.refdataCategory}" :
                             "${it}"
                 }}"
                 optionValue="${{ it.getI10n('name') }}"
                 noSelection="${message(code: 'default.select.choose.label')}"/>
</div>

<g:if test="${!hideFilterProp}">
    <div class="field">
        <label for="filterProp">${message(code: 'subscription.property.value')}</label>

        <input id="filterProp" name="filterProp" type="text"
               placeholder="${message(code: 'license.search.property.ph')}" value="${params.filterProp ?: ''}"/>
    </div>
</g:if>
<g:elseif test="${actionName == 'manageProperties'}">

    <g:if test="${params.descr in [PropertyDefinition.SUB_PROP, PropertyDefinition.LIC_PROP]}">
        <div class="field">
            <label for="objStatus"><g:message code="default.status.label"/></label>

            <input type="hidden" id="objStatus" name="objStatus">
        </div>
    </g:if>

    <g:if test="${params.descr in [PropertyDefinition.ORG_PROP]}">
        <div class="two fields">
            <g:if test="${accessService.checkPerm('ORG_CONSORTIUM')}">

                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkMyInsts">${message(code: 'menu.my.insts')}</label>
                                <input id="checkMyInsts" name="myInsts" type="checkbox"
                                       <g:if test="${params.myInsts == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>

            <div class="field">
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkMyProviderAgency">${message(code: 'default.myProviderAgency.label')}</label>
                            <input id="checkMyProviderAgency" name="myProviderAgency" type="checkbox"
                                   <g:if test="${params.myProviderAgency == "on"}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
</g:elseif>



<laser:script file="${this.getGroovyPageFileName()}">
    $(".la-popup").popup({});

        $.each($(".la-filterPropDef"), function(i, dropdown) {
            var val = $(dropdown).find(".item.active").data("value");
            var text = $(dropdown).find(".item.active").html();
            if (val != undefined) {
                $(dropdown)
                        .dropdown("set value", val)
                        .dropdown("set text", text);
            }
        });

        JSPC.app.propertyFilterController = {

            updateProp: function (selOpt) {

                if(selOpt === null) {
                    let select = '<input id="filterProp" name="filterProp" type="text" placeholder="${message(code: 'license.search.property.ph')}" value=""/>';
                    $('label[for=filterProp]').next().replaceWith(select);
                }
                //If we are working with RefdataValue, grab the values and create select box
                else if (selOpt.attr('data-rdc')) {
                    $.ajax({
                        url: '<g:createLink controller="ajaxJson" action="refdataSearchByCategory"/>' + '?cat=' + selOpt.attr('data-rdc'),
                        success: function (data) {
                            var genericNullValue = "${genericOIDService.getOID(RDStore.GENERIC_NULL_VALUE.id)}";
                            var select = '';
                            for (var index = 0; index < data.length; index++) {
                                var option = data[index];
                                var optionText = option.text;

                                if(option.value === genericNullValue) {
                                    optionText = "<em>${RDStore.GENERIC_NULL_VALUE.getI10n('value')}</em>";
                                }
                                select += '<div class="item" data-value="' + option.value + '">' + optionText + '</div>';
                            }

                            select = ' <div class="ui fluid multiple search selection dropdown la-filterProp">' +
'   <input type="hidden" id="filterProp" name="filterProp">' +
'   <i class="dropdown icon"></i>' +
'   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
'   <div class="menu">'
+ select +
'</div>' +
'</div>';


                            $('label[for=filterProp]').next().replaceWith(select);


                            $('.la-filterProp').dropdown({
                                duration: 150,
                                transition: 'fade',
                                clearable: true,
                                forceSelection: false,
                                selectOnKeydown: false,
                                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)},
                                onChange: function (value, text, $selectedItem) {
                                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                                }
                            });
    <g:if test="${params.filterPropDef && params.filterProp && genericOIDService.resolveOID(params.filterPropDef).refdataCategory}">
        let propList = []
        <% List<String> propList
        if (params.filterProp.contains(','))
            propList = params.filterProp.split(',')
        else propList = [params.filterProp]
        propList.each { filterProp -> %>
        propList.push("${filterProp}");
        <% } %>
        console.log(propList);
        $('.la-filterProp').dropdown("set selected",propList);
    </g:if>
    }, async: false

});
} else {
$.ajax({
    url: '<g:createLink controller="ajaxJson" action="getPropValues"/>' + '?oid=' + selOpt.attr('data-value'),
                        success: function (data) {
                            var select = '';
                            for (var index = 0; index < data.length; index++) {
                                var option = data[index];
                                var optionText = option.text;

                                select += '<div class="item" data-value="' + option.value + '">' + optionText + '</div>';
                            }

                            select = ' <div class="ui fluid search selection dropdown la-filterProp">' +
'   <input type="hidden" id="filterProp" name="filterProp">' +
'   <i class="dropdown icon"></i>' +
'   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
'   <div class="menu">'
+ select +
'</div>' +
'</div>';


                            $('label[for=filterProp]').next().replaceWith(select);


                            $('.la-filterProp').dropdown({
                                duration: 150,
                                transition: 'fade',
                                clearable: true,
                                forceSelection: false,
                                selectOnKeydown: false,
                                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)},
                                onChange: function (value, text, $selectedItem) {
                                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                                }
                            })
    <g:if test="${params.filterProp}">.dropdown("set selected","${params.filterProp}")</g:if>;
                        }, async: false

                    });
                    /*$('label[for=filterProp]').next().replaceWith(
                        '<input id="filterProp" type="text" name="filterProp"
                                placeholder="${message(code: 'license.search.property.ph')}"/>'
                    )*/
                }
    <g:if test="${actionName == 'manageProperties'}">
        $.ajax({
            url: '<g:createLink controller="ajaxJson" action="getOwnerStatus"/>' + '?oid=' + selOpt.attr('data-value'),
                            success: function (data) {
                                var select = '';
                                for (var index = 0; index < data.length; index++) {
                                    var option = data[index];
                                    var optionText = option.text;

                                    select += '<div class="item"
                                                    data-value="' + option.value + '">' + optionText + '</div>';
                                }

                                select = ' <div id="objStatusWrapper" class="ui fluid search selection dropdown">' +
    '   <input type="hidden" id="objStatus" name="objStatus">' +
    '   <i class="dropdown icon"></i>' +
    '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
    '   <div class="menu">'
    + select +
    '</div>' +
    '</div>';
                            $('label[for=objStatus]').next().replaceWith(select);
                            $('#objStatusWrapper').dropdown({
                                duration: 150,
                                transition: 'fade',
                                clearable: true,
                                forceSelection: false,
                                selectOnKeydown: false,
                                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)},
                                onChange: function (value, text, $selectedItem) {
                                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                                }
                            })<g:if test="${params.objStatus}">.dropdown("set selected","${params.objStatus}")</g:if>;
                        }
                    });
    </g:if>
    },

    init: function () {

        /*
        // register change event
        $('#filterPropDef').change(function (e) {
            var selOpt = $('option:selected', this);
            JSPC.app.propertyFilterController.updateProp(selOpt);
        });
     */
        $(document).ready(function() {
            $(".la-filterPropDef").dropdown({
                clearable: true,
                forceSelection: false,
                selectOnKeydown: false,
                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)},
                onChange: function (value, text, $selectedItem) {
                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                    if ((typeof $selectedItem != 'undefined')){
                        var selOpt = $selectedItem;
                        JSPC.app.propertyFilterController.updateProp(selOpt);
                    }
                    else {
                        $('#filterProp').dropdown ('clear', true)
                    }

                }
            })<g:if test="${params.filterPropDef}">.dropdown("set selected","${params.filterPropDef}")</g:if>;
                });
                    // set filterPropDef by params
                    // iterates through all the items and set the item class on 'active selected' when value and URL Parameter for filterPropDef match
                    var item = $( ".la-filterPropDef .item" );

                    var selOpt = $('.la-filterPropDef').find(item).filter(function ()
                        {
                        return  $(this).attr('data-value') == "${params.filterPropDef}";
                        }
                    ).addClass('active').addClass('selected');
                    // sets the URL Parameter on the hidden input field
                    var hiddenInput = $('#filterPropDef').val("${params.filterPropDef}");

                    JSPC.app.propertyFilterController.updateProp(selOpt);

                    // set filterProp by params
                    var paramFilterProp = "${params.filterProp}";

                    $('#filterProp').val(paramFilterProp);
            }
        }
        JSPC.app.propertyFilterController.init()

</laser:script>
<!-- O: templates/properties/_genericFilter -->