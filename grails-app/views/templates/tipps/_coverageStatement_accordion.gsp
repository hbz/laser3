<%@ page import="de.laser.ui.Icon; de.laser.IssueEntitlementCoverage" %>
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}"/>


<div class="ui list la-label-list">
    <g:if test="${(covStmt.startDate || covStmt.startVolume || covStmt.startIssue) && !objectTypeIsIE}">
        <div class="ui label la-labelBlock">${message(code: 'tipp.coverage')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
    </g:if>
    <g:elseif test="${objectTypeIsIE}">
        <div class="ui label la-labelBlock">${message(code: 'tipp.myCoverage')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
    </g:elseif>

    <div class="la-onliner">
        <g:if test="${covStmt.startDate || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted ${Icon.SYM.DATE} la-popup-tooltip"
                   data-content="${message(code: 'tipp.startDate.tooltip')}"></i>
                <ui:xEditable owner="${covStmt}" type="date" field="startDate"
                              overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.startVolume || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-books icon la-popup-tooltip"
                   data-content="${message(code: 'tipp.startVolume.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Volume.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="startVolume" emptytext="-" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.startIssue || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-notebook icon la-popup-tooltip"
                   data-content="${message(code: 'tipp.startIssue.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Issue.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="startIssue" emptytext="-" overwriteEditable="${overwriteEditable}"/>
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
                <i class="grey fitted ${Icon.SYM.DATE} la-popup-tooltip"
                   data-content="${message(code: 'tipp.endDate.tooltip')}"></i>
                <ui:xEditable owner="${covStmt}" type="date" field="endDate"
                              overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.endVolume || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-books icon la-popup-tooltip"
                   data-content="${message(code: 'tipp.endVolume.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Volume.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="endVolume" emptytext="-" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
        <g:if test="${covStmt.endIssue || objectTypeIsIE}">
            <span class="la-online-wrap">
                <i class="grey fitted la-notebook icon la-popup-tooltip"
                   data-content="${message(code: 'tipp.endIssue.tooltip')}"></i>
                <span class="la-identifier">
                    ${message(code: 'tipp.Issue.short')}
                </span>
                <ui:xEditable owner="${covStmt}" field="endIssue" emptytext="-" overwriteEditable="${overwriteEditable}"/>
            </span>
        </g:if>
    </div>
    <g:if test="${showEmbargo}">
        <div class="item">
            <i class="grey icon hand paper right la-popup-tooltip" data-content="${message(code: 'tipp.embargo')}"></i>
            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.embargo')}
                </div>
                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="embargo" overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>
%{--    <g:if test="${overwriteEditable && (covStmt instanceof IssueEntitlementCoverage) && subscription}">
        <span class="right floated">
            <g:link controller="subscription" action="removeCoverage"
                    params="${paramData + [ieCoverage: covStmt.id, id: subscription.id]}"
                    class="${Btn.ICON.SIMPLE_TOOLTIP} negative compact tiny removeCoverage"
                    data-content="Abdeckung entfernen">
                        <i class="${Icon.CMD.REMOVE}"></i>
                    </g:link>
        </span>
    </g:if>--}%
</div>

