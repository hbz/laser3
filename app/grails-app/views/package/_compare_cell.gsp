 <table class="ui celled la-table table">
	<thead>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_start')}</th>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_end')}</th>
	</thead>
	<tbody>
		<tr>
			<td style="white-space: nowrap;border:1px solid #dddddd;">
			  ${message(code:'default.date.label')}: <g:formatDate formatName="default.date.format.notime" date="${obj.startDate}"/> <br/>
			  ${message(code:'tipp.volume')}: ${obj.startVolume} <br/>
			  ${message(code:'tipp.issue')}: ${obj.startIssue}
			</td>	

			<td style="white-space: nowrap;border:1px solid #dddddd;"> 
			   ${message(code:'default.date.label')}: <g:formatDate formatName="default.date.format.notime" date="${obj.endDate}"/> <br/>
			   ${message(code:'tipp.volume')}: ${obj.endVolume} <br/>
			   ${message(code:'tipp.issue')}: ${obj.endIssue}
			</td>
		</tr>
		<tr>
			<td style="border:1px solid #dddddd;" colspan="2">${message(code:'tipp.coverageNote')}: ${obj.coverageNote}</td>
		</tr>
		<tr>
			<td style="border:1px solid #dddddd;" colspan="2">${message(code:'platform.label')}: ${obj.platform.name}</td>
		</tr>
	</tbody>
</table>
