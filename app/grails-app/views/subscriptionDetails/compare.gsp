<%@ page import ="com.k_int.kbplus.Subscription" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'subscription.label', default: 'Subscription')}"/>
		<title><g:message code="default.edit.label" args="[entityName]"/></title>
	</head>

	<body>
		<div class="container">
			<div class="row">
				<g:if test="${institutionName}">
				<h2> ${message(code:'subscription.compare.heading',default:'Compare Subscriptions of')} ${institutionName}</h2>
				</g:if>
				<g:else>
					<h2> ${message(code:'subscription.compare.label',default:'Compare Subscriptions')}</h2>
				</g:else>

				<br/>
			      <ul class="breadcrumb">
			        <li><g:link controller="home" action="index">${message(code:'default.home.label',default:'Home')}</g:link> <span class="divider">/</span>
			        <li><g:link controller="subscriptionDetails" action="compare">${message(code:'subscription.compare.label',default:'Compare Subscriptions')}</g:link></li>

			        <li class="dropdown pull-right">
			          <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">${message(code:'default.button.exports.label', default:'Exports')}<b class="caret"></b></a>

			          <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
			            <li><g:link action="compare" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link></li>
			            
			          </ul>
			        </li>

			      </ul>
				<g:if test="${flash.message}">
					<bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
				</g:if>
		        <g:if test="${request.message}">
				    <bootstrap:alert class="alert alert-error">${request.message}</bootstrap:alert>
			    </g:if>

				<g:form action="compare" controller="subscriptionDetails" method="GET">
                                        <g:set var="subs_message" value="${message(code:'subscription.plural', default:'Subscriptions')}" />
                                        <g:set var="sub_message" value="${message(code:'subscription.label', default:'Subscription')}" />
					<table class="ui celled table">
						<thead>
							<tr>
								<th></th>
								<th> ${message(code:'subscription.label', default:'Subscription')} A </th>
								<th> ${message(code:'subscription.label', default:'Subscription')} B </th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td> ${message(code:'subscription.compare.name', default:'Subscription name')} </td>
					<td>${message(code:'default.compare.restrict.after', args:[subs_message] )}
					<g:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
					${message(code:'default.compare.restrict.before', default:'and/or ending before-')} <g:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/><br/> ${message(code:'default.compare.select.first', args:[sub_message] )}<br/>
                      <input type="hidden" name="subA" id="subSelectA" value="${subA}"/> 
					</td>
					<td> 
					    ${message(code:'default.compare.restrict.after', args:[subs_message] )}
					    <g:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
				${message(code:'default.compare.restrict.before', default:'and/or ending before-')} <g:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/><br/> ${message(code:'default.compare.select.second', args:[sub_message] )}<br/>
	                      <input type="hidden" name="subB" id="subSelectB" value="${subB}" />
					</td>
							</tr>
							<tr>
								<td> ${message(code:'subscription.compare.snapshot', default:'Subscriptions on Date')}</td>
								<td>
									<div class="input-append date">
										<input class="span2" size="16" type="text" name="dateA" id="dateA" value="${params.dateA}"/>
										<span class="add-on"><i class="icon-th"></i></span>
									</div>
								</td>
								<td>
									<div class="input-append date">
										<input class="spann2" size="16" type="text" name="dateB" id="dateB" value="${params.dateB}"/>
										<span class="add-on"><i class="icon-th"></i></span>
									</div>
								</td>
						<tr>
							<td> ${message(code:'default.compare.filter.add', default:'Add Filter')}</td>
							<td colspan="2">
		        <input type="checkbox" name="insrt" value="Y" ${params.insrt=='Y'?'checked':''}/>  ${message(code:'default.compare.filter.insert', default:'Insert')}&nbsp;
		        <input type="checkbox" name="dlt" value="Y" ${params.dlt=='Y'?'checked':''}/> ${message(code:'default.compare.filter.delete', default:'Delete')} &nbsp;
		        <input type="checkbox" name="updt" value="Y" ${params.updt=='Y'?'checked':''}/> ${message(code:'default.compare.filter.update', default:'Update')} &nbsp;
		        <input type="checkbox" name="nochng" value="Y" ${params.nochng=='Y'?'checked':''}/> ${message(code:'default.compare.filter.no_change', default:'No Change')} &nbsp;
							</td>		
						</tr>
							</tr>
						</tbody>
					</table>	
					<input type="submit" class="btn btn-primary" value="${message(code:'default.button.compare.label', default:'Compare')}" />
				</g:form>
			</div>

			<g:if test="${subInsts?.get(0) && subInsts?.get(1)}">
                                <g:set var="subs_message" value="${message(code:'subscription.plural', default:'Subscriptions')}" />
				<div class="row">
				<h3>${message(code:'default.compare.overview', args:[subs_message], default:'Subscriptions Compared')}</h3>
				<table class="ui celled table">
					<thead>
						<tr>
							<th>${message(code:'default.compare.overview.value', default:'Value')}</th>
							<th>${subInsts.get(0).name}</th>
							<th>${subInsts.get(1).name}</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>${message(code:'default.dateCreated.label', default:'Date Created')}</td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(0).dateCreated}"/></td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(1).dateCreated}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.startDate.label', default:'Start Date')}</td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(0).startDate}"/></td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(1).startDate}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.endDate.label', default:'End Date')}</td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(0).endDate}"/></td>
							<td><g:formatDate format="yyyy-MM-dd" date="${subInsts.get(1).endDate}"/></td>
						</tr>
						<tr>
							<td>${message(code:'subscription.compare.overview.ies', default:'Number of IEs')}</td>
							<td>${params.countA}</td>
							<td>${params.countB}</td>
						</tr>
					</tbody>
				</table>
				</div>
				<div class="row">
				<g:form action="compare" method="GET" class="form-inline">
					<input type="hidden" name="subA" value="${params.subA}"/>
					<input type="hidden" name="subB" value="${params.subB}"/>
					<input type="hidden" name="dateA" value="${params.dateA}"/>
					<input type="hidden" name="dateB" value="${params.dateB}"/>
					<input type="hidden" name="insrt" value="${params.insrt}"/>
					<input type="hidden" name="dlt" value="${params.dlt}"/>
					<input type="hidden" name="updt" value="${params.updt}"/>
					<input type="hidden" name="nochng" value="${params.nochng}"/>
					<input type="hidden" name="countA" value="${params.countA}"/>
					<input type="hidden" name="countB" value="${params.countB}"/>
					<table>
						<tr>
							<td>
								${message(code:'subscription.compare.filter.title', default:'Filters - Title')}: <input name="filter" value="${params.filter}">
							</td>
							<td> <input type="submit" class="btn btn-primary" value="Filter Results" /> </td>
							<td> <input id="resetFilters" type="submit" class="btn btn-primary" value="${message(code:'default.button.clear.label', default:'Clear')}" /> </td>
						</tr>
					</table>
				</g:form>

				<div class="span6 offset3">
				<dt class="center">${message(code:'subscription.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
				</div>
				<table class="ui celled table">
					<thead>
						<tr>
							<th> ${message(code:'title.label', default:'Title')} </th>
							<th> ${subInsts.get(0).name} ${message(code:'default.on', default:'on')} ${subDates.get(0)}</th>
							<th> ${subInsts.get(1).name} ${message(code:'default.on', default:'on')} ${subDates.get(1)}</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><b>${message(code:'subscription.compare.results.ies.total', default:'Total IEs for query')}</b></td>
							<td><b>${listACount}</b></td>
							<td><b>${listBCount}</b></td>
						<tr>
						<g:each in="${comparisonMap}" var="entry">
							<g:set var="subAIE" value="${entry.value[0]}"/>
							<g:set var="subBIE" value="${entry.value[1]}"/>
							<g:set var="currentTitle" value="${subAIE?.tipp?.title ?:subBIE?.tipp?.title}"/>
							<g:set var="highlight" value="${entry.value[2]}"/>
							<tr>
								
								<td>
								<b><g:link action="show" controller="titleDetails" id="${currentTitle.id}">${entry.key}</g:link></b> 
								<i onclick="showMore('${currentTitle.id}')" class="icon-info-sign"></i>

								<g:each in="${currentTitle.ids}" var="id">
				                    <br>${id.identifier.ns.ns}:${id.identifier.value}
				                </g:each>
								</td>
							
								<g:if test="${subAIE}">		
									<td class="${highlight }"><g:render template="compare_cell" model="[obj:subAIE]"/></td>
								</g:if>
								<g:else><td></td></g:else>
								
								<g:if test="${subBIE}">			
									<td class="${highlight }"><g:render template="compare_cell" model="[obj:subBIE]"/></td>
								</g:if>
								<g:else><td></td></g:else>
							</tr>							
						</g:each>						
					</tbody>
				</table>
				<div class="pagination" style="text-align:center">
		 <bootstrap:paginate  action="compare" controller="subscriptionDetails" params="${params}" next="Next" prev="Prev" maxsteps="${max}" total="${unionListSize}" />
				</div>	
				</div>
			</g:if>
		</div>
		%{-- Hiding the tables from compare_details inside the main table, breaks the modal hide.
 --}%

 <g:each in="${comparisonMap}" var="entry">
		<g:set var="subAIE" value="${entry.value[0]}"/>
		<g:set var="subBIE" value="${entry.value[1]}"/>
		<g:set var="currentTitle" value="${subAIE?.tipp?.title ?:subBIE?.tipp?.title}"/>

		<g:render template="compare_details"
		 model="[subA:subAIE,subB:subBIE,currentTitle:currentTitle, subAName:"${subInsts.get(0).name}",
		 subBName:"${subInsts.get(1).name}" ]"/>
</g:each>

<r:script language="JavaScript">
    function applySelect2(filter) {
      var subA = {id:'${subInsts?.get(0)?.id}',text:"${subInsts?.get(0)?.name}"};
      var subB = {id:'${subInsts?.get(1)?.id}',text:"${subInsts?.get(1)?.name}"};
      $("#subSelect"+filter).select2({
        width: '90%',
        placeholder: "${message(code:'subscription.compare.search.ph', default:'Type subscription name...')}",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note', default:'Pleaser enter 1 or more character')}";
        },
        ajax: { 
            url: '<g:createLink controller='ajax' action='lookup'/>',
            dataType: 'json',
            data: function (term, page) {
                return {
    	            hasDate: 'true',
                	hideIdent: 'true',
                	inclSubStartDate: 'true',
                	startDate: $("#start"+filter).val(),
                	endDate: $("#end"+filter).val(),
                	hideDeleted: 'true',
                	inst_shortcode: '${params.shortcode}',
                    q: term , // search term
                    page_limit: 10,
                    baseClass:'com.k_int.kbplus.Subscription'
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
         		obj = subA;
         	}else{
         		obj = subB;
         	}
            callback(obj);
        }
        }).select2('val',':');
	
    }

	$("#resetFilters").click(function() {
	    $(this).closest('form').find("input[name=filter]").val("");
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
    	format:"yyyy-mm-dd"
    });
    $('#dateB').datepicker({
    	format:"yyyy-mm-dd"
    });

</r:script>
	</body>
</html>
