<%@ page import="de.laser.IssueEntitlementCoverage" %>
<g:set var="overwriteEditable" value="${(overwriteEditable == null) ? editable : overwriteEditable}"/>


<div class="ui list la-label-list">
    <g:if test="${(covStmt.startDate || covStmt.startVolume || covStmt.startIssue) && !objectTypeIsIE}">
        <div class="item">
            <div class="ui label">${message(code: 'tipp.coverage')}</div>
        </div>
    </g:if>
    <g:elseif test="${objectTypeIsIE}">
        <div class="item">
            <div class="ui label">${message(code: 'tipp.myCoverage')}</div>
        </div>
    </g:elseif>

    <g:if test="${covStmt.startDate || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.startDate.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.startDate')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" type="date" field="startDate"
                                  overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>
    <g:if test="${covStmt.startVolume || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                data-content="${message(code: 'tipp.startVolume.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.startVolume')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="startVolume" overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>
    <g:if test="${covStmt.startIssue || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.startIssue.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.startIssue')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="startIssue" overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>


    <g:if test="${(covStmt.endDate || covStmt.endVolume || covStmt.endIssue) && !objectTypeIsIE}">
        <ui:dateDevider/>
    </g:if>
    <g:elseif test="${objectTypeIsIE}">
        <ui:dateDevider/>
    </g:elseif>

    <g:if test="${covStmt.endDate || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted calendar alternate icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.endDate.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.endDate')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" type="date" field="endDate"
                                  overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>
    <g:if test="${covStmt.endVolume || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted la-books icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.endVolume.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.endVolume')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="endVolume" overwriteEditable="${overwriteEditable}"/>
                </div>
            </div>
        </div>
    </g:if>
    <g:if test="${covStmt.endIssue || objectTypeIsIE}">
        <div class="item">
            <i class="grey fitted la-notebook icon la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.endIssue.tooltip')}"></i>

            <div class="content">
                <div class="header">
                    ${message(code: 'tipp.endIssue')}
                </div>

                <div class="description">
                    <ui:xEditable owner="${covStmt}" field="endIssue" overwriteEditable="${overwriteEditable}"/>
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

