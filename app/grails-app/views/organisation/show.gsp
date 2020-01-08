<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup; com.k_int.kbplus.OrgSettings" %>
<%@ page import="com.k_int.kbplus.Combo;grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:if test="${RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds()}">
        <g:set var="entityName" value="${message(code: 'default.provider.label')}"/>
    </g:if>
    <g:elseif test="${institutionalView}">
        <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
    </g:elseif>
    <g:elseif test="${departmentalView}">
        <g:set var="entityName" value="${message(code: 'org.department.label')}"/>
    </g:elseif>
    <g:else>
        <g:set var="entityName" value="${message(code: 'org.label')}"/>
    </g:else>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code:'menu.institutions.org_info')}</title>

    <g:javascript src="properties.js"/>
</head>

<body>

<semui:debugInfo>
    <g:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
    <g:render template="/templates/debug/orgRoles" model="[debug: orgInstance.links]"/>
    <g:render template="/templates/debug/prsRoles" model="[debug: orgInstance.prsLinks]"/>
</semui:debugInfo>

<g:render template="breadcrumb"
          model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, departmentalView: departmentalView, institutionalView: institutionalView]}"/>

<g:if test="${accessService.checkPermX('ORG_INST,ORG_CONSORTIUM', 'ROLE_ORG_EDITOR,ROLE_ADMIN')}">
    <semui:controlButtons>
        <g:render template="actions" model="${[org: orgInstance, user: user]}"/>
    </semui:controlButtons>
</g:if>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}
    <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
              data-content="${orgInstance.getCustomerTypeI10n()}">
            <i class="chess rook icon"></i>
        </span>
    </g:if>
</h1>

<g:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: inContextOrg]}"/>

<semui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<g:if test="${departmentalView == false}">
    <g:render template="/templates/meta/identifier" model="${[object: orgInstance, editable: editable]}"/>
</g:if>

<semui:messages data="${flash}"/>

<div class="ui stackable grid">
    <div class="twelve wide column">

        <div class="la-inline-lists">

            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt><g:message code="org.name.label" default="Name"/></dt>
                        <dd>
                            <semui:xEditable owner="${orgInstance}" field="name"/>
                            <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${orgInstance.getCustomerTypeI10n()}">
                                    <i class="chess rook icon"></i>
                                </span>
                            </g:if>

                        </dd>
                    </dl>
                    <g:if test="${!inContextOrg || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                        <g:if test="${departmentalView == false}">
                            <dl>
                                <dt><g:message code="org.shortname.label" default="Shortname"/></dt>
                                <dd>
                                    <semui:xEditable owner="${orgInstance}" field="shortname"/>
                                </dd>
                            </dl>
                        </g:if>
                        <g:if test="${!(RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds())}">
                            <dl>
                                <dt>
                                    <g:message code="org.sortname.label" default="Sortname"/>
                                    <g:if test="${departmentalView == false}">
                                        <br>
                                        <g:message code="org.sortname.onlyForLibraries.label"/>
                                    </g:if>
                                </dt>
                                <dd>
                                    <semui:xEditable owner="${orgInstance}" field="sortname"/>
                                </dd>
                            </dl>
                        </g:if>
                    </g:if>
                </div>
            </div><!-- .card -->


            <div class="ui card">
                <div class="content">
                    <dl>
                        <dt><g:message code="org.url.label"/></dt>
                        <dd>
                            <semui:xEditable owner="${orgInstance}" field="url"/>
                            <g:if test="${orgInstance.url}">
                                <semui:linkIcon href="${orgInstance.url}"/>
                            </g:if>
                        </dd>
                    </dl>
                    <g:if test="${!departmentalView}">
                        <dl>
                            <dt><g:message code="org.urlGov.label"/></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="urlGov"/>
                                <g:if test="${orgInstance.urlGov}">
                                    <semui:linkIcon href="${orgInstance.urlGov}"/>
                                </g:if>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <%-- orgInstance.hasPerm("ORG_INST,ORG_CONSORTIUM") && ((!fromCreate) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')) --%>
            <g:if test="${departmentalView == false}">
                <div class="ui card">
                    <div class="content">
                        <div class="header"><g:message code="default.identifiers.label"/></div>
                    </div>

                    <div class="content">
                        <dl>
                            <dt>ISIL</dt>
                            <dd>
                                <g:set var="isils"
                                       value="${orgInstance.ids.findAll { it?.ns?.ns == 'ISIL' }}"/>
                                <g:if test="${isils}">
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${isils}" var="isil">
                                            <div class="ui item">

                                                <div class="content la-space-right">
                                                    <semui:xEditable owner="${isil}" field="value"/>
                                                </div>

                                                <div class="content">
                                                    <g:if test="${editable}">
                                                    <%-- TODO [ticket=1612] new identifier handling
                                                        <g:link class="ui mini negative button" controller="ajax"
                                                                action="deleteThrough"
                                                                params='${[contextOid: "${orgInstance.class.name}:${orgInstance.id}", contextProperty: "ids", targetOid: "${isil.class.name}:${isil.id}"]}'>
                                                            <i class="trash alternate icon"></i></g:link>
                                                    --%>
                                                        <g:link controller="ajax" action="deleteIdentifier" class="ui icon mini negative button"
                                                                params='${[owner: "${orgInstance.class.name}:${orgInstance.id}", target: "${isil.class.name}:${isil.id}"]}'>
                                                            <i class="trash alternate icon"></i></g:link>
                                                    </g:if>
                                                </div>

                                            </div>
                                        </g:each>

                                    </div>

                                </g:if>
                                <g:if test="${editable}">
                                    <%-- TODO [ticket=1612] new identifier handling --%>
                                    <g:form controller="ajax" action="addIdentifier" class="ui form">
                                        <input name="owner" type="hidden" value="${orgInstance.class.name}:${orgInstance.id}" />
                                        <input name="namespace" type="hidden" value="com.k_int.kbplus.IdentifierNamespace:${com.k_int.kbplus.IdentifierNamespace.findByNs('ISIL').id}" />

                                        <div class="fields">
                                            <div class="field">
                                                <input name="value" id="value" type="text" class="ui" />
                                            </div>
                                            <div class="field">
                                                <button type="submit" class="ui button">Identifikator hinzufügen</button>
                                            </div>
                                        </div>
                                    </g:form>
                            </g:if>
                        </dd>

                    </dl>

                    <dl>
                        <dt>WIB-ID</dt>
                        <dd>
                            <g:set var="wibid" value="${orgInstance.ids.find { it?.ns?.ns == 'wibid' }}"/>
                            <g:if test="${wibid}">
                                <semui:xEditable owner="${wibid}" field="value"/>
                            </g:if>
                        </dd>
                    </dl>
                    <dl>
                        <dt>EZB-ID</dt>
                        <dd>
                            <g:set var="ezb" value="${orgInstance.ids.find { it?.ns?.ns == 'ezb' }}"/>
                            <g:if test="${ezb}">
                                <semui:xEditable owner="${ezb}" field="value"/>
                            </g:if>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->
        </g:if>

            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                <div class="ui card">
                    <div class="content">
                        <g:if test="${orgInstance.hasPerm("ORG_INST") || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                            <dl>
                                <dt><g:message code="org.sector.label" default="Sector"/></dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="sector" config='OrgSector' overwriteEditable="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}"/>
                                </dd>
                            </dl>
                        </g:if>
                        <g:else>
                            <dl>
                                <dt><g:message code="org.sector.label" default="Sector"/></dt>
                                <dd>
                                    ${orgInstance.sector?.getI10n('value')}
                                </dd>
                            </dl>
                        </g:else>
                        <dl>
                            <dt>${message(code: 'subscription.details.status', default: 'Status')}</dt>

                        <dd>
                            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                                <semui:xEditableRefData owner="${orgInstance}" field="status" config='OrgStatus'/>
                            </g:if>
                            <g:else>
                                ${orgInstance.status?.getI10n('value')}
                            </g:else>
                        </dd>
                    </dl>
                </div>
            </div><!-- .card -->
        </g:if>

            <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                <div class="ui card">
                    <div class="content">
                        <%-- ROLE_ADMIN: all , ROLE_ORG_EDITOR: all minus Consortium --%>
                        <dl>
                            <dt><g:message code="org.orgType.label" default="Organisation Type"/></dt>
                            <dd>
                                <%
                                    // hotfix:
                                    def orgType_types = RefdataCategory.getAllRefdataValues('OrgRoleType')
                                    def orgType_editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

                                    if (!orgType_editable) {
                                        orgType_editable = SpringSecurityUtils.ifAnyGranted('ROLE_ORG_EDITOR')

                                        orgType_types = orgType_types.minus(RDStore.OT_CONSORTIUM)
                                    }

                                %>
                                <g:render template="orgTypeAsList"
                                          model="${[org: orgInstance, orgTypes: orgInstance.orgType, availableOrgTypes: orgType_types, editable: orgType_editable]}"/>
                            </dd>
                        </dl>

                        <g:render template="orgTypeModal"
                                  model="${[org: orgInstance, availableOrgTypes: orgType_types, editable: orgType_editable]}"/>
                    </div>
                </div>
            </g:if>

            <g:if test="${departmentalView == false && !(RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds())}">
                <div class="ui card">
                    <div class="content">
                            <dl>
                                <dt>
                                    <g:message code="org.libraryType.label" default="Library Type"/>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${message(code: 'org.libraryType.expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="libraryType"
                                                            config='Library Type'/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>
                                    <g:message code="org.libraryNetwork.label" default="Library Network"/>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${message(code: 'org.libraryNetwork.expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="libraryNetwork"
                                                            config='Library Network'/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>
                                    <g:message code="org.funderType.label" default="Funder Type"/>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${message(code: 'org.funderType.expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="funderType" config='Funder Type'/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>
                                    <g:message code="org.federalState.label" default="Federal State"/>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${message(code: 'org.federalState.expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="federalState"
                                                            config='Federal State'/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>
                                    <g:message code="org.country.label" default="Country"/>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${message(code: 'org.country.expl')}">
                                        <i class="question circle icon"></i>
                                    </span>
                                </dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="country" config='Country'/>
                                </dd>
                            </dl>
                        </div>
                </div><!-- .card -->
            </g:if>


            <g:if test="${(RDStore.OT_PROVIDER.id in orgInstance.getallOrgTypeIds())}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.platforms.label" default="Platforms "/></dt>
                            <dd>

                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance.platforms.sort { it?.name }}" var="platform">
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

            <g:if test="${(!fromCreate) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.addresses.label" default="Addresses"/>

                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'adressFormModal.info')}">
                                    <i class="question circle icon"></i>
                                </span>

                            </dt>
                            <dd>
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance?.addresses?.sort { it.type?.getI10n('value') }}" var="a">
                                        <g:if test="${a.org}">
                                            <g:render template="/templates/cpa/address" model="${[
                                                    address             : a,
                                                    tmplShowDeleteButton: true,
                                                    controller          : 'org',
                                                    action              : 'show',
                                                    id                  : orgInstance.id,
                                                    editable            : ((orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                            ]}"/>
                                        </g:if>
                                    </g:each>
                                </div>
                                <g:if test="${((((orgInstance.id == contextService.getOrg().id) || Combo.findByFromOrgAndToOrgAndType(orgInstance,contextService.getOrg(),RDStore.COMBO_TYPE_DEPARTMENT)) && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">

                                    <div class="ui list">
                                        <div class="item">
                                            <input class="ui button" size="35"
                                                   value="${message(code: 'default.add.label', args: [message(code: 'addressFormModalPostalAddress')])}"
                                                   data-semui="modal"
                                                   data-href="#addressFormModalPostalAddress"/>
                                            <g:render template="/address/formModal"
                                                      model="['orgId': orgInstance?.id, 'redirect': '.', modalId: 'addressFormModalPostalAddress', hideType: true]"/>

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'default.add.label', args: [message(code: 'addressFormModalBillingAddress')])}"
                                                   data-semui="modal"
                                                   data-href="#addressFormModalBillingAddress"/>
                                            <g:render template="/address/formModal"
                                                      model="['orgId': orgInstance?.id, 'redirect': '.', modalId: 'addressFormModalBillingAddress', hideType: true]"/>
                                        </div>

                                        <div class="item">

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'default.add.label', args: [message(code: 'addressFormModalLegalPatronAddress')])}"
                                                   data-semui="modal"
                                                   data-href="#addressFormModalLegalPatronAddress"/>
                                            <g:render template="/address/formModal"
                                                      model="['orgId': orgInstance?.id, 'redirect': '.', modalId: 'addressFormModalLegalPatronAddress', hideType: true]"/>

                                           %{-- <input class="ui button" size="35"
                                                   value="${message(code: 'default.add.label', args: [message(code: 'address.otherAddress')])}"
                                                   data-semui="modal"
                                                   data-href="#addressFormModal"/>
                                            <g:render template="/address/formModal"
                                                      model="['orgId': orgInstance?.id, 'redirect': '.']"/>--}%
                                        </div>
                                    </div>

                                </g:if>
                            </dd>
                        </dl>
                        %{--ERMS:1236
                        <dl>
                            <dt><g:message code="org.contacts.label" default="Contacts"/></dt>
                            <dd>
                                <div class="ui divided middle aligned selection list la-flex-list">
                                    <g:each in="${orgInstance?.contacts?.toSorted()}" var="c">
                                        <g:if test="${c.org}">
                                            <g:render template="/templates/cpa/contact" model="${[
                                                    contact             : c,
                                                    tmplShowDeleteButton: true,
                                                    controller          : 'organisation',
                                                    action              : 'show',
                                                    id                  : orgInstance.id,
                                                    editable            : ((orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                            ]}"/>
                                        </g:if>
                                    </g:each>
                                </div>
                                <g:if test="${((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                                    <input class="ui button"
                                           value="${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}"
                                           data-semui="modal"
                                           data-href="#contactFormModal"/>
                                    <g:render template="/contact/formModal" model="['orgId': orgInstance?.id]"/>
                                </g:if>
                            </dd>
                        </dl>--}%
                        <dl>
                            <dt><g:message code="org.prsLinks.label" default="Kontaktpersonen"/>

                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'personFormModal.info')}">
                                    <i class="question circle icon"></i>
                                </span>

                            </dt>
                            <dd>

                            <%-- <div class="ui divided middle aligned selection list la-flex-list"> --%>
                                <g:each in="${PersonRole.executeQuery("select distinct(prs) from PersonRole pr join pr.prs prs join pr.org oo where oo = :org and prs.isPublic = true", [org: orgInstance])}" var="prs">

                                    <g:render template="/templates/cpa/person_full_details" model="${[
                                            person              : prs,
                                            personContext       : orgInstance,
                                            tmplShowDeleteButton    : true,
                                            tmplShowAddPersonRoles  : true,
                                            tmplShowAddContacts     : true,
                                            tmplShowAddAddresses    : true,
                                            tmplShowFunctions       : true,
                                            tmplShowPositions       : true,
                                            tmplShowResponsiblities : true,
                                            tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                                            controller          : 'organisation',
                                            action              : 'show',
                                            id                  : orgInstance.id,
                                            editable            : ((orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                    ]}"/>

                                </g:each>
                            <%-- </div> --%>
                                <g:if test="${(((orgInstance.id == contextService.getOrg().id || Combo.findByFromOrgAndToOrgAndType(orgInstance,contextService.getOrg(),RDStore.COMBO_TYPE_DEPARTMENT)) && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                                    <div class="ui list">
                                        <div class="item">

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'personFormModalGeneralContactPerson')}"
                                                   data-semui="modal"
                                                   data-href="#personFormModalGeneralContactPerson"/>

                                            <g:render template="/person/formModal"
                                                      model="[tenant                           : contextOrg,
                                                              org                              : orgInstance,
                                                              isPublic                         : true,
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('General contact person', 'Person Function'),
                                                              modalId                          : 'personFormModalGeneralContactPerson',
                                                              tmplHideFunctions: true]"/>

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'personFormModalResponsibleContact')}"
                                                   data-semui="modal"
                                                   data-href="#personFormModalResponsibleContact"/>

                                            <g:render template="/person/formModal"
                                                      model="[tenant                           : contextOrg,
                                                              org                              : orgInstance,
                                                              isPublic                         : true,
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Responsible Admin', 'Person Function'),
                                                              modalId                          : 'personFormModalResponsibleContact',
                                                              tmplHideFunctions: true]"/>

                                        </div>

                                        <div class="item">

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'personFormModalBillingContact')}"
                                                   data-semui="modal"
                                                   data-href="#personFormModalBillingContact"/>

                                            <g:render template="/person/formModal"
                                                      model="[tenant                           : contextOrg,
                                                              org                              : orgInstance,
                                                              isPublic                         : true,
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Functional Contact Billing Adress', 'Person Function'),
                                                              modalId                          : 'personFormModalBillingContact',
                                                              tmplHideFunctions: true]"/>

                                            <input class="ui button" size="35"
                                                   value="${message(code: 'personFormModalTechnichalSupport')}"
                                                   data-semui="modal"
                                                   data-href="#personFormModalTechnichalSupport"/>

                                            <g:render template="/person/formModal"
                                                      model="[tenant                           : contextOrg,
                                                              org                              : orgInstance,
                                                              isPublic                         : true,
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Technichal Support', 'Person Function'),
                                                              modalId                          : 'personFormModalTechnichalSupport',
                                                              tmplHideFunctions: true]"/>

                                            %{--<input class="ui button" size="35"
                                                   value="${message(code: 'personFormModalOtherContact')}"
                                                   data-semui="modal"
                                                   data-href="#personFormModal"/>

                                            <g:render template="/person/formModal"
                                                      model="['tenant'            : contextOrg,
                                                              'org'               : orgInstance,
                                                              'isPublic'          : true,
                                                              'presetFunctionType': RefdataValue.getByValueAndCategory('General contact person', 'Person Function')]"/>--}%

                                        </div>
                                    </div>

                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <g:if test="${contextService.getUser().isAdmin() || contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']}">
                    <g:if test="${orgInstance.createdBy || orgInstance.legallyObligedBy}">
                        <div class="ui card">
                            <div class="content">
                                <g:if test="${orgInstance.createdBy}">
                                    <dl>
                                        <dt>
                                            <g:message code="org.createdBy.label" default="createdBy"/>
                                        </dt>
                                        <dd>
                                            <h5 class="ui header">
                                                <g:link controller="organisation" action="show" id="${orgInstance.createdBy.id}">${orgInstance.createdBy.name}</g:link>
                                            </h5>
                                            <g:if test="${createdByOrgGeneralContacts}">
                                                    <g:each in="${createdByOrgGeneralContacts}" var="cbogc">
                                                        <g:render template="/templates/cpa/person_full_details" model="${[
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
                                            <g:message code="org.legallyObligedBy.label" default="legallyObligedBy"/>
                                        </dt>
                                        <dd>
                                            <h5 class="ui header">
                                                <g:link controller="organisation" action="show" id="${orgInstance.legallyObligedBy.id}">${orgInstance.legallyObligedBy.name}</g:link>
                                            </h5>
                                            <g:if test="${legallyObligedByOrgGeneralContacts}">
                                                <g:each in="${legallyObligedByOrgGeneralContacts}" var="lobogc">
                                                    <g:render template="/templates/cpa/person_full_details" model="${[
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

            </g:if>

            <g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
                <div id="new-dynamic-properties-block">
                    <g:render template="properties" model="${[
                            orgInstance   : orgInstance,
                            authorizedOrgs: authorizedOrgs,
                            contextOrg: institution
                    ]}"/>
                </div><!-- #new-dynamic-properties-block -->
            </g:if>

        </div>
    </div>
    <aside class="four wide column la-sidekick">
        <g:if test="${accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM', 'INST_USER')}">
            <g:render template="/templates/documents/card"
                      model="${[ownobj: orgInstance, owntp: 'organisation']}"/>
        </g:if>
    </aside>
</div>
</body>
</html>
