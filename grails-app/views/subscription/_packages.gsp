<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.PendingChangeConfiguration; de.laser.wekb.Platform; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.finance.CostItem; de.laser.Org; de.laser.IssueEntitlement" %>
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
                        <div class="ui sixteen equal width column stackable internally grid">
                            <div class="row">
                                <div class="column">
                                    <g:link controller="package" action="show" id="${sp.pkg.id}">${sp.pkg.name}</g:link>
                                    <ui:wekbIconLink type="package" gokbId="${sp.pkg.gokbId}"/>
                                    <br>
                                    ${sp.getCurrentIssueEntitlementCountOfPackage()} <g:message code="subscription.packages.currentTitles"/>
                                </div>

                                <div class="seven wide right aligned column">
                                    <g:if test="${sp.pkg.nominalPlatform}">
                                        <i aria-hidden="true" class="${Icon.PLATFORM} grey la-popup-tooltip" data-content="${message(code: 'platform.label')}"></i>
                                        <g:link controller="platform" action="show" id="${sp.pkg.nominalPlatform.id}">${sp.pkg.nominalPlatform.name}</g:link>
                                        <ui:linkWithIcon href="${sp.pkg.nominalPlatform.primaryUrl?.startsWith('http') ? sp.pkg.nominalPlatform.primaryUrl : 'http://' + sp.pkg.nominalPlatform.primaryUrl}"/>
                                        <ui:wekbIconLink type="platform" gokbId="${sp.pkg.nominalPlatform.gokbId}"/>
                                    </g:if>
                                </div>
                            </div>
                            <div class="row">
                                <div class="column">
                                    <div>
                                        <g:if test="${sp.pkg.provider}">
                                            <i aria-hidden="true" class="${Icon.PROVIDER} grey la-popup-tooltip" data-content="${message(code: 'provider.label')}"></i>
                                            <g:link controller="provider" action="show" id="${sp.pkg.provider.id}">${sp.pkg.provider.name}</g:link>
                                            <g:if test="${sp.pkg.provider.homepage}"><ui:linkWithIcon href="${sp.pkg.provider.homepage.startsWith('http') ? sp.pkg.provider.homepage : 'http://' + sp.pkg.provider.homepage}"/></g:if>
                                            <g:if test="${sp.pkg.provider.gokbId}"><ui:wekbIconLink type="provider" gokbId="${sp.pkg.provider.gokbId}"/></g:if>
                                        </g:if>
                                    </div>
                                    <%-- deactivated as of ERMS-6375 after call with Micha
                                    <g:each in="${sp.pkg.vendors}" var="pv">
                                        <g:set var="vendorRecord" value="${packageInstanceRecord.vendors.find { rec -> rec.vendorUuid == pv.vendor.gokbId }}"/>
                                        <div>
                                            <i aria-hidden="true" class="${Icon.VENDOR} grey la-popup-tooltip" data-content="${message(code: 'vendor.label')}"></i>
                                            <g:link controller="vendor" action="show" id="${pv.vendor.id}">${pv.vendor.name}</g:link>
                                            <g:if test="${vendorRecord && vendorRecord.homepage}"><ui:linkWithIcon href="${vendorRecord.homepage.startsWith('http') ? vendorRecord.homepage : 'http://' + vendorRecord.homepage}"/></g:if>
                                            <g:if test="${pv.vendor.gokbId}"><ui:wekbIconLink type="vendor" gokbId="${pv.vendor.gokbId}"/></g:if>
                                        </div>
                                    </g:each> --%>
                                </div>
                                <div class="seven wide right aligned column">
                                    <g:if test="${editmode}">
                                        <div class="${Btn.MODERN.SIMPLE_TOOLTIP} ${buttonColor}"
                                             data-content="${message(code:'subscription.packages.config.header')}">
                                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                                        </div>
                                        <%
                                            String unlinkDisabled = '', unlinkDisabledTooltip = null, unlinkTitlesDisabled = '', unlinkTitlesDisabledTooltip = null
                                            Set<Subscription> blockingCostItems = CostItem.executeQuery('select ci.sub from CostItem ci where (ci.sub = :sub or ci.sub.instanceOf = :sub) and ci.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted', [pkg: sp.pkg, deleted: RDStore.COST_ITEM_DELETED, sub: sp.subscription, context: contextService.getOrg()])
                                            if(showConsortiaFunctions) {
                                                if(subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE) {
                                                    unlinkTitlesDisabled = 'disabled'
                                                    unlinkTitlesDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blockingHoldingEntire")
                                                }
                                                else if (blockingCostItems) {
                                                    unlinkDisabled = 'disabled'
                                                    unlinkDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blockingSubscribersConsortia")
                                                }
                                            }
                                            else {
                                                if(subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE) {
                                                    unlinkTitlesDisabled = 'disabled'
                                                    unlinkTitlesDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blockingHoldingEntire")
                                                }
                                                if(blockingCostItems) {
                                                    unlinkDisabled = 'disabled'
                                                    unlinkDisabledTooltip = message(code: "subscriptionsManagement.unlinkInfo.blocked")
                                                }
                                            }
                                            String btnClass = "item js-open-confirm-modal ${unlinkDisabled}"
                                        %>
                                        <g:if test="${showConsortiaFunctions && !sp.subscription.instanceOf}">
                                            <div class="ui buttons">
                                                <div class="ui simple dropdown clearable negative icon button la-modern-button ${unlinkDisabled}" data-content="${message(code: 'subscriptionsManagement.unlinkInfo.withIE')}">
                                                    <i aria-hidden="true" class="${Icon.CMD.UNLINK}"></i>
                                                    <div class="menu">
                                                        <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'withIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name])}"
                                                                data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                            <g:message code="subscriptionsManagement.unlinkInfo.packageParentOnly"/>
                                                        </g:link>
                                                        <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'childWithIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.package", args: [sp.pkg.name]) + ' ' + message(code: "confirm.dialog.unlink.subscription.package.consortia")}"
                                                                data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                            <g:message code="subscriptionsManagement.unlinkInfo.packageParentWithMembers"/>
                                                        </g:link>
                                                    </div>
                                                </div>
                                                <div class="or" data-text="|"></div>
                                                <g:if test="${unlinkTitlesDisabled}">
                                                    <span class="la-popup-tooltip" data-content="${unlinkTitlesDisabledTooltip}">
                                                        <i aria-hidden="true" class="${Icon.CMD.ERASE}"></i>
                                                    </span>
                                                </g:if>
                                                <g:else>
                                                    <div class="ui simple dropdown clearable negative icon button la-modern-button ${unlinkDisabled}" data-content="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}">
                                                        <i aria-hidden="true" class="${Icon.CMD.ERASE}"></i>
                                                        <div class="menu">
                                                            <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'onlyIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name])}"
                                                                    data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                                <g:message code="subscriptionsManagement.unlinkInfo.titlesParentOnly"/>
                                                            </g:link>
                                                            <g:link controller="subscription" action="unlinkPackage" class="${btnClass}" params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'childOnlyIE']}" data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name]) + ' ' + message(code: "confirm.dialog.unlink.subscription.titles.consortia")}"
                                                                    data-confirm-term-how="delete" role="button" aria-label="${message(code: "ariaLabel.unlink.subscription.package", args: [sp.pkg.name])}">
                                                                <g:message code="subscriptionsManagement.unlinkInfo.titlesParentWithMembers"/>
                                                            </g:link>
                                                        </div>
                                                    </div>
                                                </g:else>
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
                                                    <g:if test="${unlinkTitlesDisabled}">
                                                        <span class="la-popup-tooltip" data-content="${unlinkTitlesDisabledTooltip}">
                                                            <g:link controller="subscription"
                                                                    action="unlinkPackage"
                                                                    params="${[subscription: sp.subscription.id, package: sp.pkg.id, confirmed: 'Y', option: 'onlyIE']}"
                                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.titles", args: [sp.pkg.name])}"
                                                                    data-confirm-term-how="delete"
                                                                    data-content="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}"
                                                                    class="${Btn.MODERN.NEGATIVE_CONFIRM_TOOLTIP} ${unlinkTitlesDisabled}"
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
                                                </g:else>
                                            </div>
                                        </g:else>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </g:else>
                </div>
                <div class="ui fluid segment content">
                    <div class="ui grid">
                        <div class="sixteen wide column">
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
