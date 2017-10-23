<%@ page import="com.k_int.kbplus.Package;com.k_int.kbplus.RefdataCategory;org.springframework.web.servlet.support.RequestContextUtils" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap">
    <g:set var="entityName" value="${message(code: 'package', default: 'Package')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
  </head>
 <body>
    <g:set var="locale" value="${RequestContextUtils.getLocale(request)}" />

    <div class="container">
      <ul class="breadcrumb">
        <li><g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span></li>
        <li><g:link controller="packageDetails" action="index">${message(code: 'package.show.all')}</g:link><span class="divider">/</span></li>
        <li><g:link controller="packageDetails" action="show" id="${packageInstance.id}">${packageInstance.name}</g:link></li>

        <li class="dropdown pull-right">
          <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">${message(code:'default.button.exports.label', default:'Exports')}<b class="caret"></b></a>

          <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
            <li><g:link action="show" params="${params+[format:'json']}">JSON Export</g:link></li>
            <li><g:link action="show" params="${params+[format:'xml']}">XML Export</g:link></li>
            <g:each in="${transforms}" var="transkey,transval">
              <li><g:link action="show" id="${params.id}" params="${[format:'xml',transformId:transkey,mode:params.mode]}"> ${transval.name}</g:link></li>
            </g:each>
          </ul>
        </li>

        <li class="pull-right">
          <g:if test="${editable}">
              <span class="badge badge-warning">${message(code: 'default.editable')}</span>&nbsp;
          </g:if>
          ${message(code: 'package.show.view')}:
          <div class="btn-group" data-toggle="buttons-radio">
            <g:link controller="packageDetails" action="show" params="${params+['mode':'basic']}" class="btn btn-primary btn-mini ${((params.mode=='basic')||(params.mode==null))?'active':''}">${message(code:'default.basic', default:'Basic')}</g:link>
            <g:link controller="packageDetails" action="show" params="${params+['mode':'advanced']}" class="btn btn-primary btn-mini ${params.mode=='advanced'?'active':''}">${message(code:'default.advanced', default:'Advanced')}</g:link>
          </div>
          &nbsp;
        </li>
        
      </ul>
    </div>

    <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges,'flash':flash,'model':packageInstance]}"/>


      <div class="container">
        <g:if test="${params.asAt}"><h1>${message(code:'package.show.asAt', args:[params.asAt])} </h1></g:if>
        <div class="page-header">
          <div>
          <h1><g:if test="${editable}"><span id="packageNameEdit"
                        class="xEditableValue"
                        data-type="textarea"
                        data-pk="${packageInstance.class.name}:${packageInstance.id}"
                        data-name="name"
                        data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if><g:else>${packageInstance.name}</g:else></h1>
           <g:render template="nav" />
            <sec:ifAnyGranted roles="ROLE_ADMIN,KBPLUS_EDITOR">
            <g:link controller="announcement" action="index" params='[at:"Package Link: ${pkg_link_str}",as:"RE: Package ${packageInstance.name}"]'>${message(code: 'package.show.announcement')}</g:link>
            </sec:ifAnyGranted>
            <g:if test="${forum_url != null}">
              <a href="${forum_url}"> | Discuss this package in forums</a> <a href="${forum_url}" title="Discuss this package in forums (new Window)" target="_blank"><i class="icon-share-alt"></i></a>
            </g:if>

          </div>

        </div>
    </div>

        <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>

        <g:hasErrors bean="${packageInstance}">
        <bootstrap:alert class="alert-error">
        <ul>
          <g:eachError bean="${packageInstance}" var="error">
          <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
          </g:eachError>
        </ul>
        </bootstrap:alert>
        </g:hasErrors>

    <div class="container">
      <div class="row">
        <div class="span8">
            <h6>
              ${message(code: 'package.show.pkg_information')}
              <span class="btn-group pull-right" data-toggle="buttons-radio">
                <g:link controller="packageDetails" action="show" params="${params+['mode':'basic']}" class="btn btn-primary btn-mini ${((params.mode=='basic')||(params.mode==null))?'active':''}">${message(code:'default.basic', default:'Basic')}</g:link>
                <g:link controller="packageDetails" action="show" params="${params+['mode':'advanced']}" class="btn btn-primary btn-mini ${params.mode=='advanced'?'active':''}">${message(code:'default.advanced', default:'Advanced')}</g:link>
              </span>
              &nbsp;
            </h6>
            <g:hiddenField name="version" value="${packageInstance?.version}" />
            <fieldset class="inline-lists">

              <dl>
                <dt>${message(code: 'package.show.pkg_name')}</dt>
                <dd> <g:xEditable owner="${packageInstance}" field="name"/></dd>
              </dl>
              
              <dl>
                <dt>${message(code: 'package.show.persistent_id')}</dt>
                <dd>uri://laser/${grailsApplication.config.kbplusSystemId}/package/${packageInstance?.id}</dd>
              </dl>
              
              <dl>
                <dt>${message(code: 'package.show.other_ids')}</dt>
                <dd>
                  <table class="table table-bordered">
                    <thead>
                      <tr>
                        <th>${message(code: 'component.id.label')}</th>
                        <th>${message(code: 'identifier.namespace.label')}</th>
                        <th>${message(code: 'identifier.label')}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <g:each in="${packageInstance.ids}" var="io">
                          <tr>
                            <td>${io.id}</td>
                            <td>${io.identifier.ns.ns}</td>
                            <g:if test="${io.identifier.value =~ /^http/}">
                              <td><a href="${io.identifier.value}" target="_blank">${message(code:'component.originediturl.label', default:"${io.identifier.value}")}</a></td>
                            </g:if>
                            <g:else>
                              <td>${io.identifier.value}</td>
                            </g:else>
                          </tr>
                      </g:each>
                     
                    </tbody>
                  </table>

                  <g:if test="${editable}">

                      <laser:formAddIdentifier owner="${packageInstance}">
                      </laser:formAddIdentifier>

                  </g:if>

                </dd>
              </dl>

              <dl>
                <dt>${message(code: 'license.is_public')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="isPublic" config='YN'/>
                </dd>
              </dl> 

              <dl>
                <dt><g:message code="license" default="License"/></dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="license" config="Licenses"/>
                </dd>
              </dl>

              <dl>
                <dt>${message(code: 'package.show.vendor_url')}</dt>
                <dd>
                  <g:xEditable owner="${packageInstance}" field="vendorURL" />
                </dd>
              </dl>

                <dl>
                  <dt>${message(code: 'package.show.start_date')}</dt>
                  <dd>
                    <g:xEditable owner="${packageInstance}" field="startDate" type="date"/>
                </dd>
                </dl>

               <dl>
                    <dt>${message(code: 'package.show.end_date')}</dt>
                    <dd>
                       <g:xEditable owner="${packageInstance}" field="endDate" type="date"/>
                    </dd>
               </dl>



              <dl>
                <dt>${message(code: 'package.show.orglink')}</dt>
                <dd><g:render template="orgLinks" 
                            contextPath="../templates"
                            model="${[roleLinks:packageInstance?.orgs,parent:packageInstance.class.name+':'+packageInstance.id,property:'orgs',editmode:editable]}" /></dd>
              </dl>

             <dl>
                <dt>${message(code: 'package.list_status')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="packageListStatus" config="${RefdataCategory.PKG_LIST_STAT}"/>
                </dd>
             </dl>

             <dl>
                <dt>${message(code: 'package.breakable')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="breakable" config="${RefdataCategory.PKG_BREAKABLE}"/>
                </dd>
             </dl>

             <dl>
                <dt>${message(code: 'package.consistent')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="consistent" config="${RefdataCategory.PKG_CONSISTENT}"/>
                </dd>
             </dl>

             <dl>
                <dt>${message(code: 'package.fixed')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="fixed" config="${RefdataCategory.PKG_FIXED}"/>
                </dd>
             </dl>

              <dl>
                <dt>${message(code: 'package.scope')}</dt>
                <dd>
                  <g:xEditableRefData owner="${packageInstance}" field="packageScope" config="${RefdataCategory.PKG_SCOPE}"/>
                </dd>
              </dl>

          </fieldset>
        </div>


        <div class="span4">

          <div class="well notes">
            <g:if test="${(subscriptionList != null) && (subscriptionList?.size() > 0)}">
              <h5>${message(code: 'package.show.addToSub')}:</h5>
              <g:form controller="packageDetails" action="addToSub" id="${packageInstance.id}">
                <select name="subid">
                  <g:each in="${subscriptionList}" var="s">
                    <option value="${s.sub.id}">${s.sub.name ?: "unnamed subscription ${s.sub.id}"} - ${s.org.name}</option>
                  </g:each>
                </select><br/>
                ${message(code:'package.show.addEnt', default:'Create Entitlements in Subscription')}: <input type="checkbox" id="addEntitlementsCheckbox" name="addEntitlements" value="true" style="vertical-align:text-bottom;"/><br/>
                <input id="add_to_sub_submit_id" type="submit" value="${message(code:'default.button.submit.label')}"/>
              </g:form>
            </g:if>
            <g:else>
              ${message(code: 'package.show.no_subs')}
            </g:else>
          </div>


          <g:render template="/templates/documents" model="${[ ownobj:packageInstance, owntp:'pkg']}" />
          <g:render template="/templates/notes"  model="${[ ownobj:packageInstance, owntp:'pkg']}" />
        </div>
      </div>
    </div>

    <div class="container">
      <br/>
      <p>
        <span class="pull-right">
          <g:if test="${unfiltered_num_tipp_rows == num_tipp_rows}">
            ${message(code:'package.show.filter.off')}
          </g:if>
          <g:else>
            ${message(code:'package.show.filter.on', args:[num_tipp_rows,unfiltered_num_tipp_rows])}
          </g:else>
        </span>
        ${message(code: 'package.show.pagination', args: [(offset+1),lasttipp,num_tipp_rows])} (
        <g:if test="${params.mode=='advanced'}">${message(code:'package.show.switchView.basic')} <g:link controller="packageDetails" action="show" params="${params+['mode':'basic']}">${message(code:'default.basic', default:'Basic')}</g:link>
        </g:if>
        <g:else>${message(code:'package.show.switchView.advanced')} <g:link controller="packageDetails" action="show" params="${params+['mode':'advanced']}">${message(code:'default.advanced', default:'Advanced')}</g:link>
        </g:else>
          )
     </p>

        <div class="well">
          <g:form action="show" params="${params}" method="get" class="form-inline">
            <input type="hidden" name="sort" value="${params.sort}">
            <input type="hidden" name="order" value="${params.order}">
            <input type="hidden" name="mode" value="${params.mode}">
            <div>
              <label>${message(code:'package.compare.filter.title', default:'Filters - Title')}:</label> <input name="filter" value="${params.filter}"/>
              <label>${message(code:'tipp.coverageNote', default:'Coverage note')}:</label> <input name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
            </div>
            <div>
              <label>${message(code:'package.compare.filter.coverage_startsBefore', default:'Coverage Starts Before')}:</label>
              <g:simpleHiddenValue id="startsBefore" name="startsBefore" type="date" value="${params.startsBefore}"/> -
              <label>${message(code:'package.compare.filter.coverage_endsAfter', default:'Ends After')}:</label>
              <g:simpleHiddenValue id="endsAfter" name="endsAfter" type="date" value="${params.endsAfter}"/> -
              <g:if test="${params.mode!='advanced'}">
                <label>${message(code:'package.show.atDate', default:'Show package contents on specific date')}:</label>
                <g:simpleHiddenValue id="asAt" name="asAt" type="date" value="${params.asAt}"/>
              </g:if>

              <input type="submit" class="btn btn-primary pull-right" value="${message(code:'package.compare.filter.submit.label', default:'Filter Results')}" />
            </div>
          </g:form>
        </div>
          <g:form action="packageBatchUpdate" params="${[id:packageInstance?.id]}">
            <g:hiddenField name="filter" value="${params.filter}"/>
            <g:hiddenField name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
            <g:hiddenField name="startsBefore" value="${params.startsBefore}"/>
            <g:hiddenField name="endsAfter" value="${params.endsAfter}"/>
            <g:hiddenField name="sort" value="${params.sort}"/>
            <g:hiddenField name="order" value="${params.order}"/>
            <g:hiddenField name="offset" value="${params.offset}"/>
            <g:hiddenField name="max" value="${params.max}"/>
            <table class="table table-bordered">
            <thead>
            <tr class="no-background">

              <th>
                <g:if test="${editable}"><input type="checkbox" name="chkall" onClick="javascript:selectAll();"/></g:if>
              </th>

              <th colspan="7">
                <g:if test="${editable}">
                  <select id="bulkOperationSelect" name="bulkOperation" class="input-xxlarge">
                    <option value="edit">${message(code:'package.show.batch.edit.label', default:'Batch Edit Selected Rows Using the following values')}</option>
                    <option value="remove">${message(code:'package.show.batch.remove.label', default:'Batch Remove Selected Rows')}</option>
                  </select>
                  <br/>
                  <table class="table table-bordered">
                    <tr>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'subscription.details.coverageStartDate', default:'Coverage Start Date')}: <g:simpleHiddenValue id="bulk_start_date" name="bulk_start_date" type="date"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase">${message(code:'default.or', default:'or')}</i> 
                          <input type="checkbox" name="clear_start_date" />
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div> 
                      </td>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.startVolume', default:'Start Volume')}: <g:simpleHiddenValue id="bulk_start_volume" name="bulk_start_volume" />
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_start_volume"/> 
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.startIssue', default:'Start Issue')}: <g:simpleHiddenValue id="bulk_start_issue" name="bulk_start_issue"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_start_issue"/> 
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'subscription.details.coverageEndDate', default:'Coverage End Date')}:  <g:simpleHiddenValue id="bulk_end_date" name="bulk_end_date" type="date"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_end_date"/>
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.endVolume', default:'End Volume')}: <g:simpleHiddenValue id="bulk_end_volume" name="bulk_end_volume"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_end_volume"/> 
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.endIssue', default:'End Issue')}: <g:simpleHiddenValue id="bulk_end_issue" name="bulk_end_issue"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_end_issue"/> 
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                    </tr>
                    <tr>
                       <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.hostPlatformURL', default:'Host Platform URL')}: <g:simpleHiddenValue id="bulk_hostPlatformURL" name="bulk_hostPlatformURL"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_hostPlatformURL"/>
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                      <td>
                        <div style="display:inline-block;white-space:nowrap;">
                          ${message(code:'tipp.coverageNote', default:'Coverage Note')}: <g:simpleHiddenValue id="bulk_coverage_note" name="bulk_coverage_note"/>
                        </div>
                        <div style="display:inline-block;white-space:nowrap;">
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_coverage_note"/> 
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </div>
                      </td>
                      <td>${message(code:'tipp.embargo', default:'Embargo')}:  <g:simpleHiddenValue id="bulk_embargo" name="bulk_embargo"/>
                          <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                          <input type="checkbox" name="clear_embargo"/>
                          ${message(code:'package.show.checkToClear', default:'Check to clear')}
                      </td>
                    </tr>
                    <g:if test="${params.mode=='advanced'}">
                      <tr>
                        <td>
                          <div style="display:inline-block;white-space:nowrap;">
                            ${message(code:'tipp.delayedOA', default:'Delayed OA')}: <g:simpleHiddenRefdata id="bulk_delayedOA" name="bulk_delayedOA" refdataCategory="TitleInstancePackagePlatform.DelayedOA"/>
                          </div>
                          <div style="display:inline-block;white-space:nowrap;">
                            <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                            <input type="checkbox" name="clear_delayedOA"/> 
                            ${message(code:'package.show.checkToClear', default:'Check to clear')}
                          </div>
                        </td>
                        <td>
                          <div style="display:inline-block;white-space:nowrap;">
                            ${message(code:'tipp.hybridOA', default:'Hybrid OA')}: <g:simpleHiddenRefdata id="bulk_hybridOA" name="bulk_hybridOA" refdataCategory="TitleInstancePackagePlatform.HybridOA"/>
                          </div>
                          <div style="display:inline-block;white-space:nowrap;">
                            <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                            <input type="checkbox" name="clear_hybridOA"/> 
                            ${message(code:'package.show.checkToClear', default:'Check to clear')}
                          </div>
                        </td>
                        <td>
                          <div style="display:inline-block;white-space:nowrap;">
                            ${message(code:'tipp.paymentType', default:'Payment')}: <g:simpleHiddenRefdata id="bulk_payment" name="bulk_payment" refdataCategory="TitleInstancePackagePlatform.PaymentType"/>
                          </div>
                          <div style="display:inline-block;white-space:nowrap;">
                            <i style="text-transform:uppercase;">${message(code:'default.or', default:'or')}</i>
                            <input type="checkbox" name="clear_payment"/>
                            ${message(code:'package.show.checkToClear', default:'Check to clear')}
                          </div>
                        </td>
                      </tr>
                    </g:if>
                  </table>
                  <button name="BatchSelectedBtn" value="on" onClick="return confirmSubmit()" class="btn btn-primary">${message(code:'default.button.apply_batch.label')} (${message(code:'default.selected.label')})</button>
                  <button name="BatchAllBtn" value="on" onClick="return confirmSubmit()" class="btn btn-primary">${message(code:'default.button.apply_batch.label')} (${message(code:'package.show.batch.allInFL', default:'All in filtered list')})</button>
                </g:if>
              </th>
            </tr>
            <tr>
              <th>&nbsp;</th>
              <th>&nbsp;</th>
              <g:sortableColumn params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
              <th style="">${message(code:'tipp.platform', default:'Platform')}</th>
              <th style="">${message(code:'tipp.hybridOA', default:'Hybrid OA')}</th>
              <th style="">${message(code:'identifier.plural', default:'Identifiers')}</th>
              <th style="">${message(code:'tipp.coverage_start', default:'Coverage Start')}</th>
              <th style="">${message(code:'tipp.coverage_end', default:'Coverage End')}</th>
            </tr>


            </thead>
            <tbody>
            <g:set var="counter" value="${offset+1}" />
            <g:each in="${titlesList}" var="t">
              <g:set var="hasCoverageNote" value="${t.coverageNote?.length() > 0}" />
               <tr>
                <td ${hasCoverageNote==true?'rowspan="2"':''}><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${t.id}" class="bulkcheck"/></g:if></td>
                <td ${hasCoverageNote==true?'rowspan="2"':''}>${counter++}</td>
                <td style="vertical-align:top;">
                   <b>${t.title.title}</b>
                   <g:link controller="titleDetails" action="show" id="${t.title.id}">(${message(code:'title.label', default:'Title')})</g:link>
                   <g:link controller="tipp" action="show" id="${t.id}">(${message(code:'tipp.label', default:'TIPP')})</g:link><br/>
                   <ul>
                     <g:each in="${t.title.distinctEventList()}" var="h">
                       <li>

                         ${message(code:'title.history.label', default:'Title History')}: <g:formatDate date="${h.event.eventDate}" format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"/><br/>

                         <g:each status="st" in="${h.event.fromTitles()}" var="the">
                            <g:if test="${st>0}">, </g:if>
                            <g:link controller="titleDetails" action="show" id="${the.id}">${the.title}</g:link>
                            <g:if test="${the.isInPackage(packageInstance)}">(✔)</g:if><g:else>(✘)</g:else>
                         </g:each>
                         ${message(code:'package.show.became', default:'Became')}
                         <g:each status="st" in="${h.event.toTitles()}" var="the"><g:if test="${st>0}">, </g:if>
                            <g:link controller="titleDetails" action="show" id="${the.id}">${the.title}</g:link>
                            <g:if test="${the.isInPackage(packageInstance)}">(✔)</g:if><g:else>(✘)</g:else>
                         </g:each>
                       </li>
                     </g:each>
                   </ul>
                   <span title="${t.availabilityStatusExplanation}">
                    ${message(code:'default.access.label', default:'Access')}: ${t.availabilityStatus.getI10n('value')}
                  </span>
                   <g:if test="${params.mode=='advanced'}">
                     <br/> ${message(code:'subscription.details.record_status', default:'Record Status')}: <g:xEditableRefData owner="${t}" field="status" config='TIPP Status'/>
                     <br/> ${message(code:'tipp.accessStartDate', default:'Access Start')}: <g:xEditable owner="${t}" type="date" field="accessStartDate" />
                     <br/> ${message(code:'tipp.accessEndDate', default:'Access End')}: <g:xEditable owner="${t}" type="date" field="accessEndDate" />
                   </g:if>
                </td>
                <td style="white-space: nowrap;vertical-align:top;">
                   <g:if test="${t.hostPlatformURL != null}">
                     <a href="${t.hostPlatformURL}">${t.platform?.name}</a>
                   </g:if>
                   <g:else>
                     ${t.platform?.name}
                   </g:else>
                </td>
                <td style="white-space: nowrap;vertical-align:top;">
                   <g:xEditableRefData owner="${t}" field="hybridOA" config='TitleInstancePackagePlatform.HybridOA'/>
                </td>
                <td style="white-space: nowrap;vertical-align:top;">
                  <g:each in="${t.title.ids}" var="id">
                    <g:if test="${id.identifier.ns.hide != true}">
                      <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                        ${id.identifier.ns.ns}: <a href="${id.identifier.value}">${message(code:'package.show.openLink', default:'Open Link')}</a>
                      </g:if>
                      <g:else>
                        ${id.identifier.ns.ns}:${id.identifier.value}
                      </g:else>
                      <br/>
                    </g:if>
                  </g:each>
                </td>

                <td style="white-space: nowrap">
                  ${message(code:'default.date.label', default:'Date')}: <g:xEditable owner="${t}" type="date" field="startDate" /><br/>
                  ${message(code:'tipp.volume', default:'Volume')}: <g:xEditable owner="${t}" field="startVolume" /><br/>
                  ${message(code:'tipp.issue', default:'Issue')}: <g:xEditable owner="${t}" field="startIssue" />
                </td>

                <td style="white-space: nowrap"> 
                   ${message(code:'default.date.label', default:'Date')}: <g:xEditable owner="${t}" type="date" field="endDate" /><br/>
                   ${message(code:'tipp.volume', default:'Volume')}: <g:xEditable owner="${t}" field="endVolume" /><br/>
                   ${message(code:'tipp.issue', default:'Issue')}: <g:xEditable owner="${t}" field="endIssue" />
                </td>
              </tr>

              <g:if test="${hasCoverageNote==true || params.mode=='advanced'}">
               <tr>
                  <td colspan="8">coverageNote: ${t.coverageNote}
                  <g:if test="${params.mode=='advanced'}">
                    <br/> ${message(code:'tipp.hostPlatformURL', default:'Host Platform URL')}: <g:xEditable owner="${t}" field="hostPlatformURL" />
                    <br/> ${message(code:'tipp.delayedOA', default:'Delayed OA')}: <g:xEditableRefData owner="${t}" field="delayedOA" config='TitleInstancePackagePlatform.DelayedOA'/> &nbsp;
                    ${message(code:'tipp.hybridOA', default:'Hybrid OA')}: <g:xEditableRefData owner="${t}" field="hybridOA" config='TitleInstancePackagePlatform.HybridOA'/> &nbsp;
                    ${message(code:'tipp.paymentType', default:'Payment')}: <g:xEditableRefData owner="${t}" field="payment" config='TitleInstancePackagePlatform.PaymentType'/> &nbsp;
                  </g:if>
                  </td>
                </tr>
              </g:if>

            </g:each>
            </tbody>
            </table>
          </g:form>
          

        <div class="pagination" style="text-align:center">
          <g:if test="${titlesList}" >
            <bootstrap:paginate  action="show" controller="packageDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_tipp_rows}" />
          </g:if>
        </div>



        <g:if test="${editable}">
        
        <g:form controller="ajax" action="addToCollection">
          <fieldset>
            <legend>${message(code:'package.show.title.add', default:'Add A Title To This Package')}</legend>
            <input type="hidden" name="__context" value="${packageInstance.class.name}:${packageInstance.id}"/>
            <input type="hidden" name="__newObjectClass" value="com.k_int.kbplus.TitleInstancePackagePlatform"/>
            <input type="hidden" name="__recip" value="pkg"/>

            <!-- N.B. this should really be looked up in the controller and set, not hard coded here -->
            <input type="hidden" name="status" value="com.k_int.kbplus.RefdataValue:29"/>

            <label>${message(code:'package.show.title.add.title', default:'Title To Add')}</label>
            <g:simpleReferenceTypedown class="input-xxlarge" style="width:350px;" name="title" baseClass="com.k_int.kbplus.TitleInstance"/><br/>
            <span class="help-block"></span>
            <label>${message(code:'package.show.title.add.platform', default:'Platform For Added Title')}</label>
            <g:simpleReferenceTypedown class="input-large" style="width:350px;" name="platform" baseClass="com.k_int.kbplus.Platform"/><br/>
            <span class="help-block"></span>
            <button type="submit" class="btn">${message(code:'package.show.title.add.submit', default:'Add Title...')}</button>
          </fieldset>
        </g:form>


        </g:if>

      </div>


    <g:render template="enhanced_select" contextPath="../templates" />
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[linkType:packageInstance?.class?.name,roleLinks:packageInstance?.orgs,parent:packageInstance.class.name+':'+packageInstance.id,property:'orgs',recip_prop:'pkg']}" />

    <r:script language="JavaScript">
      $(function(){
        $.fn.editable.defaults.mode = 'inline';
        $('.xEditableValue').editable();
      });
      function selectAll() {
        $('.bulkcheck').attr('checked', true);
      }

      function confirmSubmit() {
        if ( $('#bulkOperationSelect').val() === 'remove' ) {
          var agree=confirm("${message(code:'default.continue.confirm', default:'Are you sure you wish to continue?')}");
          if (agree)
            return true ;
          else
            return false ;
        }
      }
     
      <g:if test="${params.asAt && params.asAt.length() > 0}"> $(function() {
        document.body.style.background = "#fcf8e3";
      });</g:if>
    </r:script>

  </body>
</html>
