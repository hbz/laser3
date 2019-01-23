<%@ page import="java.text.SimpleDateFormat;com.k_int.kbplus.License" contentType="text/html;charset=UTF-8" %>
<!doctype html>
    <html>
        <head>
            <meta name="layout" content="semanticUI" />
            <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.comp_lic')}</title>
        </head>
        <body>
            <semui:breadcrumbs>
                <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
                <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
		        <semui:crumb class="active" message="menu.institutions.comp_lic" />
	        </semui:breadcrumbs>
	        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.comp_lic')}</h1>
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
		</body>
	<r:script>
	    $(document).ready(function(){
	      	$("#availableLicenses").change(function() {
	        	var conceptName = $("#availableLicenses").length();
	        	if(conceptName > 0){
	        		$('#submitButton').removeAttr('disabled') //continue here
	        	}
	      	});
	      	/*
	      	$('#addToList').click(function() {
	      		var option = $("input[name='selectedIdentifier']").val()
	      		var option_name = option.split("||")[0]
	      		var option_id = option.split("||") [1]
	      		var list_option = "<option selected='selected' value='"+option_id+"'>"+option_name+"</option>"
	      		$("#selectedLicenses").append(list_option)
	      		$('#selectedLicenses').trigger( "change" )
			});
	      	*/

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
	    });
	</r:script>
</html>
