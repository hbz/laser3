<%@ page import="de.laser.helper.Params; de.laser.utils.LocaleUtils; de.laser.RefdataCategory; de.laser.I10nTranslation; de.laser.storage.RDConstants; de.laser.RefdataValue;de.laser.storage.RDStore;" %>

<g:set var="languageSuffix" value="${LocaleUtils.getCurrentLang()}"/>

<div class="field">
    <label for="filterCountry">${message(code: 'org.country.label')}</label>
    <select id="filterCountry" name="country" multiple="" class="ui search selection fluid dropdown">
        <option value="">${message(code: 'default.select.choose.label')}</option>
        <g:each in="${RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.COUNTRY)}" var="rdv">
            <option <%=Params.getLongList(params, 'country').contains(rdv.id) ? 'selected="selected"' : ''%>
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


<laser:script file="${this.getGroovyPageFileName()}">

        JSPC.app.updateDropdown = function () {
            var dropdownRegion = $('#filterRegion');
            var selectedCountry = $("#filterCountry").val();
            var selectedRegions = [];
            <g:each in="${params.list('region')}" var="region">
                selectedRegions.push(${region});
            </g:each>

            dropdownRegion.empty();
            //dropdownRegion.append('<option selected="true" disabled>${message(code: 'default.select.choose.label')}</option>');
            //dropdownRegion.prop('selectedIndex', 0);

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="getRegions"/>' + '?country=' + selectedCountry + '&format=json',
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

        if ($("#filterCountry").val()) { JSPC.app.updateDropdown(); }

        $("#filterCountry").change(function() { JSPC.app.updateDropdown(); });
</laser:script>
