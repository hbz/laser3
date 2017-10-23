
<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>

    <div class="container">
      <h1>${orgInstance.name}</h1>
      <g:render template="nav" contextPath="." />
    </div>

    <div class="container"> 

        <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>

        <dl>
            <dt><g:message code="org.name.label" default="Name" /></dt>
            <dd><g:fieldValue bean="${orgInstance}" field="name"/></dd>

            <dt><g:message code="org.addresses.label" default="Addresses" /></dt>
            <dd>
                <g:each in="${orgInstance?.addresses}" var="a">
                    <g:if test="${a.org}">
                            <g:render template="/templates/cpa/address" model="${[address: a]}"></g:render>
                    </g:if>
                </g:each>
                <g:link controller="address" action="create" class="btn btn-primary btn-small" params="['org.id': orgInstance.id]" >
                    ${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Adresse')])}
                </g:link>
            </dd>

            <dt><g:message code="org.contacts.label" default="Contacts" /></dt>
            <dd>
                <g:each in="${orgInstance?.contacts}" var="c">
                    <g:if test="${c.org}">
                            <g:render template="/templates/cpa/contact" model="${[contact: c]}"></g:render>
                    </g:if>
                </g:each>
                <g:link controller="contact" action="create" class="btn btn-primary btn-small" params="['org.id': orgInstance.id]" >
                    ${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}
                </g:link>
            </dd>

            <dt><g:message code="org.prsLinks.label" default="Persons" /></dt>
            <dd>
                <g:each in="${orgInstance?.prsLinks}" var="pl">
                    <g:if test="${pl?.functionType?.value && pl?.prs?.isPublic?.value!='No'}">
                        <g:render template="/templates/cpa/person_details" model="${[personRole: pl]}"></g:render>
                    </g:if>
                </g:each>
                <g:link controller="person" action="create" class="btn btn-primary btn-small"
                        params="['tenant.id': orgInstance.id, 'isPublic': RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc('YN'), 'Yes').id ]" >
                    ${message(code: 'default.add.label', args: [message(code: 'person.label', default: 'Person')])}
                </g:link>
            </dd>

            <dt><g:message code="org.type.label" default="Org Type" /></dt>
              <dd>
                <g:xEditableRefData owner="${orgInstance}" field="orgType" config='OrgType'/>
              </dd>
            <g:if test="${editable}">
              <dt><g:message code="org.ipRange.label" default="Ip Range" /></dt>

                <dd><g:fieldValue bean="${orgInstance}" field="ipRange"/></dd>
            </g:if>
            <dt><g:message code="org.sector.label" default="Sector" /></dt>
            <dd>
            	<g:xEditableRefData owner="${orgInstance}" field="sector" config='OrgSector'/>
            </dd>

            <dt><g:message code="org.membership.label" default="Membership Organisation" /></dt>
            <dd>
                <g:if test="${editable}">
                    <g:xEditableRefData owner="${orgInstance}" field="membership" config='YN'/>
                </g:if>
                <g:else>
                    <g:fieldValue bean="${orgInstance}" field="membership"/>
                </g:else>
            </dd>

            <dt><g:message code="org.ids.label" default="Ids" /></dt>
            <dd>
                <g:if test="${orgInstance?.ids}">
                  <g:each in="${orgInstance.ids}" var="i">
                    <g:link controller="identifier" action="show" id="${i.identifier.id}">${i?.identifier?.ns?.ns?.encodeAsHTML()} : ${i?.identifier?.value?.encodeAsHTML()}</g:link>
                    <br />
                  </g:each>
                </g:if>

                <g:if test="${editable}">

                    <laser:formAddIdentifier owner="${orgInstance}">
                        ${message(code:'identifier.select.text', args:['isil:DE-18'])}
                    </laser:formAddIdentifier>

                </g:if>
            </dd>

          <g:if test="${orgInstance?.outgoingCombos}">
            <dt><g:message code="org.outgoingCombos.label" default="Outgoing Combos" /></dt>
            <g:each in="${orgInstance.outgoingCombos}" var="i">
              <dd>${i.type?.getI10n('value')} - <g:link controller="organisations" action="show" id="${i.toOrg.id}">${i.toOrg?.name}</g:link>
                (<g:each in="${i.toOrg?.ids}" var="id_out">
                  ${id_out.identifier.ns.ns}:${id_out.identifier.value} 
                </g:each>)
              </dd>
            </g:each>
          </g:if>

          <g:if test="${orgInstance?.incomingCombos}">
            <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
            <g:each in="${orgInstance.incomingCombos}" var="i">
              <dd>${i.type?.getI10n('value')} - <g:link controller="organisations" action="show" id="${i.fromOrg.id}">${i.fromOrg?.name}</g:link>
                (<g:each in="${i.fromOrg?.ids}" var="id_in">
                  ${id_in.identifier.ns.ns}:${id_in.identifier.value} 
                </g:each>)
              </dd>

            </g:each>
          </g:if>

          <g:if test="${orgInstance?.links}">
            <dt><g:message code="org.links.other.label" default="Other org links" /></dt>
            <dd>
              <g:each in="${sorted_links}" var="rdv_id,link_cat">
                <div>
                  <g:set var="roletype_refdata" value="${link_cat.rdv.getI10n('value')}"/>
                  <span style="font-weight:bold;">${roletype_refdata} (${link_cat.total})</span>
                </div>
                <ul>
                  <g:each in="${link_cat.links}" var="i">
                    <li>
                      <g:if test="${i.pkg}">
                        <g:link controller="packageDetails" action="show" id="${i.pkg.id}">
                          ${message(code:'package.label', default:'Package')}: ${i.pkg.name} (${i.pkg?.packageStatus?.getI10n('value')}")})
                        </g:link>
                      </g:if>
                      <g:if test="${i.sub}">
                        <g:link controller="subscriptionDetails" action="index" id="${i.sub.id}">
                          ${message(code:'subscription.label', default:'Subscription')}: ${i.sub.name} (${message(code:"refdata.${i.sub.status?.value}", default:"${i.sub.status?.value}")})
                        </g:link>
                      </g:if>
                      <g:if test="${i.lic}">
                        <g:link controller="licenseDetails" action="index" id="${i.lic.id}">
                          ${message(code:'license.label', default:'License')}: ${i.lic.reference ?: i.lic.id} (${message(code:"refdata.${i.lic.status?.value}", default:"${i.lic.status?.value}")})
                        </g:link>
                      </g:if>
                      <g:if test="${i.title}">
                        <g:link controller="titleInstance" action="show" id="${i.title.id}">
                          ${message(code:'title.label', default:'Title')}: ${i.title.title} (${message(code:"refdata.${i.title.status?.value}", default:"${i.title.status?.value}")})
                        </g:link>
                      </g:if> 
                    </li>
                  </g:each>
                </ul>
                <g:set var="local_offset" value="${params[link_cat.rdvl] ? Long.parseLong(params[link_cat.rdvl]) : null}" />
                <div>
                  <g:if test="${link_cat.total > 10}">
                    ${message(code:'default.paginate.offset', args:[(local_offset ?: 1),(local_offset ? (local_offset + 10 > link_cat.total ? link_cat.total : local_offset + 10) : 10), link_cat.total])}
                  </g:if>
                </div>
                <div>
                  <g:if test="${link_cat.total > 10 && local_offset}">
                    <g:set var="os_prev" value="${local_offset > 9 ? (local_offset - 10) : 0}" />
                    <g:link controller="organisations" action="show" id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_prev]}">${message(code:'default.paginate.prev', default:'Previous')}</g:link>
                  </g:if>
                  <g:if test="${link_cat.total > 10 && ( !local_offset || ( local_offset < (link_cat.total - 10) ) )}">
                    <g:set var="os_next" value="${local_offset ? (local_offset + 10) : 10}" />
                    <g:link controller="organisations" action="show" id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_next]}">${message(code:'default.paginate.next', default:'Next')}</g:link>
                  </g:if>
                </div>
              </g:each>
            </dd>
          </g:if>
        
          <g:if test="${orgInstance?.impId}">
            <dt><g:message code="org.impId.label" default="Import ID" /></dt>
            
              <dd><g:fieldValue bean="${orgInstance}" field="impId"/></dd>
            
          </g:if>

        </dl>

        <g:if test="${editable}">
            <g:form>
                <g:hiddenField name="id" value="${orgInstance?.id}" />
                <div class="form-actions">
                    <g:link class="btn" action="edit" id="${orgInstance?.id}">
                        <i class="icon-pencil"></i>
                        <g:message code="default.button.edit.label" default="Edit" />
                    </g:link>
                </div>
            </g:form>
        </g:if>

    </div>

  </body>
</html>
