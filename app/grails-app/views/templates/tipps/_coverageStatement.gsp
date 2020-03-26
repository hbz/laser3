
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}" />
<%
    Map<String, Object> paramData = [ieCoverage: covStmt.id]
    if(params.sort && params.order) {
        paramData.sort = params.sort
        paramData.order = params.order
    }
    if(params.max && params.offset) {
        paramData.max = params.max
        paramData.offset = params.offset
    }
%>
<div class="content">
    <div class="la-card-column">
        <semui:xEditable owner="${covStmt}" type="date" field="startDate" overwriteEditable="${overwriteEditable}"/><br>
        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
        <semui:xEditable owner="${covStmt}" field="startVolume" overwriteEditable="${overwriteEditable}"/><br>

        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
        <semui:xEditable owner="${covStmt}" field="startIssue" overwriteEditable="${overwriteEditable}"/>
        <semui:dateDevider/>
        <!-- bis -->
        <semui:xEditable owner="${covStmt}" type="date" field="endDate" overwriteEditable="${overwriteEditable}"/><br>
        <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
        <semui:xEditable owner="${covStmt}" field="endVolume" overwriteEditable="${overwriteEditable}"/><br>

        <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.issue')}"></i>
        <semui:xEditable owner="${covStmt}" field="endIssue" overwriteEditable="${overwriteEditable}"/><br>
    </div>
    <div class="la-card-column-with-row">
        <div class="la-card-row">
            <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageNote')}"></i>
            <semui:xEditable owner="${covStmt}" field="coverageNote" overwriteEditable="${overwriteEditable}"/><br>
            <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>
            <semui:xEditable owner="${covStmt}" field="coverageDepth" overwriteEditable="${overwriteEditable}"/><br>
            <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
            <semui:xEditable owner="${covStmt}" field="embargo" overwriteEditable="${overwriteEditable}"/><br>
        </div>
        <div class="la-card-row">
            <g:if test="${overwriteEditable}">
                <span class="right floated" >
                    <g:link controller="subscription" action="removeCoverage" params="${paramData}" class="ui compact icon button negative tiny removeCoverage"><i class="ui icon minus" data-content="Lizenzzeitraum entfernen"></i></g:link>
                </span>
            </g:if>
        </div>
    </div>
</div>

