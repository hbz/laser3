<div class="ui fluid segment content" data-ajaxTargetWrap="true">
    <div class="ui stackable grid" data-ajaxTarget="true">

        <laser:render template="/templates/title_long_accordion"
                      model="${[ie         : ie, tipp: tipp,
                                showPackage: true, showPlattform: true, showCompact: showCompact, showEmptyFields: showEmptyFields]}"/>




        <div class="three wide column">
            <div class="ui list la-label-list">
                <g:if test="${tipp.accessStartDate}">
                    <div class="ui label la-label-accordion">${message(code: 'tipp.access')}</div>

                    <div class="item">
                        <div class="content">
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${tipp.accessStartDate}"/>
                        </div>
                    </div>

                </g:if>
                <g:if test="${tipp.accessEndDate}">
                    <!-- bis -->
                    <!-- DEVIDER  -->
                    <ui:dateDevider/>
                    <div class="item">
                        <div class="content">
                            <g:formatDate
                                    format="${message(code: 'default.date.format.notime')}"
                                    date="${tipp.accessEndDate}"/>
                        </div>
                    </div>
                </g:if>

            <%-- Coverage Details START --%>
                <g:each in="${tipp.coverages}" var="covStmt" status="counterCoverage">
                    <g:if test="${covStmt.coverageNote || covStmt.coverageDepth || covStmt.embargo}">
                        <div class="ui label la-label-accordion">${message(code: 'tipp.coverageDetails')} ${counterCoverage > 0 ? counterCoverage++ + 1 : ''}</div>
                    </g:if>
                    <g:if test="${covStmt.coverageNote}">
                        <div class="item">
                            <i class="grey icon quote right la-popup-tooltip la-delay"
                               data-content="${message(code: 'default.note.label')}"></i>

                            <div class="content">
                                <div class="header">
                                    ${message(code: 'default.note.label')}
                                </div>

                                <div class="description">
                                    ${covStmt.coverageNote}
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${covStmt.coverageDepth}">
                        <div class="item">
                            <i class="grey icon file alternate right la-popup-tooltip la-delay"
                               data-content="${message(code: 'tipp.coverageDepth')}"></i>

                            <div class="content">
                                <div class="header">
                                    ${message(code: 'tipp.coverageDepth')}
                                </div>

                                <div class="description">
                                    ${covStmt.coverageDepth}
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <g:if test="${covStmt.embargo}">
                        <div class="item">
                            <i class="grey icon hand paper right la-popup-tooltip la-delay"
                               data-content="${message(code: 'tipp.embargo')}"></i>

                            <div class="content">
                                <div class="header">
                                    ${message(code: 'tipp.embargo')}
                                </div>

                                <div class="description">
                                    ${covStmt.embargo}
                                </div>
                            </div>
                        </div>
                    </g:if>
                </g:each>
            <%-- Coverage Details END --%>
            </div>
        </div>
    </div>
</div>
