<%@ page import="de.laser.remote.Wekb; de.laser.addressbook.PersonRole; de.laser.addressbook.Address; de.laser.wekb.Package; de.laser.wekb.Vendor; de.laser.wekb.VendorLink; de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.addressbook.Person; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.addressbook.Contact;" %>

<laser:htmlStart message="${'menu.institutions.vendor.show'}" />

<laser:render template="breadcrumb"/>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[vendor: vendor, user: user]}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${vendor.name}" >
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[vendor: vendor, inContextOrg: inContextOrg]}"/>
<laser:render template="/templates/meta/identifier" model="${[object: vendor, editable: editable]}"/>

<ui:messages data="${flash}"/>
<%-- <laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" /> --%>

<div class="ui stackable grid">
    <div class="eleven wide column">

        <div class="la-inline-lists">
            <div class="ui card" id="js-confirmationCard">
                <div class="content">

                    <dl>
                        <dt class="control-label"><g:message code="default.name.label" /></dt>
                        <dd>
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable"
                                    owner="${vendor}" field="name"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="org.sortname.label" /></dt>
                        <dd>
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable"
                                    owner="${vendor}" field="sortname" overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="altname.plural" /></dt>
                        <dd>
                            <div id="altnames" class="ui accordion la-accordion-showMore la-accordion-altName">
                            <g:if test="${vendor.altnames}">
                                <div class="ui divided middle aligned selection list la-flex-center">
                                    <div class="item title" id="altname_title" data-objId="${genericOIDService.getOID(vendor.altnames[0])}">
                                        <div class="content la-space-right">
                                            <ui:xEditable data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                      data_confirm_term_how="ok"
                                                      class="js-open-confirm-modal-xEditable"
                                                      owner="${vendor.altnames[0]}" field="name"/>
                                        </div>
                                        <g:if test="${editable && !vendor.gokbId}">
                                            <ui:remoteLink role="button" class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: vendor.altnames[0].id]"
                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [vendor.altnames[0].name])}"
                                                           data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(vendor.altnames[0])}')">
                                                <i class="${Icon.CMD.DELETE}"></i>
                                            </ui:remoteLink>
                                        </g:if>
                                        <div class="${Btn.MODERN.SIMPLE_TOOLTIP} la-show-button"
                                             data-content="${message(code: 'altname.showAll')}">
                                            <i class="${Icon.CMD.SHOW_MORE}"></i>
                                        </div>
                                    </div>
                                    <div class="content" style="padding:0">
                                        <g:each in="${vendor.altnames.drop(1)}" var="altname">
                                            <div class="ui item" data-objId="${genericOIDService.getOID(altname)}">
                                                <div class="content la-space-right">
                                                    <ui:xEditable
                                                            data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                            data_confirm_term_how="ok"
                                                            class="js-open-confirm-modal-xEditable"
                                                            owner="${altname}" field="name" overwriteEditable="${editable}"/>
                                                </div>
                                                <g:if test="${editable && !vendor.gokbId}">
                                                    <ui:remoteLink role="button" class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                   data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                   data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                        <i class="${Icon.CMD.DELETE}"></i>
                                                    </ui:remoteLink>
                                                </g:if>
                                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                                    <icon:placeholder/><%-- Hidden Fake Button --%>
                                                </div>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </g:if>
                            </div><!-- #altnames -->
                            <g:if test="${editable && !vendor.gokbId}">
                                <input name="addAltname" id="addAltname" type="button" class="${Btn.SIMPLE} la-js-addListValue" data-objtype="altname" value="${message(code: 'altname.add')}">
                            </g:if>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="vendor.homepage.label"/></dt>
                        <dd>
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                    owner="${vendor}" type="url" field="homepage" />
                            <g:if test="${vendor.homepage}">
                                <ui:linkWithIcon href="${vendor.homepage}" />
                            </g:if>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label"><g:message code="default.status.label"/></dt>
                        <dd>
                            <ui:xEditableRefData owner="${vendor}" field="status" config="${RDConstants.VENDOR_STATUS}" overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <g:if test="${vendor.status == RDStore.VENDOR_STATUS_RETIRED}">
                        <dl>
                            <dt class="control-label">${message(code: 'vendor.retirementDate.label')}</dt>
                            <dd>
                                <g:formatDate date="${vendor.retirementDate}" format="${message(code: 'default.date.format.notime')}"/>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.ordering.header"/></h2>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.ordering.webshop.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="webShopOrders"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.ordering.xml.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="xmlOrders"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.ordering.edi.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="ediOrders"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.ordering.supportedLibrarySystems.label" />
                        </dt>
                        <dd>
                            <%
                                List<RefdataValue> supportedLibrarySystems = RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_SUPPORTED_LIBRARY_SYSTEM)
                            %>
                            <laser:render template="/templates/attributesList"
                                          model="${[ownObj: vendor, deleteAction: 'deleteAttribute', attributes: vendor.supportedLibrarySystems, field: 'librarySystem', availableAttributeIds: supportedLibrarySystems.collect { RefdataValue rdv -> rdv.id }, editable: editable && !vendor.gokbId]}"/>

                            <laser:render template="/templates/attributesModal"
                                          model="${[ownObj: vendor, addAction: 'addAttribute', modalId: 'librarySystem', buttonText: 'vendor.ordering.supportedLibrarySystems.add', label: 'vendor.ordering.supportedLibrarySystems.label', field: 'librarySystem', availableAttributes: supportedLibrarySystems, editable: editable && !vendor.gokbId]}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.ordering.electronicDeliveryDelayNotifications.label" />
                        </dt>
                        <dd>
                            <%
                                List<RefdataValue> electronicDeliveryDelayNotifications = RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_ELECTRONIC_DELIVERY_DELAY)
                            %>
                            <laser:render template="/templates/attributesList"
                                          model="${[ownObj: vendor, deleteAction: 'deleteAttribute', attributes: vendor.electronicDeliveryDelays, field: 'delayNotification', availableAttributeIds: electronicDeliveryDelayNotifications.collect { RefdataValue rdv -> rdv.id }, editable: editable && !vendor.gokbId]}"/>

                            <laser:render template="/templates/attributesModal"
                                          model="${[ownObj: vendor, addAction: 'addAttribute', modalId: 'delayNotification', buttonText: 'vendor.ordering.electronicDeliveryDelayNotifications.add', label: 'vendor.ordering.electronicDeliveryDelayNotifications.label', field: 'delayNotification', availableAttributes: electronicDeliveryDelayNotifications, editable: editable && !vendor.gokbId]}"/>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.invoicing.header"/></h2>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.formats.label" />
                        </dt>
                        <dd>
                            <%
                                List<RefdataValue> invoicingFormats = RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_FORMAT)
                            %>
                            <laser:render template="/templates/attributesList"
                                          model="${[ownObj: vendor, deleteAction: 'deleteAttribute', attributes: vendor.electronicBillings, field: 'invoicingFormat', availableAttributeIds: invoicingFormats.collect { RefdataValue rdv -> rdv.id }, editable: editable && !vendor.gokbId]}"/>

                            <laser:render template="/templates/attributesModal"
                                          model="${[ownObj: vendor, addAction: 'addAttribute', modalId: 'electronicBilling', buttonText: 'vendor.invoicing.formats.add', label: 'vendor.invoicing.formats.label', field: 'invoicingFormat', availableAttributes: invoicingFormats, editable: editable && !vendor.gokbId]}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.dispatch.label" />
                        </dt>
                        <dd>
                            <%
                                List<RefdataValue> invoiceDispatchs = RefdataCategory.getAllRefdataValues(RDConstants.VENDOR_INVOICING_DISPATCH)
                            %>
                            <laser:render template="/templates/attributesList"
                                          model="${[ownObj: vendor, deleteAction: 'deleteAttribute', attributes: vendor.invoiceDispatchs, field: 'invoiceDispatch', availableAttributeIds: invoiceDispatchs.collect { RefdataValue rdv -> rdv.id }, editable: editable && !vendor.gokbId]}"/>

                            <laser:render template="/templates/attributesModal"
                                          model="${[ownObj: vendor, addAction: 'addAttribute', modalId: 'invoiceDispatch', buttonText: 'vendor.invoicing.dispatch.add', label: 'vendor.invoicing.dispatch.label', field: 'invoiceDispatch', availableAttributes: invoiceDispatchs, editable: editable && !vendor.gokbId]}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.paperInvoice.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="paperInvoice"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.managementOfCredits.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="managementOfCredits"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.compensationPayments.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="processingOfCompensationPayments"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.invoicing.individualInvoiceDesign.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="individualInvoiceDesign"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.general.header"/></h2>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.technicalSupport.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="technicalSupport"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.metadata.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="shippingMetadata"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.usageStats.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="forwardingUsageStatisticsFromPublisher"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.newReleaseInformation.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="activationForNewReleases"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.exchangeIndividualTitles.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="exchangeOfIndividualTitles"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.general.researchPlatform.label" />
                        </dt>
                        <dd>
                            <ui:xEditable data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                          data_confirm_term_how="ok"
                                          class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                          owner="${vendor}" type="url" field="researchPlatformForEbooks" overwriteEditable="${editable && !vendor.gokbId}"/>
                            <g:if test="${vendor.researchPlatformForEbooks}">
                                <ui:linkWithIcon href="${vendor.researchPlatformForEbooks}"/>
                            </g:if>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.supplier.header"/></h2>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.supplier.prequalification.label" />
                        </dt>
                        <dd>
                            <ui:xEditableBoolean data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                 data_confirm_term_how="ok"
                                                 class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                                 owner="${vendor}" field="prequalification"  overwriteEditable="${editable && !vendor.gokbId}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt class="control-label">
                            <g:message code="vendor.supplier.infoPrequalification.label" />
                        </dt>
                        <dd>
                            <ui:xEditable data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                          data_confirm_term_how="ok"
                                          class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                          owner="${vendor}" field="prequalificationInfo"  overwriteEditable="${editable && !vendor.gokbId}"/>
                            <g:if test="${vendor.prequalificationInfo}">
                                <ui:linkWithIcon href="${vendor.prequalificationInfo}"/>
                            </g:if>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <g:if test="${links || editable}">
                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header"><g:message code="vendor.retirementLinking.label"/></h2>
                        <g:if test="${links}">
                            <table class="ui three column table">
                                <g:each in="${links}" var="row">
                                    <%
                                        String[] linkTypes = RDStore.PROVIDER_LINK_FOLLOWS.getI10n('value').split('\\|')
                                        int perspectiveIndex
                                        Vendor pair
                                        if(vendor == row.from) {
                                            perspectiveIndex = 0
                                            pair = row.to
                                        }
                                        else if(vendor == row.to) {
                                            perspectiveIndex = 1
                                            pair = row.from
                                        }
                                    %>
                                    <g:if test="${pair != null}">
                                        <th scope="row" class="control-label">${linkTypes[perspectiveIndex]}</th>
                                        <td><g:link action="show" id="${pair.id}">${pair.name}</g:link></td>
                                        <td class="right aligned">
                                            <g:if test="${subEditor}">
                                                <span class="la-popup-tooltip" data-content="${message(code:'license.details.unlink')}">
                                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.general")}"
                                                            data-confirm-term-how="unlink"
                                                            action="unlinkProviderVendor" params="[id: vendor.id, combo: row.id]"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                        <i class="${Icon.CMD.UNLINK}"></i>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                        </td>
                                    </g:if>
                                </g:each>
                            </table>
                        </g:if>
                        <g:if test="${editable}">
                            <div class="ui la-vertical buttons">
                                <%
                                    Map<String,Object> model = [tmplText:message(code: 'vendor.linking.addLink'),
                                                                tmplID:'addLink',
                                                                tmplButtonText:message(code: 'vendor.linking.addLink'),
                                                                tmplModalID:'ven_add_link',
                                                                editmode: editable,
                                                                linkInstanceType: VendorLink.class.name,
                                                                context: vendor
                                    ]
                                %>
                                <laser:render template="/templates/links/subLinksModal"
                                              model="${model}" />
                            </div>
                        </g:if>
                    </div>
                </div>
            </g:if>

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.general.objects.label"/></h2>
                    <div class="ui accordion">
%{--                        <h2 class="ui header"><g:message code="vendor.general.objects.label"/></h2>--}%
                        <div class="title">
                            <i class="dropdown icon la-dropdown-accordion"></i>
                            <div class="ui horizontal relaxed list">
                                <div class="item">
                                    <strong><g:message code="package.plural" /></strong>
                                    &nbsp;<ui:bubble count="${allPackages.size()}" />
                                </div>
                                <div class="item">
                                    <strong><g:message code="provider.plural" /></strong>
                                    &nbsp;<ui:bubble count="${providers.size()}" />
                                </div>
                            </div>
                        </div>
                        <div class="content">
                            <p class="ui header"><g:message code="package.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${allPackages}" var="pkg">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>

                            <p class="ui header"><g:message code="provider.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${providers}" var="provider">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="provider" action="show" id="${provider.id}">${provider.name}</g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.my.objects.label"/></h2>
                    <div class="ui accordion">
%{--                        <h2 class="ui header"><g:message code="vendor.my.objects.label"/></h2>--}%
                        <div class="title">
                            <i class="dropdown icon la-dropdown-accordion"></i>
                            <div class="ui horizontal relaxed list">
                                <div class="item">
                                    <strong><g:message code="org.platforms.label" /></strong>
                                    &nbsp;<ui:bubble count="${platforms.size()}" />
                                </div>
                                <div class="item">
                                    <strong><g:message code="package.plural" /></strong>
                                    &nbsp;<ui:bubble count="${packages.size()}" />
                                </div>
                                <div class="item">
                                    <strong><g:message code="subscription.plural" /></strong>
                                    &nbsp;<ui:bubble count="${currentSubscriptionsCount}/${subLinks}" />
                                </div>
                                <div class="item">
                                    <strong><g:message code="license.plural" /></strong>
                                    &nbsp;<ui:bubble count="${currentLicensesCount}/${licLinks}" />
                                </div>
                            </div>
                        </div>
                        <div class="content">
                            <p class="ui header"><g:message code="org.platforms.label" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${platforms}" var="platform">
                                    <g:if test="${platform.status == RDStore.PLATFORM_STATUS_CURRENT}">
                                        <div class="ui item">
                                            <div class="content la-space-right">
                                                <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                                            </div>
                                        </div>
                                    </g:if>
                                </g:each>
                            </div>

                            <p class="ui header"><g:message code="package.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${packages}" var="pkg">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>

                            <p class="ui header"><g:message code="subscription.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <div class="ui item">
                                    <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: vendor.globalUID, status: RDStore.SUBSCRIPTION_CURRENT.id]">
                                        <div class="content la-space-right">
                                            <i class="${Icon.LNK.FILTERED}"></i> <g:message code="subscription.plural.current" />
                                        &nbsp;<ui:bubble count="${currentSubscriptionsCount}" />
                                        </div>
                                    </g:link>
                                </div>
                                <div class="ui item">
                                    <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: vendor.globalUID, status: 'FETCH_ALL']">
                                        <div class="content la-space-right">
                                            <i class="${Icon.LNK.FILTERED}"></i> <g:message code="subscription.plural.total" />
                                        &nbsp;<ui:bubble count="${subLinks}" />
                                        </div>
                                    </g:link>
                                </div>
                            </div>

                            <p class="ui header"><g:message code="license.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <div class="ui item">
                                    <div class="content la-space-right">
                                        <g:link controller="myInstitution" action="currentLicenses" params="[vendor: vendor.id, status: RDStore.LICENSE_CURRENT.id, subStatus: RDStore.SUBSCRIPTION_CURRENT.id, filterSubmit: 'Filtern']">
                                            <i class="${Icon.LNK.FILTERED}"></i> <g:message code="license.plural.current" />
                                            &nbsp;<ui:bubble count="${currentLicensesCount}" />
                                        </g:link>
                                    </div>
                                </div>
                                <div class="ui item">
                                    <div class="content la-space-right">
                                        <g:link controller="myInstitution" action="currentLicenses" params="[vendor: vendor.id, filterSubmit: 'Filtern']">
                                            <i class="${Icon.LNK.FILTERED}"></i> <g:message code="license.plural.total" />
                                            &nbsp;<ui:bubble count="${licLinks}" />
                                        </g:link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <g:if test="${vendor.createdBy || vendor.legallyObligedBy}">
                <div class="ui card">
                    <div class="content">
                        <g:if test="${vendor.createdBy}">
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.createdBy.label" />
                                </dt>
                                <dd>
                                    <h5 class="ui header">
                                        <g:link controller="organisation" action="show" id="${vendor.createdBy.id}">${vendor.createdBy.name}</g:link>
                                    </h5>
                                    <g:if test="${createdByOrgGeneralContacts}">
                                        <g:each in="${createdByOrgGeneralContacts}" var="cbogc">
                                            <laser:render template="/addressbook/person_full_details" model="${[
                                                    person              : cbogc,
                                                    personContext       : vendor.createdBy,
                                                    tmplShowFunctions       : true,
                                                    tmplShowPositions       : true,
                                                    tmplShowResponsiblities : true,
                                                    tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax'],
                                                    editable            : false
                                            ]}"/>
                                        </g:each>
                                    </g:if>
                                </dd>
                            </dl>
                        </g:if>
                        <g:if test="${vendor.legallyObligedBy}">
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.legallyObligedBy.label" />
                                </dt>
                                <dd>
                                    <h5 class="ui header">
                                        <g:link controller="organisation" action="show" id="${vendor.legallyObligedBy.id}">${vendor.legallyObligedBy.name}</g:link>
                                    </h5>
                                    <g:if test="${legallyObligedByOrgGeneralContacts}">
                                        <g:each in="${legallyObligedByOrgGeneralContacts}" var="lobogc">
                                            <laser:render template="/addressbook/person_full_details" model="${[
                                                    person              : lobogc,
                                                    personContext       : vendor.legallyObligedBy,
                                                    tmplShowFunctions       : true,
                                                    tmplShowPositions       : true,
                                                    tmplShowResponsiblities : true,
                                                    tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax'],
                                                    editable            : false
                                            ]}"/>
                                        </g:each>
                                    </g:if>
                                </dd>
                            </dl>
                        </g:if>
                    </div>
                </div><!-- .card -->
            </g:if>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[ vendor: vendor ]}"/>
                </div><!-- #new-dynamic-properties-block -->
            </g:if>
        </div>
    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">
            <%
                Map<String, List> publicTypeAddressMap = [:], privateTypeAddressMap = [:]
                Set<String> typeNames = new TreeSet<String>()
                typeNames.add(RDStore.ADDRESS_TYPE_BILLING.getI10n('value'))
                typeNames.add(RDStore.ADDRESS_TYPE_POSTAL.getI10n('value'))
            %>
            <div id="container-provider">
                <div class="ui card">
                    <div class="content">
                        <div class="header">
                            <div class="ui grid">
                                <div class="twelve wide column">
                                    <g:message code="org.publicContacts.label"/>
                                </div>
                                <div class="right aligned four wide column">

                                </div>
                            </div>
                        </div>
                        <g:if test="${PersonRole.executeQuery('select pr from Person p join p.roleLinks pr where pr.vendor = :vendor and ((p.isPublic = false and p.tenant = :ctx) or p.isPublic = true)', [vendor: vendor, ctx: contextService.getOrg()]) ||
                                Address.executeQuery('select a from Address a where a.vendor = :vendor and (a.tenant = :ctx or a.tenant = null)', [vendor: vendor, ctx: contextService.getOrg()])}">
                            <table class="ui compact table">
                                <g:set var="vendorContacts" value="${vendorService.getContactPersonsByFunctionType(vendor, true, null)}"/>
                                <tr>
                                    <td>
                                        <g:if test="${vendorContacts}">
                                            <div class="ui segment la-timeLineSegment-contact">
                                                <div class="la-timeLineGrid">
                                                    <div class="ui grid">
                                                        <g:each in="${vendorContacts}" var="prs">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${prs.isPublic}">
                                                                        <a target="_blank" href="${Wekb.getURL() + '/public/orgContent/' + vendor.gokbId}"><i class="${Icon.WEKB} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${prs.roleLinks.collect { PersonRole pr -> pr.roleType.getI10n('value')}.join (' / ')}</div>
                                                                    <g:if test="${!(prs.last_name in [RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value'), RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_STATS_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_METADATA.getI10n('value')])}"><div class="ui header">${prs}</div></g:if>
                                                                    <g:if test="${prs.contacts}">
                                                                        <g:each in="${prs.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/addressbook/contact" model="${[
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                </div>
                                                            </div>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </div>
                                        </g:if>
                                    </td>
                                </tr>
                                <%
                                    vendor.addresses.each { Address a ->
                                        a.type.each { type ->
                                            String typeName = type.getI10n('value')
                                            typeNames.add(typeName)
                                            if(!a.tenant) {
                                                List addresses = publicTypeAddressMap.get(typeName) ?: []
                                                addresses.add(a)
                                                publicTypeAddressMap.put(typeName, addresses)
                                            }
                                            else if(a.tenant.id == contextService.getOrg().id) {
                                                List addresses = privateTypeAddressMap.get(typeName) ?: []
                                                addresses.add(a)
                                                privateTypeAddressMap.put(typeName, addresses)
                                            }
                                        }
                                    }
                                %>
                                <g:if test="${publicTypeAddressMap}">
                                    <tr>
                                        <td>
                                            <div class="ui segment la-timeLineSegment-contact">
                                                <div class="la-timeLineGrid">
                                                    <div class="ui grid">
                                                        <g:each in="${typeNames}" var="typeName">
                                                            <% List publicAddresses = publicTypeAddressMap.get(typeName) %>
                                                            <g:if test="${publicAddresses}">
                                                                <div class="row">
                                                                    <div class="two wide column">

                                                                    </div>
                                                                    <div class="fourteen wide column">
                                                                        <div class="ui label">${typeName}</div>
                                                                        <g:each in="${publicAddresses}" var="a">
                                                                            <g:if test="${a.vendor}">
                                                                                <laser:render template="/addressbook/address" model="${[
                                                                                        hideAddressType     : true,
                                                                                        address             : a,
                                                                                        tmplShowDeleteButton: false,
                                                                                        controller          : 'org',
                                                                                        action              : 'show',
                                                                                        id                  : vendor.id,
                                                                                        editable            : false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </div>
                                                                </div>
                                                            </g:if>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </g:if>
                            </table>
                        </g:if>
                    </div>
                </div>
            </div>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div id="container-contacts">
                    <div class="ui card">
                        <div class="content">
                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        <g:message code="org.contactpersons.and.addresses.my"/>
                                    </div>
                                    <div class="right aligned four wide column">
                                        <a href="#createPersonModal" class="${Btn.MODERN.SIMPLE} createContact" id="contactPersonForAgency" data-ui="modal">
                                            <i aria-hidden="true" class="${Icon.CMD.ADD}"></i>
                                        </a>
                                    </div>
                                </div>
                            </div>
                            <%
                                List visiblePersons = addressbookService.getVisiblePersons("addressbook",[vendor: vendor, sort: 'p.last_name, p.first_name', function: [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.id]])
                                List otherPersons = addressbookService.getVisiblePersons("addressbook",[vendor: vendor, sort: 'p.last_name, p.first_name'])
                            %>
                            <g:if test="${visiblePersons || otherPersons || privateTypeAddressMap}">
                                <table class="ui compact table">
                                    <tr>
                                        <td>
                                            <div class="ui segment la-timeLineSegment-contact">
                                                <div class="la-timeLineGrid">
                                                    <div class="ui grid">
                                                        <g:each in="${visiblePersons}" var="person">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${person.isPublic}">
                                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>
                                                                    <div class="ui header">${person}</div>
                                                                    <g:if test="${person.contacts}">
                                                                        <g:each in="${person.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/addressbook/contact" model="${[
                                                                                        overwriteEditable   : editable,
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: editable
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                </div>
                                                            </div>
                                                        </g:each>
                                                        <g:each in="${otherPersons}" var="person">
                                                            <%
                                                                SortedSet<String> roles = new TreeSet<String>()
                                                                SortedSet<String> responsibilities = new TreeSet<String>()
                                                                Map<String, Object> respMap = [:]
                                                                person.roleLinks.each { PersonRole pr ->
                                                                    if(pr.functionType && pr.functionType != RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)
                                                                        roles << pr.functionType.getI10n('value')
                                                                    if(pr.positionType)
                                                                        roles << pr.positionType.getI10n('value')
                                                                    if(pr.responsibilityType) {
                                                                        responsibilities << pr.responsibilityType.getI10n('value')
                                                                        List respObjects = respMap.containsKey(pr.responsibilityType.getI10n('value')) ? respMap.get(pr.responsibilityType.getI10n('value')) : []
                                                                        List respRef = pr.getReference().split(':')
                                                                        switch(respRef[0]) {
                                                                            case 'sub': Subscription s = Subscription.get(respRef[1])
                                                                                if(s.status == RDStore.SUBSCRIPTION_CURRENT) {
                                                                                    if(contextService.getOrg().isCustomerType_Consortium()) {
                                                                                        if(!s.instanceOf)
                                                                                            respObjects << s
                                                                                    }
                                                                                    else respObjects << s
                                                                                }
                                                                                break
                                                                            case 'lic': License l = License.get(respRef[1])
                                                                                if(l.status == RDStore.LICENSE_CURRENT) {
                                                                                    if(contextService.getOrg().isCustomerType_Consortium()) {
                                                                                        if (!l.instanceOf)
                                                                                            respObjects << l
                                                                                    }
                                                                                    else respObjects << l
                                                                                }
                                                                                break
                                                                            case 'pkg': de.laser.wekb.Package p = de.laser.wekb.Package.get(respRef[1])
                                                                                if(p.packageStatus != RDStore.PACKAGE_STATUS_REMOVED)
                                                                                    respObjects << p
                                                                                break
                                                                        }
                                                                        respMap.put(pr.responsibilityType.getI10n('value'), respObjects)
                                                                    }
                                                                }
                                                            %>
                                                            <g:if test="${roles || responsibilities}">
                                                                <div class="row">
                                                                    <div class="two wide column">
                                                                        <g:if test="${person.isPublic}">
                                                                            <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                                        </g:else>
                                                                    </div>
                                                                    <div class="fourteen wide column">
                                                                        <div class="ui label">${roles.join(' / ')}</div>
                                                                        <div class="ui header">${person}</div>
                                                                        <g:each in="${responsibilities}" var="responsibility">
                                                                            <g:set var="respObjects" value="${respMap.get(responsibility)}"/>
                                                                            <g:if test="${respObjects}">
                                                                                ${responsibility}
                                                                                <g:each in="${respObjects}" var="respObj">
                                                                                    <br>
                                                                                    <g:if test="${respObj instanceof Subscription}">
                                                                                        (<g:link controller="subscription" action="show" id="${respObj.id}">${respObj.name}</g:link>)
                                                                                    </g:if>
                                                                                    <g:elseif test="${respObj instanceof License}">
                                                                                        (<g:link controller="license" action="show" id="${respObj.id}">${respObj.reference}</g:link>)
                                                                                    </g:elseif>
                                                                                    <g:elseif test="${respObj instanceof de.laser.wekb.Package}">
                                                                                        (<g:link controller="package" action="show" id="${respObj.id}">${respObj.name}</g:link>)
                                                                                    </g:elseif>
                                                                                </g:each>
                                                                            </g:if>
                                                                        </g:each>
                                                                        <g:if test="${person.contacts}">
                                                                            <g:each in="${person.contacts.toSorted()}" var="contact">
                                                                                <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                    <laser:render template="/addressbook/contact" model="${[
                                                                                            overwriteEditable   : false,
                                                                                            contact             : contact,
                                                                                            tmplShowDeleteButton: false
                                                                                    ]}"/>
                                                                                </g:if>
                                                                            </g:each>
                                                                        </g:if>
                                                                    </div>
                                                                </div>
                                                            </g:if>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    <g:if test="${privateTypeAddressMap}">
                                        <tr>
                                            <td>
                                                <div class="ui segment la-timeLineSegment-contact">
                                                    <div class="la-timeLineGrid">
                                                        <div class="ui grid">
                                                            <g:each in="${typeNames}" var="typeName">
                                                                <% List privateAddresses = privateTypeAddressMap.get(typeName) %>
                                                                <g:if test="${privateAddresses}">
                                                                    <div class="row">
                                                                        <div class="two wide column">

                                                                        </div>
                                                                        <div class="fourteen wide column">
                                                                            <div class="ui label">${typeName}</div>
                                                                            <g:each in="${privateAddresses}" var="a">
                                                                                <g:if test="${a.vendor}">
                                                                                    <laser:render template="/addressbook/address" model="${[
                                                                                            hideAddressType     : true,
                                                                                            address             : a,
                                                                                            tmplShowDeleteButton: false,
                                                                                            controller          : 'org',
                                                                                            action              : 'show',
                                                                                            id                  : vendor.id,
                                                                                            editable            : false
                                                                                    ]}"/>
                                                                                </g:if>
                                                                            </g:each>
                                                                        </div>
                                                                    </div>
                                                                </g:if>
                                                            </g:each>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </g:if>
                                </table>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
            <laser:render template="/templates/sidebar/aside" model="${[ownobj: vendor, owntp: 'vendor']}"/>
        </div>
    </aside>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.createContact').click(function() {
        JSPC.app.personCreate($(this).attr('id'), ${vendor.id});
    });

    $('.la-js-addListValue').click(function() {
        let url;
        let returnSelector;
        switch($(this).attr('data-objtype')) {
            case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: genericOIDService.getOID(vendor)]"/>';
                returnSelector = '#altnames';
                break;
        }

        $.ajax({
            url: url,
            success: function(result) {
                $(returnSelector).append(result);
                r2d2.initDynamicUiStuff(returnSelector);
                r2d2.initDynamicXEditableStuff(returnSelector);
            }
        });
    });
    JSPC.app.removeListValue = function(objId) {
        $("div[data-objId='"+objId+"']").remove();
    }

    JSPC.app.personCreate = function (contactFor, vendor, supportType = "") {
        <g:if test="${vendor.gokbId}">
            let existsWekbRecord = "&existsWekbRecord=true";
        </g:if>
        <g:else>
            let existsWekbRecord = "";
        </g:else>
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&vendor=' + vendor + existsWekbRecord + '&showContacts=true' + supportType;
        var func = bb8.ajax4SimpleModalFunction("#personModal", url);
        func();
    }

<g:if test="${editable && vendor.gokbId}">
let confirmationCard = $('#js-confirmationCard');
$('.js-open-confirm-modal-xEditable',confirmationCard).editable('destroy').editable().on('shown', function() {
                                r2d2.initDynamicUiStuff('.js-open-confirm-modal-xEditable');
                            });
</g:if >
</laser:script>

<laser:htmlEnd />