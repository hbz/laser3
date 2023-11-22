<%@ page import="de.laser.TitleInstancePackagePlatform; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Address; de.laser.Org; de.laser.Subscription; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact; de.laser.remote.ApiSource" %>

<g:set var="wekbAPI" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>

    <g:if test="${isProviderOrAgency}">
        <g:set var="entityName" value="${message(code: 'default.provider.label')}"/>
    </g:if>
    <g:elseif test="${institutionalView}">
        <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
    </g:elseif>
    <g:else>
        <g:set var="entityName" value="${message(code: 'org.label')}"/>
    </g:else>

<laser:htmlStart message="${isProviderOrAgency ? 'menu.institutions.provider_info' : 'menu.institutions.org_info'}" serviceInjection="true" />

<laser:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>
<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}" model="${[org: orgInstance, user: user]}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${orgInstance.name}" >
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>


<ui:anualRings object="${orgInstance}" controller="organisation" action="show" navNext="${navNextOrg}"
               navPrev="${navPrevOrg}"/>

%{--<g:if test="${isProviderOrAgency}">--}%
%{--    <ui:cbItemMarkerAction org="${orgInstance}"/>--}%
%{--</g:if>--}%

<g:if test="${missing.size() > 0}">
    <div class="ui icon message warning">
        <i class="info icon"></i>
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

<laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, isProviderOrAgency: isProviderOrAgency]}"/>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>
<laser:render template="/templates/workflow/status" model="${[cmd: cmd, status: status]}" />

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
                                    owner="${orgInstance}" field="name"
                                    overwriteEditable="${editable && orgInstanceRecord == null}"/>
                        </dd>
                    </dl>
                    <g:if test="${!inContextOrg || isGrantedOrgRoleAdminOrOrgEditor}">
                        <dl>
                            <dt><g:message code="org.sortname.label" /></dt>
                            <dd>
                                <ui:xEditable
                                        data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                        data_confirm_term_how="ok"
                                        class="js-open-confirm-modal-xEditable"
                                        owner="${orgInstance}" field="sortname" overwriteEditable="${editable && !orgInstance.gokbId}"/>
                            </dd>
                        </dl>
                    </g:if>
                    <dl>
                        <dt><g:message code="org.altname.label" /></dt>
                        <dd>
                            <div id="altnames" class="ui divided middle aligned selection list la-flex-list accordion">
                                <g:if test="${orgInstance.altnames}">
                                    <div class="title" id="altname_title">
                                        <div data-objId="${genericOIDService.getOID(orgInstance.altnames[0])}">
                                            <ui:xEditable data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                          data_confirm_term_how="ok"
                                                          class="js-open-confirm-modal-xEditable"
                                                          owner="${orgInstance.altnames[0]}" field="name" overwriteEditable="${editable && orgInstanceRecord == null}"/>
                                            <g:if test="${editable && orgInstanceRecord == null}">
                                                <ui:remoteLink role="button" class="ui icon negative button la-modern-button js-open-confirm-modal" controller="ajaxJson" action="removeObject" params="[object: 'altname', objId: orgInstance.altnames[0].id]"
                                                               data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.altname", args: [orgInstance.altnames[0].name])}"
                                                               data-confirm-term-how="delete" data-done="JSPC.app.removeListValue('${genericOIDService.getOID(orgInstance.altnames[0])}')">
                                                    <i class="trash alternate outline icon"></i>
                                                </ui:remoteLink>
                                            </g:if>
                                        </div>
                                        <i class="dropdown icon"></i>
                                    </div>
                                    <div class="content">
                                        <g:each in="${orgInstance.altnames.drop(1)}" var="altname">
                                            <div class="ui item" data-objId="${genericOIDService.getOID(altname)}">
                                                <div class="content la-space-right">
                                                    <ui:xEditable
                                                            data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                                            data_confirm_term_how="ok"
                                                            class="js-open-confirm-modal-xEditable"
                                                            owner="${altname}" field="name" overwriteEditable="${editable && orgInstanceRecord == null}"/>
                                                </div>
                                                <g:if test="${editable && orgInstanceRecord == null}">
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
                            <g:if test="${orgInstanceRecord == null}">
                                <input name="addAltname" id="addAltname" type="button" class="ui button addListValue" data-objtype="altname" value="${message(code: 'org.altname.add')}">
                            </g:if>
                        </dd>
                    </dl>
                    <dl>
                        <dt><g:message code="default.url.label"/></dt>
                        <dd>
                            <ui:xEditable
                                    data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                    data_confirm_term_how="ok"
                                    class="js-open-confirm-modal-xEditable la-overflow la-ellipsis"
                                    owner="${orgInstance}" type="url" field="url"  overwriteEditable="${editable && orgInstanceRecord == null}"/>
                            <g:if test="${orgInstance.url}">
                                <ui:linkWithIcon href="${orgInstance.url}" />
                            </g:if>
                        </dd>
                    </dl>
                    <g:if test="${orgInstance.getCustomerType()}">
                        <dl>
                            <dt><g:message code="org.customerType.label"/></dt>
                            <dd>
                                ${orgInstance.getCustomerTypeI10n()}
                            </dd>
                        </dl>
                    </g:if>
                    <g:if test="${orgInstance.isCustomerType_Inst()}">
                        <dl>
                            <dt>
                                <g:message code="org.legalPatronName.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.legalPatronName.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditable owner="${orgInstance}" field="legalPatronName"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.urlGov.label"/>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.urlGov.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditable owner="${orgInstance}" type="url" field="urlGov" class="la-overflow la-ellipsis" />
                                <g:if test="${orgInstance.urlGov}">
                                    <ui:linkWithIcon href="${orgInstance.urlGov}" />
                                </g:if>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <g:if test="${orgInstance.isCustomerType_Inst()}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>
                                <g:message code="org.linkResolverBase.label"/>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.linkResolverBase.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditable owner="${orgInstance}" field="linkResolverBaseURL" />
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.eInvoice.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.eInvoice.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableBoolean owner="${orgInstance}" field="eInvoice"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.eInvoicePortal.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.eInvoicePortal.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="eInvoicePortal" config="${RDConstants.E_INVOICE_PORTAL}"/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>
            <g:if test="${isProviderOrAgency && orgInstanceRecord}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>
                                <g:message code="org.metadataDownloaderURL.label" />
                            </dt>
                            <dd>
                                <g:if test="${orgInstanceRecord.metadataDownloaderURL}">
                                    ${orgInstanceRecord.metadataDownloaderURL} <ui:linkWithIcon href="${orgInstanceRecord.metadataDownloaderURL}"/>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.KBARTDownloaderURL.label" />
                            </dt>
                            <dd>
                                <g:if test="${orgInstanceRecord.kbartDownloaderURL}">
                                    ${orgInstanceRecord.kbartDownloaderURL} <ui:linkWithIcon href="${orgInstanceRecord.kbartDownloaderURL}"/>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>

            <%--
            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt><g:message code="org.sector.label" /></dt>
                        <dd>
                            <ui:xEditableRefData owner="${orgInstance}" field="sector" config="${RDConstants.ORG_SECTOR}" overwriteEditable="${isGrantedOrgRoleAdminOrOrgEditor && orgInstanceRecord == null}"/>
                        </dd>
                    </dl>
                    <dl>
                        <dt>${message(code: 'default.status.label')}</dt>
                        <dd>
                            <ui:xEditableRefData owner="${orgInstance}" field="status" config="${RDConstants.ORG_STATUS}" overwriteEditable="${isGrantedOrgRoleAdminOrOrgEditor && orgInstanceRecord == null}"/>
                        </dd>
                    </dl>
                    <g:if test="${orgInstance.status == RDStore.ORG_STATUS_RETIRED}">
                        <dl>
                            <dt>${message(code: 'org.retirementDate.label')}</dt>
                            <dd>
                                <g:formatDate date="${orgInstance.retirementDate}" format="${message(code: 'default.date.format.notime')}"/>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->
            --%>

            <g:if test="${links || isGrantedOrgRoleAdminOrOrgEditor}">
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
                                                            context: orgInstance,
                                                            linkInstanceType: row.class.name,
                                                            link: row
                                                  ]}" />--%>
                                            <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
                                                <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.unlink')}">
                                                    <g:link class="ui negative icon button la-modern-button la-selectable-button js-open-confirm-modal"
                                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.subscription.subscription")}"
                                                            data-confirm-term-how="unlink"
                                                            action="unlinkOrg" params="[id: orgInstance.id, combo: row.id]"
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
                        <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
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


            <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
                <div class="ui card">
                    <div class="content">
                        <%-- ROLE_ADMIN: all --%>
                        <dl>
                            <dt><g:message code="org.orgType.label" /></dt>
                            <dd>
                                <laser:render template="orgTypeAsList"
                                          model="${[org: orgInstance, orgTypes: orgInstance.orgType, availableOrgTypes: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE), editable: isGrantedOrgRoleAdminOrOrgEditor]}"/>
                            </dd>
                        </dl>

                        <laser:render template="orgTypeModal"
                                  model="${[org: orgInstance, availableOrgTypes: RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE), editable: isGrantedOrgRoleAdminOrOrgEditor]}"/>
                    </div>
                </div>
            </g:if>

            <g:if test="${orgInstance.isCustomerType_Inst()}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>
                                <g:message code="org.libraryType.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.libraryType.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="libraryType"
                                                        config="${RDConstants.LIBRARY_TYPE}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.subjectGroup.label" />
                            </dt>
                            <dd>
                                <%
                                    List<RefdataValue> subjectGroups = RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
                                %>
                                <laser:render template="orgSubjectGroupAsList"
                                          model="${[org: orgInstance, orgSubjectGroups: orgInstance.subjectGroup, availableSubjectGroups: subjectGroups, editable: editable]}"/>

                                <laser:render template="orgSubjectGroupModal"
                                          model="${[org: orgInstance, availableSubjectGroups: subjectGroups, editable: editable]}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.discoverySystems.frontend.label" /></dt>
                            <dd>
                                <laser:render template="discoverySystemAsList"
                                              model="${[org: orgInstance, config: 'discoverySystemFrontend', editable: editable]}"/>

                                <laser:render template="discoverySystemModal"
                                              model="${[org: orgInstance, config: 'discoverySystemFrontend', editable: editable]}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.discoverySystems.index.label" /></dt>
                            <dd>
                                <laser:render template="discoverySystemAsList"
                                              model="${[org: orgInstance, config: 'discoverySystemIndex', editable: editable]}"/>

                                <laser:render template="discoverySystemModal"
                                              model="${[org: orgInstance, config: 'discoverySystemIndex', editable: editable]}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.libraryNetwork.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.libraryNetwork.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="libraryNetwork"
                                                        config="${RDConstants.LIBRARY_NETWORK}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.funderType.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.funderType.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="funderType" config="${RDConstants.FUNDER_TYPE}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.funderHSK.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.funderHSK.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="funderHskType" config="${RDConstants.FUNDER_HSK_TYPE}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="address.country.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.country.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <ui:xEditableRefData id="country" owner="${orgInstance}" field="country" config="${RDConstants.COUNTRY}" />
                                &nbsp
                            </dd>
                            <dt>
                                <g:message code="org.region.label" />
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.region.expl')}">
                                    <i class="question circle icon"></i>
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
            </g:if>

            <%--
            <div class="ui card">
                <div class="content">
                    <g:if test="${orgInstance.isCustomerType_Inst()}">
                        <h2 class="ui header"><g:message code="org.contactpersons.and.addresses.label"/></h2>
                    </g:if>

                        <g:if test="${(orgInstance.id == institution.id && user.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR'))}">
                            <g:link action="myPublicContacts" controller="organisation" params="[id: orgInstance.id, tab: 'contacts']"
                                    class="ui button">${message('code': 'org.edit.contactsAndAddresses')}</g:link>
                        </g:if>
                </div>

                <div class="description">
                    <dl>
                        <dt>
                            <dd>
                            <laser:render template="publicContacts" model="[isProviderOrAgency: isProviderOrAgency, existsWekbRecord: orgInstanceRecord != null]"/>

                            <g:if test="${isProviderOrAgency && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ) && !orgInstanceRecord}">
                                <div class="ui list">

                                    <div class="item">

                                        <a href="#createPersonModal" class="ui button" data-ui="modal"
                                           onclick="JSPC.app.personCreate('contactPersonForProviderAgencyPublic', ${orgInstance.id}, '&supportType=${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.id}');"><g:message
                                                code="personFormModalTechnichalSupport"/></a>

                                    </div>
                                    <div class="item">

                                        <a href="#createPersonModal" class="ui button" data-ui="modal"
                                           onclick="JSPC.app.personCreate('contactPersonForProviderAgencyPublic', ${orgInstance.id}, '&supportType=${RDStore.PRS_FUNC_SERVICE_SUPPORT.id}');"><g:message
                                                code="personFormModalServiceSupport"/></a>

                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${!isProviderOrAgency}">
                                <div class="ui cards">

                                    <%
                                        Set<String> typeNames = new TreeSet<String>()
                                        typeNames.add(RDStore.ADDRESS_TYPE_BILLING.getI10n('value'))
                                        typeNames.add(RDStore.ADDRESS_TYPE_POSTAL.getI10n('value'))
                                        Map<String, List> typeAddressMap = [:]
                                        orgInstance.addresses.each {
                                            it.type.each { type ->
                                                String typeName = type.getI10n('value')
                                                typeNames.add(typeName)
                                                List addresses = typeAddressMap.get(typeName) ?: []
                                                addresses.add(it)
                                                typeAddressMap.put(typeName, addresses)
                                            }
                                        }
                                    %>
                                    <g:each in="${typeNames}" var="typeName">
                                        <div class="card">
                                            <div class="content">
                                                <div class="header la-primary-header">${typeName}</div>
                                                <div class="description">
                                                    <div class="ui divided middle aligned list la-flex-list">
                                                        <% List addresses = typeAddressMap.get(typeName) %>
                                                        <g:each in="${addresses}" var="a">
                                                            <g:if test="${a.org}">
                                                                <laser:render template="/templates/cpa/address" model="${[
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
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </g:if>

                            <g:if test="${isProviderOrAgency}">

                                <div class="ui cards">
                                    <%
                                        List visiblePersons = addressbookService.getVisiblePersons("addressbook",[org: orgInstance, sort: 'p.last_name, p.first_name'])
                                    %>
                                    <g:each in="${visiblePersons}" var="person">
                                        <%
                                            SortedSet<String> roles = new TreeSet<String>()
                                            person.roleLinks.each { PersonRole pr ->
                                                if(pr.functionType)
                                                    roles << pr.functionType.getI10n('value')
                                                if(pr.positionType)
                                                    roles << pr.positionType.getI10n('value')
                                            }
                                        %>
                                        <div class="card divided">
                                            <div class="content">
                                                <div class="header la-primary-header">${roles.join(' / ')}</div>
                                                <div class="description">
                                                    <div class="ui middle aligned list la-flex-list">
                                                        <laser:render template="/templates/cpa/person_full_details" model="${[
                                                                person                 : person,
                                                                personContext          : orgInstance,
                                                                tmplShowDeleteButton   : true,
                                                                tmplShowFunctions      : false,
                                                                tmplShowPositions      : true,
                                                                tmplShowResponsiblities: true,
                                                                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax', 'address'],
                                                                controller             : 'organisation',
                                                                action                 : 'show',
                                                                id                     : orgInstance.id,
                                                                editable               : false,
                                                                noSelection            : true
                                                        ]}"/>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </dd>
                        </g:if>
                        </dt>
                    </dl>
                </div>
            </div><!-- .card -->
            --%>

            <g:if test="${isProviderOrAgency}">

                <div class="ui card">
                    <div class="content">
                        <div class="ui accordion">
                            <div class="title">
                                <i class="dropdown icon la-dropdown-accordion"></i>
                                <div class="ui horizontal relaxed list">
                                    <div class="item">
                                        <strong><g:message code="org.platforms.label" /></strong>
                                        &nbsp;<div class="ui blue circular label">${orgInstance.platforms.size()}</div>
                                    </div>
                                    <div class="item">
                                        <strong><g:message code="package.plural" /></strong>
                                        &nbsp;<div class="ui blue circular label">${packages.size()}</div>
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
                                <p class="ui header">%{--<i class="icon cloud"></i>--}% <g:message code="org.platforms.label" /></p>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance.platforms}" var="platform">
                                        <g:if test="${platform.status == RDStore.PLATFORM_STATUS_CURRENT}">
                                            <div class="ui item">
                                                <div class="content la-space-right">
                                                    <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>

                                <p class="ui header">%{--<i class="icon gift"></i>--}% <g:message code="package.plural" /></p>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${packages}" var="pkg">
                                        <div class="ui item">
                                            <div class="content la-space-right">
                                                <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>

                                <p class="ui header">%{--<i class="icon clipboard"></i>--}% <g:message code="subscription.plural" /></p>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <div class="ui item">
                                        <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: orgInstance.globalUID, status: RDStore.SUBSCRIPTION_CURRENT.id]">
                                            <div class="content la-space-right">
                                                <i class="icon filter"></i> <g:message code="subscription.plural.current" />
                                            &nbsp;<div class="ui blue circular label">${currentSubscriptionsCount}</div>
                                            </div>
                                        </g:link>
                                    </div>
                                    <div class="ui item">
                                        <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: orgInstance.globalUID, status: 'FETCH_ALL']">
                                            <div class="content la-space-right">
                                                <i class="icon filter"></i> <g:message code="subscription.plural.total" />
                                            &nbsp;<div class="ui blue circular label">${subLinks.size()}</div>
                                            </div>
                                        </g:link>
                                    </div>
                                </div>

                                <p class="ui header">%{--<i class="icon balance scale"></i>--}% <g:message code="license.plural" /></p>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="myInstitution" action="currentLicenses" params="[licensor: orgInstance.id, status: RDStore.LICENSE_CURRENT.id, subStatus: RDStore.SUBSCRIPTION_CURRENT.id, filterSubmit: 'Filtern']">
                                                <i class="icon filter"></i> <g:message code="license.plural.current" />
                                                &nbsp;<div class="ui blue circular label">${currentLicensesCount}</div></g:link>
                                        </div>
                                    </div>
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="myInstitution" action="currentLicenses" params="[licensor: orgInstance.id, filterSubmit: 'Filtern']">
                                                <i class="icon filter"></i> <g:message code="license.plural.total" />
                                                &nbsp;<div class="ui blue circular label">${licLinks.size()}</div></g:link>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>
                </div>

                %{--
                <div class="ui card">
                    <div class="content">
                        <div class="ui accordion">
                            <div class="title">
                                <i class="dropdown icon la-dropdown-accordion"></i> <g:message code="org.platforms.label" />
                                &nbsp;<div class="ui blue circular label">${orgInstance.platforms.size()}</div>
                            </div>
                            <div class="content">
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance.platforms}" var="platform">
                                        <g:if test="${platform.status == RDStore.PLATFORM_STATUS_CURRENT}">
                                            <div class="ui item">
                                                <div class="content la-space-right">
                                                    <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                    <div class="ui card">
                        <div class="content">
                            <div class="ui accordion">
                                <div class="title">
                                    <i class="dropdown icon la-dropdown-accordion"></i> <g:message code="package.plural" />
                                    &nbsp;<div class="ui blue circular label">${packages.size()}</div>
                                </div>
                                <div class="content">
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${packages}" var="pkg">
                                            <div class="ui item">
                                                <div class="content la-space-right">
                                                    <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
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
                        <div class="ui accordion">
                            <div class="title">
                                <i class="dropdown icon la-dropdown-accordion"></i> <g:message code="subscription.plural" />
                                &nbsp;<div class="ui blue circular label">${currentSubscriptionsCount}/${subLinks.size()}</div>
                            </div>
                            <div class="content">
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <div class="ui item">
                                        <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: orgInstance.globalUID, status: RDStore.SUBSCRIPTION_CURRENT.id]">
                                            <div class="content la-space-right">
                                                <i class="icon filter"></i> <g:message code="subscription.plural.current" />
                                                &nbsp;<div class="ui blue circular label">${currentSubscriptionsCount}</div>
                                            </div>
                                        </g:link>
                                    </div>
                                    <div class="ui item">
                                        <g:link controller="myInstitution" action="currentSubscriptions" params="[identifier: orgInstance.globalUID, status: 'FETCH_ALL']">
                                            <div class="content la-space-right">
                                                <i class="icon filter"></i> <g:message code="subscription.plural.total" />
                                                &nbsp;<div class="ui blue circular label">${subLinks.size()}</div>
                                            </div>
                                        </g:link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <div class="ui accordion">
                            <div class="title">
                                <i class="dropdown icon la-dropdown-accordion"></i> <g:message code="license.plural" />
                                &nbsp;<div class="ui blue circular label">${currentLicensesCount}/${licLinks.size()}</div>
                            </div>
                            <div class="content">
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="myInstitution" action="currentLicenses" params="[licensor: orgInstance.id, status: RDStore.LICENSE_CURRENT.id, subStatus: RDStore.SUBSCRIPTION_CURRENT.id, filterSubmit: 'Filtern']">
                                                <i class="icon filter"></i> <g:message code="license.plural.current" />
                                                &nbsp;<div class="ui blue circular label">${currentLicensesCount}</div></g:link>
                                        </div>
                                    </div>
                                    <div class="ui item">
                                        <div class="content la-space-right">
                                            <g:link controller="myInstitution" action="currentLicenses" params="[licensor: orgInstance.id, filterSubmit: 'Filtern']">
                                                <i class="icon filter"></i> <g:message code="license.plural.total" />
                                                &nbsp;<div class="ui blue circular label">${licLinks.size()}</div></g:link>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                --}%
                        <%--
                        <div class="ui accordion">
                            <div class="title">
                                <i class="dropdown icon la-dropdown-accordion"></i>
                            </div>
                            <div class="content">
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${subLinks}" var="subLink">
                                        <div class="ui item">
                                            <div class="content la-space-right">
                                                <strong><g:link controller="subscription" action="show"
                                                                id="${subLink.id}">${subLink.name} (${subLink.status.getI10n('value')})
                                                                <g:if test="${subLink.startDate && subLink.endDate}">
                                                                    (${ DateUtils.getLocalizedSDF_noTime().format(subLink.startDate)}-${DateUtils.getLocalizedSDF_noTime().format(subLink.endDate)})
                                                                </g:if>
                                                </g:link>
                                                </strong>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                        --%>
            </g:if>

            <g:if test="${!isProviderOrAgency}">
                <g:if test="${(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || institution.isCustomerType_Consortium()) && (institution != orgInstance)}">
                    <g:if test="${orgInstance.createdBy || orgInstance.legallyObligedBy}">
                        <div class="ui card">
                            <div class="content">
                                <g:if test="${orgInstance.createdBy}">
                                    <dl>
                                        <dt>
                                            <g:message code="org.createdBy.label" />
                                        </dt>
                                        <dd>
                                            <h5 class="ui header">
                                                <g:link controller="organisation" action="show" id="${orgInstance.createdBy.id}">${orgInstance.createdBy.name}</g:link>
                                            </h5>
                                            <g:if test="${createdByOrgGeneralContacts}">
                                                <g:each in="${createdByOrgGeneralContacts}" var="cbogc">
                                                    <laser:render template="/templates/cpa/person_full_details" model="${[
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
                                        </dd>
                                    </dl>
                                </g:if>
                                <g:if test="${orgInstance.legallyObligedBy}">
                                    <dl>
                                        <dt>
                                            <g:message code="org.legallyObligedBy.label" />
                                        </dt>
                                        <dd>
                                            <h5 class="ui header">
                                                <g:link controller="organisation" action="show" id="${orgInstance.legallyObligedBy.id}">${orgInstance.legallyObligedBy.name}</g:link>
                                            </h5>
                                            <g:if test="${legallyObligedByOrgGeneralContacts}">
                                                <g:each in="${legallyObligedByOrgGeneralContacts}" var="lobogc">
                                                    <laser:render template="/templates/cpa/person_full_details" model="${[
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
                                        </dd>
                                    </dl>
                                </g:if>
                            </div>
                        </div><!-- .card -->
                    </g:if>
                </g:if>
            </g:if>

            <g:if test="${contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[ orgInstance: orgInstance, authOrg: formalOrg, contextOrg: institution ]}"/>
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
                                    <g:if test="${inContextOrg}">
                                        <a href="#createPersonModal" class="ui icon button blue la-modern-button createContact" id="contactPersonForPublic" data-ui="modal">
                                            <i aria-hidden="true" class="plus icon"></i>
                                        </a>
                                    </g:if>
%{--                                    <g:elseif test="${isProviderOrAgency}">--}%
%{--                                        <a href="#createPersonModal" class="ui icon button blue la-modern-button createContact" id="contactPersonForProviderAgencyPublic" data-ui="modal">--}%
%{--                                            <i aria-hidden="true" class="plus icon"></i>--}%
%{--                                        </a>--}%
%{--                                    </g:elseif>--}%
                                </div>
                            </div>
                        </div>

                            <%--
                            <g:if test="${(orgInstance.id == institution.id && user.hasCtxAffiliation_or_ROLEADMIN('INST_EDITOR'))}">
                                <g:link action="myPublicContacts" controller="organisation" params="[id: orgInstance.id, tab: 'contacts']"
                                        class="ui button">${message('code': 'org.edit.contactsAndAddresses')}</g:link>
                            </g:if>
                            --%>
                        <g:if test="${PersonRole.executeQuery('select pr from Person p join p.roleLinks pr where pr.org = :org and ((p.isPublic = false and p.tenant = :ctx) or p.isPublic = true)', [org: orgInstance, ctx: institution]) ||
                                Address.executeQuery('select a from Address a where a.org = :org and (a.tenant = :ctx or a.tenant = null)', [org: orgInstance, ctx: institution])}">
                            <table class="ui compact table">
                                <g:if test="${!isProviderOrAgency}">
                                    <tr>
                                        <td>
                                            <div class="ui segment la-timeLineSegment-contact">
                                                <div class="la-timeLineGrid">
                                                    <div class="ui grid">
                                                        <g:set var="persons" value="${orgInstance.getContactPersonsByFunctionType(true, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)}"/>
                                                        <g:each in="${persons}" var="prs">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${orgInstanceRecord}">
                                                                        <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + orgInstance.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                                    </g:if>
                                                                    <g:elseif test="${prs.isPublic}">
                                                                        <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                                    </g:elseif>
                                                                    <g:else>
                                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>
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
                                                                                <laser:render template="/templates/cpa/contact" model="${[
                                                                                        overwriteEditable   : false,
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                    <%--<g:each in="${prs.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                                                                            <laser:render template="/templates/cpa/address"
                                                                                          model="${[address: address, tmplShowDeleteButton: false, editable: false]}"/>
                                                                    </g:each>--%>
                                                                </div>
                                                            </div>
                                                        </g:each>
                                                        <g:set var="persons" value="${orgInstance.getContactPersonsByFunctionType(true, RDStore.PRS_FUNC_FC_BILLING_ADDRESS)}"/>
                                                        <g:each in="${persons}" var="prs">
                                                            <div class="row">
                                                                <div class="two wide column">
                                                                    <g:if test="${prs.isPublic}">
                                                                        <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                                    </g:else>
                                                                </div>
                                                                <div class="fourteen wide column">
                                                                    <div class="ui label">${RDStore.PRS_FUNC_FC_BILLING_ADDRESS.getI10n('value')}</div>
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
                                                                                <laser:render template="/templates/cpa/contact" model="${[
                                                                                        overwriteEditable   : false,
                                                                                        contact             : contact,
                                                                                        tmplShowDeleteButton: false
                                                                                ]}"/>
                                                                            </g:if>
                                                                        </g:each>
                                                                    </g:if>
                                                                    <%--<g:each in="${prs.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                                                                            <laser:render template="/templates/cpa/address"
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
                                                                                    <laser:render template="/templates/cpa/address" model="${[
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
                                </g:if>
                                <g:if test="${isProviderOrAgency}">
                                    <g:set var="providerContacts" value="${orgInstance.getContactPersonsByFunctionType(true, null, orgInstanceRecord != null)}"/>
                                    <%--
                                    <g:if test="${isProviderOrAgency && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ) && !orgInstanceRecord}">
                                        <tr>
                                            <td>
                                                <a href="#createPersonModal" class="ui button" data-ui="modal"
                                                   onclick="JSPC.app.personCreate('contactPersonForProviderAgencyPublic', ${orgInstance.id}, '&supportType=${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.id}');"><g:message
                                                        code="personFormModalTechnichalSupport"/></a>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <a href="#createPersonModal" class="ui button" data-ui="modal"
                                                   onclick="JSPC.app.personCreate('contactPersonForProviderAgencyPublic', ${orgInstance.id}, '&supportType=${RDStore.PRS_FUNC_SERVICE_SUPPORT.id}');"><g:message
                                                        code="personFormModalServiceSupport"/></a>
                                            </td>
                                        </tr>
                                    </g:if>
                                    --%>
                                    <tr>
                                        <td>
                                            <g:if test="${providerContacts}">
                                                <div class="ui segment la-timeLineSegment-contact">
                                                    <div class="la-timeLineGrid">
                                                        <div class="ui grid">
                                                            <g:each in="${providerContacts}" var="prs">
                                                                <div class="row">
                                                                    <div class="two wide column">
                                                                        <g:if test="${orgInstanceRecord}">
                                                                            <a target="_blank" href="${wekbAPI.editUrl ? wekbAPI.editUrl + '/public/orgContent/' + orgInstance.gokbId : '#'}"><i class="circular large la-gokb icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'org.isWekbCurated.header.label')} (we:kb Link)"></i></a>
                                                                        </g:if>
                                                                        <g:elseif test="${prs.isPublic}">
                                                                            <i class="circular large address card icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.public')}"></i>
                                                                        </g:elseif>
                                                                        <g:else>
                                                                            <i class="circular large address card outline icon la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay" data-content="${message(code:'address.private')}"></i>
                                                                        </g:else>
                                                                    </div>
                                                                    <div class="fourteen wide column">
                                                                        <div class="ui label">${prs.roleLinks.collect { PersonRole pr -> pr.roleType.getI10n('value')}.join (' / ')}</div>
                                                                        <g:if test="${!(prs.last_name in [RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value'), RDStore.PRS_FUNC_METADATA.getI10n('value')])}"><div class="ui header">${prs}</div></g:if>
                                                                        <g:if test="${prs.contacts}">
                                                                            <g:each in="${prs.contacts.toSorted()}" var="contact">
                                                                                <g:if test="${contact.contentType && contact.contentType.value in ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax']}">
                                                                                    <laser:render template="/templates/cpa/contact" model="${[
                                                                                            overwriteEditable   : (orgInstanceRecord == null && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC )),
                                                                                            contact             : contact,
                                                                                            tmplShowDeleteButton: (orgInstanceRecord == null && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ))
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
                                    <div class="right aligned four wide column">
                                        <g:if test="${isProviderOrAgency}">
                                            <a href="#createPersonModal" class="ui icon button blue la-modern-button createContact" id="contactPersonForProviderAgency" data-ui="modal">
                                                <i aria-hidden="true" class="plus icon"></i>
                                            </a>
                                        </g:if>
                                        <g:else>
                                            <a href="#createPersonModal" class="ui icon button blue la-modern-button createContact" id="contactPersonForInstitution" data-ui="modal">
                                                <i aria-hidden="true" class="plus icon"></i>
                                            </a>
                                        </g:else>
                                    </div>
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
                                                                    <%--<g:each in="${person.addresses.sort { it.type.each{it?.getI10n('value') }}}" var="address">
                                                                            <laser:render template="/templates/cpa/address"
                                                                                          model="${[address: address, tmplShowDeleteButton: false, editable: false]}"/>
                                                                    </g:each>
                                                                    <laser:render template="/templates/cpa/person_full_details" model="${[
                                                                                    person                 : person,
                                                                                    personContext          : orgInstance,
                                                                                    tmplShowDeleteButton   : true,
                                                                                    tmplShowFunctions      : false,
                                                                                    tmplShowPositions      : true,
                                                                                    tmplShowResponsiblities: true,
                                                                                    tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax', 'address'],
                                                                                    controller             : 'organisation',
                                                                                    action                 : 'show',
                                                                                    id                     : orgInstance.id,
                                                                                    editable               : false,
                                                                                    noSelection            : true
                                                                        ]}"/>--%>
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
                                                                                    <g:if test="${respObj instanceof TitleInstancePackagePlatform}">
                                                                                        (<g:link controller="tipp" action="show" id="${respObj.id}">${respObj.name}</g:link>)
                                                                                    </g:if>
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

    JSPC.app.addresscreate_org = function (orgId, typeId, redirect, hideType) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?orgId=' + orgId + '&typeId=' + typeId + '&redirect=' + redirect + '&hideType=' + hideType;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }

    <%--JSPC.app.addresscreate_prs = function (prsId, typeId, redirect, hideType) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?prsId=' + prsId + '&typeId=' + typeId + '&redirect=' + redirect + '&hideType=' + hideType;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url);
        func();
    }--%>

    <g:if test="${orgInstance.isCustomerType_Inst()}">
        if($("#country").length) {
            JSPC.app.showRegionsdropdown( $("#country").editable('getValue', true) );
        }
    </g:if>
    $('.addListValue').click(function() {
        let url;
        let returnSelector;
        switch($(this).attr('data-objtype')) {
            case 'altname': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: orgInstance.id]"/>';
                returnSelector = '#altnames';
                break;
            case 'frontend': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'frontend', owner: orgInstance.id]"/>';
                returnSelector = '#discoverySystemsFrontend';
                break;
            case 'index': url = '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'index', owner: orgInstance.id]"/>';
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
        <g:if test="${orgInstanceRecord}">
            let existsWekbRecord = "&existsWekbRecord=true";
        </g:if>
        <g:else>
            let existsWekbRecord = "";
        </g:else>
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&org=' + org + existsWekbRecord + '&showAddresses=false&showContacts=true' + supportType;
        var func = bb8.ajax4SimpleModalFunction("#personModal", url);
        func();
    }

<g:if test="${editable && orgInstanceRecord == null}">
    let confirmationCard = $('#js-confirmationCard');
    $('.js-open-confirm-modal-xEditable', confirmationCard).editable('destroy').editable().on('shown', function() {
                                    r2d2.initDynamicUiStuff('.js-open-confirm-modal-xEditable');
                                });
</g:if >


</laser:script>

<laser:htmlEnd />