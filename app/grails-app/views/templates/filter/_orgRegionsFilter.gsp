<%@ page import="de.laser.I10nTranslation; de.laser.helper.RDConstants; com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory;de.laser.helper.RDStore; org.springframework.context.i18n.LocaleContextHolder;" %>

<g:set var="languageSuffix" value="${I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())}"/>

<div class="field">
    <label for="filterCountry">${message(code: 'org.country.label')}</label>
    <select id="filterCountry" name="country" multiple="" class="ui search selection fluid dropdown">
        <option value="">${message(code: 'default.select.choose.label')}</option>
        <g:each in="${RefdataCategory.getAllRefdataValues([RDConstants.COUNTRY])}" var="rdv">
            <option <%=(params.list('country').contains(rdv.id.toString())) ? 'selected="selected"' : ''%>
                    value="${rdv.id}">${rdv.getI10n("value")}</option>
        </g:each>
    </select>
</div>


<div class="field">
    <label for="filterRegion">${message(code: 'org.region.label')}</label>
    <select id="filterRegion" name="region" multiple="" class="ui search selection fluid dropdown">
        <option value="">${message(code: 'default.select.choose.label')}</option>
    </select>
</div>


<g:javascript>
         $(document).ready(function () {
            if($("#filterCountry").val()) updateDropdown();
        });

        $("#filterCountry").change(function() { updateDropdown(); });

        function updateDropdown() {
            var dropdownRegion = $('#filterRegion');
            var selectedCountry = $("#filterCountry").val();
            var selectedRegions = ${raw(params.list('region') as String)};

            dropdownRegion.empty();
            dropdownRegion.append('<option selected="true"disabled>${message(code: 'default.select.choose.label')}</option>');
            dropdownRegion.prop('selectedIndex', 0);

            $.ajax({
                url: '<g:createLink controller="ajax" action="getRegions"/>'
                + '?country=' + selectedCountry + '&format=json',
                success: function (data) {
                    $.each(data, function (key, entry) {
                        if(jQuery.inArray(entry.id, selectedRegions) >=0 ){
                            dropdownRegion.append($('<option></option>').attr('value', entry.id).attr('selected', 'selected').text(entry.${"value_" + languageSuffix}));
                        }else{
                            dropdownRegion.append($('<option></option>').attr('value', entry.id).text(entry.${"value_" + languageSuffix}));
                        }
                     });
                }
            });
        }
</g:javascript>
