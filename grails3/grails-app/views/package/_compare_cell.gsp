 <table class="ui celled la-js-responsive-table la-table table">
	<thead>
	<tr>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_start')}</th>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_end')}</th>
	</tr>
	</thead>
	<tbody>
		<tr>
			<td style="white-space: nowrap;border:1px solid #dddddd;">
			  ${message(code:'default.date.label')}: <g:formatDate formatName="default.date.format.notime" date="${obj.startDate}"/> <br />
			  ${message(code:'tipp.startVolume')}: ${obj.startVolume} <br />
			  ${message(code:'tipp.startIssue')}: ${obj.startIssue}
			</td>	

			<td style="white-space: nowrap;border:1px solid #dddddd;"> 
			   ${message(code:'default.date.label')}: <g:formatDate formatName="default.date.format.notime" date="${obj.endDate}"/> <br />
			   ${message(code:'tipp.endVolume')}: ${obj.endVolume} <br />
			   ${message(code:'tipp.endIssue')}: ${obj.endIssue}
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
