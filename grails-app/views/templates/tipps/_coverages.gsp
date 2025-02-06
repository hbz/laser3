<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${(tipp.titleType == 'monograph')}">
    <div class="ui card">
        <div class="content">

            <i class="${Icon.ATTR.TIPP_COVERAGE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.dateFirstInPrint')}"></i>
            <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.dateFirstInPrint}"/>
            <span class='ui grey horizontal divider la-date-devider'></span>
            <i class="${Icon.ATTR.TIPP_COVERAGE} fitted la-popup-tooltip" data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
            <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${tipp.dateFirstOnline}"/>
        </div>
    </div>
</g:if>
<g:elseif test="${tipp.titleType == "serial"}">
    <g:if test="${ie}">
        <div class="ui cards">
            <%
                Map<String, Object> paramData = [:]
                if(params.sort && params.order) {
                    paramData.sort = params.sort
                    paramData.order = params.order
                }
                if(params.max && params.offset) {
                    paramData.max = params.max
                    paramData.offset = params.offset
                }
                paramData.putAll(params)
            %>
            <g:each in="${ie.coverages}" var="covStmt">
                <div class="ui card">
                    <laser:render template="/templates/tipps/coverageStatement" model="${[covStmt: covStmt, paramData: paramData]}"/>
                </div>
            </g:each>


            <g:if test="${editable}">
                <br/>
                <g:link action="addCoverage" params="${paramData+[issueEntitlement: ie.id]}"
                        class="${Btn.ICON.SIMPLE_TOOLTIP} positive compact tiny"
                        data-content="${message(code: 'subscription.details.addCoverage')}">
                    <i class="${Icon.CMD.ADD}" ></i>
                </g:link>
            </g:if>
        </div>
    </g:if>
    <g:else>
        <div class="ui cards">
            <g:each in="${tipp.coverages}" var="covStmt">
                <div class="ui card">
                    <laser:render template="/templates/tipps/coverageStatement" model="${[covStmt: covStmt]}"/>
                </div>
            </g:each>
        </div>
    </g:else>
</g:elseif>
<g:else>
    <div class="ui card">
        <div class="content">
            <i class="${Icon.ATTR.TIPP_COVERAGE} fitted la-popup-tooltip"
               data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
            <g:formatDate format="${message(code: 'default.date.format.notime')}"
                          date="${tipp.dateFirstOnline}"/>
        </div>
    </div>
</g:else>