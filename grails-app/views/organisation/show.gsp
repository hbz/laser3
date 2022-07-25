<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.OrgSubjectGroup; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.PersonRole; de.laser.Org; de.laser.properties.PropertyDefinition; de.laser.properties.PropertyDefinitionGroup; de.laser.OrgSetting;de.laser.Combo" %>

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
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>


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
    <div class="ten wide column">

        <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt><g:message code="default.name.label" /></dt>
                        <dd>
                            <ui:xEditable owner="${orgInstance}" field="name"/>
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
                                <ui:xEditable owner="${orgInstance}" field="shortname"/>
                            </dd>
                        </dl>
                        <g:if test="${!isProviderOrAgency}">
                            <dl>
                                <dt>
                                    <g:message code="org.sortname.label" />
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${orgInstance}" field="sortname"/>
                                </dd>
                            </dl>
                        </g:if>
                    </g:if>
                    <dl>
                        <dt>
                            <g:message code="org.altname.label" />
                        </dt>
                        <dd>
                            <ul>
                                <g:each in="${orgInstance.altnames}" var="altname">
                                    <li>${altname.name}</li>
                                </g:each>
                            </ul>
                        </dd>
                    </dl>
                    <dl>
                        <dt><g:message code="default.url.label"/></dt>
                        <dd>
                            <ui:xEditable owner="${orgInstance}" type="url" field="url" class="la-overflow la-ellipsis" />
                            <g:if test="${orgInstance.url}">
                                <ui:linkWithIcon href="${orgInstance.url}" />
                            </g:if>
                            <br />&nbsp<br />&nbsp<br />
                        </dd>
                    </dl>
                    <g:if test="${!isProviderOrAgency}">
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
                    </g:if>
                    <g:if test="${ !isProviderOrAgency}">
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

            <g:if test="${!isProviderOrAgency}">
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
            <g:elseif test="${isProviderOrAgency && orgInstanceRecord}">
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
            </g:elseif>

            <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.sector.label" /></dt>
                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="sector" config="${RDConstants.ORG_SECTOR}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'default.status.label')}</dt>

                            <dd>
                                <ui:xEditableRefData owner="${orgInstance}" field="status" config="${RDConstants.ORG_STATUS}"/>
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
            </g:if>

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

            <g:if test="${ !isProviderOrAgency}">
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


            <g:if test="${isProviderOrAgency}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.platforms.label" /></dt>
                            <dd>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance.platforms}" var="platform">
                                        <div class="ui item">
                                            <div class="content la-space-right">
                                                <strong><g:link controller="platform" action="show"
                                                                id="${platform.id}">${platform.name}</g:link>
                                                </strong>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </dd>
                        </dl>
                    </div>
                </div>
            </g:if>


            <div class="ui card">
                <div class="content">
                    <g:if test="${!isProviderOrAgency}">
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
                        </dt>
                    </dl>
                </div>
            </div><!-- .card -->

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
    <aside class="six wide column la-sidekick">
        <div class="ui one cards">
            <laser:render template="/templates/aside1" model="${[ownobj: orgInstance, owntp: 'organisation']}"/>
        </div>
    </aside>
</div>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
%{-- grails-3: performance issue <laser:render template="/templates/debug/orgRoles" model="[debug: orgInstance.links]"/> --}%
%{--<laser:render template="/templates/debug/prsRoles" model="[debug: orgInstance.prsLinks]"/>--}%
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

    <g:if test="${!isProviderOrAgency}">
        JSPC.app.showRegionsdropdown( $("#country").editable('getValue', true) );
    </g:if>

<g:if test="${isProviderOrAgency}">

    JSPC.app.personCreate = function (contactFor, org, supportType = "") {
        var url = '<g:createLink controller="ajaxHtml" action="createPerson"/>?contactFor=' + contactFor + '&org=' + org + '&showAddresses=false&showContacts=true' + supportType;
        var func = bb8.ajax4SimpleModalFunction("#personModal", url, false);
        func();
    }
</g:if>
</laser:script>

<laser:htmlEnd />