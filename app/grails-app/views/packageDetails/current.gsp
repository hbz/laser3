<%@ page import="com.k_int.kbplus.ApiSource; com.k_int.kbplus.Package" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'package', default:'Package Details')}</title>
    </head>
    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="packageDetails" action="index" text="${message(code:'package.show.all', default:'All Packages')}" />
            <semui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
        </semui:breadcrumbs>

        <semui:modeSwitch controller="packageDetails" action="show" params="${params}" />

        <semui:controlButtons>
            <semui:exportDropdown>
                <semui:exportDropdownItem>
                    <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
                </semui:exportDropdownItem>
                <semui:exportDropdownItem>
                    <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
                </semui:exportDropdownItem>

                <g:each in="${transforms}" var="transkey,transval">
                    <semui:exportDropdownItem>
                        <g:link class="item" action="show" id="${params.id}" params="${[format:'xml', transformId:transkey, mode:params.mode]}"> ${transval.name}</g:link>
                    </semui:exportDropdownItem>
                </g:each>
            </semui:exportDropdown>
            <g:render template="actions" />
        </semui:controlButtons>

        <h1 class="ui left aligned icon header"><semui:headerIcon />

            <g:if test="${editable}"><span id="packageNameEdit"
                        class="xEditableValue"
                        data-type="textarea"
                        data-pk="${packageInstance.class.name}:${packageInstance.id}"
                        data-name="name"
                        data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
            <g:else>${packageInstance.name}</g:else>
        </h1>

        <g:render template="nav" contextPath="." />

            <sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:link class="ui button" controller="announcement" action="index" params='[at:"Package Link: ${pkg_link_str}",as:"RE: Package ${packageInstance.name}"]'>${message(code:'package.show.announcement', default:'Mention this package in an announcement')}</g:link>
            </sec:ifAnyGranted>

            <g:if test="${forum_url != null}">
              &nbsp;<a href="${forum_url}">Discuss this package in forums</a> <a href="${forum_url}" title="Discuss this package in forums (new Window)" target="_blank"><i class="icon-share-alt"></i></a>
            </g:if>

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_PACKAGE_EDITOR">
        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges, 'flash':flash, 'model':packageInstance]}"/>
    </sec:ifAnyGranted>


  <semui:messages data="${flash}" />

  <semui:errors bean="${packageInstance}" />

    <div>

        <dl>
          <dt>${message(code:'title.search.offset.text', args:[offset+1,lasttipp,num_tipp_rows])} -
            <g:if test="${params.mode=='advanced'}">${message(code:'package.show.switchView.basic')} <g:link controller="packageDetails" action="current" params="${params+['mode':'basic']}">${message(code:'default.basic', default:'Basic')}</g:link></g:if>
                <g:else>${message(code:'package.show.switchView.advanced')} <g:link controller="packageDetails" action="current" params="${params+['mode':'advanced']}" button type="button" >${message(code:'default.advanced', default:'Advanced')}</g:link></g:else>
          </dt>
          <dd>

        <semui:filter>
            <g:form action="current" params="${params}" method="get" class="ui form">
                <input type="hidden" name="sort" value="${params.sort}">
                <input type="hidden" name="order" value="${params.order}">
                <div class="fields two">
                    <div class="field">
                        <label>${message(code:'package.compare.filter.title', default:'Filters - Title')}</label>
                        <input name="filter" value="${params.filter}"/>
                    </div>
                    <div class="field">
                        <label>${message(code:'tipp.coverageNote', default:'Coverage note')}</label>
                        <input name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
                    </div>
                </div>
                <div class="fields">
                    <div class="field">
                        <semui:datepicker label="package.compare.filter.coverage_startsBefore" name="startsBefore" value="${params.startsBefore}" />
                    </div>
                    <div class="field">
                        <semui:datepicker label="package.compare.filter.coverage_endsAfter" name="endsAfter" value="${params.endsAfter}" />
                    </div>
                    <div class="field">
                        <label>&nbsp;</label>
                        <input type="submit" class="ui secondary button" value="${message(code:'package.compare.filter.submit.label', default:'Filter Results')}" />
                    </div>
                </div>


            </g:form>
        </semui:filter>

              <g:if test="${editable}">

                  <semui:form>
                      <g:form class="ui form" controller="ajax" action="addToCollection">

                          <legend><h3 class="ui header">${message(code:'package.show.title.add', default:'Add A Title To This Package')}</h3></legend>
                          <input type="hidden" name="__context" value="${packageInstance.class.name}:${packageInstance.id}"/>
                          <input type="hidden" name="__newObjectClass" value="com.k_int.kbplus.TitleInstancePackagePlatform"/>
                          <input type="hidden" name="__recip" value="pkg"/>

                          <!-- N.B. this should really be looked up in the controller and set, not hard coded here -->
                          <input type="hidden" name="status" value="com.k_int.kbplus.RefdataValue:29"/>
                          <div class="two fluid fields">
                              <div class="field">
                                  <label>${message(code:'package.show.title.add.title', default:'Title To Add')}</label>
                                  <g:simpleReferenceTypedown class="input-xxlarge" style="width:350px;" name="title" baseClass="com.k_int.kbplus.TitleInstance"/>
                              </div>
                              <div class="field">
                                  <label>${message(code:'package.show.title.add.platform', default:'Platform For Added Title')}</label>
                                  <g:simpleReferenceTypedown class="input-large" style="width:350px;" name="platform" baseClass="com.k_int.kbplus.Platform"/>
                              </div>
                          </div>
                          <button type="submit" class="ui button">${message(code:'package.show.title.add.submit', default:'Add Title...')}</button>

                      </g:form>
                  </semui:form>

              </g:if>

        <g:form action="packageBatchUpdate" params="${[id:packageInstance?.id]}">
            <g:if test="${editable}">
          <table class="ui celled la-table table ignore-floatThead la-bulk-header">

            <thead>
            <tr>

              <th>
                <g:if test="${editable}"><input id="select-all" type="checkbox" name="chkall" onClick="javascript:selectAll();"/></g:if>
              </th>

              <th colspan="7">

                  <select id="bulkOperationSelect" name="bulkOperation" class="input-xxlarge">
                    <option value="edit">${message(code:'package.show.batch.edit.label', default:'Batch Edit Selected Rows Using the following values')}</option>
                    <option value="remove">${message(code:'package.show.batch.remove.label', default:'Batch Remove Selected Rows')}</option>
                  </select>
                  <br/>
                  <table class="ui celled la-table table">
                    <tr>
                      <td>${message(code:'subscription.details.coverageStartDate', default:'Coverage Start Date')}: <semui:simpleHiddenValue id="bulk_start_date" name="bulk_start_date" type="date"/>
                          <input type="checkbox" name="clear_start_date"/> (${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.startVolume', default:'Start Volume')}: <semui:simpleHiddenValue id="bulk_start_volume" name="bulk_start_volume" />
                          <input type="checkbox" name="clear_start_volume"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.startIssue', default:'Start Issue')}: <semui:simpleHiddenValue id="bulk_start_issue" name="bulk_start_issue"/>
                          <input type="checkbox" name="clear_start_issue"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                    </tr>
                    <tr>
                      <td>${message(code:'subscription.details.coverageEndDate', default:'Coverage End Date')}:  <semui:simpleHiddenValue id="bulk_end_date" name="bulk_end_date" type="date"/>
                          <input type="checkbox" name="clear_end_date"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.endVolume', default:'End Volume')}: <semui:simpleHiddenValue id="bulk_end_volume" name="bulk_end_volume"/>
                          <input type="checkbox" name="clear_end_volume"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.endIssue', default:'End Issue')}: <semui:simpleHiddenValue id="bulk_end_issue" name="bulk_end_issue"/>
                          <input type="checkbox" name="clear_end_issue"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                    </tr>
                    <tr>
                      <td>${message(code:'tipp.coverageDepth', default:'Coverage Depth')}: <semui:simpleHiddenValue id="bulk_coverage_depth" name="bulk_coverage_depth"/>
                          <input type="checkbox" name="clear_coverage_depth"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.coverageNote', default:'Coverage Note')}: <semui:simpleHiddenValue id="bulk_coverage_note" name="bulk_coverage_note"/>
                          <input type="checkbox" name="clear_coverage_note"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                      <td>${message(code:'tipp.embargo', default:'Embargo')}:  <semui:simpleHiddenValue id="bulk_embargo" name="bulk_embargo"/>
                          <input type="checkbox" name="clear_embargo"/>(${message(code:'package.show.checkToClear', default:'Check to clear')})</td>
                    </tr>
                  </table>
                  <button name="BatchSelectedBtn" value="on" onClick="return confirmSubmit()" class="ui button">${message(code:'default.button.apply_batch.label')} (${message(code:'default.selected.label')})</button>
                  <button name="BatchAllBtn" value="on" onClick="return confirmSubmit()" class="ui button">${message(code:'default.button.apply_batch.label')} (${message(code:'package.show.batch.allInFL', default:'All in filtered list')})</button>

              </th>
            </tr>

            </thead>
                <tbody></tbody>
          </table>
            </g:if>
            <br>
            <br>

           <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
                <thead>
                    <tr>
                        <th></th>
                        <th></th>
                        <g:sortableColumn  params="${params}" property="tipp.title.sortTitle" title="${message(code:'title.label', default:'Title')}" />
                        <th>${message(code:'tipp.coverage')}</th>
                        <th>${message(code:'tipp.access')}</th>
                        <th>${message(code:'tipp.coverageDepth', default:'Coverage Depth')}</th>
                    </tr>
                    <tr>
                        <th colspan="3" rowspan="2"></th>
                        <th>${message(code:'default.from')}</th>
                        <th>${message(code:'default.from')}</th>
                        <th rowspan="2"></th>
                    </tr>
                    <tr>
                        <th>${message(code:'default.to')}</th>
                        <th>${message(code:'default.to')}</th>
                    </tr>
                </thead>
                <tbody>

            <g:set var="counter" value="${offset+1}" />
            <g:each in="${titlesList}" var="t">
              <g:set var="hasCoverageNote" value="${t.coverageNote?.length() > 0}" />
              <tr>
                <td ${hasCoverageNote==true?'rowspan="2"':''}><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${t.id}" class="bulkcheck"/></g:if></td>
                <td ${hasCoverageNote==true?'rowspan="2"':''}>${counter++}</td>
                <td>
                    <semui:listIcon type="${t.title.type.getI10n('value')}"/>
                   <strong><g:link controller="titleDetails" action="show" id="${t.title.id}">${t.title.title}</g:link></strong>
                    <br>
                   <g:link controller="tipp" action="show" id="${t.id}">${message(code:'tipp.label', default:'TIPP')}</g:link>
                    <br>

                        <g:each in="${t.title.ids.sort{it.identifier.ns.ns}}" var="id">
                            <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                                <span class="ui small teal image label">
                                    ${id.identifier.ns.ns}: <div class="detail"><a href="${id.identifier.value}">${message(code:'package.show.openLink', default:'Open Link')}</a></div>
                                    ${id.identifier.ns.ns}: <div class="detail"><a href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code:'package.show.openLink', default:'Open Link')}</a></div>
                                </span>
                            </g:if>
                            <g:else>
                                <span class="ui small teal image label">
                                    ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                                </span>
                            </g:else>
                        </g:each>

                    <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                            var="gokbAPI">
                        <g:if test="${t?.gokbId}">
                            <a target="_blank" href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + t?.gokbId : '#'}"><i
                                    title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                        </g:if>
                    </g:each>
                    <div class="ui list">
                        <div class="item"  title="${t.availabilityStatusExplanation}"><b>${message(code:'default.access.label', default:'Access')}:</b> ${t.availabilityStatus?.getI10n('value')}</div>
                        <div class="item"><b>${message(code:'title.type.label')}:</b> ${t.title.type.getI10n('value')}</div>
                        <div class="item"><b>${message(code:'default.status.label', default:'Status')}:</b>  <semui:xEditableRefData owner="${t}" field="status" config="TIPP Status"/></div>
                        <div class="item"><b>${message(code:'tipp.platform', default:'Platform')}: </b>
                            <g:if test="${t?.platform.name}">
                                ${t?.platform.name}
                            </g:if>
                            <g:else>${message(code:'default.unknown')}</g:else>
                            <g:if test="${t?.platform.name}">
                                <g:link class="ui icon mini  button la-url-button la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.changePlattform')}" controller="platform" action="show" id="${t?.platform.id}"><i class="pencil alternate icon"></i></g:link>
                            </g:if>
                            <g:if test="${t.hostPlatformURL}">
                                <a class="ui icon mini blue button la-url-button la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.callUrl')}" href="${t.hostPlatformURL.contains('http') ? t.hostPlatformURL :'http://'+t.hostPlatformURL}" target="_blank"><i class="share square icon"></i></a>
                            </g:if>
                        </div>
                    </div>
                </td>

                <td>
                    <!-- von -->
                    <semui:xEditable owner="${t}" type="date" field="startDate" /><br/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.volume')}"></i>
                    <semui:xEditable owner="${t}" field="startVolume" /><br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.issue')}"></i>
                    <semui:xEditable owner="${t}" field="startIssue" />
                    <semui:dateDevider/>
                    <!-- bis -->
                    <semui:xEditable owner="${t}" type="date" field="endDate" /><br/>
                    <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.volume')}"></i>
                    <semui:xEditable owner="${t}" field="endVolume" /><br>
                    <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.issue')}"></i>
                    <semui:xEditable owner="${t}" field="endIssue" />
                </td>
                <td>
                    <!-- von -->
                    <semui:xEditable owner="${t}" type="date" field="accessStartDate" />
                    <semui:dateDevider/>
                    <!-- bis -->
                    <semui:xEditable owner="${t}" type="date" field="accessEndDate" />
                </td>
                <td>
                  <semui:xEditable owner="${t}" field="coverageDepth" />
                </td>
              </tr>

              <g:if test="${hasCoverageNote==true}">
                <tr>
                    <td colspan="6"><b>${message(code:'tipp.coverageNote', default:'Coverage Note')}:</b> ${t.coverageNote}</td>
                </tr>
              </g:if>

            </g:each>

                </tbody>

            </table>

        </g:form>
          </dd>
        </dl>


          <g:if test="${titlesList}" >
            <semui:paginate action="current" controller="packageDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_tipp_rows}" />
          </g:if>

      </div>


    <%-- <g:render template="enhanced_select" contextPath="../templates" /> --%>
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[roleLinks:packageInstance?.orgs,parent:packageInstance.class.name+':'+packageInstance.id,property:'orgs',recip_prop:'pkg']}" />

    <r:script language="JavaScript">
      $(function(){
        $.fn.editable.defaults.mode = 'inline';
        $('.xEditableValue').editable();
      });
      function selectAll() {
         $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);

        //$('#select-all').is( ':checked' )? $('.bulkcheck').attr('checked', false) : $('.bulkcheck').attr('checked', true);
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

    </r:script>

  </body>
</html>
