<%@ page import ="de.laser.Subscription" %>
<laser:htmlStart message="subscription.compare.label" serviceInjection="true" />

		<g:set var="entityName" value="${message(code: 'default.subscription.label')}"/>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>

        <g:if test="${institutionName}">
			<ui:h1HeaderWithIcon message="menu.my.comp_sub" />
        </g:if>
        <g:else>
			<ui:h1HeaderWithIcon message="subscription.compare.label" />
        </g:else>

        <ui:messages data="${flash}" />

				<g:form action="compare" controller="subscription" method="GET" class="ui form">
					<g:set var="subs_message" value="${message(code:'subscription.plural')}" />
					<g:set var="sub_message" value="${message(code:'default.subscription.label')}" />

					<table class="ui celled la-js-responsive-table la-table table">
						<thead>
							<tr>
								<th></th>
								<th> ${message(code:'default.subscription.label')} A </th>
								<th> ${message(code:'default.subscription.label')} B </th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td> ${message(code:'subscription.compare.name')} </td>
								<td>${message(code:'default.compare.restrict.after', args:[subs_message] )}
									<ui:simpleHiddenValue id="startA" name="startA" type="date" value="${params.startA}"/>
									${message(code:'default.compare.restrict.before')}
									<ui:simpleHiddenValue id="endA" name="endA" type="date" value="${params.endA}"/><br />
									<div class="ui search selection dropdown" id="subSelectA-wrapper">
										<input type="hidden" name="subA" id="subSelectA" value="${subA}">
										<i class="dropdown icon"></i>
										<div class="default text">${message(code:'default.compare.select.first', args:[sub_message] )}</div>
										<div class="menu"></div>
									</div>
								</td>
								<td>
									${message(code:'default.compare.restrict.after', args:[subs_message] )}
									<ui:simpleHiddenValue id="startB" name="startB" type="date" value="${params.startB}"/>
									${message(code:'default.compare.restrict.before')}
									<ui:simpleHiddenValue id="endB" name="endB" type="date" value="${params.endB}"/><br />
									<div class="ui search selection dropdown" id="subSelectB-wrapper">
										<input type="hidden" name="subB" id="subSelectB" value="${subB}">
										<i class="dropdown icon"></i>
										<div class="default text">${message(code:'default.compare.select.second', args:[sub_message] )}</div>
										<div class="menu"></div>
									</div>
								</td>
							</tr>
							<tr>
								<td> ${message(code:'subscription.compare.snapshot')}</td>
								<td>
									<ui:datepicker id="dateA" name="dateA" placeholder ="default.date.label" value="${dateA ? dateA : ''}" >
									</ui:datepicker>
								</td>
								<td>
									<ui:datepicker id="dateB" name="dateB" placeholder ="default.date.label" value="${dateB ? dateB : ''}" >
									</ui:datepicker>
								</td>
							</tr>
								<tr>
									<td> ${message(code:'default.compare.filter.add')}</td>
									<td colspan="2">

										<div class="ui checkbox">
											<g:checkBox name="insrt" id="insrt" checked="${insrt ? insrt:true}"/>
											<label for="insrt">${message(code:'default.compare.filter.insert')}</label>
										</div>
										<div class="ui checkbox">
											<g:checkBox name="dlt" id="dlt" checked="${dlt ? dlt:true}"/>
											<label for="dlt">${message(code:'default.button.delete.label')}</label>
										</div>
										<div class="ui checkbox">
											<g:checkBox name="updt" id="updt" checked="${updt ? updt:true}"/>
											<label for="updt">${message(code:'default.compare.filter.update')}</label>
										</div>
										<div class="ui checkbox">
											<g:checkBox name="nochng" id="nochng" checked="${nochng ? nochng:false}"/>
											<label for="nochng">${message(code:'default.compare.filter.no_change')}</label>
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
							<input type="submit" class="ui button" value="${message(code:'default.button.compare.label')}" />
						</div>
					</div>
				</g:form>


			<g:if test="${subInsts?.get(0) && subInsts?.get(1)}">
                                <g:set var="subs_message" value="${message(code:'subscription.plural')}" />
				<div class="row">
				<h3 class="ui header">${message(code:'default.compare.overview', args:[subs_message])}</h3>
				<table class="ui celled la-js-responsive-table la-table table">
					<thead>
						<tr>
							<th>${message(code:'default.value.label')}</th>
							<th>${subInsts.get(0).name}</th>
							<th>${subInsts.get(1).name}</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td>${message(code:'default.dateCreated.label')}</td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(0).dateCreated}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(1).dateCreated}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.startDate.label')}</td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(0).startDate}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(1).startDate}"/></td>
						</tr>
						<tr>
							<td>${message(code:'default.endDate.label')}</td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(0).endDate}"/></td>
							<td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${subInsts.get(1).endDate}"/></td>
						</tr>
						<tr>
							<td>${message(code:'subscription.compare.overview.ies')}</td>
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
					 <table class="ui celled la-js-responsive-table la-table table">
						<tr>
							<td>
								${message(code:'subscription.compare.filter.title')}: <input name="filter" value="${params.filter}">
							</td>
							<td> <input type="submit" class="ui button" value="Filter Results" /> </td>
							<td> <input id="resetFilters" type="submit" class="ui button" value="${message(code:'default.button.clear.label')}" /> </td>
						</tr>
					</table>
				</g:form>

				<div>
					<dt class="center">${message(code:'subscription.compare.results.pagination', args: [offset+1,offset+comparisonMap.size(),unionListSize])}</dt>
				</div>
				<table class="ui celled la-js-responsive-table la-table table">
					<thead>
						<tr>
							<th> ${message(code:'title.label')} </th>
							<th> ${subInsts.get(0).name} ${message(code:'default.on')} ${subDates.get(0)}</th>
							<th> ${subInsts.get(1).name} ${message(code:'default.on')} ${subDates.get(1)}</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><strong>${message(code:'subscription.compare.results.ies.total')}</strong></td>
							<td><strong>${listACount}</strong></td>
							<td><strong>${listBCount}</strong></td>
						<tr>
						<g:each in="${comparisonMap}" var="entry">
							<g:set var="subAIE" value="${entry.value[0]}"/>
							<g:set var="subBIE" value="${entry.value[1]}"/>
							<g:set var="currentTitle" value="${subAIE?.tipp?.title ?:subBIE?.tipp?.title}"/>
							<g:set var="highlight" value="${entry.value[2]}"/>
							<tr>
								
								<td><ui:listIcon type="${currentTitle.titletype}"/>
								<strong><g:link action="show" controller="tipp" id="${currentTitle.id}">${entry.key}</g:link></strong>
								<i onclick="showMore('${currentTitle.id}')" class="icon-info-sign"></i>

								<g:each in="${currentTitle?.ids?.sort{it?.ns?.ns}}" var="id">
				                    <br />${id.ns.ns}: ${id.value}
				                </g:each>
								</td>
							
								<g:if test="${subAIE}">		
									<td class="${highlight }"><laser:render template="compare_cell" model="[obj:subAIE]"/></td>
								</g:if>
								<g:else><td></td></g:else>
								
								<g:if test="${subBIE}">			
									<td class="${highlight }"><laser:render template="compare_cell" model="[obj:subBIE]"/></td>
								</g:if>
								<g:else><td></td></g:else>
							</tr>							
						</g:each>						
					</tbody>
				</table>

		        <ui:paginate  action="compare" controller="subscription" params="${params}" maxsteps="${max}" total="${unionListSize}" />

				</div>
			</g:if>

		%{-- Hiding the tables from compare_details inside the main table, breaks the modal hide.
 --}%

 <g:each in="${comparisonMap}" var="entry">
		<g:set var="subAIE" value="${entry.value[0]}"/>
		<g:set var="subBIE" value="${entry.value[1]}"/>
		<g:set var="currentTitle" value="${subAIE?.tipp?.title ?:subBIE?.tipp?.title}"/>

		<laser:render template="compare_details"
		 model="[subA:subAIE,subB:subBIE,currentTitle:currentTitle, subAName:subInsts.get(0).name, subBName:subInsts.get(1).name]"/>
</g:each>

		<laser:script file="${this.getGroovyPageFileName()}">
			$("#subSelectA-wrapper, #subSelectB-wrapper").dropdown({
				apiSettings: {
				    url: "${createLink([controller: "ajaxJson", action: "lookupSubscriptions"])}",
				    cache: false
				},
				clearable: true
			});
		</laser:script>
<laser:htmlEnd />
