
<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
    <title>${message(code:'laser', default:'LAS:eR')} <g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div>
      

        <div class="page-header">
          <h1>${orgInstance.name}</h1>
        </div>

        <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>

        <dl>
          <g:if test="${orgInstance?.name}">
            <dt><g:message code="org.name.label" default="Name" /></dt>
            
              <dd><g:fieldValue bean="${orgInstance}" field="name"/></dd>
          </g:if>
        
			<g:if test="${orgInstance?.addresses}">
				<dt><g:message code="org.addresses.label" default="Addresses" /></dt>
				<g:each in="${orgInstance?.addresses}" var="a">
					<g:if test="${a.org}">
						<g:render template="/templates/cpa/address" model="${[address: a]}"></g:render>
					</g:if>
				</g:each>
			</g:if>
		
			<g:if test="${orgInstance?.contacts}">
				<dt><g:message code="org.contacts.label" default="Contacts" /></dt>
				<g:each in="${orgInstance?.contacts}" var="c">
					<g:if test="${c.org}">
						<g:render template="/templates/cpa/contact" model="${[contact: c]}"></g:render>
					</g:if>
				</g:each>
			</g:if>

        	<g:if test="${orgInstance?.prsLinks}">
				<dt><g:message code="org.prsLinks.label" default="Persons" /></dt>
				<g:each in="${orgInstance?.prsLinks}" var="pl">
					<g:if test="${pl?.functionType?.value && pl?.prs?.isPublic?.value!='No'}">		
						<g:render template="/templates/cpa/person_details" model="${[personRole: pl]}"></g:render>
					</g:if>
				</g:each>
			</g:if>
		
          <g:if test="${orgInstance?.ipRange}">
            <dt><g:message code="org.ipRange.label" default="Ip Range" /></dt>
            
              <dd>${orgInstance.ipRange}</dd>
            
          </g:if>
        
          <g:if test="${orgInstance?.sector}">
            <dt><g:message code="org.sector.label" default="Sector" /></dt>
            
              <dd>${orgInstance.sector.getI10n('value')}</dd>
            
          </g:if>

      <g:if test="${orgInstance?.membership}">
        <dt><g:message code="org.membership.label" default="Membership" /></dt>

        <dd>${orgInstance.membership.getI10n('value')}</dd>

      </g:if>
        
          <g:if test="${orgInstance?.ids}">
            <dt><g:message code="org.ids.label" default="Ids" /></dt>
              <g:each in="${orgInstance.ids}" var="i">
              <dd><g:link controller="identifier" action="show" id="${i.identifier.id}">${i?.identifier?.ns?.ns?.encodeAsHTML()} : ${i?.identifier?.value?.encodeAsHTML()}</g:link></dd>
              </g:each>
          </g:if>

          <g:if test="${orgInstance?.outgoingCombos}">
            <dt><g:message code="org.outgoingCombos.label" default="Outgoing Combos" /></dt>
            <g:each in="${orgInstance.outgoingCombos}" var="i">
              <dd>${i.type?.value} - <g:link controller="organisations" action="info" id="${i.toOrg.id}">${i.toOrg?.name}</g:link>
                (<g:each in="${i.toOrg?.ids}" var="id">
                  ${id.identifier.ns.ns}:${id.identifier.value} 
                </g:each>)
              </dd>
            </g:each>
          </g:if>

          <g:if test="${orgInstance?.incomingCombos}">
            <dt><g:message code="org.incomingCombos.label" default="Incoming Combos" /></dt>
            <g:each in="${orgInstance.incomingCombos}" var="i">
              <dd>${i.type?.value} - <g:link controller="org" action="show" id="${i.toOrg.id}">${i.fromOrg?.name}</g:link>
                (<g:each in="${i.fromOrg?.ids}" var="id">
                  ${id.identifier.ns.ns}:${id.identifier.value} 
                </g:each>)
              </dd>

            </g:each>
          </g:if>

          <g:if test="${orgInstance?.links}">
            <dt><g:message code="org.links.other.label" default="Other org links" /></dt>
            <dd>
              <g:each in="${sorted_links}" var="rdv_id,link_cat">
                <div>
                  <span style="font-weight:bold;">${link_cat.rdv.getI10n('value')} (${link_cat.total})</span>
                </div>
                <ul>
                  <g:each in="${link_cat.links}" var="i">
                    <li>
                      <g:if test="${i.pkg}">
                        <g:link controller="packageDetails" action="show" id="${i.pkg.id}">
                          ${message(code:'package.label', default:'Package')}: ${i.pkg.name} (${i.pkg?.packageStatus?.getI10n('value')})
                        </g:link>
                      </g:if>
                      <g:if test="${i.sub}">
                        <g:link controller="subscriptionDetails" action="index" id="${i.sub.id}">
                          ${message(code:'subscription.label', default:'Subscription')}: ${i.sub.name} (${i.sub.status?.getI10n('value')})
                        </g:link>
                      </g:if>
                      <g:if test="${i.lic}">
                        <g:link controller="licenseDetails" action="index" id="${i.lic.id}">
                          ${message(code:'license.label', default:'License')}: ${i.lic.reference ?: i.lic.id} (${i.lic.status?.getI10n('value')})
                        </g:link>
                      </g:if>
                      <g:if test="${i.title}">
                        <g:link controller="titleInstance" action="show" id="${i.title.id}">
                          ${message(code:'title.label', default:'Title')}: ${i.title.title} (${i.title.status?.getI10n('value')})
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
                    <g:link controller="organisations" action="info" id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_prev]}">${message(code:'default.paginate.prev', default:'Prev')}</g:link>
                  </g:if>
                  <g:if test="${link_cat.total > 10 && ( !local_offset || ( local_offset < (link_cat.total - 10) ) )}">
                    <g:set var="os_next" value="${local_offset ? (local_offset + 10) : 10}" />
                    <g:link controller="organisations" action="info" id="${orgInstance.id}" params="${params + ["rdvl_${rdv_id}": os_next]}">${message(code:'default.paginate.next', default:'Next')}</g:link>
                  </g:if>
                </div>
              </g:each>
            </dd>
          </g:if>
        
          <g:if test="${orgInstance?.impId}">
            <dt><g:message code="org.impId.label" default="Imp Id" /></dt>
            
              <dd><g:fieldValue bean="${orgInstance}" field="impId"/></dd>
            
          </g:if>
        
        
        </dl>


    </div>
  </body>
</html>
