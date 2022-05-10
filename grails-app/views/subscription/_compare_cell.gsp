 <table class="ui celled la-js-responsive-table la-table table">
	<thead>
	<tr>
		<th>${message(code:'subscription.details.coverageStartDate')}</th>
		<th>${message(code:'subscription.details.coverageStartDate')}</th>
	</tr>
	</thead>
	<tbody>
		<tr>
			<td style="white-space: nowrap">
			  ${message(code:'default.date.label')}:
			  <g:if test="${obj.startDate != null}">
			  	<g:formatDate format="${message(code:'default.date.format.notime')}" date="${obj.startDate}"/> <br />
			  </g:if> <g:else>
			  	<g:formatDate format="${message(code:'default.date.format.notime')}" date="${obj.tipp.startDate}"/> <br />
			  </g:else>
  			  ${message(code:'tipp.startVolume')}:
  			  <g:if test="${obj.startVolume != null}">
  			  	 ${obj.startVolume} <br />
  			  </g:if> <g:else>
  			  	 ${obj.tipp.startVolume} <br />
  			  </g:else>
			  ${message(code:'tipp.startIssue')}:
			  <g:if test="${obj.startIssue != null}">
			  	${obj.startIssue}
			  </g:if> <g:else>
			  	${obj.tipp.startIssue}
			  </g:else>
     
			</td>	

			<td style="white-space: nowrap"> 
				${message(code:'default.date.label')}:
			  <g:if test="${obj.endDate != null}">
			  	<g:formatDate format="${message(code:'default.date.format.notime')}" date="${obj.endDate}"/> <br />
			  </g:if> <g:else>
			  	<g:formatDate format="${message(code:'default.date.format.notime')}" date="${obj.tipp.endDate}"/> <br />
			  </g:else>
			  ${message(code:'tipp.endVolume')}:
  			  <g:if test="${obj.endVolume != null}">
  			  	${obj.endVolume} <br />
  			  </g:if> <g:else>
  			  	${obj.tipp.endVolume} <br />
  			  </g:else>
  			  ${message(code:'tipp.endIssue')}:
			  <g:if test="${obj.endIssue != null}">
			  	${obj.endIssue} 
			  </g:if> <g:else>
			  	${obj.tipp.endIssue} 
			  </g:else>
	
			</td>
		</tr>
		<tr >
			<td colspan="2">${message(code:'default.note.label')}:
			  <g:if test="${obj.endIssue != null}"> 
			  ${obj.coverageNote}</td>
			  </g:if> <g:else>
			  	${obj.tipp.coverageNote}</td>
			  </g:else>
			
		</tr>
	</tbody>
</table>
