<%@ page import ="com.k_int.kbplus.Subscription" %>
<laser:serviceInjection />

<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'subscription.label', default: 'Subscription')}"/>
		<title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.compare.label',default:'Compare Subscriptions')}</title>
	</head>

    <body>
        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <g:if test="${institutionName}">
            <h2 class="ui header">${message(code:'subscription.compare.heading',default:'Compare Subscriptions of')} ${institutionName}</h2>
        </g:if>
        <g:else>
            <h2 class="ui header">${message(code:'subscription.compare.label',default:'Compare Subscriptions')}</h2>
        </g:else>

        <semui:messages data="${flash}" />

		<g:if test="${request.message}">
			<bootstrap:alert class="alert alert-error">${request.message}</bootstrap:alert>
		</g:if>

				<g:form action="compare" controller="subscription" method="GET" class="ui form">
					<g:set var="subs_message" value="${message(code:'subscription.plural', default:'Subscriptions')}" />
					<g:set var="sub_message" value="${message(code:'subscription.label', default:'Subscription')}" />

					<table class="ui celled la-table table">
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
					<semui:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
					${message(code:'default.compare.restrict.before', default:'and/or ending before-')} <semui:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/><br/> ${message(code:'default.compare.select.first', args:[sub_message] )}<br/>
                      <input type="hidden" name="subA" id="subSelectA" value="${subA}"/> 
					</td>
					<td> 
					    ${message(code:'default.compare.restrict.after', args:[subs_message] )}
					    <semui:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
				${message(code:'default.compare.restrict.before', default:'and/or ending before-')} <semui:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/><br/> ${message(code:'default.compare.select.second', args:[sub_message] )}<br/>
	                      <input type="hidden" name="subB" id="subSelectB" value="${subB}" />
					</td>
							</tr>
							<tr>
								<td> ${message(code:'subscription.compare.snapshot', default:'Subscriptions on Date')}</td>
								<td>
									<semui:datepicker name="dateA" placeholder ="default.date.label" value="${params.dateA}" >
									</semui:datepicker>
								</td>
								<td>
									<semui:datepicker name="dateB" placeholder ="default.date.label" value="${params.dateB}" >
									</semui:datepicker>
								</td>
						<tr>
							<td> ${message(code:'default.compare.filter.add', default:'Add Filter')}</td>
							<td colspan="2">

                                <div class="ui checkbox">
                                    <input type="checkbox" class="hidden" name="insrt" value="Y" ${params.insrt=='Y'?'checked':''}/>
                                    <label>${message(code:'default.compare.filter.insert', default:'Insert')}</label>
                                </div>
                                <div class="ui checkbox">
                                    <input type="checkbox" class="hidden" name="dlt" value="Y" ${params.dlt=='Y'?'checked':''}/>
                                    <label>${message(code:'default.compare.filter.delete', default:'Delete')}</label>
                                </div>
                                <div class="ui checkbox">
                                    <input type="checkbox" class="hidden" name="updt" value="Y" ${params.updt=='Y'?'checked':''}/>
                                    <label>${message(code:'default.compare.filter.update', default:'Update')}</label>
                                </div>
                                <div class="ui checkbox">
                                    <input type="checkbox" class="hidden" name="nochng" value="Y" ${params.nochng=='Y'?'checked':''}/>
                                    <label>${message(code:'default.compare.filter.no_change', default:'No Change')}</label>
                                </div>

							</td>		
						</tr>
							</tr>
						</tbody>
					</table>
					<div class="fields">
                  		<div class="field">
                  			<a href="${request.forwardURI}" class="ui button">${message(code:'default.button.comparereset.label')}</a>
                    	</div>
						<div class="field">
							<input type="submit" class="ui button" value="${message(code:'default.button.compare.label', default:'Compare')}" />
						</div>
					</div>
				</g:form>


			<g:if test="${subInsts?.get(0) && subInsts?.get(1)}">
                                <g:set var="subs_message" value="${message(code:'subscription.plural', default:'Subscriptions')}" />
				<div class="row">
				<h3 class="ui header">${message(code:'default.compare.overview', args:[subs_message], default:'Subscriptions Compared')}</h3>
				<table class="ui celled la-table table">
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
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(0).dateCreated}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(1).dateCreated}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.startDate.label', default:'Start Date')}</td>
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(0).startDate}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(1).startDate}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.endDate.label', default:'End Date')}</td>
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(0).endDate}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${subInsts.get(1).endDate}"/></td>
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
				<g:form action="compare" method="GET" class="ui form">
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
					 <table class="ui celled la-table table">
						<tr>
							<td>
								${message(code:'subscription.compare.filter.title', default:'Filters - Title')}: <input name="filter" value="${params.filter}">
							</td>
							<td> <input type="submit" class="ui button" value="Filter Results" /> </td>
							<td> <input id="resetFilters" type="submit" class="ui button" value="${message(code:'default.button.clear.label', default:'Clear')}" /> </td>
						</tr>
					</table>
				</g:form>

				<div class="span6 offset3">
				<dt class="center">${message(code:'subscription.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
				</div>
				<table class="ui celled la-table table">
					<thead>
						<tr>
							<th> ${message(code:'title.label', default:'Title')} </th>
							<th> ${subInsts.get(0).name} ${message(code:'default.on', default:'on')} ${subDates.get(0)}</th>
							<th> ${subInsts.get(1).name} ${message(code:'default.on', default:'on')} ${subDates.get(1)}</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><strong>${message(code:'subscription.compare.results.ies.total', default:'Total IEs for query')}</strong></td>
							<td><strong>${listACount}</strong></td>
							<td><strong>${listBCount}</strong></td>
						<tr>
						<g:each in="${comparisonMap}" var="entry">
							<g:set var="subAIE" value="${entry.value[0]}"/>
							<g:set var="subBIE" value="${entry.value[1]}"/>
							<g:set var="currentTitle" value="${subAIE?.tipp?.title ?:subBIE?.tipp?.title}"/>
							<g:set var="highlight" value="${entry.value[2]}"/>
							<tr>
								
								<td><semui:listIcon type="${currentTitle?.type?.value}"/>
								<strong><g:link action="show" controller="title" id="${currentTitle.id}">${entry.key}</g:link></strong>
								<i onclick="showMore('${currentTitle.id}')" class="icon-info-sign"></i>

								<g:each in="${currentTitle.ids.sort{it.identifier.ns.ns}}" var="id">
				                    <br>${id.identifier.ns.ns}: ${id.identifier.value}
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

		        <semui:paginate  action="compare" controller="subscription" params="${params}" next="Next" prev="Prev" maxsteps="${max}" total="${unionListSize}" />

				</div>
			</g:if>

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
            return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
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
                	inst_shortcode: '${contextService.getOrg()?.shortcode}',
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


</r:script>
	</body>
</html>
