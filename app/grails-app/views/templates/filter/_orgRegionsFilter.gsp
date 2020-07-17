<%@ page import="de.laser.base.AbstractI10nTranslatable; de.laser.helper.RDConstants; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;de.laser.helper.RDStore" %>

<g:set var="languageSuffix" value="${AbstractI10nTranslatable.getLanguageSuffix()}"/>

<div class="field">
    <label for="country">${message(code: 'org.country.label')}</label>
    <laser:select class="ui search dropdown" id="country" name="country"
                  from="${RefdataCategory.getAllRefdataValues(RDConstants.COUNTRY)}"
                  optionKey="id"
                  optionValue="value"
                  value="${params.country}"
                  noSelection="${['':message(code:'default.select.choose.label')]}"/>
</div>


<div class="field">
    <label for="region">${message(code: 'org.region.label')}</label>
    <select id="region" name="region" multiple="" class="ui selection fluid dropdown">
        <option value="">${message(code:'default.select.choose.label')}</option>
        %{--<g:each in="${RefdataCategory.getAllRefdataValues([RDConstants.REGIONS_DE,
                                                           RDConstants.REGIONS_AT,
                                                           RDConstants.REGIONS_CH])}" var="rdv">
            <option <%=(params.list('region').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
        </g:each>--}%
    </select>
</div>


<g:javascript>
         $(document).ready(function () {
            if($("#country").val()) updateDropdown();
        });

        $("#country").change(function() { updateDropdown(); });

        function updateDropdown() {
            var dropdownRegion = $('#region');
            var selectedCountry = $("#country").val();
            var selectedRegions = "${params.list('region')}";
            console.log(selectedCountry)
            if(!selectedRegions) {
                dropdownRegion.empty();
                dropdownRegion.append('<option selected="true" disabled>${message(code:'default.select.choose.label')}</option>');
                dropdownRegion.prop('selectedIndex', 0);
            }
            console.log(selectedRegions)
            console.log(typeof(selectedRegions[0]))

            $.ajax({
                url: '<g:createLink controller="ajax" action="getRegions"/>'
                + '?country=' + selectedCountry + '&format=json',
                success: function (data) {
                    $.each(data, function (key, entry) {
                        dropdownRegion.append($('<option></option>').attr('value', entry.id).text(entry.${"value_"+languageSuffix}));

                        console.log(jQuery.inArray(entry.id.toString(), selectedRegions))
                        if(jQuery.inArray(entry.id.toString(), selectedRegions)){
                            console.log(entry.id)

                        }
                     });
                    dropdownRegion.dropdown('set selected',selectedRegions[0]);
                }
            });

        }
</g:javascript>
