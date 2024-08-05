<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.PendingChangeConfiguration; de.laser.Platform; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.finance.CostItem; de.laser.Org; de.laser.IssueEntitlement" %>
<laser:serviceInjection />

<div class="ui card">
    <div class="content">
        <h2 class="ui header">${message(code: 'subscription.packages.label')}</h2>
        <div class="ui accordion la-accordion-showMore">
        <g:each in="${subscription.packages}" var="sp">
            <g:set var="packageInstanceRecord" value="${packageMetadata.get(sp.pkg.gokbId)}"/>
            <% String buttonColor = "blue"
            if(sp.pendingChangeConfig.size() > 0) {
                buttonColor = "green"
            }%>
            <div class="ui raised segments la-accordion-segments">
                <div class="ui fluid segment title">
                    <g:if test="${subscriptionService.checkThreadRunning('PackageUnlink_'+subscription.id)}">
                        <ui:msg class="info" showIcon="true" message="subscriptionsManagement.unlinkInfo.unlinkingInProgress" args="${[sp.pkg.name]}" />
                    </g:if>
                    <g:else>
                        <div class="ui stackable equal width grid">
                            <div class="four wide column">
                                <g:link controller="package" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                <ui:wekbIconLink type="package" gokbId="${sp.pkg.gokbId}"/>
                                <br>
                                ${sp.getCurrentIssueEntitlementCountOfPackage()} <g:message code="subscription.packages.currentTitles"/>
                            </div>
                            <div class="three wide column">
                                <div>
                                    <g:if test="${sp.pkg.provider}">
                                        <i aria-hidden="true" class="${Icon.PROVIDER} grey outline la-popup-tooltip" data-content="${message(code: 'provider.label')}"></i>
                                        <g:link controller="provider" action="show" id="${sp.pkg.provider.id}">${sp.pkg.provider.name}</g:link>
                                        <g:if test="${sp.pkg.provider.homepage}"><ui:linkWithIcon href="${sp.pkg.provider.homepage.startsWith('http') ? sp.pkg.provider.homepage : 'http://' + sp.pkg.provider.homepage}"/></g:if>
                                        <g:if test="${sp.pkg.provider.gokbId}"><ui:wekbIconLink type="provider" gokbId="${sp.pkg.provider.gokbId}"/></g:if>
                                    </g:if>
                                </div>
                                <g:each in="${sp.pkg.vendors}" var="pv">
                                    <g:set var="vendorRecord" value="${packageInstanceRecord.vendors.find { rec -> rec.vendorUuid == pv.vendor.gokbId }}"/>
                                    <div>
                                        <i aria-hidden="true" class="${Icon.VENDOR} grey la-popup-tooltip" data-content="${message(code: 'vendor.label')}"></i>
                                        <g:link controller="vendor" action="show" id="${pv.vendor.id}">${pv.vendor.name}</g:link>
                                        <g:if test="${vendorRecord.homepage}"><ui:linkWithIcon href="${vendorRecord.homepage.startsWith('http') ? vendorRecord.homepage : 'http://' + vendorRecord.homepage}"/></g:if>
                                        <g:if test="${pv.vendor.gokbId}"><ui:wekbIconLink type="vendor" gokbId="${pv.vendor.gokbId}"/></g:if>
                                    </div>
                                </g:each>
                            </div>
                            <div class="three wide column">
                                <g:if test="${sp.pkg.nominalPlatform}">
                                    <i aria-hidden="true" class="${Icon.PLATFORM} grey la-popup-tooltip" data-content="${message(code: 'platform.label')}"></i>
                                    <g:link controller="platform" action="show" id="${sp.pkg.nominalPlatform.id}">${sp.pkg.nominalPlatform.name}</g:link>
                                    <ui:linkWithIcon href="${sp.pkg.nominalPlatform.primaryUrl?.startsWith('http') ? sp.pkg.nominalPlatform.primaryUrl : 'http://' + sp.pkg.nominalPlatform.primaryUrl}"/>
                                    <ui:wekbIconLink type="platform" gokbId="${sp.pkg.nominalPlatform.gokbId}"/>
                                </g:if>
                            <%--
                            <g:if test="${packageService.getCountOfNonDeletedTitles(sp.pkg) > 0}">
                                <g:each in="${Platform.executeQuery('select distinct tipp.platform from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg',[pkg:sp.pkg])}" var="platform">
                                    <g:if test="${platform}">
                                        <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                                        <ui:linkWithIcon href="${platform.primaryUrl?.startsWith('http') ? platform.primaryUrl : 'http://' + platform.primaryUrl}"/>
                                    </g:if>
                                </g:each>
                            </g:if>
                            --%>
                            </div>
                            <div class="six wide right aligned column">
                                <g:if test="${editmode}">
                                    <div class="${Btn.MODERN.SIMPLE_TOOLTIP} ${buttonColor}"
                                         data-content="${message(code:'subscription.packages.config.header')}">
                                        <i class="${Icon.CMD.SHOW_MORE}"></i>
                                    </div>
                                    <%
                                        String unlinkDisabled = '', unlinkDisabledTooltip = null
                                        Set<Subscription> blockingCostItems = CostItem.executeQuery('select ci.sub from CostItem ci where (ci.sub = :sub or ci.sub.instanceOf = :sub) and ci.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted', [pkg: sp.pkg, deleted: RDStore.COST_ITEM_DELETED, sub: sp.subscription, context: institution])
                                        if(showConsortiaFunctions) {
                                            if(auditService.getAuditConfig(subscription.instanceOf, 'holdingSelection')) {
                                                unlinkDisabled = 'disabled'
                                                unlinkDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blockingInheritanceSetting")
                                            }
                                            else if (blockingCostItems) {
                                                unlinkDisabled = 'disabled'
                                                unlinkDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blockingSubscribersConsortia")
                                            }
                                        }
                                        else {
                                            if(blockingCostItems) {
                                                unlinkDisabled = 'disabled'
                                                unlinkDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blocked")
                                            }
                                        }
                                        String btnClass = "item js-open-confirm-modal ${unlinkDisabled}"
                                    %>
                                    <g:if test="${showConsortiaFunctions && !sp.subscription.instanceOf}">
                                        <div class="ui buttons">
                                            <div class="ui simple dropdown negative icon button la-modern-button ${unlinkDisabled}" data-content="${message(code: 'subscriptionsManagement.unlinkInfo.withIE')}">
                                                <i aria-hidden="true" class="${Icon.CMD.UNLINK}"></i>
                                                <div class="menu">
                                                    <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'withIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                                                            data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        Paket auf Elternebene entknüpfen. Auf Einrichtungsebene Paket <strong>behalten</strong>.
                                                    </g:link>
                                                    <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'childWithIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name]) + ' ' + message(code: "confirm.dialog.unlink.subscription.package.consortia")}"
                                                            data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        Paket auf Elternebene entknüpfen. Auf Einrichtungsebene Paket <strong>löschen</strong>.
                                                    </g:link>
                                                </div>
                                            </div>
                                            <div class="or" data-text="|"></div>
                                            <div class="ui simple dropdown negative icon button la-modern-button ${unlinkDisabled}" data-content="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}">
                                                <i aria-hidden="true" class="${Icon.CMD.ERASE}"></i>
                                                <div class="menu">
                                                    <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'onlyIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name])}"
                                                            data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        Titel auf Elternebene löschen. Auf Einrichtungsebene Titel <strong>behalten</strong>.
                                                    </g:link>
                                                    <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'childOnlyIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name]) + ' ' + message(code: "confirm.dialog.unlink.subscription.titles.consortia")}"
                                                            data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        Titel auf Elternebene löschen. Auf Einrichtungsebene Titel <strong>löschen</strong>.
                                                    </g:link>
                                                </div>
                                            </div>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <div class="ui buttons">
                                            <g:if test="${unlinkDisabled}">
                                                <span class="la-popup-tooltip" data-content="${unlinkDisabledTooltip}">
                                                    <g:link controller="subscription"
                                                            action="unlinkPackage"
                                                            params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'withIE']}"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                                                            data-confirm-term-how="delete"
                                                            class="${Btn.MODERN.NEGATIVE_CONFIRM} ${unlinkDisabled}"
                                                            role="button"
                                                            aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        <i aria-hidden="true" class="${Icon.CMD.UNLINK}"></i>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription"
                                                        action="unlinkPackage"
                                                        params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'withIE']}"
                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                                                        data-confirm-term-how="delete"
                                                        data-content="${message(code: 'subscriptionsManagement.unlinkInfo.withIE')}"
                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM_TOOLTIP}"
                                                        role="button"
                                                        aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                    <i aria-hidden="true" class="${Icon.CMD.UNLINK}"></i>
                                                </g:link>
                                            </g:else>
                                            <div class="or" data-text="|"></div>
                                            <g:if test="${unlinkDisabled}">
                                                <span class="la-popup-tooltip" data-content="${unlinkDisabledTooltip}">
                                                    <g:link controller="subscription"
                                                            action="unlinkPackage"
                                                            params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'onlyIE']}"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name])}"
                                                            data-confirm-term-how="delete"
                                                            class="${Btn.MODERN.NEGATIVE_CONFIRM} ${unlinkDisabled}"
                                                            role="button"
                                                            aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                        <i aria-hidden="true" class="${Icon.CMD.ERASE}"></i>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription"
                                                        action="unlinkPackage"
                                                        params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'onlyIE']}"
                                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name])}"
                                                        data-confirm-term-how="delete"
                                                        data-content="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}"
                                                        class="${Btn.MODERN.NEGATIVE_CONFIRM_TOOLTIP}"
                                                        role="button"
                                                        aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                    <i aria-hidden="true" class="${Icon.CMD.ERASE}"></i>
                                                </g:link>
                                            </g:else>
                                        </div>
                                    </g:else>
                                </g:if>

                                <g:if test="${subscription.packages.size() > 1}">
                                    <a class="${Btn.SIMPLE} right floated" data-href="#showPackagesModal" data-ui="modal"><g:message
                                            code="subscription.details.details.package.label"/></a>
                                </g:if>

                                <g:if test="${subscription.packages.size() == 1}">
                                    <g:link class="${Btn.SIMPLE} right floated" controller="package" action="show"
                                            id="${subscription.packages[0].pkg.id}"><g:message
                                            code="subscription.details.details.package.label"/></g:link>
                                </g:if>
                            </div>
                        </div>
                    </g:else>
                </div>
                <div class="ui fluid segment content">
                    <div class="ui grid">
                        <div class="eight wide column">
                            <dl>
                                <dt>${message(code: 'default.status.label')}</dt>
                                <dd>${sp.pkg.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <g:if test="${packageInstanceRecord}">
                                <dl>
                                    <dt>${message(code: 'package.show.altname')}</dt>
                                    <dd>
                                        <div class="ui list">
                                            <g:each in="${packageInstanceRecord.altname}" var="altname">
                                              <div class="item">
                                                  <i class="${Icon.SYM.ALTNAME} grey"></i>
                                                  <div class="content">
                                                    ${altname}
                                                  </div>
                                              </div>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.curatoryGroup.label')}</dt>
                                    <dd>
                                        <div class="ui list">
                                            <g:each in="${packageInstanceRecord.curatoryGroups}" var="curatoryGroup">
                                                <div class="item">
                                                    <div class="content">
                                                        ${curatoryGroup.name} ${curatoryGroup.type ? "(${curatoryGroup.type})" : ""}
                                                        <ui:wekbIconLink type="curatoryGroup" gokbId="${curatoryGroup.curatoryGroup}"/>
                                                    </div>
                                                </div>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.lastUpdated.label')}</dt>
                                    <dd>
                                        <g:if test="${packageInstanceRecord.lastUpdatedDisplay}">
                                            <g:formatDate formatName="default.date.format.notime"
                                                          date="${DateUtils.parseDateGeneric(packageInstanceRecord.lastUpdatedDisplay)}"/>
                                        </g:if>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.breakable')}</dt>
                                    <dd>${packageInstanceRecord.breakable ? RefdataValue.getByValueAndCategory(packageInstanceRecord.breakable, RDConstants.PACKAGE_BREAKABLE).getI10n("value") : message(code: 'default.not.available')}</dd>
                                </dl>
                            </g:if>
                            <dl>
                                <dt>${message(code: 'package.contentType.label')}</dt>
                                <dd>${sp.pkg.contentType?.getI10n("value")}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.file')}</dt>
                                <dd>${sp.pkg.file?.getI10n("value")}</dd>
                            </dl>
                            <%--
                            <g:if test="${packageInstanceRecord.source}">
                                <dl>
                                    <dt>${message(code: 'package.source.label')}</dt>
                                    <dd>
                                        ${packageInstanceRecord.source.name}<ui:linkWithIcon href="${editUrl}resource/show/${packageInstanceRecord.source.uuid}"/>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="package.source.url.label"/></dt>
                                    <dd>
                                        <g:if test="${packageInstanceRecord.source.url}">
                                            <g:message code="package.source.url"/><ui:linkWithIcon target="_blank" href="${packageInstanceRecord.source.url}"/>
                                        </g:if>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="package.source.frequency"/></dt>
                                    <dd>${packageInstanceRecord.source.frequency ? RefdataValue.getByValueAndCategory(packageInstanceRecord.source.frequency, RDConstants.PLATFORM_STATISTICS_FREQUENCY)?.getI10n('value') : packageInstanceRecord.source.frequency}</dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="package.source.automaticUpdates"/></dt>
                                    <dd>${Boolean.valueOf(packageInstanceRecord.source.automaticUpdates) ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="package.source.lastRun"/></dt>
                                    <dd>
                                        <g:if test="${packageInstanceRecord.source.lastRun}">
                                            <g:set var="sourceLastRun" value="${DateUtils.parseDateGeneric(packageInstanceRecord.source.lastRun)}"/>
                                            <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${sourceLastRun}"/>
                                        </g:if>
                                    </dd>
                                </dl>
                            </g:if>
                            <g:else>
                                <dl>
                                    <dt><g:message code="package.source.automaticUpdates"/></dt>
                                    <dd><g:message code="package.index.result.noAutomaticUpdates"/></dd>
                                </dl>
                            </g:else>
                            --%>
                        </div>
                        <g:if test="${packageInstanceRecord}">
                            <div class="eight wide column">
                                <dl>
                                    <dt>${message(code: 'package.scope.label')}</dt>
                                    <dd>
                                        ${packageInstanceRecord.scope ? RefdataValue.getByValueAndCategory(packageInstanceRecord.scope, RDConstants.PACKAGE_SCOPE).getI10n("value") : message(code: 'default.not.available')}
                                        <g:if test="${packageInstanceRecord.scope == RDStore.PACKAGE_SCOPE_NATIONAL.value}">
                                            <dl>
                                                <dt>${message(code: 'package.nationalRange.label')}</dt>
                                                <g:if test="${packageInstanceRecord.nationalRanges}">
                                                    <dd>
                                                        <div class="ui bulleted list">
                                                            <g:each in="${packageInstanceRecord.nationalRanges}"
                                                                    var="nr">
                                                                <div class="item">${RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY) ? RefdataValue.getByValueAndCategory(nr.value, RDConstants.COUNTRY).getI10n('value') : nr}</div>
                                                            </g:each>
                                                        </div>
                                                    </dd>
                                                </g:if>
                                            </dl>
                                            <dl>
                                                <dt>${message(code: 'package.regionalRange.label')}</dt>
                                                <g:if test="${packageInstanceRecord.regionalRanges}">
                                                    <dd>
                                                        <div class="ui bulleted list">
                                                            <g:each in="${packageInstanceRecord.regionalRanges}"
                                                                    var="rr">
                                                                <div class="item">${RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE) ? RefdataValue.getByValueAndCategory(rr.value, RDConstants.REGIONS_DE).getI10n('value') : rr}</div>
                                                            </g:each>
                                                        </div>
                                                    </dd>
                                                </g:if>
                                            </dl>
                                        </g:if>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.paymentType.label')}</dt>
                                    <dd>${packageInstanceRecord.paymentType ? RefdataValue.getByValueAndCategory(packageInstanceRecord.paymentType, RDConstants.PAYMENT_TYPE).getI10n("value") : message(code: 'default.not.available')}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.openAccess.label')}</dt>
                                    <dd>${packageInstanceRecord.openAccess ? RefdataValue.getByValueAndCategory(packageInstanceRecord.openAccess, RDConstants.LICENSE_OA_TYPE)?.getI10n("value") : RDStore.LICENSE_OA_TYPE_EMPTY.getI10n("value")}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.freeTrial.label')}</dt>
                                    <dd>${packageInstanceRecord.freeTrial ? RefdataValue.getByValueAndCategory(packageInstanceRecord.freeTrial, RDConstants.Y_N).getI10n("value") : message(code: 'default.not.available')}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.freeTrialPhase.label')}</dt>
                                    <dd>${packageInstanceRecord.freeTrialPhase ?: message(code: 'default.not.available')}</dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.ddc.label')}</dt>
                                    <dd>
                                        <div class="ui list">
                                            <g:each in="${packageInstanceRecord.ddcs}" var="ddc">
                                                <div class="item">
                                                    <i class="sitemap grey icon"></i>

                                                    <div class="content">
                                                        ${RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC) ? RefdataValue.getByValueAndCategory(ddc.value, RDConstants.DDC).getI10n('value') : message(code: 'package.ddc.invalid')}
                                                    </div>
                                                </div>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt>${message(code: 'package.archivingAgency.label')}</dt>
                                    <dd>
                                        <div class="ui list">
                                            <g:each in="${packageInstanceRecord.packageArchivingAgencies}"
                                                    var="arcAgency">
                                                <div class="item">
                                                    <i class="archive grey icon"></i>

                                                    <div class="content">
                                                        <div class="header">${arcAgency.archivingAgency ? RefdataValue.getByValueAndCategory(arcAgency.archivingAgency, RDConstants.ARCHIVING_AGENCY).getI10n("value") : message(code: 'package.archivingAgency.invalid')}</div>

                                                        <div class="description">${message(code: 'package.archivingAgency.openAccess.label')}: ${arcAgency.openAccess ? RefdataValue.getByValueAndCategory(arcAgency.openAccess, RDConstants.Y_N_P).getI10n("value") : ""}</div>

                                                        <div class="description">${message(code: 'package.archivingAgency.postCancellationAccess.label')}: ${arcAgency.postCancellationAccess ? RefdataValue.getByValueAndCategory(arcAgency.postCancellationAccess, RDConstants.Y_N_P).getI10n("value") : ""}</div>
                                                    </div>
                                                </div>
                                            </g:each>
                                        </div>
                                    </dd>
                                </dl>
                            </div>
                        </g:if>
                        <%--
                        <div class="nine wide column">
                            <g:form controller="subscription" action="setupPendingChangeConfiguration" params="[id:sp.subscription.id,pkg:sp.pkg.id]">
                                <h5 class="ui header">
                                    <g:message code="subscription.packages.config.label" args="${[sp.pkg.name]}"/>
                                </h5>
                                <table class="ui table stackable la-noSticky">
                                    <thead>
                                    <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                                        <tr>
                                            <th></th>
                                            <th></th>
                                            <th></th>
                                            <th class="control-label la-border-left" colspan="2">
                                                <g:message code="subscription.details.linkPackage.children.label"/>
                                            </th>
                                        </tr>
                                    </g:if>
                                    <tr>
                                        <th class="control-label"><g:message code="subscription.packages.changeType.label"/></th>
                                        <th class="control-label">
                                            <g:message code="subscription.packages.setting.label"/>
                                        </th>
                                        <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                                            <th class="control-label">
                                                <span class="la-popup-tooltip" data-content="${message(code:"subscription.packages.notification.label")}">
                                                    <i class="ui large icon bullhorn"></i>
                                                </span>
                                            </th>
                                            <th class="control-label la-border-left" >
                                                <span class="la-popup-tooltip" data-content="${message(code:'subscription.packages.auditable')}">
                                                    <i class="${Icon.SIG.INHERITANCE} large"></i>
                                                </span>
                                            </th>
                                            <th class="control-label">
                                                <span class="la-popup-tooltip" data-content="${message(code:'subscription.packages.notification.auditable')}">
                                                    <i class="ui large icon bullhorn"></i>
                                                </span>
                                            </th>
                                        </g:if>
                                    </tr>
                                    </thead>
                                    <g:set var="excludes" value="${PendingChangeConfiguration.NOTIFICATION_ONLY}"/>
                                    <g:each in="${PendingChangeConfiguration.SETTING_KEYS}" var="settingKey">
                                        <%
                                            PendingChangeConfiguration pcc = sp.getPendingChangeConfig(settingKey)
                                        %>
                                        <tr>
                                            <th class="control-label">
                                                <g:message code="subscription.packages.${settingKey}"/>
                                            </th>
                                            <td>
                                                <g:if test="${subscription.instanceOf}">
                                                    ${(pcc && pcc.settingValue) ? pcc.settingValue.getI10n("value") : "Einstellung wird nicht geerbt"}
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${!(settingKey in excludes)}">
                                                        <g:if test="${editmode}">
                                                            <ui:select class="ui fluid dropdown"
                                                                       name="${settingKey}!§!setting" from="${RefdataCategory.getAllRefdataValues(RDConstants.PENDING_CHANGE_CONFIG_SETTING)}"
                                                                       optionKey="id" optionValue="value"
                                                                       value="${(pcc && pcc.settingValue) ? pcc.settingValue.id : RDStore.PENDING_CHANGE_CONFIG_PROMPT.id}"
                                                            />
                                                        </g:if>
                                                        <g:else>
                                                            ${(pcc && pcc.settingValue) ? pcc.settingValue.getI10n("value") : RDStore.PENDING_CHANGE_CONFIG_PROMPT.getI10n("value")}
                                                        </g:else>
                                                    </g:if>
                                                </g:else>
                                            </td>
                                            <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                                                <td>
                                                    <g:if test="${editmode}">
                                                        <g:checkBox class="ui checkbox" name="${settingKey}!§!notification" checked="${pcc?.withNotification}"/>
                                                    </g:if>
                                                    <g:else>
                                                        ${(pcc && pcc.withNotification) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                    </g:else>
                                                </td>
                                                <td class="la-border-left">
                                                    <g:if test="${!(settingKey in excludes)}">
                                                        <g:if test="${editmode}">
                                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!auditable" checked="${pcc ? auditService.getAuditConfig(subscription, settingKey) : false}"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${pcc && auditService.getAuditConfig(subscription, settingKey) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                        </g:else>
                                                    </g:if>
                                                </td>
                                                <td>
                                                    <g:if test="${!(settingKey in excludes)}">
                                                        <g:if test="${editmode}">
                                                            <g:checkBox class="ui checkbox" name="${settingKey}!§!notificationAudit" checked="${pcc ? auditService.getAuditConfig(subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX) : false}"/>
                                                        </g:if>
                                                        <g:else>
                                                            ${pcc && auditService.getAuditConfig(subscription, settingKey+PendingChangeConfiguration.NOTIFICATION_SUFFIX) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                        </g:else>
                                                    </g:if>
                                                </td>
                                            </g:if>
                                        </tr>
                                    </g:each>
                                    <tr>
                                        <th class="control-label">
                                            <g:message code="subscription.packages.freezeHolding"/> <span class="la-popup-tooltip" data-content="${message(code: 'subscription.packages.freezeHolding.expl')}"><i class="${Icon.TOOLTIP.HELP}"></i></span>
                                        </th>
                                        <td>
                                            <g:if test="${!subscription.instanceOf}">
                                                <g:if test="${editmode}">
                                                    <g:checkBox class="ui checkbox" name="freezeHolding" checked="${sp.freezeHolding}"/>
                                                </g:if>
                                                <g:else>
                                                    ${sp.freezeHolding ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                </g:else>
                                            </g:if>
                                            <g:else>
                                                <g:set var="parentSp" value="${SubscriptionPackage.findBySubscriptionAndPkg(subscription.instanceOf, sp.pkg)}"/>
                                                ${parentSp.freezeHolding ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                            </g:else>
                                        </td>

                                            <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                                                <td class="la-border-left">
                                                    <g:if test="${editmode}">
                                                        <g:checkBox class="ui checkbox" name="freezeHoldingAudit" checked="${auditService.getAuditConfig(subscription, SubscriptionPackage.FREEZE_HOLDING)}"/>
                                                    </g:if>
                                                    <g:else>
                                                        ${auditService.getAuditConfig(subscription, SubscriptionPackage.FREEZE_HOLDING) ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")}
                                                    </g:else>
                                                </td>
                                                <td></td>
                                            </g:if>

                                    </tr>
                                    <g:if test="${editmode && !subscription.instanceOf}">
                                        <tr>
                                            <td colspan="2" class="control-label"><g:submitButton class="${Btn.PRIMARY}" name="${message(code:'subscription.packages.submit.label')}"/></td>
                                            <g:set var="now" value="${new Date()}"/>
                                            <td colspan="2" class="control-label">
                                                <g:if test="${subscription.endDate < now}">
                                                    <%
                                                        boolean disabled = false
                                                        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
                                                        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
                                                        threadArray.each { Thread thread ->
                                                            if (thread.name == 'PackageTransfer_' + subscription.id) {
                                                                disabled = true
                                                            }
                                                        }
                                                    %>
                                                    <g:if test="${!disabled}">
                                                        <g:link controller="subscription" action="resetHoldingToSubEnd" class="ui button negative js-open-confirm-modal la-popup-tooltip"
                                                                params="[id: subscription.id, subPkg: sp.id]"
                                                                data-confirm-tokenMsg="${message(code: "confirm.dialog.resetSubToEndDate")}"
                                                                data-confirm-term-how="ok">
                                                            <g:message code="subscription.packages.resetToSubEnd.label"/>
                                                        </g:link>
                                                    </g:if>
                                                    <g:else>
                                                        <g:link class="${Btn.NEGATIVE_TOOLTIP} disabled"
                                                                data-content="${message(code: 'subscription.packages.resetToSubEnd.threadRunning')}">
                                                            <g:message code="subscription.packages.resetToSubEnd.label"/>
                                                        </g:link>
                                                    </g:else>
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:if>
                                </table>
                            </g:form>
                        </div>
                    --%>
                        </div>
                    </div>
                </div>
            </g:each>
        </div>
    </div><!-- .content -->

    <ui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
        <div class="ui ordered list">
            <g:each in="${subscription.packages.sort { it.pkg.name.toLowerCase() }}" var="subPkg">
                <div class="item">
                    ${subPkg.pkg.name}
                    <g:if test="${subPkg.pkg.provider}">
                        (${subPkg.pkg.provider.name})
                    </g:if>:
                    <g:link controller="package" action="show" id="${subPkg.pkg.id}"><g:message
                            code="subscription.details.details.package.label"/></g:link>
                </div>
            </g:each>
        </div>

    </ui:modal>
</div>
