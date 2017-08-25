<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'title.plural', default:'Titles')}</title>
    
    <style>
      .filtering-dropdown-menu {max-height: 400px; overflow: hidden; overflow-y: auto;}
    </style>
  </head>

  <body>

    <laser:breadcrumbs>
        <laser:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
        <laser:crumb message="myinst.currentTitles.label" class="active" />
        <li class="dropdown pull-right">
            <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">
                ${message(code:'default.button.exports.label', default:'Exports')}<b class="caret"></b></a>&nbsp;
            <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
                <li>
                    <g:link action="currentTitles" params="${params+[format:'csv']}">CSV</g:link>
                </li>
                <li>
                    <g:link action="currentTitles" params="${params+[format:'json']}">JSON</g:link>
                </li>
                <li>
                    <g:link action="currentTitles" params="${params+[format:'xml',shortcode:params.shortcode]}">XML</g:link>
                </li>

                <g:each in="${transforms}" var="transkey,transval">
                    <li><g:link action="currentTitles" id="${params.id}" params="${params+[format:'xml',transformId:transkey]}"> ${transval.name}</g:link></li>
                </g:each>

            </ul>
        </li>
    </laser:breadcrumbs>

    <g:if test="${flash.message}">
      <div class="container">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </div>
    </g:if>

    <g:if test="${flash.error}">
      <div class="container">
        <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
      </div>
    </g:if>

    <div class="container">
	  <g:form id="filtering-form" action="currentTitles" params="${[shortcode:params.shortcode]}" controller="myInstitutions" method="get" class="form-inline">
	    <div class="container">
	    <h1>${institution?.name} - ${message(code:'myinst.currentTitles.label', default:'Current Titles')}</h1>
	      
		<g:set var="filterSub" value="${params.filterSub?params.list('filterSub'):"all"}" />
		<g:set var="filterPvd" value="${params.filterPvd?params.list('filterPvd'):"all"}" />
		<g:set var="filterHostPlat" value="${params.filterHostPlat?params.list('filterHostPlat'):"all"}" />
		<g:set var="filterOtherPlat" value="${params.filterOtherPlat?params.list('filterOtherPlat'):"all"}" />
      
      	<select size="5" name="filterSub" class="span3" multiple="multiple"> <!-- onchange="this.form.submit()" -->
      		<option<%= (filterSub.contains("all")) ? ' selected="selected"' : '' %> value="all">${message(code:'myinst.currentTitles.all_subs', default:'All Subscriptions')}</option>
      		<g:each in="${subscriptions}" var="s">
              <option<%= (filterSub.contains(s.id.toString())) ? ' selected="selected"' : '' %> value="${s.id}" title="${s.name}${s.consortia?' ('+s.consortia.name+')':''}">
                ${s.name} <g:if test="${s.consortia}">( ${s.consortia.name} )</g:if>
              </option>
            </g:each>
      	</select>
      	<select size="5" name="filterPvd" class="span3" multiple="multiple">
      		<option<%= (filterPvd.contains("all")) ? ' selected="selected"' : '' %> value="all">${message(code:'myinst.currentTitles.all_providers', default:'All Content Providers')}</option>
      		<g:each in="${providers}" var="p">
              <% 
              def pvdId = p.id.toString()
              def pvdName = p.name 
              %>
              <option<%= (filterPvd.contains(pvdId)) ? ' selected="selected"' : '' %> value="${pvdId}" title="${pvdName}">
                ${pvdName}
              </option>
            </g:each>
      	</select>
      	<select size="5" name="filterHostPlat" class="span3" multiple="multiple">
      		<option<%= (filterHostPlat.contains("all")) ? ' selected="selected"' : '' %> value="all">${message(code:'myinst.currentTitles.all_host_platforms', default:'All Host Platforms')}</option>
      		<g:each in="${hostplatforms}" var="hp">
              <% 
              def hostId = hp.id.toString()
              def hostName = hp.name 
              %>
              <option<%= (filterHostPlat.contains(hostId)) ? ' selected="selected"' : '' %> value="${hostId}" title="${hostName}">
                ${hostName}
              </option>
            </g:each>
      	</select>
      	<select size="5" name="filterOtherPlat" class="span3" multiple="multiple">
      		<option<%= (filterOtherPlat.contains("all")) ? ' selected="selected"' : '' %> value="all">${message(code:'myinst.currentTitles.all_other_platforms', default:'All Additional Platforms')}</option>
      		<g:each in="${otherplatforms}" var="op">
              <% 
              def platId = op.id.toString()
              def platName = op.name 
              %>
              <option<%= (filterOtherPlat.contains(platId)) ? ' selected="selected"' : '' %> value="${platId}" title="${platName}">
                ${platName}
              </option>
            </g:each>
      	</select>
    	</div>
      	<br/>
	    <div class="container" style="text-align:center">
      		<div class="pull-left">
      			<label class="checkbox">
      				<input type="checkbox" name="filterMultiIE" value="${true}"<%=(params.filterMultiIE)?' checked="true"':''%>/> ${message(code:'myinst.currentTitles.dupes', default:'Titles we subscribe to through 2 or more packages')}
				</label>
      		</div>
	    	<div class="pull-right">
		        <input type="hidden" name="sort" value="${params.sort}">
		        <input type="hidden" name="order" value="${params.order}">
		        <label>${message(code:'default.search.text', default:'Search text')}:</label>
		        <input name="filter" value="${params.filter}" style="padding-left:5px;" placeholder="${message(code:'default.search.ph', default:'enter search term...')}"/>
		        <label>${message(code:'myinst.currentTitles.subs_valid_on', default:'Subscriptions Valid on')}</label>
                        <g:simpleHiddenValue id="validOn" name="validOn" type="date" value="${validOn}"/>
		        &nbsp;<input type="submit" class="btn btn-primary" value="${message(code:'default.button.search.label', default:'Search')}"/>
	        </div>
	    </div>
    </g:form>
  	  <br/>
    </div>
  
    <div class="container">
      <dl>
        <dt>${message(code:'title.plural', default:'Titles')} ( ${message(code:'default.paginate.offset', args:[(offset+1),(offset+(titles.size())),num_ti_rows])} )</dt>
        <dd>
          <g:form action="subscriptionBatchUpdate" params="${[id:subscriptionInstance?.id]}" class="form-inline">
          <g:set var="counter" value="${offset+1}" />
          <table  class="table table-striped table-bordered">

            <tr>
              <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
              <th>ISSN</th>
              <th>eISSN</th>
              <th>${message(code:'subscription.details.startDate', default:'Earliest Date')}</th>
              <th>${message(code:'subscription.details.endDate', default:'Latest Date')}</th>
              <th>${message(code:'myinst.currentTitles.sub_content', default:'Subscribed Content')}</th>
            </tr>  
            
            <g:each in="${titles}" var="ti">
              <tr>
                <td><g:link controller="titleDetails" action="show" id="${ti.id}">${ti.title}</g:link>
                <br/> 
                <g:link controller="public" action="journalLicenses" params="${['journal':'kb:'+ti.id,'org':institution.id]}">${message(code:'myinst.currentTitles.check_license_terms', default:'Check current license terms')}</g:link>
                </td>
                <td style="white-space:nowrap">${ti.getIdentifierValue('ISSN')}</td>
                <td style="white-space:nowrap">${ti.getIdentifierValue('eISSN')}</td>

                <g:set var="title_coverage_info" value="${ti.getInstitutionalCoverageSummary(institution, session.sessionPreferences?.globalDateFormat, date_restriction)}" />

                <td  style="white-space:nowrap">${title_coverage_info.earliest}</td>
                <td  style="white-space:nowrap">${title_coverage_info.latest ?: message(code:'myinst.currentTitles.to_current', default:'To Current')}</td>
                <td>
                  <g:each in="${title_coverage_info.ies}" var="ie">
                      <p>
                        <g:link controller="subscriptionDetails" action="index" id="${ie.subscription.id}">${ie.subscription.name}</g:link>:
                        <g:if test="${ie.startVolume}">${message(code:'tipp.volume.short', default:'Vol.')} ${ie.startVolume}</g:if>
                        <g:if test="${ie.startIssue}">${message(code:'tipp.issue.short', default:'Iss.')} ${ie.startIssue}</g:if>
                        <g:formatDate format="yyyy" date="${ie.startDate}"/>
                        -
                        <g:if test="${ie.endVolume}">${message(code:'tipp.volume.short', default:'Vol.')} ${ie.endVolume}</g:if>
                        <g:if test="${ie.endIssue}">${message(code:'tipp.issue.short', default:'Iss.')} ${ie.endIssue}</g:if>
                        <g:formatDate format="yyyy" date="${ie.endDate}"/>
                        (<g:link controller="issueEntitlement" action="show" id="${ie.id}">${message(code:'myinst.currentTitles.full_ie', default:'Full Issue Entitlement Details')}</g:link>)
                      </p>
                  </g:each>
                </td>
              </tr>
            </g:each>
            
          </table>
          </g:form>
        </dd>
      </dl>

      <div class="pagination" style="text-align:center">
        <g:if test="${titles}" >
          <bootstrap:paginate  action="currentTitles" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_ti_rows}" />
        </g:if>
      </div>
      
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
	              <table  class="table table-striped table-bordered">
	                <tr>
	                  <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
	                  <th>ISSN</th>
	                  <th>eISSN</th>
	                  <th>${message(code:'subscription.details.startDate', default:'Earliest Date')}</th>
	                  <th>${message(code:'subscription.details.endDate', default:'Latest Date')}</th>
	                  <th>${message(code:'subscription.label', default:'Subscription')}</th>
	                  <th>${message(code:'package.content_provider', default:'Content Provider')}</th>
	                  <th>${message(code:'tipp.host_platform', default:'Host Platform')}</th>
	                  <th>${message(code:'tipp.other_platform', default:'Other Platform')}</th>
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
