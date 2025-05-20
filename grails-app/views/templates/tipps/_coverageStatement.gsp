<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.IssueEntitlementCoverage" %>
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}" />
<div class="content">
    <div class="la-card-column">
        <div class="la-card-flexRow">
            <i class="grey fitted ${Icon.SYM.DATE} la-popup-tooltip" data-content="${message(code: 'tipp.startDate.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" type="date" field="startDate" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="la-card-flexRow">
            <i class="${Icon.ATTR.TIPP_COVERAGE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.startVolume.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" field="startVolume" emptytext="-" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="la-card-flexRow">
            <i class="${Icon.ATTR.TIPP_ISSUE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.startIssue.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" field="startIssue" emptytext="-" overwriteEditable="${overwriteEditable}"/>
        </div>
        <ui:dateDevider/>
        <!-- bis -->
        <div class="la-card-flexRow">
            <i class="grey fitted ${Icon.SYM.DATE} la-popup-tooltip" data-content="${message(code: 'tipp.endDate.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" type="date" field="endDate" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="la-card-flexRow">
            <i class="${Icon.ATTR.TIPP_COVERAGE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.endVolume.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" field="endVolume" emptytext="-" overwriteEditable="${overwriteEditable}"/>
        </div>
        <div class="la-card-flexRow">
            <i class="${Icon.ATTR.TIPP_ISSUE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.endIssue.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" field="endIssue" emptytext="-" overwriteEditable="${overwriteEditable}"/>
        </div>
    </div>
    <div class="la-card-column-with-row">
        <div class="la-card-row">
            <div class="la-card-flexRow">
                <i class="${Icon.ATTR.TIPP_COVERAGE_NOTE} la-popup-tooltip" data-content="${message(code: 'default.note.label')}"></i>
                <ui:xEditable owner="${covStmt}" field="coverageNote" overwriteEditable="${overwriteEditable}"/>
            </div>
            <div class="la-card-flexRow">
                <i class="${Icon.ATTR.TIPP_COVERAGE_DEPTH} la-popup-tooltip" data-content="${message(code: 'tipp.coverageDepth')}"></i>
                <ui:xEditable owner="${covStmt}" field="coverageDepth" overwriteEditable="${overwriteEditable}"/>
            </div>
            <div class="la-card-flexRow">
                <i class="${Icon.ATTR.TIPP_EMBARGO} la-popup-tooltip" data-content="${message(code: 'tipp.embargo')}"></i>
                <ui:xEditable owner="${covStmt}" field="embargo" overwriteEditable="${overwriteEditable}"/>
            </div>
        </div>
        <div class="la-card-row">
            <g:if test="${overwriteEditable && (covStmt instanceof IssueEntitlementCoverage) && subscription}">
                <span class="right floated" >
                    <g:link controller="subscription" action="removeCoverage" params="${paramData+ [ieCoverage: covStmt.id, id: subscription.id]}"
                            class="${Btn.ICON.SIMPLE_TOOLTIP} negative compact tiny removeCoverage"
                            data-content="Abdeckung entfernen">
                        <i class="${Icon.CMD.REMOVE}"></i>
                    </g:link>
                </span>
            </g:if>
        </div>
    </div>
</div>

