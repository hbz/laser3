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
        <h2 class="ui header center aligned"><g:message code="renewEntitlementsWithSurvey.currentEntitlements" /> (${targetIEs.size()?:0})</h2>

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
        <h2 class="ui header center aligned"><g:message code="renewEntitlementsWithSurvey.selectableTitles" /> (${sourceIEs.size()?:0})</h2>

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
                <g:sortableColumn class="ten wide" params="${params}" property="tipp.title.sortTitle" title="${message(code: 'title.label')}"/>
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
                    <tr data-gokbId="${tipp.gokbId}" data-ieId="${ie?.id}" data-index="${counter}">
                        <td>

                            <g:if test="${surveyFunction && !isContainedByTarget && editable && side == 'source'}">
                                <input type="checkbox" name="bulkflag" data-index="${ie.id}" class="bulkcheck">
                            </g:if>

                            <g:if test="${!surveyFunction && !isContainedByTarget && editable}">
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

                            <semui:listIcon type="${tipp.title?.type?.value}"/>
                            <strong><g:link controller="title" action="show"
                                            id="${tipp.title.id}">${tipp.title.title}</g:link></strong>

                            <g:if test="${tipp.hostPlatformURL}">
                                <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                <%-- data-content="${message(code: 'tipp.tooltip.callUrl')}" --%>
                                   data-content="${tipp?.platform.name}"
                                   href="${tipp.hostPlatformURL.contains('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"
                                   target="_blank"><i class="cloud icon"></i></a>
                            </g:if>
                            <br>
                            <div class="la-icon-list">
                                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance }">
                                    <div class="item">
                                        <i class="grey icon la-books la-popup-tooltip la-delay" data-content="${message(code: 'tipp.volume')}"></i>
                                        <div class="content">
                                            ${tipp?.title?.volume}
                                        </div>
                                    </div>
                                </g:if>

                                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance && (tipp?.title?.firstAuthor || tipp?.title?.firstEditor)}">
                                    <div class="item">
                                        <i class="grey icon user circle la-popup-tooltip la-delay" data-content="${message(code: 'author.slash.editor')}"></i>
                                        <div class="content">
                                            ${tipp?.title?.getEbookFirstAutorOrFirstEditor()}
                                        </div>
                                    </div>
                                </g:if>

                                <%-- TODO; @moe: check merge conflict --%>
                                <g:link controller="tipp" action="show" id="${tipp.id}">${message(code: 'platform.show.full_tipp', default: 'Full TIPP Details')}</g:link>&nbsp;&nbsp;&nbsp;
                                <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}" var="gokbAPI">
                                    <g:if test="${tipp?.gokbId}">
                                        <a target="_blank" href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}">
                                            <i title="${gokbAPI.name} Link" class="external alternate icon"></i>
                                        </a>
                                    </g:if>
                                </g:each>
                                <%-- TODO; @moe: check merge conflict --%>

                                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                                    <div class="item">
                                        <i class="grey icon copy la-popup-tooltip la-delay" data-content="${message(code: 'title.editionStatement.label')}"></i>
                                        <div class="content">
                                            ${tipp?.title?.editionStatement}
                                        </div>
                                    </div>
                                </g:if>

                                <g:if test="${tipp?.title instanceof com.k_int.kbplus.BookInstance}">
                                    <div class="item">
                                        <i class="grey icon list la-popup-tooltip la-delay" data-content="${message(code: 'title.summaryOfContent.label')}"></i>
                                        <div class="content">
                                            ${tipp?.title?.summaryOfContent}
                                        </div>
                                    </div>
                                </g:if>

                            </div>

                            <g:each in="${tipp?.title?.ids?.sort { it?.ns?.ns }}" var="id">
                                <span class="ui small teal image label">
                                    ${id.ns.ns}: <div class="detail">${id.value}</div>
                                </span>
                            </g:each>

                            <div class="la-icon-list">

                               %{-- <g:if test="${tipp.availabilityStatus?.getI10n('value')}">
                                    <div class="item">
                                        <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label')}"></i>
                                        <div class="content">
                                            ${tipp.availabilityStatus?.getI10n('value')}
                                        </div>
                                    </div>
                                </g:if>--}%

                                <g:if test="${tipp.status.getI10n("value")}">
                                    <div class="item">
                                        <i class="grey clipboard check clip icon la-popup-tooltip la-delay" data-content="${message(code: 'default.status.label')}"></i>
                                        <div class="content">
                                            ${tipp.status.getI10n("value")}
                                        </div>
                                    </div>
                                </g:if>

                                <g:if test="${showPackage}">
                                    <div class="item">
                                        <i class="grey icon gift scale la-popup-tooltip la-delay" data-content="${message(code: 'package.label')}"></i>
                                        <div class="content">
                                            <g:link controller="package" action="show"
                                                    id="${tipp?.pkg?.id}">${tipp?.pkg?.name}</g:link>
                                        </div>
                                    </div>
                                </g:if>
                                <g:if test="${showPlattform}">
                                    <div class="item">
                                        <i class="grey icon cloud la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.changePlattform')}"></i>
                                        <div class="content">
                                            <g:if test="${tipp?.platform.name}">
                                                <g:link controller="platform" action="show" id="${tipp?.platform.id}">
                                                    ${tipp?.platform.name}
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                ${message(code: 'default.unknown')}
                                            </g:else>
                                        </div>
                                    </div>
                                </g:if>

                                <g:if test="${tipp?.id}">
                                    <div class="la-title">${message(code: 'default.details.label')}</div>
                                    <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                            data-content="${message(code: 'laser')}"
                                            href="${tipp?.hostPlatformURL.contains('http') ? tipp?.hostPlatformURL : 'http://' + tipp?.hostPlatformURL}"
                                            target="_blank"
                                            controller="tipp" action="show"
                                            id="${tipp?.id}">
                                        <i class="book icon"></i>
                                    </g:link>
                                </g:if>
                                <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                                        var="gokbAPI">
                                    <g:if test="${tipp?.gokbId}">
                                        <a class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                           data-content="${message(code: 'gokb')}"
                                           href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + tipp?.gokbId : '#'}"
                                           target="_blank"><i class="la-gokb  icon"></i>
                                        </a>
                                    </g:if>
                                </g:each>

                            </div>
                        </td>
                        <td>
                            <g:if test="${side == 'source'}">
                                <g:if test="${ie?.priceItem}">
                                    <g:formatNumber number="${ie?.priceItem?.listPrice}" type="currency" currencySymbol="${ie?.priceItem?.listCurrency}" currencyCode="${ie?.priceItem?.listCurrency}"/><br>
                                    <g:formatNumber number="${ie?.priceItem?.localPrice}" type="currency" currencySymbol="${ie?.priceItem?.localCurrency}" currencyCode="${ie?.priceItem?.localCurrency}"/><br>
                                    %{--<semui:datepicker class="ieOverwrite" name="priceDate" value="${ie?.priceItem?.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>--}%

                                    <g:set var="sumlistPrice" value="${sumlistPrice+(ie?.priceItem?.listPrice ?: 0)}"/>
                                    <g:set var="sumlocalPrice" value="${sumlocalPrice+(ie?.priceItem?.localPrice ?: 0)}"/>

                                </g:if>
                            </g:if>
                            <g:if test="${side == 'target'}">
                                <g:if test="${targetIE?.priceItem}">
                                    <g:formatNumber number="${ie?.priceItem?.listPrice}" type="currency" currencySymbol="${ie?.priceItem?.listCurrency}" currencyCode="${ie?.priceItem?.listCurrency}"/><br>
                                    <g:formatNumber number="${ie?.priceItem?.localPrice}" type="currency" currencySymbol="${ie?.priceItem?.localCurrency}" currencyCode="${ie?.priceItem?.localCurrency}"/><br>
                                %{--<semui:datepicker class="ieOverwrite" name="priceDate" value="${ie?.priceItem?.priceDate}" placeholder="${message(code:'tipp.priceDate')}"/>--}%

                                    <g:set var="sumlistPrice" value="${sumlistPrice+(ie?.priceItem?.listPrice ?: 0)}"/>
                                    <g:set var="sumlocalPrice" value="${sumlocalPrice+(ie?.priceItem?.localPrice ?: 0)}"/>

                                </g:if>
                            </g:if>
                        </td>
                        <td>

                            <g:if test="${surveyFunction}">
                                <g:if test="${side == 'target' && isContainedByTarget && targetIE?.acceptStatus == de.laser.helper.RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION && editable}">
                                    <g:link class="ui icon negative button la-popup-tooltip la-delay" action="processRemoveIssueEntitlementsSurvey"
                                            params="${[id: subscriptionInstance.id, singleTitle: isContainedByTarget.id, packageId: packageId, surveyConfigID: surveyConfig?.id]}"
                                            data-content="${message(code: 'subscription.details.addEntitlements.remove_now')}">
                                        <i class="minus icon"></i>
                                    </g:link>
                                </g:if>
                                <g:elseif test="${side == 'source' && !isContainedByTarget && editable}">
                                    <g:link class="ui icon positive button la-popup-tooltip la-delay" action="processAddIssueEntitlementsSurvey"
                                            params="${[id: subscriptionInstance.id, singleTitle: ie.id, surveyConfigID: surveyConfig?.id]}"
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