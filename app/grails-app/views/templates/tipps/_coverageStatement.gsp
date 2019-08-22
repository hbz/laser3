<semui:xEditable owner="${covStmt}" type="date" field="startDate"/><br>
<i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
<semui:xEditable owner="${covStmt}" field="startVolume"/><br>

<i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
<semui:xEditable owner="${covStmt}" field="startIssue"/>
<semui:dateDevider/>
<!-- bis -->
<semui:xEditable owner="${covStmt}" type="date" field="endDate"/><br>
<i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
<semui:xEditable owner="${covStmt}" field="endVolume"/><br>

<i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
<semui:xEditable owner="${covStmt}" field="endIssue"/>

<i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageNote')}"></i>
<semui:xEditable owner="${covStmt}" field="coverageNote"/>
<i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>
<semui:xEditable owner="${covStmt}" field="coverageDepth"/>
<i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
<semui:xEditable owner="${covStmt}" field="embargo"/>

<g:link controller="subscription" action="removeCoverage" params="${[ieCoverage: covStmt.id]}" class="ui button negative tiny removeCoverage"><i class="ui icon minus" data-content="Lizenzzeitraum entfernen"></i></g:link>