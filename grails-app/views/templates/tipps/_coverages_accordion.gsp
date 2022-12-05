%{-- BOOK  --}%
<g:if test="${(tipp.titleType == 'Book')}">
    %{-- IE  --}%
    <g:if test="${ie}">
        <div class="ui list la-label-list">
            <div class="item">
                <div class="content">

                    <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}</div>

                    <div class="description">
                    <!-- von --->
                        <g:if test="${editable}">
                            <ui:xEditable owner="${ie}" type="date"
                                          field="accessStartDate"/>
                            <i class="grey question circle icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                        </g:if>
                        <g:else>
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${ie.accessStartDate}"/>
                        </g:else>

                    </div>
                </div>
            </div>

            <div class="item">
                <div class="content">
                    <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}</div>

                    <div class="description">
                    <!-- bis -->
                        <g:if test="${editable}">
                            <ui:xEditable owner="${ie}" type="date"
                                          field="accessEndDate"/>
                            <i class="grey question circle icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                        </g:if>
                        <g:else>
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${ie.accessEndDate}"/>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    %{-- TIPP  --}%
    <g:else>
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
    </g:else>
</g:if>

%{-- JOURNAL  --}%
<g:elseif test="${tipp.titleType == "Journal"}">
    <div class="ui stackable grid"></div>

    %{-- IE  --}%
    <g:if test="${ie}">
        <div class="ui stackable grid">
            <div class="eleven wide column">
                <%
                    Map<String, Object> paramData = [:]
                    if (params.sort && params.order) {
                        paramData.sort = params.sort
                        paramData.order = params.order
                    }
                    if (params.max && params.offset) {
                        paramData.max = params.max
                        paramData.offset = params.offset
                    }
                    paramData.putAll(params)
                %>
                <g:each in="${ie.coverages}" var="covStmt">
                    <laser:render template="/templates/tipps/coverageStatement_accordion"
                                  model="${[covStmt: covStmt, paramData: paramData, showEmbargo: true, objectTypeIsIE: true, overwriteEditable: overwriteEditable]}"/>
                </g:each>
            </div>
            <div class="five wide column">
                <div class="ui list la-label-list">
                    <div class="item">
                        <div class="content">

                            <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}</div>

                            <div class="description">
                            <!-- von --->
                                <g:if test="${editable}">
                                    <ui:xEditable owner="${ie}" type="date"
                                                  field="accessStartDate"/>
                                    <i class="grey question circle icon la-popup-tooltip la-delay"
                                       data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                                </g:if>
                                <g:else>
                                    <g:formatDate
                                            format="${message(code: 'default.date.format.notime')}"
                                            date="${ie.accessStartDate}"/>
                                </g:else>

                            </div>
                        </div>
                    </div>

                    <div class="item">
                        <div class="content">
                            <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}</div>

                            <div class="description">
                            <!-- bis -->
                                <g:if test="${editable}">
                                    <ui:xEditable owner="${ie}" type="date"
                                                  field="accessEndDate"/>
                                    <i class="grey question circle icon la-popup-tooltip la-delay"
                                       data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                                </g:if>
                                <g:else>
                                    <g:formatDate
                                            format="${message(code: 'default.date.format.notime')}"
                                            date="${ie.accessEndDate}"/>
                                </g:else>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
%{--        <g:if test="${editable}">
            <br/>
            <g:link action="addCoverage" params="${paramData + [issueEntitlement: ie.id]}"
                    class="ui compact icon button positive tiny"><i class="ui icon plus"
                                                                    data-content="${message(code: 'subscription.details.addCoverage')}"></i></g:link>
        </g:if>--}%
    </g:if>

    %{-- TIPP  --}%
    <g:else>
        <g:each in="${tipp.coverages}" var="covStmt">

            <laser:render template="/templates/tipps/coverageStatement_accordion"
                          model="${[covStmt: covStmt , showEmbargo: false, objectTypeIsIE: false]}"/>

        </g:each>
    </g:else>
</g:elseif>

%{-- DATABASE  --}%
<g:else>
    %{-- IE  --}%
    <g:if test="${ie}">
        <div class="ui list la-label-list">
            <div class="item">
                <div class="content">

                    <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.from')}</div>

                    <div class="description">
                    <!-- von --->
                        <g:if test="${editable}">
                            <ui:xEditable owner="${ie}" type="date"
                                          field="accessStartDate"/>
                            <i class="grey question circle icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                        </g:if>
                        <g:else>
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${ie.accessStartDate}"/>
                        </g:else>

                    </div>
                </div>
            </div>

            <div class="item">
                <div class="content">
                    <div class="ui label">${message(code: 'subscription.details.access_dates')} ${message(code: 'default.to')}</div>

                    <div class="description">
                    <!-- bis -->
                        <g:if test="${editable}">
                            <ui:xEditable owner="${ie}" type="date"
                                          field="accessEndDate"/>
                            <i class="grey question circle icon la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                        </g:if>
                        <g:else>
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${ie.accessEndDate}"/>
                        </g:else>
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    %{-- TIPP  --}%
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
</g:else>