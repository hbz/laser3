<%@ page import="de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.OrgRole; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Org; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo; de.laser.Contact" %>

<laser:htmlStart message="menu.institutions.org_info" serviceInjection="true" />

    <g:if test="${isProviderOrAgency}">
        <g:set var="entityName" value="${message(code: 'default.provider.label')}"/>
    </g:if>
    <g:elseif test="${institutionalView}">
        <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
    </g:elseif>
    <g:else>
        <g:set var="entityName" value="${message(code: 'org.label')}"/>
    </g:else>


<laser:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>


<ui:controlButtons>
    <laser:render template="actions" model="${[org: orgInstance, user: user]}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${orgInstance.name}" />

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

<laser:render debug="true" template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, isProviderOrAgency: isProviderOrAgency]}"/>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>

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

                            <g:if test="${orgInstance.getCustomerType() == 'ORG_INST'}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${orgInstance.getCustomerTypeI10n()}">
                                    <i class="chess rook grey icon"></i>
                                </span>
                            </g:if>
                        </dd>
                    </dl>
                    <g:if test="${!inContextOrg || isGrantedOrgRoleAdminOrOrgEditor}">
                        <dl>
                            <dt><g:message code="org.shortname.label" /></dt>
                            <dd>
                                <ui:xEditable
                                        data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                        data_confirm_term_how="ok"
                                        class="js-open-confirm-modal-xEditable"
                                        owner="${orgInstance}" field="shortname" overwriteEditable="${editable && orgInstanceRecord == null}"/>
                            </dd>
                        </dl>
                        <g:if test="${!isProviderOrAgency}">
                            <dl>
                                <dt>
                                    <g:message code="org.sortname.label" />
                                </dt>
                                <dd>
                                    <ui:xEditable
                                            data_confirm_tokenMsg="${message(code: 'confirmation.content.central')}"
                                            data_confirm_term_how="ok"
                                            class="js-open-confirm-modal-xEditable"
                                            owner="${orgInstance}" field="sortname"/>
                                </dd>
                            </dl>
                        </g:if>
                    </g:if>
                    <dl>
                        <dt>
                            <g:message code="org.altname.label" />
                        </dt>
                        <dd>
                            <div id="altnames" class="ui divided middle aligned selection list la-flex-list">
                                <g:each in="${orgInstance.altnames}" var="altname">
                                    <div class="ui item" data-objId="${altname.id}">
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
                                                                   data-confirm-term-how="delete" data-done="JSPC.app.removeAltname(${altname.id})">
                                                        <i class="trash alternate outline icon"></i>
                                                    </ui:remoteLink>
                                                </div>
                                            </div>
                                        </g:if>
                                    </div>
                                </g:each>
                            </div>
                            <g:if test="${orgInstanceRecord == null}">
                                <input name="addAltname" id="addAltname" type="button" class="ui button" value="${message(code: 'org.altname.add')}">
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
                            <br />&nbsp<br />&nbsp<br />
                        </dd>
                    </dl>
                    <g:if test="${orgInstance.getCustomerType() == 'ORG_INST'}">
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
                                <br />&nbsp<br />&nbsp<br />
                            </dd>
                        </dl>
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

            <g:if test="${orgInstance.getCustomerType() in ['ORG_BASIC_MEMBER','ORG_INST']}">
                <div class="ui card">
                    <div class="content">
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
                                    ${orgInstanceRecord.metadataDownloaderURL} <a href="${orgInstanceRecord.metadataDownloaderURL}"><i title="${message(code: 'org.metadataDownloaderURL.label')} Link" class="external alternate icon"></i></a>
                                </g:if>
                            </dd>
                        </dl>
                        <dl>
                            <dt>
                                <g:message code="org.KBARTDownloaderURL.label" />
                            </dt>
                            <dd>
                                <g:if test="${orgInstanceRecord.kbartDownloaderURL}">
                                    ${orgInstanceRecord.kbartDownloaderURL} <a href="${orgInstanceRecord.kbartDownloaderURL}"><i title="${message(code: 'org.KBARTDownloaderURL.label')} Link" class="external alternate icon"></i></a>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>

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

            <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER']}">
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


            <div class="ui card">
                <div class="content">
                    <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER']}">
                        <h2 class="ui header"><g:message code="org.contactpersons.and.addresses.label"/></h2>
                    </g:if>

                        <g:if test="${(orgInstance.id == institution.id && user.hasAffiliation('INST_EDITOR'))}">
                            <g:link action="myPublicContacts" controller="organisation" params="[id: orgInstance.id, tab: 'contacts']"
                                    class="ui button">${message('code': 'org.edit.contactsAndAddresses')}</g:link>
                        </g:if>
                </div>

                <div class="description">
                    <dl>
                        <dt>
                            <dd>
                            <laser:render template="publicContacts" model="[isProviderOrAgency: isProviderOrAgency, existsWekbRecord: orgInstanceRecord != null]"/>

                            <g:if test="${isProviderOrAgency && accessService.checkPermAffiliationX('ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN') && !orgInstanceRecord}">
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
                                        typeNames.add(RDStore.ADRESS_TYPE_BILLING.getI10n('value'))
                                        typeNames.add(RDStore.ADRESS_TYPE_POSTAL.getI10n('value'))
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
                            </dd>
                        <g:if test="${isProviderOrAgency}">
                            <dd>
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


            <g:if test="${isProviderOrAgency}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.platforms.label" /></dt>
                            <dd>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance.platforms}" var="platform">
                                        <g:if test="${platform.status == RDStore.PLATFORM_STATUS_CURRENT}">
                                            <div class="ui item">
                                                <div class="content la-space-right">
                                                    <strong><g:link controller="platform" action="show"
                                                                    id="${platform.id}">${platform.name}</g:link>
                                                    </strong>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:each>
                                </div>
                            </dd>
                        </dl>
                    </div>
                </div>
                <g:if test="${params.my}">
                    <div class="ui card">
                        <div class="content">
                            <div class="ui accordion">
                                <div class="title">
                                    <div class="ui blue ribbon label">${packages.size()}</div>
                                    <g:message code="package.plural" /><i class="dropdown icon la-dropdown-accordion"></i>
                                </div>
                                <div class="content">
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${packages}" var="pkg">
                                            <div class="ui item">
                                                <div class="content la-space-right">
                                                    <strong>
                                                        <g:link controller="package" action="show" id="${pkg.id}">${pkg.name}</g:link>
                                                    </strong>
                                                </div>
                                            </div>
                                        </g:each>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>
                <div class="ui card">
                    <div class="content">
                        <div class="ui accordion">
                            <div class="title">
                                <div class="ui blue ribbon label">${subLinks.size()}</div>
                                <g:message code="subscription.plural" /><i class="dropdown icon la-dropdown-accordion"></i>
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
                    </div>
                </div>
            </g:if>

                <g:if test="${(user.isAdmin() || institution.getCustomerType()  == 'ORG_CONSORTIUM') && (institution != orgInstance)}">
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
                                                            tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
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
                                                            tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
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

            <g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
                <div id="new-dynamic-properties-block">
                    <laser:render template="properties" model="${[
                            orgInstance   : orgInstance,
                            authorizedOrgs: authorizedOrgs,
                            contextOrg: institution
                    ]}"/>
                </div><!-- #new-dynamic-properties-block -->
            </g:if>

        </div>
    </div>
    <aside class="five wide column la-sidekick">
        <div class="ui one cards">
            <laser:render template="/templates/aside1" model="${[ownobj: orgInstance, owntp: 'organisation']}"/>
        </div>
    </aside>
</div>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<laser:script file="${this.getGroovyPageFileName()}">
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
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url, false);
        func();
    }

    JSPC.app.addresscreate_prs = function (prsId, typeId, redirect, hideType) {
        var url = '<g:createLink controller="ajaxHtml" action="createAddress"/>?prsId=' + prsId + '&typeId=' + typeId + '&redirect=' + redirect + '&hideType=' + hideType;
        var func = bb8.ajax4SimpleModalFunction("#addressFormModal", url, false);
        func();
    }

    <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER']}">
        JSPC.app.showRegionsdropdown( $("#country").editable('getValue', true) );
    </g:if>
    $('#addAltname').click(function() {
        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="addObject" params="[object: 'altname', owner: orgInstance.id]"/>',
            success: function(result) {
                $('#altnames').append(result);
                r2d2.initDynamicUiStuff('#altnames');
                r2d2.initDynamicXEditableStuff('#altnames');
            }
        });
    });
    JSPC.app.removeAltname = function(objId) {
        $("div[data-objId='"+objId+"']").remove();
    }

<g:if test="${isProviderOrAgency}">

    JSPC.app.personCreate = function (contactFor, org, supportType = "") {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&org=' + org + '&showAddresses=false&showContacts=true' + supportType;
        var func = bb8.ajax4SimpleModalFunction("#personModal", url, false);
        func();
    }
</g:if>
    let confirmationCard = $('#js-confirmationCard')
    $('.js-open-confirm-modal-xEditable', confirmationCard).editable('destroy').editable().on('shown', function() {
                                    r2d2.initDynamicUiStuff('.js-open-confirm-modal-xEditable');
                                });

</laser:script>

<laser:htmlEnd />