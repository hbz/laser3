<%@page import="de.laser.helper.RDStore; com.k_int.kbplus.BookInstance;com.k_int.kbplus.ApiSource" %>
<div class="eight wide column">
    <g:set var="counter" value="${1}"/>
    <table class="ui sortable celled la-table table la-ignore-fixed la-bulk-header" id="${side}">
        <thead>
            <tr>
                <th>
                    <g:if test="${editable}">
                        <input class="select-all" type="checkbox" name="chkall">
                    </g:if>
                </th>
                <th>${message(code: 'sidewide.number')}</th>
                <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle" title="${message(code: 'title.label', default: 'Title')}"/>
                <th class="two wide"><g:message code="tipp.price"/></th>
                <th class="two wide"><g:message code="default.actions.label"/></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${ies.sourceIEs}" var="ie">
                <g:set var="tipp" value="${ie.tipp}"/>
                <g:set var="isContainedByTarget" value="${ies.targetIEs.find { it.tipp == tipp && it.status != RDStore.TIPP_DELETED}}" />
                <g:if test="${side == 'source' || (side == 'target' && isContainedByTarget)}">
                    <tr data-gokbId="${tipp.gokbId}" data-index="${counter}">
                        <td><input type="checkbox" name="bulkflag" data-index="${tipp.gokbId}" class="bulkcheck"></td>
                        <td>${counter++}</td>
                        <td class="titleCell">
                            <semui:listIcon type="${tipp.title?.type?.value}"/>
                            <strong><g:link controller="title" action="show" id="${tipp.title.id}">${tipp.title.title}</g:link></strong>
                            <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && tipp?.title?.volume}">
                                (${message(code: 'title.volume.label')} ${tipp?.title?.volume})
                            </g:if>
                            <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && (tipp?.title?.firstAuthor || tipp?.title?.firstEditor)}">
                                <br><b>${tipp?.title?.getEbookFirstAutorOrFirstEditor()}</b>
                            </g:if>
                            <br>
                            <g:link controller="tipp" action="show" id="${tipp.id}">${message(code: 'platform.show.full_tipp', default: 'Full TIPP Details')}</g:link>&nbsp;&nbsp;&nbsp;
                            <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}" var="gokbAPI">
                                <g:if test="${tipp?.gokbId}">
                                    <a target="_blank" href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}">
                                        <i title="${gokbAPI.name} Link" class="external alternate icon"></i>
                                    </a>
                                </g:if>
                            </g:each>
                            <br>
                            <g:if test="${tipp?.title instanceof BookInstance}">
                                <div class="item">
                                    <b>${message(code: 'title.editionStatement.label')}:</b> ${tipp?.title?.editionStatement}
                                </div>
                            </g:if>
                            <g:if test="${tipp.hostPlatformURL}">
                                <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay" data-content="${message(code: 'tipp.tooltip.callUrl')}" href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}" target="_blank">
                                    <i class="share square icon"></i>
                                </a>
                            </g:if>
                            <g:each in="${tipp?.title?.ids?.sort { it?.identifier?.ns?.ns }}" var="id">
                                <g:if test="${id.identifier.ns.ns == 'originEditUrl'}">
                                <%--<span class="ui small teal image label">
                                    ${id.identifier.ns.ns}: <div class="detail">
                                        <a href="${id.identifier.value}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                                    </div>
                                </span>
                                <span class="ui small teal image label">
                                    ${id.identifier.ns.ns}: <div class="detail">
                                        <a href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code: 'package.show.openLink', default: 'Open Link')}</a>
                                    </div>
                                </span>--%>
                                </g:if>
                                <g:else>
                                    <span class="ui small teal image label">
                                        ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                                    </span>
                                </g:else>
                            </g:each>
                            <div class="ui list">
                                <div class="item" title="${tipp.availabilityStatusExplanation}">
                                    <b>${message(code: 'default.access.label')}:</b> ${tipp.availabilityStatus?.getI10n('value')}
                                </div>
                            </div>
                            <div class="item">
                                <b>${message(code: 'default.status.label')}:</b>
                                <semui:xEditableRefData owner="${tipp}" field="status" config="TIPP Status"/>
                            </div>
                            <div class="item">
                                <b>${message(code: 'tipp.package', default: 'Package')}:</b>
                                <div class="la-flexbox">
                                    <i class="icon gift scale la-list-icon"></i>
                                    <g:link controller="package" action="show" id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                                </div>
                            </div>
                            <div class="item">
                                <b>${message(code: 'tipp.platform')}:</b>
                                <g:if test="${tipp?.platform.name}">
                                    ${tipp?.platform.name}
                                </g:if>
                                <g:else>${message(code: 'default.unknown')}</g:else>
                                <g:if test="${tipp?.platform.name}">
                                    <g:link class="ui icon mini  button la-url-button la-popup-tooltip la-delay" data-content="${message(code: 'tipp.tooltip.changePlattform')}" controller="platform" action="show" id="${tipp?.platform.id}">
                                        <i class="pencil alternate icon"></i>
                                    </g:link>
                                </g:if>
                                <g:if test="${tipp?.platform?.primaryUrl}">
                                    <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay" data-content="${message(code: 'tipp.tooltip.callUrl')}" href="${tipp?.platform?.primaryUrl?.contains('http') ? tipp?.platform?.primaryUrl : 'http://' + tipp?.platform?.primaryUrl}" target="_blank">
                                        <i class="share square icon"></i>
                                    </a>
                                </g:if>
                            </div>
                        </td>
                        <td>
                            <g:if test="${ie.priceItem}">
                                <g:formatNumber number="${ie.priceItem.listPrice}" type="currency" currencySymbol="${ie.priceItem.listPriceCurrency}" currencyCode="${ie.priceItem.listPriceCurrency}"/><br>
                                <g:formatNumber number="${ie.priceItem.localPrice}" type="currency" currencySymbol="${ie.priceItem.localPriceCurrency}" currencyCode="${ie.priceItem.localPriceCurrency}"/><br>
                                <semui:datepicker class="ieOverwrite" name="priceDate" value="${ie.priceItem.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${side == 'target' && isContainedByTarget}">
                                <g:link class="ui icon negative button la-popup-tooltip la-delay" action="processRemoveEntitlements"
                                        params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId, packageId: packageId]}"
                                        data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                    <i class="minus icon"></i>
                                </g:link>
                            </g:if>
                            <g:elseif test="${side == 'source' && !isContainedByTarget}">
                                <g:link class="ui icon positive button la-popup-tooltip la-delay" action="processAddEntitlements"
                                        params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId]}"
                                        data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                    <i class="plus icon"></i>
                                </g:link>
                            </g:elseif>
                        </td>
                    </tr>
                </g:if>
                <g:else>
                    <tr data-gokbId="${tipp.gokbId}" data-empty="true" data-index="${counter}">
                        <td></td>
                        <td>${counter++}</td>
                        <td class="titleCell"></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:else>
            </g:each>
        </tbody>
    </table>
</div>

<r:script language="JavaScript">
    $("simpleHiddenRefdata").editable({
        url: function(params) {
            var hidden_field_id = $(this).data('hidden-id');
            $("#"+hidden_field_id).val(params.value);
            // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
        }
    });
</r:script>