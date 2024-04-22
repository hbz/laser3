<%@ page import="de.laser.TitleInstancePackagePlatform; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Address; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact;" %>

<laser:htmlStart message="${'menu.institutions.vendor.show'}" serviceInjection="true" />

<laser:render template="breadcrumb"/>

<%--<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[vendor: vendor, user: user]}"/>
</ui:controlButtons>--%>

<ui:h1HeaderWithIcon text="${vendor.name}" >
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyVendor: isMyVendor]}"/>
</ui:h1HeaderWithIcon>

<%--<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[vendor: vendor, inContextOrg: inContextOrg, isProviderOrAgency: isProviderOrAgency]}"/>--%>

<ui:messages data="${flash}"/>
<%-- <laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" /> --%>

<div class="ui stackable grid">
    <div class="eleven wide column">

        <div class="la-inline-lists">
            <div class="ui card" id="js-confirmationCard">
                <div class="content">

                    <dl>
                        <dt><g:message code="default.name.label" /></dt>
                        <dd>
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable"
                                    owner="${vendor}" field="name"/>
                        </dd>
                    </dl>
                    <%--
                    <dl>
                        <dt><g:message code="org.altname.label" /></dt>
                        <dd>
                            <div id="altnames" class="ui divided middle aligned selection list la-flex-list accordion">
                                <g:if test="${vendor.altnames}">
                                    <div class="title" id="altname_title">
                                        <div data-objId="${genericOIDService.getOID(vendor.altnames[0])}">
                                            <ui:xEditable data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                          data_confirm_term_how="ok"
                                                          class="js-open-confirm-modal-xEditable"
                                                          owner="${vendor.altnames[0]}" field="name"/>
                                            <g:if test="${editable}">
                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: vendor.altnames[0].id]"
                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [vendor.altnames[0].name])}"
                                                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(vendor.altnames[0])}')">
                                                    <i class="trash alternate outline icon"></i>
                                                </ui:remoteLink>
                                            </g:if>
                                        </div>
                                        <i class="dropdown icon"></i>
                                    </div>
                                    <div class="content">
                                        <g:each in="${vendor.altnames.drop(1)}" var="altname">
                                            <div class="ui item" data-objId="${genericOIDService.getOID(altname)}">
                                                <div class="content la-space-right">
                                                    <ui:xEditable
                                                            data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                            data_confirm_term_how="ok"
                                                            class="js-open-confirm-modal-xEditable"
                                                            owner="${altname}" field="name" overwriteEditable="${editable}"/>
                                                </div>
                                                <g:if test="${editable && vendor.gokbId}">
                                                    <div class="content la-space-right">
                                                        <div class="ui buttons">
                                                            <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                           data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(altname)}')">
                                                                <i class="trash alternate outline icon"></i>
                                                            </ui:remoteLink>
                                                        </div>
                                                    </div>
                                                </g:if>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:if>
                            </div>
                            do not use if vendors can come only from we:kb
                            <g:if test="${vendor.gokbId}">
                                <input name="addAltname" id="addAltname" type="button" class="ui button addListValue" data-objtype="altname" value="${message(code: 'org.altname.add')}">
                            </g:if>
                        </dd>
                    </dl>
                    --%>
                    <dl>
                        <dt><g:message code="vendor.homepage.label"/></dt>
                        <dd>
                            ${vendor.homepage}
                            <%--
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                    owner="${vendor}" type="url" field="url" />
                            <g:if test="${vendor.url}">
                                <ui:linkWithIcon href="${vendor.url}" />
                            </g:if>
                            --%>
                        </dd>
                    </dl>
                    <dl>
                        <dt><g:message code="default.status.label"/></dt>
                        <dd>
                            ${vendor.status.getI10n('value')}
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.ordering.header"/></h2>
                    <dl>
                        <dt>
                            <g:message code="vendor.ordering.webshop.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.webShopOrders)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.ordering.xml.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.xmlOrders)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.ordering.edi.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.ediOrders)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.ordering.supportedLibrarySystems.label" />
                        </dt>
                        <dd>
                            <ul>
                                <g:each in="${vendor.supportedLibrarySystems}" var="row">
                                    <li>${row.librarySystem.getI10n('value')}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.ordering.electronicDeliveryDelayNotifications.label" />
                        </dt>
                        <dd>
                            <ul>
                                <g:each in="${vendor.electronicDeliveryDelays}" var="row">
                                    <li>${row.delayNotification.getI10n('value')}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.invoicing.header"/></h2>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.formats.label" />
                        </dt>
                        <dd>
                            <ul>
                                <g:each in="${vendor.electronicBillings}" var="row">
                                    <li>${row.invoicingFormat.getI10n('value')}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.dispatch.label" />
                        </dt>
                        <dd>
                            <ul>
                                <g:each in="${vendor.invoiceDispatchs}" var="row">
                                    <li>${row.invoiceDispatch.getI10n('value')}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.paperInvoice.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.paperInvoice)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.managementOfCredits.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.managementOfCredits)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.compensationPayments.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.processingOfCompensationPayments)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.invoicing.individualInvoiceDesign.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.individualInvoiceDesign)}
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            <div class="ui card">
                <div class="content">
                    <h2 class="ui header"><g:message code="vendor.general.header"/></h2>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.technicalSupport.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.technicalSupport)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.metadata.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.shippingMetadata)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.usageStats.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.forwardingUsageStatisticsFromPublisher)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.newReleaseInformation.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.activationForNewReleases)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.exchangeIndividualTitles.label" />
                        </dt>
                        <dd>
                            ${RefdataValue.displayBoolean(vendor.exchangeOfIndividualTitles)}
                        </dd>
                    </dl>
                    <dl>
                        <dt>
                            <g:message code="vendor.general.researchPlatform.label" />
                        </dt>
                        <dd>
                            <g:if test="${vendor.researchPlatformForEbooks}">
                                <ui:link url="${vendor.researchPlatformForEbooks}" target="_blank">${vendor.researchPlatformForEbooks}</ui:link>
                            </g:if>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->

            %{--
            <g:if test="${links || subEditor}">
                <div class="ui card">
                    <div class="content">
                        <h2 class="ui header"><g:message code="org.retirementLinking.label"/></h2>
                        <g:if test="${links}">
                            <table class="ui three column table">
                                <g:each in="${links}" var="row">
                                    <%
                                        String[] linkTypes = RDStore.COMBO_TYPE_FOLLOWS.getI10n('value').split('\\|')
                                        int perspectiveIndex
                                        Org pair
                                        if(orgInstance == row.fromOrg) {
                                            perspectiveIndex = 0
                                            pair = row.toOrg
                                        }
                                        else if(orgInstance == row.toOrg) {
                                            perspectiveIndex = 1
                                            pair = row.fromOrg
                                        }
                                    %>
                                    <g:if test="${pair != null}">
                                        <th scope="row" class="control-label">${linkTypes[perspectiveIndex]}</th>
                                        <td><g:link action="show" id="${pair.id}">${pair.name}</g:link></td>
                                        <td class="right aligned">
                                        <%--<laser:render template="/templates/links/subLinksModal"
                                                  model="${[tmplText:message(code:'org.details.editLink'),
                                                            tmplIcon:'write',
                                                            tmplCss: 'icon la-selectable-button la-popup-tooltip la-delay',
                                                            tmplID:'editLink',
                                                            tmplModalID:"org_edit_link_${row.id}",
                                                            editmode: editable,
                                                            context: vendor,
                                                            linkInstanceType: row.class.name,
                                                            link: row
                                                  ]}" />--%>
                                            <g:if test="${subEditor}">
                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.unlink')}">
                                                    <g:link class="ui negative icon button la-modern-button la-selectable-button js-open-confirm-modal"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                            data-confirm-term-how="unlink"
                                                            action="unlinkOrg" params="[id: vendor.id, combo: row.id]"
                                                            role="button"
                                                            aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                                        <i class="unlink icon"></i>
                                                    </g:link>
                                                </span>
                                            </g:if>
                                        </td>
                                    </g:if>
                                </g:each>
                            </table>
                        </g:if>
                        <g:if test="${subEditor}">
                            <div class="ui la-vertical buttons">
                                <%
                                    Map<String,Object> model = [tmplText:message(code: 'org.linking.addLink'),
                                                                tmplID:'addLink',
                                                                tmplButtonText:message(code: 'org.linking.addLink'),
                                                                tmplModalID:'org_add_link',
                                                                editmode: editable,
                                                                linkInstanceType: Combo.class.name,
                                                                context: orgInstance
                                    ]
                                %>
                                <laser:render template="/templates/links/subLinksModal"
                                              model="${model}" />
                            </div>
                        </g:if>
                    </div>
                </div>
            </g:if>
            --}%

            <div class="ui card">
                <div class="content">
                    <div class="ui accordion">
                        <div class="title">
                            <i class="dropdown icon la-dropdown-accordion"></i>
                            <div class="ui horizontal relaxed list">
                                <div class="item">
                                    <strong><g:message code="org.platforms.label" /></strong>
                                    &nbsp;<div class="ui blue circular label">${platforms.size()}</div>
                                </div>
                                <div class="item">
                                    <strong><g:message code="package.plural" /></strong>
                                    &nbsp;<div class="ui blue circular label">${vendor.packages.size()}</div>
                                </div>
                                <div class="item">
                                    <strong><g:message code="subscription.plural" /></strong>
                                    &nbsp;<div class="ui blue circular label">${currentSubscriptionsCount}/${subLinks.size()}</div>
                                </div>
                                <div class="item">
                                    <strong><g:message code="license.plural" /></strong>
                                    &nbsp;<div class="ui blue circular label">${currentLicensesCount}/${licLinks.size()}</div>
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
                                <g:each in="${vendor.packages}" var="vp">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="package" action="show" id="${vp.pkg.id}">${vp.pkg.name}</g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>

                            <p class="ui header"><g:message code="subscription.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <div class="ui item">
                                    <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: vendor.globalUID, status: RDStore.SUBSCRIPTION_CURRENT.id]">
                                        <div class="content la-space-right">
                                            <i class="icon filter"></i> <g:message code="subscription.plural.current" />
                                        &nbsp;<div class="ui blue circular label">${currentSubscriptionsCount}</div>
                                        </div>
                                    </g:link>
                                </div>
                                <div class="ui item">
                                    <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: vendor.globalUID, status: 'FETCH_ALL']">
                                        <div class="content la-space-right">
                                            <i class="icon filter"></i> <g:message code="subscription.plural.total" />
                                        &nbsp;<div class="ui blue circular label">${subLinks.size()}</div>
                                        </div>
                                    </g:link>
                                </div>
                            </div>

                            <p class="ui header"><g:message code="license.plural" /></p>

                            <div class="ui divided middle aligned selection list la-flex-list">
                                <div class="ui item">
                                    <div class="content la-space-right">
                                        <g:link controller="myInstitution" action="currentLicenses" params="[vendor: vendor.id, status: RDStore.LICENSE_CURRENT.id, subStatus: RDStore.SUBSCRIPTION_CURRENT.id, filterSubmit: 'Filtern']">
                                            <i class="icon filter"></i> <g:message code="license.plural.current" />
                                            &nbsp;<div class="ui blue circular label">${currentLicensesCount}</div>
                                        </g:link>
                                    </div>
                                </div>
                                <div class="ui item">
                                    <div class="content la-space-right">
                                        <g:link controller="myInstitution" action="currentLicenses" params="[vendor: vendor.id, filterSubmit: 'Filtern']">
                                            <i class="icon filter"></i> <g:message code="license.plural.total" />
                                            &nbsp;<div class="ui blue circular label">${licLinks.size()}</div>
                                        </g:link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <g:if test="${institution.isCustomerType_Consortium() || institution.isCustomerType_Support() || institution.isCustomerType_Inst_Pro()}">
                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[ vendor: vendor, institution: institution ]}"/>
                </div><!-- #new-dynamic-properties-block -->
            </g:if>
        </div>
    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">
            <%
                Map<String, List> privateTypeAddressMap = [:]
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
                        <g:if test="${PersonRole.executeQuery('select pr from Person p join p.roleLinks pr where pr.vendor = :vendor and ((p.isPublic = false and p.tenant = :ctx) or p.isPublic = true)', [vendor: vendor, ctx: institution]) ||
                                Address.executeQuery('select a from Address a where a.vendor = :vendor and (a.tenant = :ctx or a.tenant = null)', [vendor: vendor, ctx: institution])}">
                            <table class="ui compact table">
                                <g:set var="vendorContacts" value="${vendorService.getContactPersonsByFunctionType(vendor, institution, true, null, true)}"/>
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
                                                                        <a target="_blank" href="${wekbApi.editUrl ? wekbApi.editUrl + '/public/orgContent/' + vendor.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${prs.roleLinks.collect { PersonRole pr -> pr.roleType.getI10n('value')}.join (' / ')}</div>
                                                                    <g:if test="${!(prs.last_name in [RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value'), RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_METADATA.getI10n('value')])}"><div class="ui header">${prs}</div></g:if>
                                                                    <g:if test="${prs.contacts}">
                                                                        <g:each in="${prs.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/templates/cpa/contact" model="${[
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
                            </table>
                        </g:if>
                    </div>
                </div>
            </div>
            <g:if test="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) && !inContextOrg}">
                <div id="container-contacts">
                    <div class="ui card">
                        <div class="content">
                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        <g:message code="org.contactpersons.and.addresses.my"/>
                                    </div>
                                    <div class="right aligned four wide column">
                                        <a href="#createPersonModal" class="ui icon button blue la-modern-button createContact" id="contactPersonForProviderAgency" data-ui="modal">
                                            <i aria-hidden="true" class="plus icon"></i>
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
                                                                        <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>
                                                                    <div class="ui header">${person}</div>
                                                                    <g:if test="${person.contacts}">
                                                                        <g:each in="${person.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/templates/cpa/contact" model="${[
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
                                                                                    if(institution.isCustomerType_Consortium()) {
                                                                                        if(!s.instanceOf)
                                                                                            respObjects << s
                                                                                    }
                                                                                    else respObjects << s
                                                                                }
                                                                                break
                                                                            case 'lic': License l = License.get(respRef[1])
                                                                                if(l.status == RDStore.LICENSE_CURRENT) {
                                                                                    if(institution.isCustomerType_Consortium()) {
                                                                                        if (!l.instanceOf)
                                                                                            respObjects << l
                                                                                    }
                                                                                    else respObjects << l
                                                                                }
                                                                                break
                                                                            case 'pkg': de.laser.Package p = de.laser.Package.get(respRef[1])
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
                                                                            <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
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
                                                                                    <g:elseif test="${respObj instanceof de.laser.Package}">
                                                                                        (<g:link controller="package" action="show" id="${respObj.id}">${respObj.name}</g:link>)
                                                                                    </g:elseif>
                                                                                </g:each>
                                                                            </g:if>
                                                                        </g:each>
                                                                        <g:if test="${person.contacts}">
                                                                            <g:each in="${person.contacts.toSorted()}" var="contact">
                                                                                <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                    <laser:render template="/templates/cpa/contact" model="${[
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
                                                                                <g:if test="${a.org}">
                                                                                    <laser:render template="/templates/cpa/address" model="${[
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
            %{--<laser:render template="/templates/sidebar/aside" model="${[ownobj: vendor, owntp: 'vendor']}"/>--}%
        </div>
    </aside>
</div>

%{--
<laser:script file="${this.getGroovyPageFileName()}">
    $('.createContact').click(function() {
        JSPC.app.personCreate($(this).attr('id'), ${vendor.id});
    });

    JSPC.app.showRegionsdropdown = function (newValue) {
        $("*[id^=regions_]").hide();
        if(newValue){
            var id = newValue.split(':')[1]
            // $("#regions_" + id).editable('setValue', null);
            $("#regions_" + id).show();
        }
    };

    JSPC.app.addresscreate_org = function (orgId, typeId, redirect, hideType) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?orgId=' + orgId + '&typeId=' + typeId + '&redirect=' + redirect + '&hideType=' + hideType;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }

$('.addListValue').click(function() {
    let url;
    let returnSelector;
    switch($(this).attr('data-objtype')) {
        case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: vendor.id]"/>';
            returnSelector = '#altnames';
            break;
        case 'frontend': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'frontend', owner: vendor.id]"/>';
            returnSelector = '#discoverySystemsFrontend';
            break;
        case 'index': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'index', owner: vendor.id]"/>';
            returnSelector = '#discoverySystemsIndex';
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

JSPC.app.personCreate = function (contactFor, org, supportType = "") {
    <g:if test="${vendor.gokbId}">
        let existsWekbRecord = "&existsWekbRecord=true";
    </g:if>
    <g:else>
        let existsWekbRecord = "";
    </g:else>
    var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&org=' + org + existsWekbRecord + '&showAddresses=false&showContacts=true' + supportType;
    var func = bb8.ajax4SimpleModalFunction("#personModal", url);
    func();
}

%{--
<g:if test="${editable && vendor.gokbId}">
let confirmationCard = $('#js-confirmationCard');
$('.js-open-confirm-modal-xEditable',tionCard).editable('destroy').editable().on('shown', function() {
                                r2d2.initDynamicUiStuff('.js-open-confirm-modal-xEditable');
                            });
</g:if >
</laser:script>
--}%

<laser:htmlEnd />