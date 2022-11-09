
<g:if test="${(tipp.titleType == 'Book')}">
    <g:if test="${tipp.dateFirstInPrint}">
        <div class="ui label">${message(code: 'tipp.dateFirstInPrint')}</div>
        <div class="description">

                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.dateFirstInPrint')}"></i>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstInPrint}"/>
        </div>
    </g:if>
    <g:if test="${tipp.dateFirstOnline}">
        <div class="ui label">${message(code: 'tipp.dateFirstOnline')}</div>
        <div class="description">
                    <span class='ui grey horizontal divider la-date-devider'></span>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                       data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                  date="${tipp.dateFirstOnline}"/>
        </div>
    </g:if>
</g:if>

<g:elseif test="${tipp.titleType == "Journal"}">
    <div class="ui label">${message(code: 'tipp.dateFirstInPrint')}</div>
    <div class="description">
        <g:if test="${ie}">

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

                    <laser:render template="/templates/tipps/coverageStatement_accordion" model="${[covStmt: covStmt, paramData: paramData]}"/>

            </g:each>


            <g:if test="${editable}">
                <br/>
                <g:link action="addCoverage" params="${paramData+[issueEntitlement: ie.id]}"
                        class="ui compact icon button positive tiny"><i class="ui icon plus"
                                                                        data-content="${message(code: 'subscription.details.addCoverage')}"></i></g:link>
            </g:if>

    </g:if>
    <g:else>

            <g:each in="${tipp.coverages}" var="covStmt">

                    <laser:render template="/templates/tipps/coverageStatement" model="${[covStmt: covStmt]}"/>

            </g:each>

    </g:else>
    </div>
</g:elseif>
<g:else>
    <g:if test="${tipp.dateFirstOnline}">
        <div class="ui label">${message(code: 'tipp.dateFirstOnline')}</div>
        <div class="description">

                <i class="grey fitted la-books icon la-popup-tooltip la-delay"
                   data-content="${message(code: 'tipp.dateFirstOnline')}"></i>
                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                              date="${tipp.dateFirstOnline}"/>
        </div>
    </g:if>

</g:else>