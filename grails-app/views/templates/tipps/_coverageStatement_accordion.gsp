<%@ page import="de.laser.IssueEntitlementCoverage" %>
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}"/>


<div class="ui list la-label-list">
    <g:if test="${(covStmt.startDate || covStmt.startVolume || covStmt.startIssue) && !objectTypeIsIE}">
        <div class="item">
            <div class="ui label">${message(code: 'tipp.coverage')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
        </div>
    </g:if>
    <g:elseif test="${objectTypeIsIE}">
        <div class="item">
            <div class="ui label">${message(code: 'tipp.myCoverage')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
        </div>
    </g:elseif>

    <div class="la-onliner">
        <g:if test="${covStmt.startDate || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.startDate.tooltip')}"></i>
                <ui:xEditable owner="${covStmt}" type="date" field="startDate"
                              overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.startVolume || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.startVolume.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Volume.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="startVolume" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.startIssue || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.startIssue.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Issue.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="startIssue" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>


        <g:if test="${(covStmt.endDate || covStmt.endVolume || covStmt.endIssue) && !objectTypeIsIE}">
            <ui:dateDevider/>
        </g:if>
        <g:elseif test="${objectTypeIsIE}">
            <ui:dateDevider/>
        </g:elseif>

        <g:if test="${covStmt.endDate || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.endDate.tooltip')}"></i>
                <ui:xEditable owner="${covStmt}" type="date" field="endDate"
                              overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.endVolume || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.endVolume.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Volume.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="endVolume" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.endIssue || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.endIssue.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Issue.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="endIssue" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
    </div>
    <g:if test="${showEmbargo}">
        <div class="item">
            <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.embargo')}
                </div>
                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="embargo" overwriteEditable="true"/>
                </div>
            </div>
        </div>
    </g:if>
%{--    <g:if test="${overwriteEditable && (covStmt instanceof IssueEntitlementCoverage) && subscription}">
        <span class="right floated">
            <g:link controller="subscription" action="removeCoverage"
                    params="${paramData + [ieCoverage: covStmt.id, id: subscription.id]}"
                    class="ui compact icon button negative tiny removeCoverage"><i class="ui icon minus"
                                                                                   data-content="Abdeckung entfernen"></i></g:link>
        </span>
    </g:if>--}%
</div>

