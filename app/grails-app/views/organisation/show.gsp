<%@ page import="com.k_int.kbplus.GenericOIDService; com.k_int.kbplus.Person; com.k_int.kbplus.OrgSubjectGroup; com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup; com.k_int.kbplus.OrgSettings" %>
<%@ page import="com.k_int.kbplus.Combo;grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="allOrgTypeIds" value="${orgInstance.getallOrgTypeIds()}" />
    <g:set var="isProvider" value="${RDStore.OT_PROVIDER.id in allOrgTypeIds}" />
    <g:set var="isGrantedOrgRoleAdminOrOrgEditor" value="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}" />
    <g:set var="isGrantedOrgRoleAdmin" value="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}" />

    <g:if test="${isProvider}">
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
    <title>${message(code: 'laser')} : ${message(code:'menu.institutions.org_info')}</title>

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

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${orgInstance.name}</h1>

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
                        <dt><g:message code="default.name.label" /></dt>
                        <dd>
                            <semui:xEditable owner="${orgInstance}" field="name"/>
                            <g:if test="${orgInstance.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${orgInstance.getCustomerTypeI10n()}">
                                    <i class="chess rook grey icon"></i>
                                </span>
                            </g:if>
                        </dd>
                    </dl>
                    <g:if test="${!inContextOrg || isGrantedOrgRoleAdminOrOrgEditor}">
                        <g:if test="${departmentalView == false}">
                            <dl>
                                <dt><g:message code="org.shortname.label" /></dt>
                                <dd>
                                    <semui:xEditable owner="${orgInstance}" field="shortname"/>
                                </dd>
                            </dl>
                        </g:if>
                        <g:if test="${!isProvider}">
                            <dl>
                                <dt>
                                    <g:message code="org.sortname.label" />
                                </dt>
                                <dd>
                                    <semui:xEditable owner="${orgInstance}" field="sortname"/>
                                </dd>
                            </dl>
                        </g:if>
                    </g:if>
                    <dl>
                        <dt><g:message code="org.url.label"/></dt>
                        <dd>
                            <semui:xEditable owner="${orgInstance}" type="url" field="url" class="la-overflow la-ellipsis" />
                            <g:if test="${orgInstance.url}">
                                <semui:linkIcon href="${orgInstance.url}" />
                            </g:if>
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
                            <semui:xEditable owner="${orgInstance}" field="legalPatronName"/>
                        </dd>
                    </dl>
                    <g:if test="${!departmentalView}">
                        <dl>
                            <dt>
                                <g:message code="org.urlGov.label"/>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: 'org.urlGov.expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" type="url" field="urlGov" class="la-overflow la-ellipsis" />
                                <g:if test="${orgInstance.urlGov}">
                                    <semui:linkIcon href="${orgInstance.urlGov}" />
                                </g:if>
                            </dd>
                        </dl>
                    </g:if>
                </div>
            </div><!-- .card -->

            <g:if test="${isGrantedOrgRoleAdmin}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.sector.label" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="sector" config="${RDConstants.ORG_SECTOR}" overwriteEditable="${isGrantedOrgRoleAdminOrOrgEditor}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'default.status.label')}</dt>

                            <dd>
                                <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
                                    <semui:xEditableRefData owner="${orgInstance}" field="status" config="${RDConstants.ORG_STATUS}"/>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
            </g:if>

            <g:if test="${isGrantedOrgRoleAdminOrOrgEditor}">
                <div class="ui card">
                    <div class="content">
                        <%-- ROLE_ADMIN: all , ROLE_ORG_EDITOR: all minus Consortium --%>
                        <dl>
                            <dt><g:message code="org.orgType.label" /></dt>
                            <dd>
                                <%
                                    // hotfix:
                                    def orgType_types = RefdataCategory.getAllRefdataValues(RDConstants.ORG_TYPE)
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

            <g:if test="${departmentalView == false && !isProvider}">
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
                                    <semui:xEditableRefData owner="${orgInstance}" field="libraryType"
                                                            config="${RDConstants.LIBRARY_TYPE}"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>
                                    <g:message code="org.subjectGroup.label" />
                                    %{--<span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"--}%
                                          %{--data-content="${message(code: 'org.libraryType.expl')}">--}%
                                        %{--<i class="question circle icon"></i>--}%
                                    %{--</span>--}%
                                </dt>
                                <dd>
                                    <%
                                        def subjectGroups = RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP)
                                    %>
                                    <g:render template="orgSubjectGroupAsList"
                                              model="${[org: orgInstance, orgSubjectGroups: orgInstance.subjectGroup, availableSubjectGroups: subjectGroups, editable: editable]}"/>

                                    <g:render template="orgSubjectGroupModal"
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
                                    <semui:xEditableRefData owner="${orgInstance}" field="libraryNetwork"
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
                                    <semui:xEditableRefData owner="${orgInstance}" field="funderType" config="${RDConstants.FUNDER_TYPE}"/>
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
                                    <semui:xEditableRefData owner="${orgInstance}" field="funderHskType" config="${RDConstants.FUNDER_HSK_TYPE}"/>
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
                                    <semui:xEditableRefData id="country" owner="${orgInstance}" field="country" config="${RDConstants.COUNTRY}" />
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
                                    <semui:xEditableRefData id="regions_${de.laser.helper.RDStore.COUNTRY_DE.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_DE}"/>
                                    <semui:xEditableRefData id="regions_${de.laser.helper.RDStore.COUNTRY_AT.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_AT}"/>
                                    <semui:xEditableRefData id="regions_${de.laser.helper.RDStore.COUNTRY_CH.id}" owner="${orgInstance}" field="region" config="${RDConstants.REGIONS_CH}"/>
                                </dd>
                            </dl>
                        </div>
                </div><!-- .card -->
            </g:if>


            <g:if test="${isProvider}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.platforms.label" /></dt>
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

            <g:if test="${(!fromCreate) || isGrantedOrgRoleAdminOrOrgEditor}">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            %{--_____________________________________--}%
                            <dt><g:message code="org.addresses.label" default="Addresses"/></dt>
                            %{--_____________________________________--}%
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
                                %{--____________________________--}%
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
                                %{--____________________________--}%
                            </dd>
                        </dl>
                        %{--ERMS:1236
                        <dl>
                            <dt><g:message code="org.contacts.label" /></dt>
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
                                           value="${message(code: 'default.add.label', args: [message(code: 'contact.label')])}"
                                           data-semui="modal"
                                           data-href="#contactFormModal"/>
                                    <g:render template="/contact/formModal" model="['orgId': orgInstance?.id]"/>
                                </g:if>
                            </dd>
                        </dl>--}%
                        <dl>
                            <dt><g:message code="org.prsLinks.label" />
                            </dt>
                            <dd>

                            <%-- <div class="ui divided middle aligned selection list la-flex-list"> --%>
                                <%
                                    List<PersonRole> allPRs = PersonRole.executeQuery("select distinct(pr) from PersonRole pr join pr.prs prs join pr.org oo where oo = :org and prs.isPublic = true", [org: orgInstance])
                                    Map<RefdataValue, List<PersonRole>> allPRMap = [:]
                                    List<RefdataValue> usedRDV = []
                                    allPRs.each{
                                        if (it.functionType) {
                                            List l = allPRMap.get(it.functionType)?: []
                                            l.add(it)
                                            allPRMap.put(it.functionType, l)
                                            usedRDV.add(it.functionType)
                                        }
                                        if (it.positionType) {
                                            List l = allPRMap.get(it.positionType)?: []
                                            l.add(it)
                                            allPRMap.put(it.positionType, l)
                                            usedRDV.add(it.positionType)
                                        }
                                        if (it.responsibilityType) {
                                            List l = allPRMap.get(it.responsibilityType)?: []
                                            l.add(it)
                                            allPRMap.put(it.responsibilityType, l)
                                            usedRDV.add(it.responsibilityType)
                                        }
                                    }
                                    usedRDV?.unique().sort(it?.getI10n('value'))
                                    int genConPrsNdx = usedRDV.indexOf(RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)
                                    if (genConPrsNdx != -1){
                                        RefdataValue genConPrs = usedRDV.get(genConPrsNdx)
                                        usedRDV.remove(RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)
                                        usedRDV.add(0, genConPrs)
                                    }
                                %>
                                <g:each in="${usedRDV}" var="rdv">
                                    <h3>${rdv.getI10n('value')}</h3>
                                    <g:each in="${allPRMap.get(rdv)}" var="pr">
                                        %{--Workaround wg NPE bei CacheEntry.getValue--}%
                                        <% com.k_int.kbplus.Person prs = PersonRole.get(pr.id).prs%>
                                        <g:render template="/templates/cpa/person_full_details" model="${[
                                                person              : prs,
                                                personContext       : orgInstance,
                                                tmplShowDeleteButton    : true,
                                                tmplShowAddPersonRoles  : true,
                                                tmplShowAddContacts     : true,
                                                tmplShowAddAddresses    : true,
                                                tmplShowFunctions       : false,
                                                tmplShowPositions       : false,
                                                tmplShowResponsiblities : true,
                                                tmplConfigShow      : ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                                                controller          : 'organisation',
                                                action              : 'show',
                                                id                  : orgInstance.id,
                                                editable            : ((orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_EDITOR')) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                        ]}"/>
                                    </g:each>
                                </g:each>
                            <%-- </div> --%>
                                %{--_______________________________________--}%
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
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION),
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
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Responsible Admin', RDConstants.PERSON_FUNCTION),
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
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Functional Contact Billing Adress', RDConstants.PERSON_FUNCTION),
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
                                                              presetFunctionType               : RefdataValue.getByValueAndCategory('Technichal Support', RDConstants.PERSON_FUNCTION),
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
                                                              'presetFunctionType': RefdataValue.getByValueAndCategory('General contact person', RDConstants.PERSON_FUNCTION)]"/>--}%

                                        </div>
                                    </div>

                                </g:if>                                %{--_______________________________________--}%
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <g:if test="${(contextService.getUser().isAdmin() || contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']) && (contextService.getOrg() != orgInstance)}">
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
                                            <g:message code="org.legallyObligedBy.label" />
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
<r:script>
    $('#country').on('save', function(e, params) {
        showRegionsdropdown(params.newValue);
    });


    function showRegionsdropdown(newValue) {
         $("*[id^=regions_]").hide();
         if(newValue){
             var id = newValue.split(':')[1]
             // $("#regions_" + id).editable('setValue', null);
             $("#regions_" + id).show();
         }
    };

    $(document).ready(function(){
        var country = $("#country").editable('getValue', true);
        showRegionsdropdown(country);
    });
</r:script>