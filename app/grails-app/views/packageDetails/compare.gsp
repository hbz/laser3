<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="package.compare" default="Package Comparison" /></title>
  </head>
 <body>

<div>
<div class="row">
	<h2 class="ui header">${message(code:'package.compare', default:'Package Comparison')}</h2>

	<semui:breadcrumbs>
		<semui:crumb controller="packageDetails" action="index" message="package.show.all" />
		<semui:crumb class="active" message="package.compare.compare" />

        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
	</semui:breadcrumbs>

	<semui:messages data="${flash}" />

        <g:if test="${request.message}">
		    <bootstrap:alert class="alert alert-error">${request.message}</bootstrap:alert>
	    </g:if>

	<g:form action="compare" controller="packageDetails" method="GET">
		<table class="ui celled table">
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
					<td>${message(code:'package.compare.restrict.after', default:'Restrict this list to packages starting after-')} <g:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
							${message(code:'package.compare.restrict.before', default:'and/or ending before-')} <g:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/><br/>
                                              ${message(code:'package.compare.select.first', default:'Now select first package to compare (Filtered by dates above). Use \'%\' as wildcard.')}<br/>
                                              <input type="hidden" name="pkgA" id="packageSelectA" value="${pkgA}"/> 
					</td>
					<td> 
					    ${message(code:'package.compare.restrict.after', default:'Restrict this list to packages starting after-')} <g:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
							${message(code:'package.compare.restrict.before', default:'and/or ending before-')} <g:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/><br/>
                                              ${message(code:'package.compare.select.second', default:'Select second package to compare (Filtered by dates above). Use \'%\' as wildcard.')}<br/>
                                              <input type="hidden" name="pkgB" id="packageSelectB" value="${pkgB}" />
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.snapshot', default:'Package On date')}</td>
					<td>
						<div class="input-append date" id="dateA">
							<input class="span2" size="16" type="text" 
							name="dateA" value="${params.dateA}">
							<span class="add-on"><i class="icon-th"></i></span> 
						</div>
					</td>
					<td> 
						<div class="input-append date" id="dateB">
							<input class="span2" size="16" type="text" 
							name="dateB" value="${params.dateB}">
							<span class="add-on"><i class="icon-th"></i></span> 
						</div>
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.filter.add', default:'Add Filter')}</td>
					<td colspan="2">
        <input type="checkbox" name="insrt" style="vertical-align:top" value="Y" ${params.insrt=='Y'?'checked':''}/> ${message(code:'package.compare.filter.insert', default:'Insert')}&nbsp;
        <input type="checkbox" name="dlt" style="vertical-align:top" value="Y" ${params.dlt=='Y'?'checked':''}/> ${message(code:'package.compare.filter.delete', default:'Delete')}&nbsp;
        <input type="checkbox" name="updt" style="vertical-align:top" value="Y" ${params.updt=='Y'?'checked':''}/> ${message(code:'package.compare.filter.update', default:'Update')}&nbsp;
        <input type="checkbox" name="nochng" style="vertical-align:top" value="Y" ${params.nochng=='Y'?'checked':''}/> ${message(code:'package.compare.filter.no_change', default:'No Change')}&nbsp;
					</td>		
				</tr>
			</tbody>
		</table>

		<input type="submit" class="ui primary button" value="${message(code:'default.button.compare.label', default:'Compare')}">
	</g:form>
</div>


<g:if test="${pkgInsts?.get(0) && pkgInsts?.get(1)}">

	<div class="row">
	<h3 class="ui header">${message(code:'package.compare.overview', default:'Packages Compared')}</h3>
	<table class="ui celled table">
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
	</div>
<div class="row">
<g:form action="compare" method="get" class="form-inline">
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

	 <table class="ui celled table">
		<tr>
			<td style="text-align:right;padding-right:10px;">
				${message(code:'package.compare.filter.title', default:'Filters - Title')}: <input type="text" name="filter" value="${params.filter}"/>
			</td>
			<td>
				${message(code:'package.compare.filter.coverage_startsBefore', default:'Coverage Starts Before')}:
                                <g:simpleHiddenValue id="startsBefore" name="startsBefore" type="date" value="${params.startsBefore}"/>
			</td>
			<td style="padding-left:10px;"> <input type='button' class="ui primary button" id="resetFilters" value="${message(code:'default.button.clear.label', default:'Clear')}"/></td>
		</tr>
		<tr>
		<td style="text-align:right;padding-right:10px;">
			${message(code:'package.compare.filter.coverage_note', default:'Coverage note')}: <input type="text" name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
		</td>
		<td>
			${message(code:'package.compare.filter.coverage_endsAfter', default:'Coverage Ends After')}:
			<g:simpleHiddenValue id="endsAfter" name="endsAfter" type="date" value="${params.endsAfter}"/>
		</td>

			<td  style="padding-left:10px;"> <input type="submit" class="ui primary button" value="${message(code:'package.compare.filter.submit.label', default:'Filter Results')}" /> </td>
		</tr>
	</table>

</g:form>


<div class="span6 offset3">
<dt class="center">${message(code:'package.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
</div>
<table class="ui celled table">
	<thead>
		<tr> 
			<td> ${message(code:'title.label', default:'Title')} </td>
			<td> ${pkgInsts.get(0).name} ${message(code:'default.on', default:'on')} ${pkgDates.get(0)} </td>
			<td> ${pkgInsts.get(1).name} ${message(code:'default.on', default:'on')} ${pkgDates.get(1)} </td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td><b>${message(code:'package.compare.results.tipps.total', default:'Total Titles (TIPPs) for query')}</b></td>
			<td><b>${listACount.titles} (${listACount.tipps})</b></td>
			<td><b>${listBCount.titles} (${listBCount.tipps})</b></td>
		<tr>
		<g:each in="${comparisonMap}" var="entry">
                  <g:set var="pkgATipp" value="${entry.value[0]}"/>
                  <g:set var="pkgBTipp" value="${entry.value[1]}"/>
                  <g:set var="currentTitle" value="${pkgATipp?.title ?:pkgBTipp?.title}"/>
                  <g:set var="highlight" value="${entry.value[2]}"/>

		<tr>
			
			<td>
                          <b><g:link action="show" controller="titleDetails" id="${currentTitle[0].id}">${entry.key}</g:link></b>
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
<div class="pagination" style="text-align:center">

 <bootstrap:paginate action="compare" controller="packageDetails" params="${params}" first="first" last="Last" maxsteps="${max}" total="${unionListSize}" />
</div>

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

    $('#dateA').datepicker({
      format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
      language:"${message(code:'default.locale.label', default:'en')}",
      autoclose:true
    });
    $('#dateB').datepicker({
      format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
      language:"${message(code:'default.locale.label', default:'en')}",
      autoclose:true
    });

</r:script>

  </body>
</html>
