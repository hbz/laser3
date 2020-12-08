<!-- A: templates/properties/_genericFilter -->
<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<%--params.filterProp: ${params.filterProp}--%>
<div class="field">
    <label for="filterPropDef">${message(code: 'subscription.property.search')}
        <i class="question circle icon la-popup"></i>
        <span class="ui popup">
            <i class="shield alternate icon"></i> = ${message(code: 'subscription.properties.my')}
        </span>
    </label>
    <%-- value="${params.filterPropDef}" --%>
    <semui:dropdown id="filterPropDef" name="filterPropDef"
                    class="la-filterPropDef"
                    from="${propList}"
                    iconWhich = "shield alternate"
                    optionKey="${{
                        it.refdataCategory ?
                                "${it}\" data-rdc=\"${it.refdataCategory}":
                                 "${it}"
                    }}"
                    optionValue="${{ it.name_de }}"
                    noSelection="${message(code: 'default.select.choose.label')}" />
</div>

<g:if test="${!hideFilterProp}">
<div class="field">
    <label for="filterProp">${message(code: 'subscription.property.value')}</label>

    <input  id="filterProp" name="filterProp" type="text"
           placeholder="${message(code: 'license.search.property.ph')}" value="${params.filterProp ?: ''}"/>
</div>
</g:if>



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

                //If we are working with RefdataValue, grab the values and create select box
                if (selOpt.attr('data-rdc')) {
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
                                select += '<div class="item"  data-value="' + option.value + '">' + optionText + '</div>';
                            }

                            select = ' <div class="ui fluid multiple search selection dropdown la-filterProp">' +
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

                            select = ' <div   class="ui fluid search selection dropdown la-filterProp">' +
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
                    JSPC.app.propertyFilterController.updateProp(selOpt);
                });
             */
                $(document).ready(function() {
                    $(".la-filterPropDef").dropdown({
                        clearable: true,
                        forceSelection: false,
                        selectOnKeydown: false,
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
                    });
                })
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