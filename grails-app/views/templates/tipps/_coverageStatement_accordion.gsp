<%@ page import="de.laser.IssueEntitlementCoverage" %>
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}" />
<div class="content">
    <div class="la-card-column">
        <div class="la-card-flexRow">
            <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.startDate.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" type="date" field="startDate" overwriteEditable="${overwriteEditable}"/>
        </div>
        <ui:dateDevider/>
        <!-- bis -->
        <div class="la-card-flexRow">
            <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay" data-content="${message(code: 'tipp.endDate.tooltip')}"></i>
            <ui:xEditable owner="${covStmt}" type="date" field="endDate" overwriteEditable="${overwriteEditable}"/>
        </div>
    </div>
    <div class="la-card-column-with-row">
        <div class="la-card-row">
            <g:if test="${overwriteEditable && (covStmt instanceof IssueEntitlementCoverage) && subscription}">
                <span class="right floated" >
                    <g:link controller="subscription" action="removeCoverage" params="${paramData+ [ieCoverage: covStmt.id, id: subscription.id]}" class="ui compact icon button negative tiny removeCoverage"><i class="ui icon minus" data-content="Abdeckung entfernen"></i></g:link>
                </span>
            </g:if>
        </div>
    </div>
</div>

