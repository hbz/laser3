<%@page import="de.laser.helper.RDStore; com.k_int.kbplus.BookInstance;com.k_int.kbplus.ApiSource" %>
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

    <g:if test="${side == 'target' && surveyFunction}">
        <h2 class="ui header center aligned"><g:message code="renewEntitlementsWithSurvey.currentEntitlements" /></h2>

        <semui:form>
            <g:message code="subscription" />: <b><g:link action="show" id="${newSub?.id}">${newSub?.name}</g:link></b>
            <br>
            <br>
            %{--<g:message code="package" />:--}%<br>
            <div class="ui list">
                <g:each in="${newSub?.packages.sort{it?.pkg?.name}}" var="subPkg">
                    <div class="item">
                        <br>
                    </div>
                </g:each>
            </div>
        </semui:form>

    </g:if>

    <g:if test="${side == 'source' && surveyFunction}">
        <h2 class="ui header center aligned"><g:message code="renewEntitlementsWithSurvey.selectableTitles" /></h2>

        <semui:form>
            <g:message code="subscription" />: <b>${subscription?.name}</b>
        <br>
            <br>
            <g:message code="package" />:
            <div class="ui bulleted list">
            <g:each in="${subscription?.packages.sort{it?.pkg?.name}}" var="subPkg">
                <div class="item">
                    <b>${subPkg?.pkg?.name}</b> (<g:message code="title.plural" />: ${subPkg?.pkg?.tipps?.size()?: 0})
                </div>
            </g:each>
            </div>
        </semui:form>
    </g:if>


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
                <g:set var="targetIE" value="${ies.targetIEs.find { it.tipp == tipp}}" />
                <g:if test="${side == 'source' || (side == 'target' && isContainedByTarget)}">
                    <tr data-gokbId="${tipp.gokbId}" data-index="${counter}">
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
                                <div class="item">
                                     ${tipp?.title?.summaryOfContent}
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
                                <g:if test="${surveyFunction}">
                                    <semui:xEditableRefData owner="${ie}" field="status" config="TIPP Status"/>
                                </g:if>
                                <g:else>
                                    <semui:xEditableRefData owner="${tipp}" field="status" config="TIPP Status"/>
                                </g:else>
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
                                <g:formatNumber number="${ie?.priceItem?.listPrice}" type="currency" currencySymbol="${ie?.priceItem?.listCurrency}" currencyCode="${ie?.priceItem?.listCurrency}"/><br>
                                <g:formatNumber number="${ie?.priceItem?.localPrice}" type="currency" currencySymbol="${ie?.priceItem?.localCurrency}" currencyCode="${ie?.priceItem?.localCurrency}"/><br>
                                %{--<semui:datepicker class="ieOverwrite" name="priceDate" value="${ie?.priceItem?.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>--}%

                                <g:set var="sumlistPrice" value="${sumlistPrice+(ie?.priceItem?.listPrice ?: 0)}"/>
                                <g:set var="sumlocalPrice" value="${sumlocalPrice+(ie?.priceItem?.localPrice ?: 0)}"/>

                            </g:if>
                        </td>
                        <td>

                            <g:if test="${surveyFunction}">
                                <g:if test="${side == 'target' && isContainedByTarget && targetIE?.acceptStatus == de.laser.helper.RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION && editable}">
                                    <g:link class="ui icon negative button la-popup-tooltip la-delay" action="processRemoveIssueEntitlementsSurvey"
                                            params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${side == 'source' && !isContainedByTarget && editable}">
                                    <g:link class="ui icon positive button la-popup-tooltip la-delay" action="processAddIssueEntitlementsSurvey"
                                            params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                        <i class="plus icon"></i>
                                    </g:link>
                                </g:elseif>
                            </g:if>
                            <g:else>
                                <g:if test="${side == 'target' && isContainedByTarget && targetIE?.acceptStatus == de.laser.helper.RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION && editable}">
                                    <g:link class="ui icon negative button la-popup-tooltip la-delay" action="processRemoveEntitlements"
                                            params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId, packageId: packageId]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${side == 'source' && !isContainedByTarget && editable}">
                                    <g:link class="ui icon positive button la-popup-tooltip la-delay" action="processAddEntitlements"
                                            params="${[id: subscriptionInstance.id, singleTitle: tipp.gokbId]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.add_now')}">
                                        <i class="plus icon"></i>
                                    </g:link>
                                </g:elseif>
                            </g:else>


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
        <tfoot>
            <tr>
                <th></th>
                <th></th>
                <th></th>
                <th><g:message code="financials.export.sums"/> <br>
                    <g:message code="tipp.listPrice"/>: <g:formatNumber number="${sumlistPrice}" type="currency"/><br>
                    %{--<g:message code="tipp.localPrice"/>: <g:formatNumber number="${sumlocalPrice}" type="currency"/>--}%
                </th>
                <th></th>
            </tr>
        </tfoot>
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