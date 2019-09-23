<div class="content">
    <div class="la-card-column">
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
        <semui:xEditable owner="${covStmt}" field="endIssue"/><br>
    </div>
    <div class="la-card-column-with-row">
        <div class="la-card-row">
            <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageNote')}"></i>
            <semui:xEditable owner="${covStmt}" field="coverageNote"/><br>
            <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>
            <semui:xEditable owner="${covStmt}" field="coverageDepth"/><br>
            <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
            <semui:xEditable owner="${covStmt}" field="embargo"/><br>
        </div>
        <div class="la-card-row">
            <span class="right floated" >
                <g:link controller="subscription" action="removeCoverage" params="${[ieCoverage: covStmt.id]}" class="ui compact icon button negative tiny removeCoverage"><i class="ui icon minus" data-content="Lizenzzeitraum entfernen"></i></g:link>
            </span>
        </div>
    </div>
</div>

