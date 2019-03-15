<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.PersonRole; com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition; com.k_int.properties.PropertyDefinitionGroup" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection />

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:if test="${RefdataValue.getByValueAndCategory('Provider','OrgRoleType' in orgInstance.orgType)}">
            <g:set var="entityName" value="${message(code: 'default.provider.label', default: 'Provider')}" />
        </g:if>
        <g:else>
            <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        </g:else>
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>

        <g:javascript src="properties.js"/>
    </head>
    <body>

    <semui:debugInfo>
        <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        <g:render template="/templates/debug/orgRoles"  model="[debug: orgInstance.links]" />
        <g:render template="/templates/debug/prsRoles"  model="[debug: orgInstance.prsLinks]" />
    </semui:debugInfo>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        ${orgInstance.name}
    </h1>

    <g:render template="nav"/>

    <semui:objectStatus object="${orgInstance}" status="${orgInstance.status}" />

    <g:render template="/templates/meta/identifier" model="${[object: orgInstance, editable: editable]}" />

    <semui:messages data="${flash}" />

    <div class="ui grid">
        <div class="twelve wide column">

            <div class="la-inline-lists">
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.name.label" default="Name" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="name"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.shortname.label" default="Shortname" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="shortname"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.sortname.label" default="Sortname" /><br>
                                <g:message code="org.sortname.onlyForLibraries.label" default="(Nur für Bibliotheken)" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="sortname"/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <g:if test="${!institutionalView}">
                    <div class="ui card">
                        <div class="content">
                            <g:if test="${(RDStore.OT_INSTITUTION in orgInstance.orgType)}">
                            <dl>
                                <dt><g:message code="org.sector.label" default="Sector" /></dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="sector" config='OrgSector'/>
                                </dd>
                            </dl>
                            </g:if>
                            <g:else>
                                <dl>
                                    <dt><g:message code="org.sector.label" default="Sector" /></dt>
                                    <dd>
                                        ${orgInstance.sector?.getI10n('value')}
                                    </dd>
                                </dl>
                            </g:else>
                            <dl>
                                <dt>${message(code:'subscription.details.status', default:'Status')}</dt>

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

                <g:if test="${!institutionalView}">
                    <div class="ui card">
                        <div class="content">
                            <%-- ROLE_ADMIN: all , ROLE_ORG_EDITOR: all minus Consortium --%>
                            <dl>
                                <dt><g:message code="org.orgType.label" default="Organisation Type" /></dt>
                                <dd>
                                    <%
                                        // hotfix:
                                        def orgType_types = RefdataCategory.getAllRefdataValues('OrgRoleType')
                                        def orgType_editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

                                        if (! orgType_editable) {
                                            orgType_editable = SpringSecurityUtils.ifAnyGranted('ROLE_ORG_EDITOR')

                                            orgType_types = orgType_types.minus(RDStore.OT_CONSORTIUM)
                                        }

                                    %>
                                    <g:render template="orgTypeAsList"
                                              model="${[org:orgInstance, orgTypes:orgInstance.orgType, availableOrgTypes:orgType_types, editable:orgType_editable]}" />
                                </dd>
                            </dl>

                            <g:render template="orgTypeModal"
                                      model="${[org:orgInstance, availableOrgTypes:orgType_types, editable:orgType_editable]}" />
                        </div>
                    </div>
                </g:if>


                <div class="ui card">
                    <div class="content">
                    <g:if test="${(RDStore.OT_INSTITUTION.id in orgInstance.getallOrgTypeIds())}">
                        <dl>
                            <dt><g:message code="org.libraryType.label" default="Library Type" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="libraryType" config='Library Type'/>
                            </dd>
                        </dl>
                        <g:if test="${!institutionalView}">
                            <dl>
                                <dt><g:message code="org.libraryNetwork.label" default="Library Network" /></dt>
                                <dd>
                                    <semui:xEditableRefData owner="${orgInstance}" field="libraryNetwork" config='Library Network'/>
                                </dd>
                            </dl>
                        </g:if>
                        <dl>
                            <dt><g:message code="org.funderType.label" default="Funder Type" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="funderType" config='Funder Type'/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.federalState.label" default="Federal State" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="federalState" config='Federal State'/>
                            </dd>
                        </dl>
                    </g:if>
                        <dl>
                            <dt><g:message code="org.country.label" default="Country" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="country" config='Country'/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <g:if test="${!institutionalView}">
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt><g:message code="org.addresses.label" default="Addresses" /></dt>
                                <dd>
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${orgInstance?.addresses?.sort{it.type?.getI10n('value')}}" var="a">
                                            <g:if test="${a.org}">
                                                <g:render template="/templates/cpa/address" model="${[
                                                        address: a,
                                                        tmplShowDeleteButton: true,
                                                        controller: 'org',
                                                        action: 'show',
                                                        id: orgInstance.id,
                                                        editable: ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                                ]}"/>
                                            </g:if>
                                        </g:each>
                                    </div>
                                    <g:if test="${((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                                        <input class="ui button"
                                               value="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Adresse')])}"
                                               data-semui="modal"
                                               href="#addressFormModal" />
                                        <g:render template="/address/formModal" model="['orgId': orgInstance?.id, 'redirect': '.']"/>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt><g:message code="org.contacts.label" default="Contacts" /></dt>
                                <dd>
                                    <div class="ui divided middle aligned selection list la-flex-list">
                                        <g:each in="${orgInstance?.contacts?.toSorted()}" var="c">
                                            <g:if test="${c.org}">
                                                <g:render template="/templates/cpa/contact" model="${[
                                                        contact: c,
                                                        tmplShowDeleteButton: true,
                                                        controller: 'organisations',
                                                        action: 'show',
                                                        id: orgInstance.id,
                                                        editable: ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                                ]}"/>
                                            </g:if>
                                        </g:each>
                                    </div>
                                    <g:if test="${((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                                        <input class="ui button"
                                               value="${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}"
                                               data-semui="modal"
                                               href="#contactFormModal" />
                                        <g:render template="/contact/formModal" model="['orgId': orgInstance?.id]"/>
                                    </g:if>
                                </dd>
                            </dl>
                            <dl>
                                <dt><g:message code="org.prsLinks.label" default="Kontaktpersonen" /></dt>
                                <dd>
                                <%-- <div class="ui divided middle aligned selection list la-flex-list"> --%>
                                    <g:each in="${orgInstance?.prsLinks?.toSorted()}" var="pl">
                                        <g:if test="${(pl?.functionType || pl?.positionType) && pl?.prs?.isPublic?.value!='No'}">
                                            <g:render template="/templates/cpa/person_details" model="${[
                                                    personRole: pl,
                                                    tmplShowDeleteButton: true,
                                                    tmplConfigShow: ['E-Mail', 'Mail', 'Url', 'Phone', 'Fax', 'address'],
                                                    controller: 'organisations',
                                                    action: 'show',
                                                    id: orgInstance.id,
                                                    editable: ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))
                                            ]}"/>
                                        </g:if>
                                    </g:each>
                                <%-- </div> --%>
                                    <g:if test="${((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                                        <g:if test="${ ! SpringSecurityUtils.ifAnyGranted('ROLE_ORG_COM_EDITOR') }">
                                            <input class="ui button"
                                                   value="${message(code: 'person.create_new.contactPerson.label')}"
                                                   data-semui="modal"
                                                   href="#personFormModal" />

                                            <g:render template="/person/formModal"
                                                      model="['tenant': contextOrg,
                                                              'org': orgInstance,
                                                              'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'Yes'),
                                                              'presetFunctionType': RefdataValue.getByValueAndCategory('General contact person', 'Person Function')]"/>
                                        </g:if>
                                    </g:if>
                                </dd>
                            </dl>
                        </div>
                    </div><!-- .card -->

                    <g:if test="${orgInstance?.outgoingCombos && ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
                        <g:if test="${orgInstance.id == contextService.getOrg().id}">
                            <div class="ui card">
                        </g:if>
                        <g:elseif test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                            <div class="ui card la-role-admin">
                        </g:elseif>
                        <g:else>
                            <div class="ui card la-role-yoda">
                        </g:else>
                        <div class="content">
                            <dl>
                                <dt><g:message code="org.outgoingCombos.label" default="Outgoing Combos" /></dt>
                                <dd>
                                    <g:each in="${orgInstance.outgoingCombos.sort{it.toOrg.name}}" var="i">
                                        <g:link controller="organisations" action="show" id="${i.toOrg.id}">${i.toOrg?.name}</g:link>
                                        (<g:each in="${i?.toOrg?.ids?.sort{it?.identifier?.ns?.ns}}" var="id_out">
                                        ${id_out.identifier.ns.ns}: ${id_out.identifier.value}
                                    </g:each>)
                                        <br />
                                    </g:each>
                                </dd>
                            </dl>
                        </div>
                        </div><!--.card-->
                    </g:if>

                    <g:if test="${orgInstance?.incomingCombos}">

                        <g:if test="${orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_ADMIN')}">
                            <div class="ui card">
                                <div class="content">
                                    <dl>
                                        <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
                                        <dd>
                                            <g:each in="${orgInstance.incomingCombos.sort{it.fromOrg.name}}" var="i">
                                                <g:link controller="organisations" action="show" id="${i.fromOrg.id}">${i.fromOrg?.name}</g:link>
                                                (<g:each in="${i?.fromOrg?.ids?.sort{it?.identifier?.ns?.ns}}" var="id_in">
                                                ${id_in.identifier.ns.ns}: ${id_in.identifier.value}
                                            </g:each>)
                                                <br />
                                            </g:each>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!--.card-->
                        </g:if>
                        <g:elseif test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
                            <div class="ui card la-role-admin">
                                <div class="content">
                                    <dl>
                                        <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
                                        <dd>
                                            <g:each in="${orgInstance.incomingCombos.sort{it.fromOrg.name}}" var="i">
                                                <g:link controller="organisations" action="show" id="${i.fromOrg.id}">${i.fromOrg?.name}</g:link>
                                                (<g:each in="${i?.fromOrg?.ids?.sort{it?.identifier?.ns?.ns}}" var="id_in">
                                                ${id_in.identifier.ns.ns}: ${id_in.identifier.value}
                                            </g:each>)
                                                <br />
                                            </g:each>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!--.card-->
                        </g:elseif>
                        <g:elseif test="${SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                            <div class="ui card la-role-yoda">
                                <div class="content">
                                    <dl>
                                        <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
                                        <dd>
                                            <g:each in="${orgInstance.incomingCombos.sort{it.fromOrg.name}}" var="i">
                                                <g:link controller="organisations" action="show" id="${i.fromOrg.id}">${i.fromOrg?.name}</g:link>
                                                (<g:each in="${i?.fromOrg?.ids?.sort{it?.identifier?.ns?.ns}}" var="id_in">
                                                ${id_in.identifier.ns.ns}: ${id_in.identifier.value}
                                            </g:each>)
                                                <br />
                                            </g:each>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!--.card-->
                        </g:elseif>

                    </g:if><%-- incomingCombos --%>

                    <g:if test="${sorted_links}">
                        <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_YODA')}">
                            <div class="ui card la-role-yoda">
                                <div class="content">
                                    <g:render template="/templates/links/orgRoleContainer" model="[listOfLinks: sorted_links]" />
                                </div>
                            </div><!--.card-->
                        </g:if>
                        <g:elseif test="${orgInstance.id == contextService.getOrg().id && user.hasAffiliation('INST_ADMIN')}">
                            <div class="ui card">
                                <div class="content">
                                    <g:render template="/templates/links/orgRoleContainer" model="[listOfLinks: sorted_links]" />
                                </div>
                            </div><!--.card-->
                        </g:elseif>
                    </g:if><%-- sorted_links --%>
                </g:if>


                <div id="new-dynamic-properties-block">
                    <g:render template="properties" model="${[
                            orgInstance: orgInstance,
                            authorizedOrgs: authorizedOrgs
                    ]}" />
                </div><!-- #new-dynamic-properties-block -->

                </div>
            </div>
        </div>
    </div>
    </div>
  </body>
</html>
