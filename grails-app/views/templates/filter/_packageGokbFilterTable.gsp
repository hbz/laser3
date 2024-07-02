<%@page import="de.laser.Provider; de.laser.CustomerTypeService; de.laser.survey.SurveyPackageResult; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.Vendor; de.laser.convenience.Marker; de.laser.utils.DateUtils; de.laser.storage.RDConstants; de.laser.Package; de.laser.Org; de.laser.Platform; de.laser.RefdataValue" %>
<laser:serviceInjection/>
<table class="ui sortable celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <g:if test="${tmplShowCheckbox}">
                <th>
                    <g:if test="${records}">
                        <g:checkBox name="pkgListToggler" id="pkgListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>

            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
                <g:if test="${tmplConfigItem == 'lineNumber'}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'name'}">
                    <g:sortableColumn property="name" title="${message(code: 'package.show.pkg_name')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'status'}">
                    <th>${message(code: 'package.status.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'titleCount'}">
                    <g:sortableColumn property="currentTippCount" title="${message(code: 'package.compare.overview.tipps')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'counts'}">
                    <th>Laser <br>${message(code: 'package.show.nav.current')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.current')}</th>
                    <th>Laser <br>${message(code: 'package.show.nav.planned')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.planned')}</th>
                    <th>Laser <br>${message(code: 'package.show.nav.expired')}</th>
                    <th>Wekb <br>${message(code: 'package.show.nav.expired')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'provider'}">
                    <g:sortableColumn property="provider.name" title="${message(code: 'provider.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'vendor'}">
                    <g:sortableColumn property="vendor.name" title="${message(code: 'vendor.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'platform'}">
                    <g:sortableColumn property="nominalPlatform.name" title="${message(code: 'platform.label')}" params="${params}"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'curatoryGroup'}">
                    <th>${message(code: 'package.curatoryGroup.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'automaticUpdates'}">
                    <th>${message(code: 'package.source.automaticUpdates')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'lastUpdatedDisplay'}">
                    <g:sortableColumn property="lastUpdatedDisplay" title="${message(code: 'package.lastUpdated.label')}" params="${params}" defaultOrder="desc"/>
                </g:if>
                <g:if test="${tmplConfigItem == 'subscription'}">
                    <th>${message(code:'myinst.currentPackages.assignedSubscriptions')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'my'}">
                    <th class="center aligned">
                        <ui:myXIcon tooltip="${message(code: 'menu.my.packages')}" />
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem == 'marker'}">
                    <th class="center aligned"><ui:markerIcon type="WEKB_CHANGES" /></th>
                </g:if>
                <g:if test="${tmplConfigItem == 'surveyCostItemsPackages'}">
                    <th>${message(code:'surveyCostItemsPackages.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'surveyPackagesComments'}">
                    <th>
                        <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                            ${message(code: 'surveyResult.participantComment')}
                        </g:if>
                        <g:else>
                            ${message(code: 'surveyResult.commentParticipant')}
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                            ${message(code: 'surveyResult.commentOnlyForOwner')}
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                        <g:else>
                            ${message(code: 'surveyResult.commentOnlyForParticipant')}
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:else>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem == 'linkPackage' || tmplConfigItem == 'linkSurveyPackage' || tmplConfigItem == 'unLinkSurveyPackage' || tmplConfigItem == 'removeSurveyPackageResult' || tmplConfigItem == 'addSurveyPackageResult'}">
                    <th class="center aligned">${message(code: 'default.actions.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'markPerpetualAccess'}">
                    <th class="x center aligned">${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem == 'yodaActions'}">
                    <th class="x center aligned">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="left center" data-content="${message(code: 'menu.yoda.reloadPackages')}">
                            <g:link class="ui icon button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: 'menu.yoda.reloadPackages.confirm')}"
                                    data-confirm-term-how="ok"
                                    controller="yoda" action="reloadPackages">
                                <i class="icon cloud download alternate" style="color:white"></i>
                            </g:link>
                        </span>
                    </th>
                </g:if>
            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${records}" var="entry" status="jj">
            <g:if test="${entry._source}">
                <g:set var="record" value="${entry._source}"/>
            </g:if>
            <g:else>
                <g:set var="record" value="${entry}"/>
            </g:else>
            <g:set var="pkg" value="${Package.findByGokbId(record.uuid)}"/>
            <%
                Provider provider
                SortedSet<Vendor> vendors = new TreeSet<Vendor>()
                Set<Map> nonSyncedVendors = []
                Platform plat
                if(record.providerUuid)
                    provider = Provider.findByGokbId(record.providerUuid)
                else
                    provider = pkg.provider
                if(record.nominalPlatformUuid)
                    plat = Platform.findByGokbId(record.nominalPlatformUuid)
                else
                    plat = pkg?.nominalPlatform
                if(record.containsKey('vendors') && record.vendors.size() > 0) {
                    record.vendors.each { ven ->
                        Vendor vendor = Vendor.findByGokbId(ven.vendorUuid)
                        if(vendor)
                            vendors << vendor
                        else nonSyncedVendors << ven
                    }
                }
                boolean perpetuallySubscribed = false
            %>
            <tr>
                <g:if test="${tmplShowCheckbox}">
                    <td>
                        <g:if test="${editable && (!uuidPkgs || !(record.uuid in uuidPkgs))}">
                            <g:checkBox id="selectedPkgs_${jj}" name="selectedPkgs" value="${record.uuid}" checked="false"/>
                        </g:if>
                    </td>
                </g:if>

                <g:each in="${tmplConfigShow}" var="tmplConfigItem">
                    <g:if test="${tmplConfigItem == 'lineNumber'}">
                        <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'name'}">
                        <td>
                            <%--UUID: ${record.uuid} --%>
                            <%--Package: ${Package.findByGokbId(record.uuid)} --%>
                            <g:if test="${pkg}">
                                <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
                            </g:if>
                            <g:else>
                                <ui:wekbIconLink type="package" gokbId="${record.uuid}" /> ${record.name}
                            </g:else>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'status'}">
                        <td>
                            <g:if test="${record.status}">
                                ${RefdataValue.getByValueAndCategory(record.status, RDConstants.PACKAGE_STATUS)?.getI10n("value")}
                            </g:if>
                            <g:else>
                                ${pkg.packageStatus?.getI10n("value")}
                            </g:else>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'titleCount'}">
                        <td>
                            <g:if test="${record.containsKey('currentTippCount')}">
                                <g:if test="${record.currentTippCount}">
                                    <g:if test="${pkg}">
                                        <g:link controller="package" action="current" id="${pkg.id}">
                                            ${record.currentTippCount}
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        ${record.currentTippCount}
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:if test="${pkg}">
                                        <g:link controller="package" action="current" id="${pkg.id}">
                                            0
                                        </g:link>
                                    </g:if>
                                    <g:else>
                                        0
                                    </g:else>
                                </g:else>
                            </g:if>
                            <g:elseif test="${pkg}">
                                <g:link controller="package" action="current" id="${pkg.id}">
                                    ${pkg.getCurrentTippsCount()}
                                </g:link>
                            </g:elseif>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'counts'}">
                        <g:set var="laserCurrentTitles" value="${pkg ? pkg.getCurrentTippsCount() : 0}"/>
                        <g:set var="laserRetiredTitles" value="${pkg ? pkg.getRetiredTippsCount() : 0}"/>
                        <g:set var="laserExpectedTitles" value="${pkg ? pkg.getExpectedTippsCount() : 0}"/>
                        <g:set var="wekbCurrentTitles" value="${record.currentTippCount ?: 0}"/>
                        <g:set var="wekbRetiredTitles" value="${record.retiredTippCount ?: 0}"/>
                        <g:set var="wekbExpectedTitles" value="${record.expectedTippCount ?: 0}"/>
                        <td class=" ${pkg && wekbCurrentTitles != laserCurrentTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserCurrentTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbCurrentTitles}"/>
                        </td>

                        <td class=" ${pkg && wekbExpectedTitles != laserExpectedTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserExpectedTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbExpectedTitles}"/>
                        </td>

                        <td class=" ${pkg && wekbRetiredTitles != laserRetiredTitles ? 'negative' : ''}">
                            <g:formatNumber number="${laserRetiredTitles}"/>
                        </td>

                        <td>
                            <g:formatNumber number="${wekbRetiredTitles}"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'provider'}">
                        <td>
                            <g:if test="${provider}">
                                <g:if test="${provider.gokbId}">
                                    <ui:wekbIconLink type="provider" gokbId="${provider.gokbId}" />
                                </g:if>
                                <g:link controller="provider" action="show" id="${provider.id}">${provider.name}</g:link>
                            </g:if>
                            <g:else>${record.providerName}</g:else>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'vendor'}">
                        <td>
                            <ul>
                                <g:each in="${vendors}" var="vendor">
                                    <li>
                                        <g:if test="${vendor.gokbId}">
                                            <ui:wekbIconLink type="vendor" gokbId="${vendor.gokbId}" />
                                        </g:if>
                                        <g:link controller="vendor" action="show" id="${vendor.id}">${vendor.name}</g:link>
                                    </li>
                                </g:each>
                                <g:each in="${nonSyncedVendors}" var="vendor">
                                    <li>
                                        <ui:wekbIconLink type="vendor" gokbId="${vendor.vendorUuid}" />
                                        ${vendor.vendor}
                                    </li>
                                </g:each>
                            </ul>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'platform'}">
                        <td>
                            <g:if test="${plat}">
                                <g:if test="${plat.gokbId}">
                                    <ui:wekbIconLink type="platform" gokbId="${plat.gokbId}" />
                                </g:if>
                                <g:link controller="platform" action="show" id="${plat.id}">${plat.name}</g:link>
                            </g:if>
                            <g:else>${record.nominalPlatformName}</g:else>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'curatoryGroup'}">
                        <td>
                            <g:if test="${record.curatoryGroups}">
                                <g:each in="${record.curatoryGroups}" var="curatoryGroup">
                                    <ui:wekbIconLink type="curatoryGroup" gokbId="${curatoryGroup.curatoryGroup}" />
                                    ${curatoryGroup.name}
                                %{--<g:link url="${editUrl.endsWith('/') ? editUrl : editUrl+'/'}resource/show/${curatoryGroup.curatoryGroup}" target="_blank">--}%
                                %{--    <i class="${Icons.LINK_EXTERNAL} icon"></i>--}%
                                %{--</g:link>--}%
                                    <br />
                                </g:each>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'automaticUpdates'}">
                        <td>
                            <g:if test="${record.source?.automaticUpdates}">
                                <g:message code="package.index.result.automaticUpdates"/>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${record.source.frequency}">
                                    <i class="grey question circle icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <g:message code="package.index.result.noAutomaticUpdates"/>
                            </g:else>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'lastUpdatedDisplay'}">
                        <td>
                            <g:if test="${record.lastUpdatedDisplay}">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${DateUtils.parseDateGeneric(record.lastUpdatedDisplay)}"/>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'subscription'}">
                        <td>
                            <ul class="la-simpleList">
                                <g:each in="${subscriptionMap.get('package_' + pkg.gokbId)}" var="sub">
                                    <%
                                        String period = sub.startDate ? g.formatDate(date: sub.startDate, format: message(code: 'default.date.format.notime'))  : ''
                                        period = sub.endDate ? period + ' - ' + g.formatDate(date: sub.endDate, format: message(code: 'default.date.format.notime'))  : ''
                                        period = period ? '('+period+')' : ''
                                        perpetuallySubscribed = sub.hasPerpetualAccess
                                    %>
                                    <li>
                                        <g:link controller="subscription" action="show" id="${sub.id}">${sub.name + ' ' +period}</g:link>
                                    </li>
                                </g:each>
                            </ul>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'surveyCostItemsPackages'}">
                        <td class="center aligned">
                                    <table class="ui very basic compact table">
                                        <tbody>
                                        <g:each in="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndPkg(surveyOrg, RDStore.COST_ITEM_DELETED, pkg)}"
                                                var="costItem">
                                            <tr>
                                                <td>
                                                    ${costItem.costItemElement.getI10n('value')}
                                                </td>
                                                <td>
                                                    <strong><g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}"
                                                                            minFractionDigits="2"
                                                                            maxFractionDigits="2" type="number"/></strong>

                                                    (<g:formatNumber number="${costItem.costInBillingCurrency}" minFractionDigits="2"
                                                                     maxFractionDigits="2" type="number"/>)
                                                </td>
                                                <td>
                                                    ${costItem.billingCurrency?.getI10n('value')}
                                                </td>
                                                <td>
                                                    <g:if test="${costItem.startDate || costItem.endDate}">
                                                        ${costItem.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.startDate) : ''} - ${costItem.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItem.endDate) : ''}
                                                    </g:if>
                                                </td>
                                            </tr>
                                        </g:each>
                                        </tbody>
                                    </table>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'surveyPackagesComments'}">
                        <g:set var="surveyPackageResult"
                               value="${SurveyPackageResult.findByParticipantAndSurveyConfigAndPkg(participant, surveyConfig, pkg)}"/>
                        <g:if test="${surveyPackageResult}">
                            <td>
                                <ui:xEditable owner="${surveyPackageResult}" type="textarea" field="comment"/>
                            </td>
                            <td>
                                <g:if test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                                    <ui:xEditable owner="${surveyPackageResult}" type="textarea" field="ownerComment"/>
                                </g:if>
                                <g:else>
                                    <ui:xEditable owner="${surveyPackageResult}" type="textarea" field="participantComment"/>
                                </g:else>
                            </td>
                        </g:if>
                        <g:else>
                            <td></td>
                            <td></td>
                        </g:else>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'my'}">
                        <td class="center aligned">
                            <g:if test="${pkg && pkg.id in currentPackageIdSet}">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.packages')}">
                                    <i class="icon yellow star"></i>
                                </span>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'marker'}">
                        <td class="center aligned">
                            <g:if test="${pkg && pkg.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                                <ui:cbItemMarkerAction package="${pkg}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'linkPackage'}">
                        <td class="right aligned">
                            <g:if test="${editable && (!pkgs || !(record.uuid in pkgs))}">
                                <g:set var="disabled" value="${bulkProcessRunning ? 'disabled' : ''}" />
                                <button type="button" class="ui icon button la-popup-tooltip la-delay ${disabled}"
                                        data-addUUID="${record.uuid}"
                                        data-packageName="${record.name}"
                                        data-ui="modal"
                                        data-href="#linkPackageModal"
                                        data-content="${message(code: 'subscription.details.linkPackage.button', args: [record.name])}"><g:message
                                        code="subscription.details.linkPackage.label"/></button>

                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'linkSurveyPackage'}">
                        <td class="right aligned">
                            <g:if test="${editable}">
                                <g:if test="${(!uuidPkgs || !(record.uuid in uuidPkgs))}">
                                    <g:link type="button" class="ui icon button" controller="${controllerName}" action="${actionName}" id="${params.id}"
                                            params="[addUUID: record.uuid, surveyConfigID: surveyConfig.id]"><g:message
                                            code="surveyPackages.linkPackage"/></g:link>

                                </g:if>
                                <g:else>
                                    <g:link type="button" class="ui button negative" controller="${controllerName}" action="${actionName}" id="${params.id}"
                                            params="[removeUUID: record.uuid, surveyConfigID: surveyConfig.id]"><g:message
                                            code="surveyPackages.unlinkPackage"/></g:link>

                                </g:else>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'unLinkSurveyPackage'}">
                        <td class="right aligned">
                            <g:if test="${editable && (!uuidPkgs || !(record.uuid in uuidPkgs))}">
                                <g:link type="button" class="ui button negative" controller="${controllerName}" action="${actionName}" id="${params.id}"
                                        params="[removeUUID: record.uuid, surveyConfigID: surveyConfig.id]"><g:message
                                        code="surveyPackages.unlinkPackage"/></g:link>

                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'addSurveyPackageResult'}">
                        <td class="right aligned">
                            <g:if test="${editable && (!uuidPkgs || !(record.uuid in uuidPkgs))}">
                                <g:link type="button" class="ui button" controller="${controllerName}" action="${actionName}" id="${params.id}"
                                        params="${parame+ [viewTab: 'packageSurvey', actionsForSurveyPackages: 'addSurveyPackage', pkgUUID: record.uuid]}"><g:message
                                        code="surveyPackages.linkPackage"/></g:link>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'removeSurveyPackageResult'}">
                        <td class="right aligned">
                            <g:if test="${editable && (!uuidPkgs || !(record.uuid in uuidPkgs))}">
                                <g:link type="button" class="ui button negative" controller="${controllerName}" action="${actionName}" id="${params.id}"
                                        params="${parame+ [viewTab: 'packageSurvey', actionsForSurveyPackages: 'removeSurveyPackage', pkgUUID: record.uuid]}"><g:message
                                        code="surveyPackages.unlinkPackage"/></g:link>

                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'markPerpetualAccess'}">
                        <td class="x">
                            <g:if test="${pkg}">
                                <g:if test="${perpetuallySubscribed}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'subscription.perpetuallySubscribed')}">
                                        <i class="flag outline icon"></i>
                                    </span>
                                </g:if>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem == 'yodaActions'}">
                        <td class="x">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.reloadPackage')}">
                                <g:link controller="yoda" action="reloadPackage" params="${[packageUUID: record.uuid]}" class="ui icon button">
                                    <i class="icon cloud download alternate"></i>
                                </g:link>
                            </span>
                            <g:if test="${pkg}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top center" data-content="${message(code: 'menu.yoda.retriggerPendingChanges')}">
                                    <g:if test="${pkg}">
                                        <g:link controller="yoda" action="matchPackageHoldings" params="${[pkgId: pkg.id]}" class="ui icon button">
                                            <i class="icon wrench"></i>
                                        </g:link>
                                    </g:if>
                                </span>
                            </g:if>
                        </td>
                    </g:if>
                </g:each>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#pkgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedPkgs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedPkgs]").prop('checked', false)
            }
        })
    </laser:script>

</g:if>