<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="package.compare" default="Package Comparison" /></title>
  </head>
 <body>

	<semui:breadcrumbs>
		<semui:crumb controller="packageDetails" action="index" message="package.show.all" />
		<semui:crumb class="active" message="package.compare.compare" />
	</semui:breadcrumbs>

	<semui:controlButtons>
		<semui:exportDropdown>
			<semui:exportDropdownItem>
				<g:link class="item" action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
			</semui:exportDropdownItem>
		</semui:exportDropdown>
	</semui:controlButtons>

	<h1 class="ui header"><semui:headerIcon />${message(code:'package.compare', default:'Package Comparison')}</h1>

	<semui:messages data="${flash}" />

        <g:if test="${request.message}">
		    <bootstrap:alert class="alert alert-error">${request.message}</bootstrap:alert>
	    </g:if>

	<g:form action="compare" controller="packageDetails" method="GET" class="ui form">
		<table class="ui celled la-table table">
			<thead>
				<tr>
					<th></th>
					<th>${message(code:'package.label', default:'Package')} A</th>
					<th>${message(code:'package.label', default:'Package')} B</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>${message(code:'package.show.pkg_name', default:'Package Name')}</td>
					<td>
						${message(code:'package.compare.restrict.after', default:'Restrict this list to packages starting after-')}
						<semui:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
						${message(code:'package.compare.restrict.before', default:'and/or ending before-')}
						<semui:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/>
						<br/>
						${message(code:'package.compare.select.first', default:'Now select first package to compare (Filtered by dates above). Use \'%\' as wildcard.')}
						<br/>
                        <input type="hidden" name="pkgA" id="packageSelectA" value="${pkgA}"/>
					</td>
					<td> 
					    ${message(code:'package.compare.restrict.after', default:'Restrict this list to packages starting after-')}
						<semui:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
						${message(code:'package.compare.restrict.before', default:'and/or ending before-')}
						<semui:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/>
						<br/>
						${message(code:'package.compare.select.second', default:'Select second package to compare (Filtered by dates above). Use \'%\' as wildcard.')}
						<br/>
                        <input type="hidden" name="pkgB" id="packageSelectB" value="${pkgB}"/>
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.snapshot', default:'Package On date')}</td>
					<td>
						<semui:datepicker name="dateA" placeholder ="default.date.label" value="${params.dateA}" >
						</semui:datepicker>
					</td>
					<td>
						<semui:datepicker name="dateB" placeholder ="default.date.label" value="${params.dateB}" >
						</semui:datepicker>
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.filter.add', default:'Add Filter')}</td>
					<td colspan="2">
						<div class="ui checkbox">
        					<input type="checkbox" class="hidden" name="insrt" value="Y" ${params.insrt=='Y'?'checked':''}/>
							<label>${message(code:'package.compare.filter.insert', default:'Insert')}</label>
						</div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" name="dlt" value="Y" ${params.dlt=='Y'?'checked':''}/>
                            <label>${message(code:'package.compare.filter.delete', default:'Delete')}</label>
                        </div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" name="updt" value="Y" ${params.updt=='Y'?'checked':''}/>
                            <label>${message(code:'package.compare.filter.update', default:'Update')}</label>
                        </div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" name="nochng" value="Y" ${params.nochng=='Y'?'checked':''}/>
                            <label>${message(code:'package.compare.filter.no_change', default:'No Change')}</label>
                        </div>
					</td>		
				</tr>
			</tbody>
		</table>

		<input type="submit" class="ui button" value="${message(code:'default.button.compare.label', default:'Compare')}">
	</g:form>

<g:if test="${pkgInsts?.get(0) && pkgInsts?.get(1)}">

	<div class="row">
	<h3 class="ui header">${message(code:'package.compare.overview', default:'Packages Compared')}</h3>
	<table class="ui celled la-table table">
		<thead>
			<tr>
				<th>${message(code:'default.compare.overview.value', default:'Value')}</th>
				<th>${pkgInsts.get(0).name}</th>
				<th>${pkgInsts.get(1).name}</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>${message(code:'default.dateCreated.label', default:'Date Created')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(0).dateCreated}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(1).dateCreated}"/></td>
			</tr>
			<tr>
				<td>${message(code:'default.startDate.label', default:'Start Date')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(0).startDate}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(1).startDate}"/></td>
			</tr>
			<tr>
				<td>${message(code:'default.endDate.label', default:'End Date')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(0).endDate}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${pkgInsts.get(1).endDate}"/></td>
			</tr>
			<tr>
				<td>${message(code:'package.compare.overview.tipps', default:'Number of TIPPs')}</td>
				<td>${params.countA}</td>
				<td>${params.countB}</td>
			</tr>
		</tbody>
	</table>

<div class="row">
<g:form action="compare" method="get" class="ui form">
	<input type="hidden" name="pkgA" value="${params.pkgA}"/>
	<input type="hidden" name="pkgB" value="${params.pkgB}"/>
	<input type="hidden" name="dateA" value="${params.dateA}"/>
	<input type="hidden" name="dateB" value="${params.dateB}"/>
	<input type="hidden" name="insrt" value="${params.insrt}"/>
	<input type="hidden" name="dlt" value="${params.dlt}"/>
	<input type="hidden" name="updt" value="${params.updt}"/>
	<input type="hidden" name="nochng" value="${params.nochng}"/>
	<input type="hidden" name="countA" value="${params.countA}"/>
	<input type="hidden" name="countB" value="${params.countB}"/>

	 <table class="ui celled la-table table">
		<tr>
			<td style="text-align:right;padding-right:10px;">
				${message(code:'package.compare.filter.title', default:'Filters - Title')}: <input type="text" name="filter" value="${params.filter}"/>
			</td>
			<td>
				${message(code:'package.compare.filter.coverage_startsBefore', default:'Coverage Starts Before')}:
                                <semui:simpleHiddenValue id="startsBefore" name="startsBefore" type="date" value="${params.startsBefore}"/>
			</td>
			<td style="padding-left:10px;"> <input type='button' class="ui button" id="resetFilters" value="${message(code:'default.button.clear.label', default:'Clear')}"/></td>
		</tr>
		<tr>
		<td style="text-align:right;padding-right:10px;">
			${message(code:'package.compare.filter.coverage_note', default:'Coverage note')}: <input type="text" name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
		</td>
		<td>
			${message(code:'package.compare.filter.coverage_endsAfter', default:'Coverage Ends After')}:
			<semui:simpleHiddenValue id="endsAfter" name="endsAfter" type="date" value="${params.endsAfter}"/>
		</td>

			<td  style="padding-left:10px;"> <input type="submit" class="ui button" value="${message(code:'package.compare.filter.submit.label', default:'Filter Results')}" /> </td>
		</tr>
	</table>

</g:form>


<div class="span6 offset3">
<dt class="center">${message(code:'package.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
</div>
<table class="ui celled la-table table">
	<thead>
		<tr> 
			<th> ${message(code:'title.label', default:'Title')} </th>
			<th> ${pkgInsts.get(0).name} ${message(code:'default.on', default:'on')} ${pkgDates.get(0)} </th>
			<th> ${pkgInsts.get(1).name} ${message(code:'default.on', default:'on')} ${pkgDates.get(1)} </th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td><strong>${message(code:'package.compare.results.tipps.total', default:'Total Titles (TIPPs) for query')}</strong></td>
			<td><strong>${listACount.titles} (${listACount.tipps})</strong></td>
			<td><strong>${listBCount.titles} (${listBCount.tipps})</strong></td>
		<tr>
		<g:each in="${comparisonMap}" var="entry">
                  <g:set var="pkgATipp" value="${entry.value[0]}"/>
                  <g:set var="pkgBTipp" value="${entry.value[1]}"/>
                  <g:set var="currentTitle" value="${pkgATipp?.title ?:pkgBTipp?.title}"/>
                  <g:set var="highlight" value="${entry.value[2]}"/>

		<tr>
			
			<td>
                          <strong><g:link action="show" controller="titleDetails" id="${currentTitle[0].id}">${entry.key}</g:link></strong>
                          <i onclick="showMore('${currentTitle[0].id}')" class="icon-info-sign"></i>

                          <g:each in="${currentTitle[0].ids}" var="id">
                            <g:if test="${id.identifier.ns.ns != 'originediturl'}">
                              <br>${id.identifier.ns.ns}:${id.identifier.value}
                            </g:if>
                          </g:each>
			</td>
			
			<g:if test="${pkgATipp}">		
				<td class="${highlight }">
                                  <g:each in="${pkgATipp}" var="t">
                                    <g:render template="compare_cell" model="[obj:t]"/>
                                    <div style="height:3px;"></div>
                                  </g:each>
                                </td>
			</g:if>
			<g:else><td></td></g:else>
			
			<g:if test="${pkgBTipp}">			
				<td class="${highlight }">
                                  <g:each in="${pkgBTipp}" var="t">
                                    <g:render template="compare_cell" model="[obj:t]"/>
                                    <div style="height:3px;"></div>
                                  </g:each>
                                </td>
			</g:if>
			<g:else><td></td></g:else>

		</tr>
			
		</g:each>
	</tbody>
</table>

 <semui:paginate action="compare" controller="packageDetails" params="${params}" first="first" last="Last" maxsteps="${max}" total="${unionListSize}" />

</g:if>
</div>
%{-- Hiding the tables from compare_details inside the main table, breaks the modal hide.
 --}%
<g:each in="${comparisonMap}" var="entry">
		<g:set var="pkgATipp" value="${entry.value[0]}"/>
		<g:set var="pkgBTipp" value="${entry.value[1]}"/>
		<g:set var="currentTitle" value="${pkgATipp?.title ?:pkgBTipp?.title}"/>

		<g:render template="compare_details"
		 model="[pkgA:pkgATipp,pkgB:pkgBTipp,currentTitle:currentTitle, pkgAName:"${pkgInsts.get(0).name}",
		 pkgBName:"${pkgInsts.get(1).name}" ]"/>
</g:each>

<r:script language="JavaScript">
    function applySelect2(filter) {
      var pkgA = {id:'${pkgInsts?.get(0)?.id}',text:"${pkgInsts?.get(0)?.name}"};
      var pkgB = {id:'${pkgInsts?.get(1)?.id}',text:"${pkgInsts?.get(1)?.name}"};

      $("#packageSelect"+filter).select2({
      	width: "90%",
        placeholder: "${message(code:'package.compare.search.ph', default:'Type package name...')}",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note', default:'Pleaser enter 1 or more character')}";
        },
        ajax: { 
            url: '<g:createLink controller='ajax' action='lookup'/>',
            dataType: 'json',
            data: function (term, page) {
                return {
                	hideIdent: 'true',
                	hasDate: 'true',
                	inclPkgStartDate: 'true',
                	startDate: $("#start"+filter).val(),
                	endDate: $("#end"+filter).val(),
                    q: term , // search term
                    page_limit: 10,
                    baseClass:'com.k_int.kbplus.Package'
                };
            },
            
            results: function (data, page) {
                return {results: data.values};
            }
        },
	    allowClear: true,
         formatSelection: function(data) { 
            return data.text; 
        },
        initSelection : function (element, callback) {
	        var obj
         	if(filter == "A"){
         		obj = pkgA;
         	}else{
         		obj = pkgB;
         	}
            callback(obj);
        }
        }).select2('val',':');
    }

	$("#resetFilters").click(function() {
	    $(this).closest('form').find("input[name=filter], input[type=coverageNoteFilter],input[type=coverageNoteFilter],input[name=startsBefore],input[name=endsAfter]").val("");
	    $(this).closest('form').submit();
	});

    function showMore(ident) {
		$("#compare_details"+ident).modal('show')
    }

    $(function(){
    	applySelect2("A")
     	applySelect2("B")
    });


</r:script>

  </body>
</html>
