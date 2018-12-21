<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.k_int.kbplus.onixpl.OnixPLService" %>
<!doctype html>
<html>
<head>
<meta name="layout" content="semanticUI" />
<title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.comp_lic')}</title>

</head>

<body>

	<semui:breadcrumbs>
		<g:if test="${institution}">
			<semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
			<semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
		</g:if>
		<semui:crumb class="active" message="menu.institutions.comp_lic" />
	</semui:breadcrumbs>

	<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.comp_lic')}</h1>


				<g:form class="ui form" id="compare" name="compare" action="compare" method="get">
					<input type="hidden" name="institution" value="${institution?.id}"/>

					<div class="field">
						<label for="addIdentifierSelect">${message(code:'onixplLicense.compare.add_id.label', default:'Search license for comparison:')}</label>

						<input type="hidden" name="selectedIdentifier" id="addIdentifierSelect"/>
						<button type="button" style="margin-top:10px" class="ui positive button" id="addToList" >${message(code:'default.button.add.label', default:'Add')}</button>
					</div>

					<div class="field">
						<label for="selectedLicenses">${message(code:'onixplLicense.compare.selected.label', default:'Licenses selected for comparison:')}</label>
					
						<g:select style="width:90%; word-wrap: break-word;" id="selectedLicenses" name="selectedLicenses" class="compare-license" from="${[]}" multiple="true" />
					</div>

					<div class="fields">
						<div class="field">
							<a href="${request.forwardURI}" class="ui button">${message(code:'default.button.comparereset.label')}</a>
							&nbsp;
						  	<input id="submitButton" disabled='true' type="submit" value="${message(code:'default.button.compare.label', default:'Compare')}"  name="Compare" class="ui button" />
						</div>
					</div>
				</g:form>

	  <r:script language="JavaScript">


	    $(function(){

	      var main = $('#selectedLicenses');
	  
	      // Now add the onchange.
	      main.change(function() {
	        var conceptName = main.find(":selected");
	        if(conceptName != null){
	        	$('#submitButton').removeAttr('disabled')
	        }
	      });

	      $('#addToList').click(function() {
	      		var option = $("input[name='selectedIdentifier']").val()
	      		var option_name = option.split("||")[0]
	      		var option_id = option.split("||") [1]
	      		var list_option = "<option selected='selected' value='"+option_id+"'>"+option_name+"</option>"
	      		$("#selectedLicenses").append(list_option)
	      		$('#selectedLicenses').trigger( "change" )
			});

	      $("#addIdentifierSelect").select2({
  	        width: '90%',
	        placeholder: "${message(code:'onixplLicense.compare.search.ph', default:'Search for a license...')}",
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
                },
	        minimumInputLength: 1,
	        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
	          url: "<g:createLink controller='ajax' action='lookup'/>",
	          dataType: 'json',
	          data: function (term, page) {
	              return {
	                  q: "%"+term+"%",
	                  inst:"${institution?.id}",
	                  roleTypes: [${licensee_role?.id}, ${licensee_cons_role?.id}, ${licensing_cons_role?.id}],
	                  isPublic:"${isPublic?.id}",
	                  page_limit: 10,
	                  baseClass:'com.k_int.kbplus.License'
	              }
	          },
	          results: function (data, page) {
	            return {results: data.values};
	          }
	        }
	      });
	    });
      </r:script>
</body>
</html>
