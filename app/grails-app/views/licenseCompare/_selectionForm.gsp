<%@page import="com.k_int.kbplus.*;java.text.SimpleDateFormat" %>
<semui:form>
    <g:form class="ui form" id="compare" name="compare" action="compare" method="post">
        <input type="hidden" name="institution" value="${institution.id}"/>
        <div class="fields">
            <g:set var="sdf" value="${new SimpleDateFormat(message(code:'default.date.format.notime'))}"/>
            <div class="field">
                <label for="availableLicenses">${message(code:'onixplLicense.compare.add_id.label', default:'Search licenses for comparison:')}</label>
                <input name="availableLicenses" id="availableLicenses"/>
            </div>
        </div>
        <div class="fields">
            <div class="field">
                <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.comparereset.label')}</a>
                &nbsp;
                <input id="submitButton" disabled='true' type="submit" value="${message(code:'default.button.compare.label', default:'Compare')}"  name="Compare" class="ui button" />
            </div>
        </div>
    </g:form>
</semui:form>
<r:script>
	$(document).ready(function(){
	    $("#availableLicenses").select2({
	      	width: "100%" ,
	        placeholder: "${message(code:'onixplLicense.compare.search.ph', default:'Search for a license...')}",
	        minimumInputLength: 0,
	        multiple: true,
	        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: "<g:createLink controller='ajax' action='lookupLicenses'/>",
                dataType: 'json',
                data: function (term, page) {
                    return {
                        q: term,
                        page_limit: 30
                    }
                },
                results: function (data, page) {
                    return {results: data.values};
                }
	        }
	    });
	    <g:if test="${selectedLicenses}">
            var data = [
                <g:each in="${selectedLicenses}" var="${license}">
                    {id: "${license.class.name}:${license.id}",
                     text: "${license.reference} (${license.startDate ? sdf.format(license.startDate) : '???'} - ${license.endDate ? sdf.format(license.endDate) : ''})"},
                </g:each>
            ]
            $('#submitButton').removeAttr('disabled')
        </g:if>
        //duplicate call needed to preselect value
        if(typeof(data) !== "undefined")
            $("#availableLicenses").select2('data', data)
	    $("#availableLicenses").on('change',function(e) {
	      	var selectedLicenses = e.val.length;
	       	if(selectedLicenses > 1){
	       		$('#submitButton').removeAttr('disabled')
	       	}
	       	else if(selectedLicenses <= 1) {
	       	    $('#submitButton').attr('disabled',true)
	       	}
	    });
	});
</r:script>