<%@page import="com.k_int.kbplus.*;java.text.SimpleDateFormat" %>
<semui:form>
    <g:form class="ui form" id="compare" name="compare" action="compare" method="post">
        <input type="hidden" name="institution" value="${institution.id}"/>
        <div class="fields">
            <g:set var="sdf" value="${new SimpleDateFormat(message(code:'default.date.format.notime'))}"/>
            <div class="field">
                <label for="availableLicenses">${message(code:'onixplLicense.compare.add_id.label', default:'Search licenses for comparison:')}</label>
                <div class="ui multiple search selection dropdown" id="availableLicenses">
                    <input type="hidden" name="availableLicenses">
                    <i class="dropdown icon"></i>
                    <input type="text" class="search">
                    <div class="default text">${message(code:'onixplLicense.compare.search.ph')}</div>
                </div>
            </div>
        </div>
        <div class="fields">
            <div class="field">
                <g:link controller="licenseCompare" action="index" class="ui button">${message(code:'default.button.comparereset.label')}</g:link>
                &nbsp;
                <input id="submitButton" disabled='true' type="submit" value="${message(code:'default.button.compare.label', default:'Compare')}"  name="Compare" class="ui button" />
            </div>
        </div>
    </g:form>
</semui:form>
<r:script>
	$(document).ready(function(){
        $("#availableLicenses").dropdown({
            apiSettings: {
                url: "<g:createLink controller="ajax" action="lookupLicenses"/>",
                cache: false
            },
            clearable: true,
            minCharacters: 0
        });
	    $("#availableLicenses").on('change',function() {
	      	var selectedLicenses = $(this).dropdown("get values").length;
	       	if(selectedLicenses > 1){
	       		$('#submitButton').removeAttr('disabled')
	       	}
	       	else if(selectedLicenses <= 1) {
	       	    $('#submitButton').attr('disabled',true)
	       	}
	    });
	});
</r:script>