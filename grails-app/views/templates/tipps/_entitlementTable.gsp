<%@page import="de.laser.titles.BookInstance; de.laser.helper.RDStore; de.laser.ApiSource" %>
<div class="eight wide column">
    <g:set var="counter" value="${1}"/>
    <g:set var="sumlistPrice" value="${0}"/>
    <g:set var="sumlocalPrice" value="${0}"/>

    <g:if test="${side == 'target' && targetInfoMessage}">
        <h3 class="ui header center aligned"><g:message code="${targetInfoMessage}" /></h3>
    </g:if>

    <g:if test="${side == 'source' && sourceInfoMessage}">
        <h3 class="ui header center aligned"><g:message code="${sourceInfoMessage}" /></h3>
    </g:if>


    <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header" id="${side}">
        <thead>
            <tr>
                <th>
                    <g:if test="${editable}">
                        <input class="select-all" type="checkbox" name="chkall">
                    </g:if>
                </th>
                <th>${message(code: 'sidewide.number')}</th>
                <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortname" title="${message(code: 'title.label')}"/>
                <th class="two wide"><g:message code="tipp.price"/></th>
                <th class="two wide"><g:message code="default.actions.label"/></th>
            </tr>
        </thead>
        <tbody>


            <g:each in="${ies.sourceIEs}" var="ie">
                <g:set var="tipp" value="${ie.tipp}"/>
                <g:set var="isContainedByTarget" value="${ies.targetIEs.find { it.tipp == tipp && !(it.status in [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED])}}" />
                <g:set var="targetIE" value="${ies.targetIEs.find { it.tipp == tipp}}" />
                <g:if test="${side == 'source' || (side == 'target' && isContainedByTarget)}">
                    <tr data-gokbId="${tipp.gokbId}" data-ieId="${ie?.id}" data-index="${counter}">
                        <td>

                            <g:if test="${!isContainedByTarget && editable}">
                                <input type="checkbox" name="bulkflag" data-index="${tipp.gokbId}" class="bulkcheck">
                            </g:if>

                        </td>
                        <td>${counter++}</td>
                        <td class="titleCell">
                            <g:if test="${side == 'target' && targetIE}">
                                 <semui:ieAcceptStatusIcon status="${targetIE?.acceptStatus}"/>
                            </g:if>
                            <g:else>
                                <div class="la-inline-flexbox la-popup-tooltip la-delay">
                                    <i class="icon"></i>
                                </div>
                            </g:else>

                            <!-- START TEMPLATE -->
                                <g:render template="/templates/title_short"
                                          model="${[ie: ie, tipp: ie.tipp,
                                                    showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                            <!-- END TEMPLATE -->
                        </td>
                        <td>
                            <g:if test="${side == 'source'}">
                                <g:if test="${ie?.priceItems}">
                                    <g:each in="${ie.priceItems}" var="priceItem" status="i">
                                        <g:message code="tipp.price.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                             owner="${priceItem}"
                                                                                             format=""/> <semui:xEditableRefData
                                            field="listCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                        <g:message code="tipp.price.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                              owner="${priceItem}"/> <semui:xEditableRefData
                                            field="localCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                    <%--<semui:xEditable field="startDate" type="date"
                                                     owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                                        field="endDate" type="date"
                                        owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                        <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                        <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                        <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                                    </g:each>
                                </g:if>
                            </g:if>
                            <g:if test="${side == 'target'}">
                                <g:if test="${targetIE?.priceItem}">
                                    <g:each in="${targetIE.priceItems}" var="priceItem" status="i">
                                        <g:message code="tipp.price.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                             owner="${priceItem}"
                                                                                             format=""/> <semui:xEditableRefData
                                            field="listCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                        <g:message code="tipp.price.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                              owner="${priceItem}"/> <semui:xEditableRefData
                                            field="localCurrency" owner="${priceItem}"
                                            config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                    <%--<semui:xEditable field="startDate" type="date"
                                                     owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                                        field="endDate" type="date"
                                        owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                        <g:if test="${i < targetIE.priceItems.size() - 1}"><hr></g:if>
                                        <g:set var="sumlistPrice" value="${sumlistPrice + (priceItem.listPrice ?: 0)}"/>
                                        <g:set var="sumlocalPrice" value="${sumlocalPrice + (priceItem.localPrice ?: 0)}"/>
                                    </g:each>
                                </g:if>
                            </g:if>
                        </td>
                        <td>

                                <g:if test="${side == 'target' && isContainedByTarget && targetIE?.acceptStatus == RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION && editable}">
                                    <g:link class="ui icon negative button la-popup-tooltip la-delay" action="processRemoveEntitlements"
                                            params="${[id: subscription.id, singleTitle: tipp.gokbId, packageId: packageId]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${side == 'source' && !isContainedByTarget && editable}">
                                    <g:link class="ui icon button blue la-modern-button la-popup-tooltip la-delay" action="processAddEntitlements"
                                            params="${[id: subscription.id, singleTitle: tipp.gokbId]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                        <i class="plus icon"></i>
                                    </g:link>
                                </g:elseif>

                        </td>
                    </tr>
                </g:if>
                <g:else>
                    <tr data-gokbId="${tipp.gokbId}" data-ieId="${ie?.id}" data-empty="true" data-index="${counter}">
                        <td></td>
                        <td>${counter++}</td>
                        <td class="titleCell"></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:else>
            </g:each>
        </tbody>
        <tfoot>
            <tr>
                <th></th>
                <th></th>
                <th></th>
                <th><g:message code="financials.export.sums"/> <br />
                    <g:message code="tipp.price.listPrice"/>: <g:formatNumber number="${sumlistPrice}" type="currency"/><br />
                    %{--<g:message code="tipp.price.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                </th>
                <th></th>
            </tr>
        </tfoot>
    </table>
</div>
