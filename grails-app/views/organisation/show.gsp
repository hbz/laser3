<%@ page import="de.laser.addressbook.PersonRole; de.laser.addressbook.Address; de.laser.wekb.TitleInstancePackagePlatform; de.laser.wekb.Package; de.laser.ui.Btn; de.laser.ui.Icon; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.addressbook.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.addressbook.Contact" %>

    <g:if test="${institutionalView}">
        <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
    </g:if>
    <g:else>
        <g:set var="entityName" value="${message(code: 'org.label')}"/>
    </g:else>

<laser:htmlStart message="${'menu.institutions.org.show'}" />

<ui:debugInfo>
    <div style="padding: 1em 0;">
        <p>orgInstance.dateCreated: ${orgInstance.dateCreated}</p>
        <p>orgInstance.lastUpdated: ${orgInstance.lastUpdated}</p>
    </div>
</ui:debugInfo>

<laser:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[org: orgInstance, user: user]}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}" />
</ui:h1HeaderWithIcon>

<g:if test="${missing.size() > 0}">
    <div class="ui icon message warning">
        <i class="${Icon.UI.WARNING}"></i>
        <div class="content">
            <div class="header">${message(code: 'org.eInvoice.info.header')}</div>
            ${message(code: 'org.eInvoice.info.text')}
            <div class="ui bulleted list">
                <g:if test="${missing.eInvoicePortal}">
                    <div class="item">${missing.eInvoicePortal}</div>
                </g:if>
                <g:if test="${missing.leitID}">
                    <div class="item">${missing.leitID}</div>
                </g:if>
            </div>
        </div>
    </div>
</g:if>

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

<ui:objectStatus object="${orgInstance}" />

<ui:messages data="${flash}"/>
<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

<div class="ui stackable grid">
    <div class="eleven wide column">

        <div class="la-inline-lists">
            <div class="ui card" id="js-confirmationCard">
                <div class="content">

                    <g:if test="${!inContextOrg && contextService.getOrg().isCustomerType_Consortium()}">
                        <a href="#" class="ui icon la-float-right infoFlyout-trigger" data-template="org" data-org="${orgInstance.id}">
                            <i class="icon circular blue info inverted"></i>
                        </a>
                    </g:if>

                    <dl>
                        <dt class="control-label"><g:message code="default.name.label" /></dt>
                        <dd>
                            <ui:xEditable owner="${orgInstance}" field="name" overwriteEditable="${editable}"/>
                        </dd>
                    </dl>
                    <g:if test="${!inContextOrg || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                        <dl>
                            <dt class="control-label"><g:message code="org.sortname.label" /></dt>
                            <dd>
                                <ui:xEditable owner="${orgInstance}" field="sortname" overwriteEditable="${editable}"/>
                            </dd>
                        </dl>
                    </g:if>
                    <dl>
                        <dt class="control-label"><g:message code="altname.plural" /></dt>
                        <dd>
                            <div id="altnames" class="ui accordion la-accordion-showMore la-accordion-altName">
                                <g:if test="${orgInstance.altnames}">
                                    <div class="ui divided middle aligned selection list la-flex-center">
                                        <div class="item title" id="altname_title"  data-objId="altname-${orgInstance.altnames[0].id}">
                                            <div class="content la-space-right">
                                                <ui:xEditable owner="${orgInstance.altnames[0]}" field="name" overwriteEditable="${editable}"/>
                                            </div>
                                            <g:if test="${editable}">
                                                <ui:remoteLink role="button" class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: orgInstance.altnames[0].id]"
                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [orgInstance.altnames[0].name])}"
                                                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('altname-${orgInstance.altnames[0].id}')">
                                                    <i class="${Icon.CMD.DELETE}"></i>
                                                </ui:remoteLink>
                                            </g:if>
                                            <div class="${Btn.MODERN.SIMPLE_TOOLTIP} la-show-button" data-content="${message(code: 'altname.showAll')}">
                                                <i class="${Icon.CMD.SHOW_MORE}"></i>
                                            </div>
                                        </div>
                                        <div class="content" style="padding:0">
                                            <g:each in="${orgInstance.altnames.drop(1)}" var="altname">
                                                <div class="ui item" data-objId="altname-${altname.id}">
                                                    <div class="content la-space-right">
                                                        <ui:xEditable owner="${altname}" field="name" overwriteEditable="${editable}"/>
                                                    </div>
                                                    <g:if test="${editable}">
                                                        <ui:remoteLink role="button" class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: altname.id]"
                                                                       data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [altname.name])}"
                                                                       data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('altname-${altname.id}')">
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
                        </dd>
                        <dd>
                            <g:if test="${editable}">
                                <button data-content="${message(code: 'altname.add')}" data-objtype="altname" id="addAltname" class="${Btn.MODERN.POSITIVE} la-js-addItem blue la-popup-tooltip">
                                    <i class="${Icon.CMD.ADD}"></i>
                                </button>
                            </g:if>
                        </dd>
                    </dl>
                    <div></div>%{-- Breaks DL for a reason --}%
                    <dl>
                        <dt class="control-label"><g:message code="default.url.label"/></dt>
                        <dd>
                            <ui:xEditable owner="${orgInstance}" type="url" field="url"  overwriteEditable="${editable}"/>
                            <g:if test="${orgInstance.url}">
                                <ui:linkWithIcon href="${orgInstance.url}" />
                            </g:if>
                        </dd>
                    </dl>

                    <g:if test="${orgInstance.isCustomerType_Inst()}">
                        <dl>
                            <dt class="control-label">
                                <g:message code="org.libraryType.label" />
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${message(code: 'org.libraryType.expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="libraryType" config="${RDConstants.LIBRARY_TYPE}"/>
                            </dd>
                        </dl>
                    </g:if>

                    <g:if test="${orgInstance.getCustomerType()}">
                        <dl>
                            <dt class="control-label"><g:message code="org.customerType.label"/></dt>
                            <dd>
                                ${orgInstance.getCustomerTypeI10n()}
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <g:if test="${orgInstance.isCustomerType_Inst()}">
                <div class="ui two doubling stackable cards">
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.libraryNetwork.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.libraryNetwork.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditableRefData owner="${orgInstance}" field="libraryNetwork" config="${RDConstants.LIBRARY_NETWORK}"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.legalPatronName.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.legalPatronName.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${orgInstance}" field="legalPatronName"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.urlGov.label"/>
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.urlGov.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${orgInstance}" type="url" field="urlGov" class="la-overflow la-ellipsis" />
                                    <g:if test="${orgInstance.urlGov}">
                                        <ui:linkWithIcon href="${orgInstance.urlGov}" />
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.funderType.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.funderType.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditableRefData owner="${orgInstance}" field="funderType" config="${RDConstants.FUNDER_TYPE}"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.funderHSK.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.funderHSK.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditableRefData owner="${orgInstance}" field="funderHskType" config="${RDConstants.FUNDER_HSK_TYPE}"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="address.country.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.country.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <ui:xEditableRefData id="country" owner="${orgInstance}" field="country" config="${RDConstants.COUNTRY}" />
                                    &nbsp
                                </dd>
                            </dl>
                            <dl>
                                <dt class="control-label">
                                    <g:message code="org.region.label" />
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${message(code: 'org.region.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <%-- for non-editable views, the region is displayed three times - not for editable views! --%>
                                    <g:if test="${editable}">
                                        <ui:xEditableRefData id="regions_${RDStore.COUNTRY_DE.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_DE}"/>
                                        <ui:xEditableRefData id="regions_${RDStore.COUNTRY_AT.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_AT}"/>
                                        <ui:xEditableRefData id="regions_${RDStore.COUNTRY_CH.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_CH}"/>
                                    </g:if>
                                    <g:else>
                                        ${orgInstance.region?.getI10n("value")}
                                    </g:else>
                                </dd>
                            </dl>
                        </div>
                    </div><!-- .card -->

                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt class="control-label">${message(code:'org.subjectGroup.label')}</dt>
                                <dd>
                                    <%
                                        List<RefdataValue> subjectGroups = RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
                                    %>
                                    <laser:render template="orgSubjectGroupAsList"
                                                  model="${[org: orgInstance, orgSubjectGroups: orgInstance.subjectGroup, availableSubjectGroups: subjectGroups, editable: editable]}"/>

                                    <laser:render template="orgSubjectGroupModal"
                                                  model="${[org: orgInstance, availableSubjectGroups: subjectGroups, editable: editable]}"/>
                                </dd>
                                <dd>
                                    <g:if test="${editable}">
                                        <a  data-content="<g:message code="org.subjectGroup.add.label"/>" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-ui="modal" href="#subjectGroup" >
                                            <i class="${Icon.CMD.ADD}"></i>
                                        </a>
                                    </g:if>
                                </dd>
                            </dl>
%{--                            <div class="ui header">${message(code:'org.subjectGroup.label')}</div>--}%
                        </div>
                    </div>
                </div>
            </g:if>

            <g:if test="${orgInstance.isCustomerType_Inst()}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">
                                <g:message code="org.eInvoice.label" />
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${message(code: 'org.eInvoice.expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableBoolean owner="${orgInstance}" field="eInvoice"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                <g:message code="org.eInvoicePortal.label" />
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${message(code: 'org.eInvoicePortal.expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="eInvoicePortal" config="${RDConstants.E_INVOICE_PORTAL}"/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>

            <g:if test="${orgInstance.isCustomerType_Inst()}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt class="control-label">
                                <g:message code="org.linkResolverBase.label"/>
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${message(code: 'org.linkResolverBase.expl')}">
                                    <i class="${Icon.TOOLTIP.HELP}"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditable owner="${orgInstance}" field="linkResolverBaseURL" />
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label"><g:message code="org.supportedLibrarySystem.label" /></dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="supportedLibrarySystem" config="${RDConstants.SUPPORTED_LIBRARY_SYSTEM}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label"><g:message code="org.discoverySystems.frontend.label" /></dt>
                            <dd>
                                <laser:render template="discoverySystemAsList"
                                              model="${[org: orgInstance, config: 'discoverySystemFrontend', editable: editable]}"/>

                                <laser:render template="discoverySystemModal"
                                              model="${[org: orgInstance, config: 'discoverySystemFrontend', editable: editable]}"/>
                            </dd>
                            <dd>
                                <g:if test="${editable}">
                                    <a class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'org.discoverySystems.frontend.add')}" data-ui="modal" href="#frontend">
                                        <i class="${Icon.CMD.ADD}"></i>
                                    </a>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label"><g:message code="org.discoverySystems.index.label" /></dt>
                            <dd>
                                <laser:render template="discoverySystemAsList"
                                              model="${[org: orgInstance, config: 'discoverySystemIndex', editable: editable]}"/>

                                <laser:render template="discoverySystemModal"
                                              model="${[org: orgInstance, config: 'discoverySystemIndex', editable: editable]}"/>
                            </dd>
                            <dd>
                                <g:if test="${editable}">
                                    <a class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'org.discoverySystems.index.add')}" data-ui="modal" href="#index">
                                        <i class="${Icon.CMD.ADD}"></i>
                                    </a>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div>
            </g:if>

            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                <div class="ui card">
                    <div class="content">
                        <ui:cardLabelAdminOnly />
                        <dl>
                            <dt class="control-label"><g:message code="org.isBetaTester.label" /></dt>
                            <dd>
                                <ui:xEditableBoolean owner="${orgInstance}" field="isBetaTester" overwriteEditable="true"/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>

        <g:set var="showAdminTab" value="${(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || institution.isCustomerType_Consortium()) && (institution != orgInstance)}" />

        <g:if test="${showAdminTab && (orgInstance.createdBy || orgInstance.legallyObligedBy)}">
            <div class="ui card">
                <div class="content">
                    <g:if test="${institution.isCustomerType_Consortium()}">
                        <ui:cardLabelConsortiumOnly />
                    </g:if>
                    <g:else>
                        <ui:cardLabelAdminOnly />
                    </g:else>
                    <div class="ui stackable grid two column">
                        <div class="column">
                            <div class="control-label" style="font-weight:700">
                                <g:message code="org.createdBy.label" />
                            </div>

                            <g:if test="${createdByOrgGeneralContacts}">
                                <p style="margin:1rem 0.5rem; font-weight:bold">
                                    <ui:customerTypeIcon org="${orgInstance.createdBy}" />
                                    <g:link controller="organisation" action="show" id="${orgInstance.createdBy.id}">${orgInstance.createdBy.name}</g:link>
                                </p>

                                <g:each in="${createdByOrgGeneralContacts}" var="cbogc">
                                    <laser:render template="/addressbook/person_full_details" model="${[
                                            person              : cbogc,
                                            personContext       : orgInstance.createdBy,
                                            tmplShowFunctions       : true,
                                            tmplShowPositions       : true,
                                            tmplShowResponsiblities : true,
                                            tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax'],
                                            editable            : false
                                    ]}"/>
                                </g:each>
                            </g:if>
                        </div>
                        <div class="column">
                            <div class="control-label" style="font-weight:700">
                                <g:message code="org.legallyObligedBy.label" />
                            </div>

                            <g:if test="${legallyObligedByOrgGeneralContacts}">
                                <p style="margin:1rem 0.5rem; font-weight:bold">
                                    <ui:customerTypeIcon org="${orgInstance.legallyObligedBy}" />
                                    <g:link controller="organisation" action="show" id="${orgInstance.legallyObligedBy.id}">${orgInstance.legallyObligedBy.name}</g:link>
                                </p>

                                <g:each in="${legallyObligedByOrgGeneralContacts}" var="lobogc">
                                    <laser:render template="/addressbook/person_full_details" model="${[
                                            person              : lobogc,
                                            personContext       : orgInstance.legallyObligedBy,
                                            tmplShowFunctions       : true,
                                            tmplShowPositions       : true,
                                            tmplShowResponsiblities : true,
                                            tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax'],
                                            editable            : false
                                    ]}"/>
                                </g:each>
                            </g:if>
                        </div>
                    </div>

                </div>
            </div>
        </g:if>

            <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[ orgInstance: orgInstance, authOrg: formalOrg ]}"/>%{-- ERMS-6070 --}%
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
                                    <g:if test="${inContextOrg && contextService.isInstEditor()}">
                                        <a href="#createPersonModal" class=" ${Btn.MODERN.SIMPLE} createContact" id="contactPersonForPublic" data-ui="modal">
                                            <i aria-hidden="true" class="${Icon.CMD.ADD}"></i>
                                        </a>
                                    </g:if>
%{--                                    <g:elseif test="${isProviderOrAgency}">--}%
%{--                                        <a href="#createPersonModal" class="${Btn.MODERN.SIMPLE} createContact" id="contactPersonForProviderAgencyPublic" data-ui="modal">--}%
%{--                                            <i aria-hidden="true" class="${Icon.CMD.ADD}"></i>--}%
%{--                                        </a>--}%
%{--                                    </g:elseif>--}%
                                </div>
                            </div>
                        </div>

                            <%--
                            <g:if test="${(orgInstance.id == institution.id && user.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR'))}">
                                <g:link action="contacts" controller="organisation" params="[id: orgInstance.id, tab: 'contacts']"
                                        class="${Btn.SIMPLE}">${message('code': 'org.edit.contactsAndAddresses')}</g:link>
                            </g:if>
                            --%>

                        <g:if test="${PersonRole.executeQuery('select pr from Person p join p.roleLinks pr where pr.org = :org and ((p.isPublic = false and p.tenant = :ctx) or p.isPublic = true)', [org: orgInstance, ctx: institution]) ||
                                Address.executeQuery('select a from Address a where a.org = :org and (a.tenant = :ctx or a.tenant = null)', [org: orgInstance, ctx: institution])}">
                            <table class="ui compact table">
                                    <tr>
                                        <td>
                                            <div class="ui segment la-timeLineSegment-contact">
                                                <div class="la-timeLineGrid">
                                                    <div class="ui grid">
                                                        <g:set var="persons" value="${orgInstance.getContactPersonsByFunctionType(true, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)}"/>
                                                        <g:each in="${persons}" var="prs">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${prs.isPublic}">
                                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>
                                                                    <g:if test="${prs.preferredBillingPerson}">
                                                                        <i class="${Icon.SURVEY} circular la-popup-tooltip" data-content="${message(code:'person.preferredBillingPerson')}"></i>
                                                                    </g:if>
                                                                    <g:if test="${prs.preferredSurveyPerson}">
                                                                        <i class="${Icon.SURVEY} circular la-popup-tooltip" data-content="${message(code:'person.preferredSurveyPerson')}"></i>
                                                                    </g:if>
                                                                    <div class="ui header">${prs}</div>
                                                                    <g:each in="${prs.roleLinks}" var="personRole">
                                                                        <g:if test="${personRole.org.id == orgInstance.id && personRole.positionType}">
                                                                            ${personRole.positionType.getI10n('value')}
                                                                        </g:if>
                                                                        <g:elseif test="${personRole.org.id == orgInstance.id && personRole.responsibilityType && (personRole.sub?.status == RDStore.SUBSCRIPTION_CURRENT || personRole.lic?.status == RDStore.LICENSE_CURRENT)}">
                                                                            ${personRole.responsibilityType.getI10n('value')}
                                                                        </g:elseif>
                                                                    </g:each>
                                                                    <g:if test="${prs.contacts}">
                                                                        <g:each in="${prs.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/addressbook/contact" model="${[
                                                                                        overwriteEditable   : false,
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                    <%--<g:each in="${prs.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                                                                            <laser:render template="/addressbook/address"
                                                                                          model="${[address: address, tmplShowDeleteButton: false, editable: false]}"/>
                                                                    </g:each>--%>
                                                                </div>
                                                            </div>
                                                        </g:each>
                                                        <g:set var="persons" value="${orgInstance.getContactPersonsByFunctionType(true, RDStore.PRS_FUNC_INVOICING_CONTACT)}"/>
                                                        <g:each in="${persons}" var="prs">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${prs.isPublic}">
                                                                        <i class="${Icon.ACP_PUBLIC} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.public')}"></i>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="${Icon.ACP_PRIVATE} circular large la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value')}</div>
                                                                    <g:if test="${prs.preferredBillingPerson}">
                                                                        <i class="${Icon.SURVEY} circular la-popup-tooltip" data-content="${message(code:'person.preferredBillingPerson')}"></i>
                                                                    </g:if>
                                                                    <g:if test="${prs.preferredSurveyPerson}">
                                                                        <i class="${Icon.SURVEY} circular la-popup-tooltip" data-content="${message(code:'person.preferredSurveyPerson')}"></i>
                                                                    </g:if>
                                                                    <div class="ui header">${prs}</div>
                                                                    <g:each in="${prs.roleLinks}" var="personRole">
                                                                        <g:if test="${personRole.org.id == orgInstance.id && personRole.positionType}">
                                                                            ${personRole.positionType.getI10n('value')}
                                                                        </g:if>
                                                                        <g:elseif test="${personRole.org.id == orgInstance.id && personRole.responsibilityType && (personRole.sub?.status == RDStore.SUBSCRIPTION_CURRENT || personRole.lic?.status == RDStore.LICENSE_CURRENT)}">
                                                                            ${personRole.responsibilityType.getI10n('value')}
                                                                        </g:elseif>
                                                                    </g:each>
                                                                    <g:if test="${prs.contacts}">
                                                                        <g:each in="${prs.contacts.toSorted()}" var="contact">
                                                                            <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                <laser:render template="/addressbook/contact" model="${[
                                                                                        overwriteEditable   : false,
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                    <%--<g:each in="${prs.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                                                                            <laser:render template="/addressbook/address"
                                                                                          model="${[address: address, tmplShowDeleteButton: false, editable: false]}"/>
                                                                    </g:each>--%>
                                                                </div>
                                                            </div>
                                                        </g:each>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    <%
                                        orgInstance.addresses.each { Address a ->
                                            a.type.each { type ->
                                                String typeName = type.getI10n('value')
                                                typeNames.add(typeName)
                                                if(!a.tenant) {
                                                    List addresses = publicTypeAddressMap.get(typeName) ?: []
                                                    addresses.add(a)
                                                    publicTypeAddressMap.put(typeName, addresses)
                                                }
                                                else if(a.tenant.id == institution.id) {
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
                                                                                <g:if test="${a.org}">
                                                                                    <g:if test="${a.preferredForSurvey}">
                                                                                        <i class="${Icon.SURVEY} circular la-popup-tooltip" data-content="${message(code:'address.preferredForSurvey')}"></i>
                                                                                    </g:if>
                                                                                    <laser:render template="/addressbook/address" model="${[
                                                                                            hideAddressType     : true,
                                                                                            address             : a,
                                                                                            tmplShowDeleteButton: false,
                                                                                            controller          : 'org',
                                                                                            action              : 'show',
                                                                                            id                  : orgInstance.id,
                                                                                            editable            : false,
                                                                                            showPreferredForSurvey: true
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
            <g:if test="${(institution.isCustomerType_Consortium() || institution.isCustomerType_Inst_Pro()) && !inContextOrg}">
                <div id="container-contacts">
                    <div class="ui card">
                        <div class="content">
                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        <g:message code="org.contactpersons.and.addresses.my"/>
                                    </div>
                                    <g:if test="${contextService.isInstEditor()}">
                                        <div class="right aligned four wide column">
                                            <a href="#createPersonModal" class="${Btn.MODERN.SIMPLE} createContact" id="contactPersonForInstitution" data-ui="modal">
                                                <i aria-hidden="true" class="${Icon.CMD.ADD}"></i>
                                            </a>
                                        </div>
                                    </g:if>
                                </div>
                            </div>
                            <%
                                List visiblePersons = addressbookService.getVisiblePersons("addressbook",[org: orgInstance, sort: 'p.last_name, p.first_name', function: [RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.id]])
                                List otherPersons = addressbookService.getVisiblePersons("addressbook",[org: orgInstance, sort: 'p.last_name, p.first_name'])
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
                                                                            case 'pkg': de.laser.wekb.Package p = de.laser.wekb.Package.get(respRef[1])
                                                                                if(p.packageStatus != RDStore.PACKAGE_STATUS_REMOVED)
                                                                                    respObjects << p
                                                                                break
                                                                            case 'tipp': TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(respRef[1])
                                                                                if(tipp.status == RDStore.TIPP_STATUS_CURRENT) {
                                                                                    respObjects << tipp
                                                                                }
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
                                                                                    <g:if test="${respObj instanceof TitleInstancePackagePlatform}">
                                                                                        (<g:link controller="tipp" action="show" id="${respObj.id}">${respObj.name}</g:link>)
                                                                                    </g:if>
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
                                                                                <g:if test="${a.org}">
                                                                                    <laser:render template="/addressbook/address" model="${[
                                                                                            hideAddressType     : true,
                                                                                            address             : a,
                                                                                            tmplShowDeleteButton: false,
                                                                                            controller          : 'org',
                                                                                            action              : 'show',
                                                                                            id                  : orgInstance.id,
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
            <laser:render template="/templates/sidebar/aside" model="${[ownobj: orgInstance, owntp: 'organisation']}"/>
        </div>
    </aside>
</div>

<laser:render template="/info/flyoutWrapper"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.createContact').click(function() {
        JSPC.app.personCreate($(this).attr('id'), ${orgInstance.id});
    });

    $('#country').on('save', function(e, params) {
        JSPC.app.showRegionsdropdown(params.newValue);
    });

    JSPC.app.showRegionsdropdown = function (newValue) {
        $("*[id^=regions_]").hide();
        if(newValue){
            var id = newValue.split(':')[1]
            // $("#regions_" + id).editable('setValue', null);
            $("#regions_" + id).show();
        }
    };

    <g:if test="${orgInstance.isCustomerType_Inst()}">
        if($("#country").length) {
            JSPC.app.showRegionsdropdown( $("#country").editable('getValue', true) );
        }
    </g:if>
    $('.la-js-addItem').click(function() {
        let url;
        let returnSelector;
        switch($(this).attr('data-objtype')) {
            case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: genericOIDService.getOID(orgInstance)]"/>';
                returnSelector = '#altnames';
                break;
            case 'frontend': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'frontend', owner: genericOIDService.getOID(orgInstance)]"/>';
                returnSelector = '#discoverySystemsFrontend';
                break;
            case 'index': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'index', owner: genericOIDService.getOID(orgInstance)]"/>';
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
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&org=' + org + '&showContacts=true' + supportType;
        var func = bb8.ajax4SimpleModalFunction("#personModal", url);
        func();
    }

<g:if test="${editable}">
    let confirmationCard = $('#js-confirmationCard');
    $('.js-open-confirm-modal-xEditable', confirmationCard).editable('destroy').editable().on('shown', function() {
                                    r2d2.initDynamicUiStuff('.js-open-confirm-modal-xEditable');
                                });
</g:if >


</laser:script>

<laser:htmlEnd />