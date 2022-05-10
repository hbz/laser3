<%@ page import="de.laser.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <g:set var="entityName" value="${message(code: 'package.label')}" />
    <title>${message(code:'laser')} : <g:message code="package.compare" /></title>
  </head>
 <body>

	<semui:breadcrumbs>
		<semui:crumb controller="package" action="index" message="package.show.all" />
		<semui:crumb class="active" message="package.compare" />
	</semui:breadcrumbs>

	<semui:controlButtons>
		<semui:exportDropdown>
			<semui:exportDropdownItem>
				<g:link class="item" action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
			</semui:exportDropdownItem>
		</semui:exportDropdown>
	</semui:controlButtons>

	<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'package.compare')}</h1>

	<semui:messages data="${flash}" />

        <g:if test="${request.message}">
		    <semui:msg class="negative" text="${request.message}" />
	    </g:if>

	<g:form action="compare" controller="package" method="GET" class="ui form">
		<table class="ui celled la-js-responsive-table la-table table">
			<thead>
				<tr>
					<th></th>
					<th>${message(code:'package.label')} A</th>
					<th>${message(code:'package.label')} B</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>${message(code:'package.show.pkg_name')}</td>
					<td>
						${message(code:'package.compare.restrict.after')}
                        <semui:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
						${message(code:'package.compare.restrict.before')}
						<semui:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/>
						<br />
						${message(code:'package.compare.select.first')}
						<br />
                        <input type="hidden" name="pkgA" id="packageSelectA" value="${pkgA}"/>
					</td>
					<td> 
					    ${message(code:'package.compare.restrict.after')}
                        <semui:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
						${message(code:'package.compare.restrict.before')}
                        <semui:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/>
						<br />
						${message(code:'package.compare.select.second')}
						<br />
                        <input type="hidden" name="pkgB" id="packageSelectB" value="${pkgB}"/>
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.snapshot')}</td>
					<td>
						<semui:datepicker id="dateA" name="dateA" placeholder ="default.date.label" value="${params.dateA}" >
						</semui:datepicker>
					</td>
					<td>
						<semui:datepicker id="dateB" name="dateB" placeholder ="default.date.label" value="${params.dateB}" >
						</semui:datepicker>
					</td>
				</tr>
				<tr>
					<td>${message(code:'package.compare.filter.add')}</td>
					<td colspan="2">
						<div class="ui checkbox">
        					<input type="checkbox" class="hidden" id="insrt" name="insrt" value="Y" ${params.insrt=='Y'?'checked':''}/>
							<label for="insrt">${message(code:'package.compare.filter.insert')}</label>
						</div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" id="dlt" name="dlt" value="Y" ${params.dlt=='Y'?'checked':''}/>
                            <label for="dlt">${message(code:'package.compare.filter.delete')}</label>
                        </div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" id="updt" name="updt" value="Y" ${params.updt=='Y'?'checked':''}/>
                            <label for="updt">${message(code:'package.compare.filter.update')}</label>
                        </div>
                        <div class="ui checkbox">
                            <input type="checkbox" class="hidden" id="nochng" name="nochng" value="Y" ${params.nochng=='Y'?'checked':''}/>
                            <label for="nochng">${message(code:'package.compare.filter.no_change')}</label>
                        </div>
					</td>		
				</tr>
			</tbody>
		</table>
		<div class="fields">
                  <div class="field">
                  <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.comparereset.label')}</a>
                    </div>
                  <div class="field">
					<input type="submit" class="ui button" value="${message(code:'default.button.compare.label')}">
				  </div>
		</div>
	</g:form>

<g:if test="${pkgInsts?.get(0) && pkgInsts?.get(1)}">

	<div class="row">
	<h3 class="ui header">${message(code:'package.compare.overview')}</h3>
	<table class="ui celled la-js-responsive-table la-table table">
		<thead>
			<tr>
				<th>${message(code:'default.value.label')}</th>
				<th>${pkgInsts.get(0).name}</th>
				<th>${pkgInsts.get(1).name}</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>${message(code:'default.dateCreated.label')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(0).dateCreated}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(1).dateCreated}"/></td>
			</tr>
			<tr>
				<td>${message(code:'default.startDate.label')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(0).startDate}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(1).startDate}"/></td>
			</tr>
			<tr>
				<td>${message(code:'default.endDate.label')}</td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(0).endDate}"/></td>
				<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkgInsts.get(1).endDate}"/></td>
			</tr>
			<tr>
				<td>${message(code:'package.compare.overview.tipps')}</td>
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

	 <table class="ui celled la-js-responsive-table la-table table">
		<tr>
			<td style="text-align:right;padding-right:10px;">
				${message(code:'package.compare.filter.title')}: <input type="text" name="filter" value="${params.filter}"/>
			</td>
			<td>
				${message(code:'package.compare.filter.coverage_startsBefore')}:
                                <semui:simpleHiddenValue id="startsBefore" name="startsBefore" type="date" value="${params.startsBefore}"/>
			</td>
			<td style="padding-left:10px;"> <input type='button' class="ui button" id="resetFilters" value="${message(code:'default.button.clear.label')}"/></td>
		</tr>
		<tr>
		<td style="text-align:right;padding-right:10px;">
			${message(code:'package.compare.filter.coverage_note')}: <input type="text" name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
		</td>
		<td>
			${message(code:'package.compare.filter.coverage_endsAfter')}:
			<semui:simpleHiddenValue id="endsAfter" name="endsAfter" type="date" value="${params.endsAfter}"/>
		</td>

			<td  style="padding-left:10px;"> <input type="submit" class="ui button" value="${message(code:'package.compare.filter.submit.label')}" /> </td>
		</tr>
	</table>

</g:form>


<div class="span6 offset3">
<dt class="center">${message(code:'package.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
</div>
<table class="ui celled la-js-responsive-table la-table table">
	<thead>
		<tr> 
			<th> ${message(code:'title.label')} </th>
			<th> ${pkgInsts.get(0).name} ${message(code:'default.on')} ${pkgDates.get(0)} </th>
			<th> ${pkgInsts.get(1).name} ${message(code:'default.on')} ${pkgDates.get(1)} </th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td><strong>${message(code:'package.compare.results.tipps.total')}</strong></td>
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
				<semui:listIcon type="${currentTitle[0].medium?.value}"/><strong><g:link action="show" controller="title" id="${currentTitle[0].id}">${entry.key}</g:link></strong>
                          <i onclick="JSPC.app.showMore('${currentTitle[0].id}')" class="icon-info-sign"></i>

                          <g:each in="${currentTitle[0].ids?.sort{it?.ns?.ns}}" var="id">
                              <br />${id.ns.ns}: ${id.value}
                          </g:each>
			</td>
			
			<g:if test="${pkgATipp}">		
				<td class="${highlight }">
                                  <g:each in="${pkgATipp}" var="t">
                                    <laser:render template="compare_cell" model="[obj:t]"/>
                                    <div style="height:3px;"></div>
                                  </g:each>
                                </td>
			</g:if>
			<g:else><td></td></g:else>
			
			<g:if test="${pkgBTipp}">			
				<td class="${highlight }">
                                  <g:each in="${pkgBTipp}" var="t">
                                    <laser:render template="compare_cell" model="[obj:t]"/>
                                    <div style="height:3px;"></div>
                                  </g:each>
                                </td>
			</g:if>
			<g:else><td></td></g:else>

		</tr>
			
		</g:each>
	</tbody>
</table>

 <semui:paginate action="compare" controller="package" params="${params}" first="first" last="Last" max="${max}" total="${unionListSize}" />

</g:if>
</div>
%{-- Hiding the tables from compare_details inside the main table, breaks the modal hide.
 --}%
<g:each in="${comparisonMap}" var="entry">
		<g:set var="pkgATipp" value="${entry.value[0]}"/>
		<g:set var="pkgBTipp" value="${entry.value[1]}"/>
		<g:set var="currentTitle" value="${pkgATipp?.title ?:pkgBTipp?.title}"/>

		<laser:render template="compare_details"
		 model="[pkgA:pkgATipp,pkgB:pkgBTipp,currentTitle:currentTitle, pkgAName:pkgInsts.get(0).name,
		 pkgBName:pkgInsts.get(1).name ]"/>
</g:each>

<laser:script file="${this.getGroovyPageFileName()}">
	JSPC.app.applySelect2 = function (filter) {
      var pkgA = {id:'${pkgInsts?.get(0)?.id}',text:"${pkgInsts?.get(0)?.name}"};
      var pkgB = {id:'${pkgInsts?.get(1)?.id}',text:"${pkgInsts?.get(1)?.name}"};

      $("#packageSelect"+filter).select2({
      	width: "90%",
        placeholder: "${message(code:'package.compare.search.ph')}",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note')}";
        },
        ajax: { 
            url: '<g:createLink controller='ajaxJson' action='lookup'/>',
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
                    baseClass:'${Package.class.name}'
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

    JSPC.app.showMore = function (ident) {
		$("#compare_details"+ident).modal('show')
    }

	JSPC.app.applySelect2("A")
	JSPC.app.applySelect2("B")

</laser:script>

  </body>
</html>
