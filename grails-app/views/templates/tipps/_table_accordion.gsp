<%@ page import="de.laser.remote.ApiSource; de.laser.Platform; de.laser.titles.BookInstance" %>
<g:set var="counter" value="${(offset ?: 0) + 1}"/>
<g:if test="${tipps}">
    <div class="ui fluid card">
        <div class="content">
            <div class="ui accordion la-accordion-showMore">
                <g:each in="${tipps}" var="tipp">
                    <div class="ui raised segments la-accordion-segments">
                        <div class="ui fluid segment title" data-ajaxTippId="${tipp.id}" data-ajaxIeId="${ie ? ie.id : null}">
                            <div class="ui stackable equal width grid">

                                <div class="one wide column">
                                    ${counter++}
                                </div>

                                <div class="column">
                                    <div class="ui list">

                                        <!-- START TEMPLATE -->
                                        <laser:render template="/templates/title_short_accordion"
                                                      model="${[ie: null, tipp: tipp,
                                                                showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false]}"/>
                                        <!-- END TEMPLATE -->

                                    </div>
                                </div>

                                <div class="column">
                                    <laser:render template="/templates/tipps/coverages_accordion"
                                                  model="${[ie: null, tipp: tipp, overwriteEditable: false]}"/>
                                </div>

                                <div class="four wide column">

                                    <!-- START TEMPLATE -->
                                    <laser:render template="/templates/identifier"
                                                  model="${[ie: null, tipp: tipp]}"/>
                                    <!-- END TEMPLATE -->
                                </div>

                                <div class="two wide column">
                                    <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                                        <g:if test="${priceItem.listCurrency}">
                                            <div class="ui list">
                                                <div class="item">
                                                    <div class="contet">
                                                        <div class="header"><g:message code="tipp.price.listPrice"/></div>
                                                        <div class="content"><g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}"
                                                                                             currencySymbol="${priceItem.listCurrency.value}"/>
                                                        </div>



                                                    </div>
                                                </div>
                                            </div>


                                        </g:if>
                                    </g:each>
                                </div>

                                <div class="one wide column">
                                    <div class="ui right floated buttons">
                                        <div class="right aligned wide column">

                                        </div>

                                        <div class="ui icon blue button la-modern-button "><i
                                                class="ui angle double down icon"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

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
                                                    <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'default.note.label')}"></i>
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
                                                    <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>
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
                                                    <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>
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
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</g:if>
