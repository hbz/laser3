<%@page import="de.laser.ReportingService;de.laser.RefdataValue;de.laser.helper.RDStore" %>
<laser:serviceInjection/>

    <g:if test="${secondLevel == 'orgProperty'}">
        <div id="orgPropertySelection">
            <label for="filterPropDef">${message(code: 'subscription.property.search')}
                <%--<i class="question circle icon la-popup"></i>
                <span class="ui  popup ">
                    <i class="shield alternate icon"></i> = ${message(code: 'subscription.properties.my')}
                </span>--%>
            </label>
            <semui:dropdown id="filterPropDef" name="filterPropDef"
                            class="la-filterPropDef"
                            requestParam="${queried}"
                            display="${ReportingService.CONFIG_ORG_PROPERTY}"
                            from="${propList}"
                            iconWhich = "shield alternate"
                            optionKey="${{
                                it.refdataCategory ?
                                        "${genericOIDService.getOID(it)}\" data-rdc=\"${it.refdataCategory}"
                                        : "${genericOIDService.getOID(it)}"
                            }}"
                            optionValue="${{ it.getI10n('name') }}"
                            noSelection="${message(code: 'default.select.choose.label')}"/>
            <%--<label for="filterProp">${message(code: 'subscription.property.value')}</label>

            <input  id="filterProp" name="filterProp" type="text" class="generalLoadingParam" data-requestParam="${queried}"
                    placeholder="${message(code: 'license.search.property.ph')}" value="${params.filterProp ?: ''}"/>--%>
        </div>
        <laser:script>
                JSPC.propertyFilterController = {

                    updateProp: function (selOpt) {

                        //If we are working with RefdataValue, grab the values and create select box
                        if (selOpt.attr('data-rdc')) {
                            $.ajax({
                                url: '<g:createLink controller="ajaxJson" action="refdataSearchByCategory"/>' + '?cat=' + selOpt.attr('data-rdc'),
                                success: function (data) {
                                    var genericNullValue = "${RefdataValue.class.name}:${RDStore.GENERIC_NULL_VALUE.id}";
                                    var select = '';
                                    for (var index = 0; index < data.length; index++) {
                                        var option = data[index];
                                        var optionText = option.text;

                                        if(option.value === genericNullValue) {
                                            optionText = "<em>${RDStore.GENERIC_NULL_VALUE.getI10n('value')}</em>";
                                        }
                                        select += '<div class="item"  data-value="' + option.value + '">' + optionText + '</div>';
                                    }

                                    select = ' <div class="ui fluid multiple search selection dropdown la-filterProp" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_ORG_PROPERTY}">' +
                                        '   <input type="hidden" id="filterProp" name="filterProp">' +
                                        '   <i class="dropdown icon"></i>' +
                                        '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
                                        '   <div class="menu">'
                                        + select +
                                        '   </div>' +
                                        '</div>';

                                    $('label[for=filterProp]').next().replaceWith(select);

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
                        } else {
                            $.ajax({
                                url: '<g:createLink controller="ajaxJson" action="getPropValues"/>' + '?oid=' + selOpt.attr('data-value'),
                                success: function (data) {
                                    var select = '';
                                    for (var index = 0; index < data.length; index++) {
                                        var option = data[index];
                                        var optionText = option.text;

                                        select += '<div class="item"  data-value="' + option.value + '">' + optionText + '</div>';
                                    }

                                    select = ' <div   class="ui fluid search selection dropdown la-filterProp" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_ORG_PROPERTY}">' +
                                        '   <input type="hidden" id="filterProp" name="filterProp">' +
                                        '   <i class="dropdown icon"></i>' +
                                        '   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
                                        '   <div class="menu">'
                                        + select +
                                        '   </div>' +
                                        '</div>';

                                    $('label[for=filterProp]').next().replaceWith(select);

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
                            /*$('label[for=filterProp]').next().replaceWith(
                                '<input id="filterProp" type="text" name="filterProp" placeholder="${message(code:'license.search.property.ph')}" />'
                    )*/
                        }
                    },

                    init: function () {

                        /*
                        // register change event
                        $('#filterPropDef').change(function (e) {
                            var selOpt = $('option:selected', this);
                            JSPC.propertyFilterController.updateProp(selOpt);
                        });
                     */

                        $(".la-filterPropDef").dropdown({
                            clearable: true,
                            forceSelection: false,
                            selectOnKeydown: false,
                            onChange: function (value, text, $selectedItem) {
                                value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                                if ((typeof $selectedItem != 'undefined')){
                                    var selOpt = $selectedItem;
                                    JSPC.propertyFilterController.updateProp(selOpt);
                                }
                                else {
                                    $('#filterProp').dropdown ('clear', true)
                                }
                            }
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

                        JSPC.propertyFilterController.updateProp(selOpt);

                        // set filterProp by params
                        var paramFilterProp = "${params.filterProp}";

                        $('#filterProp').val(paramFilterProp);
                    }
                }

                JSPC.propertyFilterController.init()
        </laser:script>
    </g:if>
    <g:elseif test="${secondLevel == 'subscription'}">
        <div id="subscriptionSelection">
            <%-- move from updateSubscriptions --%>
        </div>
    </g:elseif>