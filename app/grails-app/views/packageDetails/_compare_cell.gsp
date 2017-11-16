 <table class="ui celled table">
	<thead>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_start', default:'Coverage Start')}</th>
		<th style="border:1px solid #dddddd;">${message(code:'tipp.coverage_end', default:'Coverage End')}</th>
	</thead>
	<tbody>
		<tr>
			<td style="white-space: nowrap;border:1px solid #dddddd;">
			  ${message(code:'default.date.label', default:'Date')}: <g:formatDate formatName="default.date.format.notime" date="${obj.startDate}"/> <br/>
			  ${message(code:'tipp.volume', default:'Volume')}: ${obj.startVolume} <br/>
			  ${message(code:'tipp.issue', default:'Issue')}: ${obj.startIssue}
			</td>	

			<td style="white-space: nowrap;border:1px solid #dddddd;"> 
			   ${message(code:'default.date.label', default:'Date')}: <g:formatDate formatName="default.date.format.notime" date="${obj.endDate}"/> <br/>
			   ${message(code:'tipp.volume', default:'Volume')}: ${obj.endVolume} <br/>
			   ${message(code:'tipp.issue', default:'Issue')}: ${obj.endIssue}
			</td>
		</tr>
		<tr>
			<td style="border:1px solid #dddddd;" colspan="2">${message(code:'tipp.coverageNote', default:'Coverage Note')}: ${obj.coverageNote}</td>
		</tr>
		<tr>
			<td style="border:1px solid #dddddd;" colspan="2">${message(code:'platform.label', default:'Host Platform')}: ${obj.platform.name}</td>
		</tr>
	</tbody>
</table>
