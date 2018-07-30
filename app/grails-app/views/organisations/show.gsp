<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.properties.PropertyDefinition" %>
<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:if test="${orgInstance.orgType == com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider','OrgType')}">
            <g:set var="entityName" value="${message(code: 'default.provider.label', default: 'Provider')}" />
        </g:if>
        <g:else>
            <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        </g:else>
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.show.label" args="[entityName]" /></title>

        <g:javascript src="properties.js"/>
    </head>
    <body>

    <g:render template="breadcrumb" model="${[ orgInstance:orgInstance, params:params ]}"/>

    <h1 class="ui header"><semui:headerIcon />
        ${orgInstance.name}
    </h1>

    <g:render template="nav" contextPath="." />

    <semui:meta>
        <div class="inline-lists">

            <dl>
                <g:if test="${orgInstance.globalUID}">
                    <dt><g:message code="org.globalUID.label" default="Global UID" /></dt>
                    <dd>
                        <g:fieldValue bean="${orgInstance}" field="globalUID"/>
                    </dd>
                </g:if>

                <g:if test="${orgInstance.impId}">
                    <dt><g:message code="org.impId.label" default="Import ID" /></dt>
                    <dd>
                        <g:fieldValue bean="${orgInstance}" field="impId"/>
                    </dd>
                </g:if>

                <dt><g:message code="org.ids.label" default="Ids" /></dt>
                <dd>
                    <g:if test="${orgInstance?.ids}">
                        <g:each in="${orgInstance.ids.sort{it.identifier.ns.ns}}" var="i">
                            <g:link controller="identifier" action="show" id="${i.identifier.id}">${i?.identifier?.ns?.ns?.encodeAsHTML()} : ${i?.identifier?.value?.encodeAsHTML()}</g:link>
                            <br />
                        </g:each>
                    </g:if>

                    <g:if test="${editable}">

                        <semui:formAddIdentifier owner="${orgInstance}">
                            ${message(code:'identifier.select.text', args:['isil:DE-18'])}
                        </semui:formAddIdentifier>

                    </g:if>
                </dd>
            </dl>
        </div>
    </semui:meta>

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
                            <dt><g:message code="org.sortname.label" default="Sortname" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="sortname"/>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <div class="ui card">
                    <div class="content">
                        <g:if test="${orgInstance.orgType != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'OrgType')}">
                        <dl>
                            <dt><g:message code="org.sector.label" default="Sector" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="sector" config='OrgSector'/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.type.label" default="Org Type" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="orgType" config='OrgType'/>
                            </dd>
                        </dl>
                        </g:if>
                        <g:else>
                            <dl>
                                <dt><g:message code="org.sector.label" default="Sector" /></dt>
                                <dd>
                                    ${orgInstance.sector.getI10n('value')}
                                </dd>
                            </dl>
                            <dl>
                                <dt><g:message code="org.type.label" default="Org Type" /></dt>
                                <dd>
                                    ${orgInstance.orgType.getI10n('value')}
                                </dd>
                            </dl>
                        </g:else>
                        <dl>
                            <dt>${message(code:'subscription.details.status', default:'Status')}</dt>
                            <dd>${orgInstance.status?.getI10n('value')}</dd>
                        </dl>
                    </div>
                </div><!-- .card -->

                <div class="ui card">
                    <div class="content">
                    <g:if test="${orgInstance.orgType != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider', 'OrgType')}">
                        <dl>
                            <dt><g:message code="org.libraryType.label" default="Library Type" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="libraryType" config='Library Type'/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.libraryNetwork.label" default="Library Network" /></dt>
                            <dd>
                                <semui:xEditableRefData owner="${orgInstance}" field="libraryNetwork" config='Library Network'/>
                            </dd>
                        </dl>
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

                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.addresses.label" default="Addresses" /></dt>
                            <dd>
                                <div class="ui relaxed list">
                                    <g:each in="${orgInstance?.addresses.sort{it.type?.getI10n('value')}}" var="a">
                                        <g:if test="${a.org}">
                                            <g:render template="/templates/cpa/address" model="${[
                                                    address: a,
                                                    tmplShowDeleteButton: true,
                                                    controller: 'org',
                                                    action: 'show',
                                                    id: orgInstance.id
                                            ]}"></g:render>
                                        </g:if>
                                    </g:each>
                                </div>
                                <g:if test="${editable}">
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
                                <div class="ui relaxed list la-contact-info-list">
                                    <g:each in="${orgInstance?.contacts.sort{it.content}}" var="c">
                                        <g:if test="${c.org}">
                                            <g:render template="/templates/cpa/contact" model="${[
                                                    contact: c,
                                                    tmplShowDeleteButton: true,
                                                    controller: 'org',
                                                    action: 'show',
                                                    id: orgInstance.id
                                            ]}"></g:render>
                                        </g:if>
                                    </g:each>
                                </div>
                                <g:if test="${editable}">
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
                                <div class="ui relaxed list">
                                    <g:each in="${orgInstance?.prsLinks}" var="pl">
                                        <g:if test="${pl?.functionType?.value && pl?.prs?.isPublic?.value!='No'}">
                                            <g:render template="/templates/cpa/person_details" model="${[personRole: pl]}"></g:render>
                                        </g:if>
                                    </g:each>
                                </div>
                                <g:if test="${editable}">
                                    <g:if test="${ ! SpringSecurityUtils.ifAnyGranted('ROLE_ORG_COM_EDITOR') }">
                                        <input class="ui button"
                                               value="${message(code: 'person.create_new.contactPerson.label')}"
                                               data-semui="modal"
                                               href="#personFormModal" />

                                        <g:render template="/person/formModal"
                                                  model="['tenant': contextOrg,
                                                          'org': orgInstance,
                                                          'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'Yes'),
                                                          presetFunctionType: RefdataValue.getByValueAndCategory('General contact person', 'Person Function'),
                                                          tmplHideResponsibilities: true]"/>
                                    </g:if>
                                </g:if>
                            </dd>
                        </dl>
                    </div>
                </div><!-- .card -->
                <g:if test="${orgInstance.orgType != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Provider','OrgType')}">
                %{--<div class="ui card">
                    <div class="content">
                        <dl>
                            <dt><g:message code="org.fteStudents.label" default="Fte Students" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="fteStudents"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt><g:message code="org.fteStaff.label" default="Fte Staff" /></dt>
                            <dd>
                                <semui:xEditable owner="${orgInstance}" field="fteStaff"/>
                            </dd>
                        </dl>
                    </div>
                </div><!--.card-->--}%
                </g:if>

            <% /*
            <dt><g:message code="org.membership.label" default="Membership Organisation" /></dt>
            <dd>
                <g:if test="${editable}">
                    <semui:xEditableRefData owner="${orgInstance}" field="membership" config='YN'/>
                </g:if>
                <g:else>
                    <g:fieldValue bean="${orgInstance}" field="membership"/>
                </g:else>
            </dd>
            */ %>

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
                                                (<g:each in="${i.toOrg?.ids.sort{it.identifier.ns.ns}}" var="id_out">
                                                    ${id_out.identifier.ns.ns}: ${id_out.identifier.value}
                                                </g:each>)
                                                <br />
                                            </g:each>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!--.card-->
                    </g:if>

                    <g:if test="${orgInstance?.incomingCombos && ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
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
                                        <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
                                        <dd>
                                            <g:each in="${orgInstance.incomingCombos.sort{it.fromOrg.name}}" var="i">
                                                <g:link controller="organisations" action="show" id="${i.fromOrg.id}">${i.fromOrg?.name}</g:link>
                                                    (<g:each in="${i.fromOrg?.ids.sort{it.identifier.ns.ns}}" var="id_in">
                                                        ${id_in.identifier.ns.ns}: ${id_in.identifier.value}
                                                    </g:each>)
                                                    <br />
                                            </g:each>
                                        </dd>
                                    </dl>
                                </div>
                            </div><!--.card-->
                    </g:if>

                    <g:if test="${orgInstance?.links && ((orgInstance.id == contextService.getOrg().id) || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'))}">
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
                                    <g:each in="${sorted_links}" var="rdv_id,link_cat">

                                        <dl>
                                            <dt>
                                                <h5 class="ui header">Als ${link_cat.rdv.getI10n('value')}</h5>
                                            <dd>
                                                <div class="ui list">
                                                  <g:each in="${link_cat.links}" var="i">
                                                    <div class="item">
                                                      <g:if test="${i.pkg}">
                                                            <g:link controller="packageDetails" action="show" id="${i.pkg.id}">
                                                                ${message(code:'package.label', default:'Package')}: ${i.pkg.name}
                                                            </g:link>
                                                            (${i.pkg?.packageStatus?.getI10n('value')})
                                                      </g:if>
                                                      <g:if test="${i.sub}">
                                                            <g:link controller="subscriptionDetails" action="show" id="${i.sub.id}">
                                                                ${message(code:'subscription.label', default:'Subscription')}: ${i.sub.name}
                                                            </g:link>
                                                            (${i.sub.status?.getI10n('value')})
                                                      </g:if>
                                                      <g:if test="${i.lic}">
                                                            <g:link controller="licenseDetails" action="show" id="${i.lic.id}">
                                                                ${message(code:'license.label', default:'License')}: ${i.lic.reference ?: i.lic.id}
                                                            </g:link>
                                                          (${i.lic.status?.getI10n('value')})
                                                      </g:if>
                                                      <g:if test="${i.title}">
                                                            <g:link controller="titleDetails" action="show" id="${i.title.id}">
                                                                ${message(code:'title.label', default:'Title')}: ${i.title.title}
                                                            </g:link>
                                                            (${i.title.status?.getI10n('value')})
                                                      </g:if>
                                                    </div>
                                                  </g:each>

                                                    <g:set var="local_offset" value="${params[link_cat.rdvl] ? Long.parseLong(params[link_cat.rdvl]) : null}" />

                                                    <g:if test="${link_cat.total > 10}">
                                                        <div class="item">
                                                            ${message(code:'default.paginate.offset', args:[(local_offset ?: 1),(local_offset ? (local_offset + 10 > link_cat.total ? link_cat.total : local_offset + 10) : 10), link_cat.total])}
                                                        </div>
                                                        <div class="item">
                                                            <g:if test="${local_offset}">
                                                                <g:set var="os_prev" value="${local_offset > 9 ? (local_offset - 10) : 0}" />
                                                                <g:link controller="organisations" action="show" class="ui icon button tiny"
                                                                    id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_prev]}">
                                                                        <i class="left arrow icon"></i>
                                                                </g:link>
                                                            </g:if>
                                                            <g:if test="${!local_offset || ( local_offset < (link_cat.total - 10) )}">
                                                                <g:set var="os_next" value="${local_offset ? (local_offset + 10) : 10}" />
                                                                <g:link controller="organisations" action="show" class="ui icon button tiny"
                                                                    id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_next]}">
                                                                        <i class="right arrow icon"></i>
                                                                </g:link>
                                                            </g:if>
                                                        </div>
                                                    </g:if>
                                                </div>
                                            </dd>
                                        </dl>
                                    </g:each>
                                </div>
                            </div><!--.card-->
                    </g:if>


                <div class="ui card la-dl-no-table">
                    <div class="content">
                        <h5 class="ui header">${message(code:'org.properties')}</h5>

                        <div id="custom_props_div_props">
                            <g:render template="/templates/properties/custom" model="${[
                                    prop_desc: PropertyDefinition.ORG_PROP,
                                    ownobj: orgInstance,
                                    custom_props_div: "custom_props_div_props" ]}"/>
                        </div>
                    </div>
                </div><!--.card-->

            <r:script language="JavaScript">
                $(document).ready(function(){
                    c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_props");
                });
            </r:script>

            <g:each in="${authorizedOrgs}" var="authOrg">
                <g:if test="${authOrg.name == contextOrg?.name}">
                    <div class="ui card la-dl-no-table">
                        <div class="content">
                            <h5 class="ui header">${message(code:'org.properties.private')} ${authOrg.name}</h5>

                            <div id="custom_props_div_${authOrg.id}">
                                <g:render template="/templates/properties/private" model="${[
                                        prop_desc: PropertyDefinition.ORG_PROP,
                                        ownobj: orgInstance,
                                        custom_props_div: "custom_props_div_${authOrg.id}",
                                        tenant: authOrg]}"/>

                                <r:script language="JavaScript">
                                    $(document).ready(function(){
                                        c3po.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_${authOrg.id}", ${authOrg.id});
                                    });
                                </r:script>
                            </div>
                        </div>
                    </div><!--.card-->
                </g:if>
            </g:each>



                </div>
            </div>
        </div>
    </div>

  </body>
</html>
