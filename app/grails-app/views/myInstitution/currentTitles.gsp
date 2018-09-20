<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.currentTitles.label')}</title>
    
    <style>
      .filtering-dropdown-menu {max-height: 400px; overflow: hidden; overflow-y: auto;}
    </style>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
        <semui:crumb message="myinst.currentTitles.label" class="active" />
    </semui:breadcrumbs>
    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="currentTitles" params="${params + [format:'csv']}">CSV Export</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="currentTitles" params="${params + [format:'json']}">JSON Export</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="currentTitles" params="${params + [format:'xml']}">XML Export</g:link>
            </semui:exportDropdownItem>
            <g:each in="${transforms}" var="transkey,transval">
                <semui:exportDropdownItem>
                    <g:link class="item" action="currentTitles" id="${params.id}" params="${params + [format:'xml', transformId:transkey]}"> ${transval.name}</g:link>
                </semui:exportDropdownItem>
            </g:each>
        </semui:exportDropdown>
    </semui:controlButtons>

    <semui:messages data="${flash}" />

    <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'myinst.currentTitles.label', default:'Current Titles')}</h1>

    <semui:filter>
      <g:form id="filtering-form" action="currentTitles" controller="myInstitution" method="get" class="ui form">

        <g:set var="filterSub" value="${params.filterSub?params.list('filterSub'):"all"}" />
        <g:set var="filterPvd" value="${params.filterPvd?params.list('filterPvd'):"all"}" />
        <g:set var="filterHostPlat" value="${params.filterHostPlat?params.list('filterHostPlat'):"all"}" />
        <g:set var="filterOtherPlat" value="${params.filterOtherPlat?params.list('filterOtherPlat'):"all"}" />

          <div class="fields">
              <div class="field eight wide">

                <select name="filterSub" <%--multiple="multiple"--%> class="ui search selection fluid dropdown">
                  <option <%= (filterSub.contains("all")) ? ' selected' : '' %> value="all">${message(code:'myinst.currentTitles.all_subs', default:'All Subscriptions')}</option>
                  <g:each in="${subscriptions}" var="s">
                    <option <%= (filterSub.contains(s.id.toString())) ? 'selected="selected"' : '' %> value="${s.id}" title="${s.name}${s.consortia?' ('+s.consortia.name+')':''}">
                      ${s.name} <g:if test="${s.consortia}">( ${s.consortia.name} )</g:if>
                    </option>
                  </g:each>
                </select>
              </div>
              <div class="field eight wide">

                <select name="filterPvd" <%--multiple="multiple"--%> class="ui search selection fluid dropdown">
                  <option <%= (filterPvd.contains("all")) ? 'selected' : '' %> value="all">${message(code:'myinst.currentTitles.all_providers', default:'All Content Providers')}</option>
                  <g:each in="${providers}" var="p">
                    <%
                    def pvdId = p.id.toString()
                    def pvdName = p.name
                    %>
                    <option <%= (filterPvd.contains(pvdId)) ? 'selected' : '' %> value="${pvdId}" title="${pvdName}">
                      ${pvdName}
                    </option>
                  </g:each>
                </select>
              </div>
          </div>
          <div class="fields">
              <div class="field eight wide">

                <select name="filterHostPlat" <%--multiple="multiple"--%> class="ui search selection fluid dropdown">
                  <option <%= (filterHostPlat.contains("all")) ? 'selected' : '' %> value="all">${message(code:'myinst.currentTitles.all_host_platforms', default:'All Host Platforms')}</option>
                  <g:each in="${hostplatforms}" var="hp">
                    <%
                    def hostId = hp.id.toString()
                    def hostName = hp.name
                    %>
                    <option <%= (filterHostPlat.contains(hostId)) ? 'selected' : '' %> value="${hostId}" title="${hostName}">
                      ${hostName}
                    </option>
                  </g:each>
                </select>
              </div>
              <div class="field eight wide">

                <select name="filterOtherPlat" <%--multiple="multiple"--%> class="ui search selection fluid dropdown">
                  <option <%= (filterOtherPlat.contains("all")) ? 'selected' : '' %> value="all">${message(code:'myinst.currentTitles.all_other_platforms', default:'All Additional Platforms')}</option>
                  <g:each in="${otherplatforms}" var="op">
                    <%
                    def platId = op.id.toString()
                    def platName = op.name
                    %>
                    <option <%= (filterOtherPlat.contains(platId)) ? 'selected' : '' %> value="${platId}" title="${platName}">
                      ${platName}
                    </option>
                  </g:each>
                </select>
              </div>
          </div>

          <div class="fields">

              <div class="field">
                  <label>${message(code:'default.search.text', default:'Search text')}</label>
                  <input type="hidden" name="sort" value="${params.sort}">
                  <input type="hidden" name="order" value="${params.order}">
                  <input type="text" name="filter" value="${params.filter}" style="padding-left:5px;" placeholder="${message(code:'default.search.ph', default:'enter search term...')}"/>
              </div>
              <div class="field">
                  <semui:datepicker label="myinst.currentTitles.subs_valid_on" name="validOn" value="${validOn}" />
              </div>
              <div class="field">
                  <label>${message(code:'myinst.currentTitles.dupes', default:'Titles we subscribe to through 2 or more packages')}</label>
                  <div class="ui checkbox">
                      <input type="checkbox" class="hidden" name="filterMultiIE" value="${true}"<%=(params.filterMultiIE)?' checked="true"':''%>/>
                  </div>
              </div>
          </div>
          <div class="fields">
                    <div class="field">
                              <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                    </div>
                    <div class="field">
                      <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}"/>
                    </div>
          </div>
      </g:form>
    </semui:filter>

    <div>
      <div>
        <span>${message(code:'title.plural', default:'Titles')} ( ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(titles.size())),num_ti_rows])} )</span>
        <div>
          <g:form action="subscriptionBatchUpdate" params="${[id:subscriptionInstance?.id]}" class="ui form">
          <g:set var="counter" value="${offset+1}" />
          <table  class="ui sortable celled la-table table">
            <thead>
                <tr>
                  <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                  <th>ISSN</th>
                  <th>eISSN</th>
                  <th>${message(code:'subscription.details.startDate', default:'Earliest Date')}</th>
                  <th>${message(code:'subscription.details.endDate', default:'Latest Date')}</th>
                  <th>${message(code:'myinst.currentTitles.sub_content', default:'Subscribed Content')}</th>
                </tr>
            </thead>
            <g:each in="${titles}" var="ti">
              <tr>
                <td>
                    <semui:listIcon type="${ti.type.('value')}"/>
                    <g:link controller="titleDetails" action="show" id="${ti.id}"><strong>${ti.title}</strong></g:link>
                    <%--<br/>
                    <g:link controller="public" action="journalLicenses" params="${['journal':'kb:'+ti.id,'org':institution.id]}">${message(code:'myinst.currentTitles.check_license_terms', default:'Check current license terms')}</g:link>
                    --%>
                </td>
                <td style="white-space:nowrap">${ti.getIdentifierValue('ISSN')}</td>
                <td style="white-space:nowrap">${ti.getIdentifierValue('eISSN')}</td>

                <g:set var="title_coverage_info" value="${ti.getInstitutionalCoverageSummary(institution, session.sessionPreferences?.globalDateFormat, date_restriction)}" />

                <td  style="white-space:nowrap">${title_coverage_info.earliest}</td>
                <td  style="white-space:nowrap">${title_coverage_info.latest ?: message(code:'myinst.currentTitles.to_current', default:'To Current')}</td>
                <td>
                  <g:each in="${title_coverage_info.ies}" var="ie">
                        <i class="icon folder open outline la-list-icon"></i>
                        <g:link controller="subscriptionDetails" action="index" id="${ie.subscription.id}">${ie.subscription.name}</g:link>
                        <br />

                        <g:if test="${ie.startVolume}">${message(code:'tipp.volume.short', default:'Vol.')} ${ie.startVolume}</g:if>
                        <g:if test="${ie.startIssue}">${message(code:'tipp.issue.short', default:'Iss.')} ${ie.startIssue}</g:if>
                        <g:formatDate format="yyyy" date="${ie.startDate}"/>
                        -
                        <g:if test="${ie.endVolume}">${message(code:'tipp.volume.short', default:'Vol.')} ${ie.endVolume}</g:if>
                        <g:if test="${ie.endIssue}">${message(code:'tipp.issue.short', default:'Iss.')} ${ie.endIssue}</g:if>
                        <g:formatDate format="yyyy" date="${ie.endDate}"/>
                        <br />
                        <g:link controller="issueEntitlement" action="show" id="${ie.id}">${message(code:'myinst.currentTitles.full_ie', default:'Full Issue Entitlement Details')}</g:link>

                  </g:each>
                </td>
              </tr>
            </g:each>
            
          </table>
          </g:form>
        </div>
      </div>


        <g:if test="${titles}" >
          <semui:paginate  action="currentTitles" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_ti_rows}" />
        </g:if>

      <g:if env="development">
      <!-- For Test Only -->
	      <div class="accordion" id="accordions">
	        <div class="accordion-group"> 
	          <div class="accordion-heading">
	            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordions" href="#collapse-full-table">
	              For Test Only: Full Table (show/hide)
	            </a>
	          </div>
	          <div id="collapse-full-table" class="accordion-body collapse out">
	            <div class="accordion-inner">
	              <table  class="ui sortable celled la-table table">
	                <tr>
	                  <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
	                  <th>ISSN</th>
	                  <th>eISSN</th>
	                  <th>${message(code:'subscription.details.startDate', default:'Earliest Date')}</th>
	                  <th>${message(code:'subscription.details.endDate', default:'Latest Date')}</th>
	                  <th>${message(code:'subscription.label', default:'Subscription')}</th>
	                  <th>${message(code:'package.content_provider', default:'Content Provider')}</th>
	                  <th>${message(code:'tipp.host_platform', default:'Host Platform')}</th>
	                  <th>${message(code:'tipp.additionalPlatforms', default:'Additional Platforms')}</th>
	                </tr>
	                <g:each in="${entitlements}" var="ie">
	                  <tr>
	                    <td>${ie.tipp.title.title}</td>
	                    <td>${ie.tipp.title.getIdentifierValue('ISSN')}</td>
	                    <td>${ie.tipp.title.getIdentifierValue('eISSN')}</td>
	                    <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.startDate}"/></td>
	                    <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.endDate}"/></td>
	                    <td>${ie.subscription.name}</td>
	                    <td>
	                      <g:each in="${ie.tipp.pkg.orgs}" var="role">
	                        <g:if test="${role.roleType?.value?.equals('Content Provider')}" >${role.org.name}</g:if>
	                      </g:each>
	                    </td>
	                    <td><div><i class="icon-globe"></i><span>${ie.tipp.platform.name}</span></div></td>
	                    <td>
	                      <g:each in="${ie.tipp.additionalPlatforms}" var="p">
	                        <div><i class="icon-globe"></i><span>${p.platform.name}</span></div>
	                      </g:each>
	                    </td>
	                  </tr> 
	                </g:each>
	              </table>
	            </div>
	          </div>
	        </div>
	      </div>
      <!-- End - For Test Only -->
      </g:if>
    </div>
  
  </body>
</html>
