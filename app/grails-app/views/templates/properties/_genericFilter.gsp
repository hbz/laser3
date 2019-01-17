<%@ page import="com.k_int.kbplus.RefdataCategory" %>
<!-- genericFilter.gsp -->

    <div class="field">
        <label>${message(code:'subscription.property.search')}</label>

        <%-- value="${params.filterPropDef}" --%>

        <g:select id="filterPropDef" name="filterPropDef" class="ui search selection dropdown"

                  from="${propList}" optionKey="${{
                    it.refdataCategory ?
                              "com.k_int.properties.PropertyDefinition:${it.id}\" data-rdc=\"com.k_int.kbplus.RefdataCategory:${RefdataCategory.findByDesc(it.refdataCategory)?.id}"
                            : "com.k_int.properties.PropertyDefinition:${it.id}" }}" optionValue="${{ it.getI10n('name') }}"
                  noSelection="['':message(code:'default.select.choose.label', default:'Please Choose...')]"
        />

    </div>

    <div class="field">
        <label for="filterProp">${message(code:'subscription.property.value')}</label>

        <input id="filterProp" name="filterProp" type="text"
               placeholder="${message(code: 'license.search.property.ph')}" value="${params.filterProp ?: ''}"/>
    </div>


<script type="text/javascript">

    $(function() {

        var propertyFilterController = {

            updateProp: function (selOpt) {

                //If we are working with RefdataValue, grab the values and create select box
                if (selOpt.attr('data-rdc')) {
                    $.ajax({
                        url: '<g:createLink controller="ajax" action="refdataSearchByOID"/>' + '?oid=' + selOpt.attr('data-rdc') + '&format=json',
                        success: function (data) {
                            var select = '<option value></option>';
                            for (var index = 0; index < data.length; index++) {
                                var option = data[index];
                                select += '<option value="' + option.value + '">' + option.text + '</option>';
                            }
                            select = '<select id="filterProp" name="filterProp" class="ui search selection dropdown">' + select + '</select>';

                            $('label[for=filterProp]').next().replaceWith(select);

                            $('#filterProp').dropdown({
                                duration: 150,
                                transition: 'fade'
                            });

                        }, async: false
                    });
                } else {
                    $('label[for=filterProp]').next().replaceWith(
                        '<input id="filterProp" type="text" name="filterProp" placeholder="${message(code:'license.search.property.ph', default:'property value')}" />'
                    )
                }
            },

            init: function() {

                // register change event
                $('#filterPropDef').change(function (e) {
                    var selOpt = $('option:selected', this);
                    propertyFilterController.updateProp(selOpt);
                });

                // set filterPropDef by params
                var selOpt = $('#filterPropDef option').filter(function () {
                    return $(this).val() == "${params.filterPropDef}";
                }).prop('selected', true);

                propertyFilterController.updateProp(selOpt);

                // set filterProp by params
                var paramFilterProp = "${params.filterProp}";
                if ($('#filterProp').is('input')) {
                    $('#filterProp').val(paramFilterProp);
                }
                else {
                    $('#filterProp option').filter(function () {
                        return $(this).val() == paramFilterProp;
                    }).prop('selected', true);
                }
            }
        }



        propertyFilterController.init()
    });
</script>

<!-- genericFilter.gsp -->