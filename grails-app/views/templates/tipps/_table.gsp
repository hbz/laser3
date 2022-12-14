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
                                <%-- My Area START--%>
%{--                                <div class="seven wide column">
                                    <i class="grey icon circular inverted fingerprint la-icon-absolute la-popup-tooltip la-delay"
                                       data-content="${message(code: 'tipp.tooltip.myArea')}"></i>

                                    <div class="ui la-segment-with-icon">

                                        <laser:render template="/templates/tipps/coverages_accordion"
                                                      model="${[ie: ie, tipp: tipp]}"/>

                                        <div class="ui list">
                                            <g:if test="${ie}">
                                                <div class="item">
                                                    <i class="grey save icon la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'issueEntitlement.perpetualAccessBySub.label')}"></i>

                                                    <div class="content">
                                                        <div class="header">
                                                            ${showCompact ? '' : message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}
                                                        </div>

                                                        <div class="description">
                                                            <ui:xEditableBoolean owner="${subscription}"
                                                                                 field="hasPerpetualAccess"/>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="item">
                                                    <i class="grey icon edit la-popup-tooltip la-delay"
                                                       data-content="${message(code: 'issueEntitlement.myNotes')}"></i>
                                                    <div class="content">
                                                        <div class="header"><g:message code="issueEntitlement.myNotes"/></div>
                                                        <div class="description">
                                                            <ui:xEditable owner="${ie}" type="text"
                                                                          field="notes"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </g:if>

                                            <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                                <div class="item">
                                                    <i class="money grey icon la-popup-tooltip la-delay"></i>

                                                    <div class="content">
                                                        <div class="header"><g:message
                                                                code="tipp.price.localPrice"/>:</div>

                                                        <div class="description">
                                                            <ui:xEditable field="localPrice"
                                                                          owner="${priceItem}"/> <ui:xEditableRefData
                                                                field="localCurrency"
                                                                owner="${priceItem}"
                                                                config="Currency"/>
                                                            <g:if test="${editable}">
                                                                <span class="right floated">
                                                                    <g:link controller="subscription"
                                                                            action="removePriceItem"
                                                                            params="${[priceItem: priceItem.id, id: subscription.id]}"
                                                                            class="ui compact icon button tiny"><i
                                                                            class="ui icon minus"
                                                                            data-content="Preis entfernen"></i></g:link>
                                                                </span>
                                                            </g:if>
                                                        </div>

                                                    </div>
                                                </div>

                                                <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                            </g:each>
                                            <g:if test="${editable && ie.priceItems.size() < 1}">
                                                <g:link action="addEmptyPriceItem"
                                                        class="ui tiny button"
                                                        params="${[ieid: ie.id, id: subscription.id]}">
                                                    <i class="money icon"></i>${message(code: 'subscription.details.addEmptyPriceItem.info')}
                                                </g:link>
                                            </g:if>

                                        <%-- GROUPS START--%>
                                            <g:if test="${subscription.ieGroups.size() > 0}">
                                                <g:each in="${ie.ieGroups.sort { it.ieGroup.name }}" var="titleGroup">
                                                    <div class="item">
                                                        <i class="grey icon object group la-popup-tooltip la-delay"
                                                           data-content="${message(code: 'issueEntitlementGroup.label')}"></i>
                                                        <div class="content">
                                                            <div class="header"><g:message code="subscription.details.ieGroups"/></div>
                                                            <div class="description"><g:link controller="subscription" action="index"
                                                                                             id="${subscription.id}"
                                                                                             params="[titleGroup: titleGroup.ieGroup.id]">${titleGroup.ieGroup.name}</g:link>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <g:if test="${editable}">
                                                        <g:link action="editEntitlementGroupItem"
                                                                params="${[cmd: 'edit', ie: ie.id, id: subscription.id]}"
                                                                class="ui tiny button">
                                                            <i class="object group icon"></i>${message(code: 'subscription.details.ieGroups.edit')}
                                                        </g:link>
                                                    </g:if>
                                                </g:each>
                                            </g:if>


                                        <%-- GROUPS END--%>
                                        </div>
                                    </div>
                                </div>--}%
                                <%-- My Area END ---%>
                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    </div>
</g:if>


<table class="ui sortable celled la-js-responsive-table la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortname"
                          title="${message(code: 'title.label')}"/>
        <th class="two wide">${message(code: 'tipp.coverage')}</th>
        <th class="two wide">${message(code: 'tipp.access')}</th>
        <th class="two wide">${message(code: 'tipp.price')}</th>
    </tr>
    <tr>
        <th colspan="2" rowspan="2"></th>
        <th>${message(code: 'default.from')}</th>
        <th>${message(code: 'default.from')}</th>
    </tr>
    <tr>
        <th>${message(code: 'default.to')}</th>
        <th>${message(code: 'default.to')}</th>
    </tr>
    </thead>
    <tbody>

    <g:set var="counter" value="${(offset ?: 0) + 1}"/>
    <g:each in="${tipps}" var="tipp">
        <tr>
            <td>${counter++}</td>
            <td>
                <!-- START TEMPLATE -->
                <laser:render template="/templates/title_short"
                          model="${[ie: null, tipp: tipp,
                                    showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false]}"/>
                <!-- END TEMPLATE -->
            </td>

            <td class="coverageStatements la-js-responsive-table la-tableCard">

                <laser:render template="/templates/tipps/coverages" model="${[ie: null, tipp: tipp]}"/>

            </td>
            <td>
                <!-- von -->
                <g:formatDate date="${tipp.accessStartDate}" format="${message(code: 'default.date.format.notime')}"/>
                <ui:dateDevider/>
                <!-- bis -->
                <g:formatDate date="${tipp.accessEndDate}" format="${message(code: 'default.date.format.notime')}"/>
            </td>
            <td>
                <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                    <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice"
                                                                         owner="${priceItem}"
                                                                         format=""/> <ui:xEditableRefData
                        field="listCurrency" owner="${priceItem}"
                        config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                <%--<ui:xEditable field="startDate" type="date"
                                 owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                    field="endDate" type="date"
                    owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                    <g:if test="${i < tipp.priceItems.size() - 1}"><hr></g:if>
                </g:each>
            </td>
        </tr>

    </g:each>

    </tbody>

</table>