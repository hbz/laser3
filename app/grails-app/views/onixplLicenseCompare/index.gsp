<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.k_int.kbplus.onixpl.OnixPLService" %>
<!doctype html>
<html>
<head>
<meta name="layout" content="semanticUI" />
<title>${message(code:'laser', default:'LAS:eR')} ${message(code:'onixplLicense.compare.label', default:'ONIX-PL License Comparison')}</title>

</head>

<body>
	<div>
		<ul class="breadcrumb">
			<li><g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span
				class="divider">/</span></li>
			<li>${message(code:'menu.institutions.comp_onix')}</li>
		</ul>
	</div>

	<div>
		<h1 class="ui header">${message(code:'menu.institutions.comp_onix')}</h1>
	</div>

	<div>
		<div class="row">
			<div class="span8">
				<g:form id="compare" name="compare" action="matrix" method="post">
					<div>
						<label for="addIdentifierSelect">${message(code:'onixplLicense.compare.add_id.label', default:'Search license for comparison:')}</label>

		                <input type="hidden" name="selectedIdentifier" id="addIdentifierSelect"/>
		                <button type="button" class="ui positive button" id="addToList" style="margin-top:10px">${message(code:'default.button.add.label', default:'Add')}</button>
					</div>
					
					<label for="selectedLicenses">${message(code:'onixplLicense.compare.selected.label', default:'Licenses selected for comparison:')}</label>
					<g:select style="width:90%; word-wrap: break-word;" id="selectedLicenses" name="selectedLicenses" class="compare-license" from="${[]}" multiple="true" />


					<div>
						<label for="section">${message(code:'onixplLicense.compare.section.label', default:'Compare section:')}</label>
						<g:treeSelect name="sections" id="section" class="compare-section"
							options="${termList}" selected="true" multiple="true" />
					</div>

					<div>
					  <input id="submitButton" disabled='true' type="submit" value="${message(code:'default.button.compare.label', default:'Compare')}"  name="Compare" class="ui primary button" />
					</div>
				</g:form>
			</div>
		</div>
	</div>
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
	      		var list_option = "<option selected='selected' value='"+option_id+"''>"+option_name+"</option>"
	      		$("#selectedLicenses").append(list_option)
	      		$('#selectedLicenses').trigger( "change" )
			});

	      $("#addIdentifierSelect").select2({
  	        width: '90%',
	        placeholder: "${message(code:'onixplLicense.compare.search.ph', default:'Search for a license...')}",
	        minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note', default:'Pleaser enter 1 or more character')}";
                },
	        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
	          url: "<g:createLink controller='ajax' action='lookup'/>",
	          dataType: 'json',
	          data: function (term, page) {
	              return {
	                  q: "%" + term + "%", // search term
	                  page_limit: 10,
	                  baseClass:'com.k_int.kbplus.OnixplLicense'
	              };
	          },
	          results: function (data, page) {
	            return {results: data.values};
	          },
	        }
	      });
	    });
      </r:script>
</body>
</html>
