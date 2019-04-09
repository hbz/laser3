<%@ page import="com.k_int.kbplus.Package; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.ApiSource;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'Title Instance')}"/>
    <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="default.edit.label" args="[entityName]"/></title>
</head>
  <body>

      <semui:breadcrumbs>
          <semui:crumb controller="title" action="list" message="menu.public.all_titles" />
          <semui:crumb class="active" text="${message(code:'title.title.label')}: ${ti.title}" />
      </semui:breadcrumbs>

  <h1 class="ui left aligned icon header">
            <semui:headerTitleIcon type="${ti.type.('value')}"/>

            <% /*
            <g:if test="${editable}"><span id="titleEdit"
                                     class="xEditableValue"
                                     data-type="textarea"
                                     data-pk="${ti.class.name}:${ti.id}"
                                     data-name="title"
                                     data-url='<g:createLink controller="ajax" action="editableSetValue"/>'
                                     data-original-title="${ti.title}">${ti.title}</span></g:if>
             <g:else>${ti.title}</g:else> */ %>
            ${ti.title}
            <g:if test="${ti.status?.value && ti.status.value != 'Current'}">
                <span class="badge badge-error" style="vertical-align:middle;">${ti.status.getI10n('value')}</span>
            </g:if>
        </h1>

        <g:render template="nav" />

        <g:render template="/templates/meta/identifier" model="${[object: ti, editable: editable]}" />

        <semui:messages data="${flash}" />

        <div class="ui grid">

            <div class="twelve wide column">

                  <div class="item"><b>${message(code:'title.type.label')}:</b> ${ti.type.getI10n('value')}</div>

                <g:if test="${ti instanceof com.k_int.kbplus.BookInstance}">
                    <div class="item"><b>${message(code:'title.firstAuthor.label')}:</b> ${ti?.firstAuthor}</div>
                    <div class="item"><b>${message(code:'title.firstEditor.label')}:</b> ${ti?.firstEditor}</div>
                    <div class="item"><b>${message(code:'title.editionNumber.label')}:</b> ${ti?.editionNumber}</div>
                    <div class="item"><b>${message(code:'title.editionStatement.label')}:</b> ${ti?.editionStatement}</div>
                    <div class="item"><b>${message(code:'title.volume.label')}:</b> ${ti?.volume}</div>
                    <div class="item"><b>${message(code:'title.dateFirstInPrint.label')}:</b> <g:formatDate formatName="default.date.format.notime" date="${ti?.dateFirstInPrint}"/></div>
                    <div class="item"><b>${message(code:'title.dateFirstOnline.label')}:</b> <g:formatDate formatName="default.date.format.notime" date="${ti?.dateFirstOnline}"/></div>
                </g:if>

                <div class="item"><b>${message(code:'default.status.label')}:</b> <semui:xEditableRefData owner="${ti}" field="status" config='${RefdataCategory.TI_STATUS}'/></div>

            </div><!-- .twelve -->

            <div class="twelve wide column">

              <g:each in="${duplicates}" var="entry">
                  <bootstrap:alert class="alert-info">
                      ${message(code:'title.edit.duplicate.warn', args:[entry.key])}:
                      <ul>
                          <g:each in ="${entry.value}" var="dup_title">
                              <li><g:link controller='title' action='show' id="${dup_title.id}">${dup_title.title}</g:link></li>
                          </g:each>
                      </ul>
                  </bootstrap:alert>
              </g:each>

            </div><!-- .twelve -->
        </div><!-- .grid -->

          <h3 class="ui header">${message(code:'title.edit.orglink')}</h3>

            <table class="ui celled la-table table ">
              <thead>
                <tr>
                  %{--<th>${message(code:'title.edit.component_id.label')}</th>--}%
                  <th>${message(code:'template.orgLinks.name')}</th>
                  <th>${message(code:'template.orgLinks.role')}</th>
                  <th>${message(code:'title.edit.orglink.from')}</th>
                  <th>${message(code:'title.edit.orglink.to')}</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${ti.orgs}" var="org">
                  <tr>
                    %{--<td>${org.org.id}</td>--}%
                    <td><g:link controller="organisation" action="show" id="${org.org.id}">${org.org.name}</g:link></td>
                    <td>${org?.roleType?.getI10n("value")}</td>
                    <td>
                      <semui:xEditable owner="${org}" type="date" field="startDate"/>
                    </td>
                    <td>
                      <semui:xEditable owner="${org}" type="date" field="endDate"/>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>

        %{--
            <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:ti?.orgs,editmode:editable]}" />

            <g:render template="orgLinksModal"
                contextPath="../templates"
                model="${[linkType:ti?.class?.name,roleLinks:ti?.orgs,parent:ti.class.name+':'+ti.id,property:'orgLinks',recip_prop:'title']}" />
        --}%
            <h3 class="ui header">${message(code: 'title.show.history.label')}</h3>

            <table class="ui celled la-table table">
              <thead>
                <tr>
                  <th>${message(code: 'title.show.history.date')}</th>
                  <th>${message(code: 'title.show.history.from')}</th>
                  <th>${message(code: 'title.show.history.to')}</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${titleHistory}" var="th">
                  <tr>
                    <td><g:formatDate date="${th.eventDate}" formatName="default.date.format.notime"/></td>
                    <td>
                      <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='from'}">
                          <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br/>
                        </g:if>
                      </g:each>
                    </td>
                    <td>
                      <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='to'}">
                          <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br/>
                        </g:if>
                      </g:each>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>
            <g:if test="${ti.getIdentifierValue('originediturl') != null}">
              <span class="pull-right">
                ${message(code: 'title.show.gokb')} <a href="${ti.getIdentifierValue('originediturl')}">GOKb</a>.
              </span>
            </g:if>

  <h3 class="ui left aligned icon header">${message(code:'title.edit.tipp')}
  <semui:totalNumber total="${ti.tipps.size()}"/>
  </h3>
%{--<% /*
              <table class="ui celled la-table table">
                  <thead>
                  <tr>
                      <th>${message(code:'tipp.startDate')}</th><th>${message(code:'tipp.startVolume')}</th><th>${message(code:'tipp.startIssue')}</th>
                      <th>${message(code:'tipp.endDate')}</th><th>${message(code:'tipp.endVolume')}</th><th>${message(code:'tipp.endIssue')}</th><th>${message(code:'tipp.coverageDepth')}</th>
                      <th>${message(code:'tipp.platform')}</th><th>${message(code:'tipp.package')}</th><th>${message(code:'default.actions')}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <g:each in="${ti.tipps}" var="t">
                      <tr>
                          <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${t.startDate}"/></td>
                          <td>${t.startVolume}</td>
                          <td>${t.startIssue}</td>
                          <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${t.endDate}"/></td>
                          <td>${t.endVolume}</td>
                          <td>${t.endIssue}</td>
                          <td>${t.coverageDepth}</td>
                          <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                          <td><g:link controller="package" action="show" id="${t.pkg.id}">${t.pkg.name}</g:link></td>
                          <td><g:link controller="tipp" action="show" id="${t.id}">${message(code:'title.edit.tipp.show', default:'Full TIPP record')}</g:link></td>
                      </tr>
                  </g:each>
                  </tbody>
              </table>
*/ %>

            --}%%{--<g:form id="${params.id}" controller="title" action="batchUpdate" class="ui form"> BULK_REMOVE --}%%{--
              <table class="ui celled la-rowspan table">
                  <thead>
                    <tr>
                  --}%%{--<th rowspan="2"></th> BULK_REMOVE --}%%{--
                      <th>${message(code:'tipp.platform')}</th><th>${message(code:'tipp.package')}</th>
                      <th>${message(code:'tipp.start')}</th>
                      <th>${message(code:'tipp.end')}</th>
                      <th>${message(code:'tipp.start')}</th>
                      <th>${message(code:'tipp.end')}</th>
                      <th>${message(code:'tipp.coverageDepth')}</th>
                      <th>${message(code:'default.actions')}</th>
                    </tr>
                    <tr>
                      <th colspan="6">${message(code:'tipp.coverageNote')}</th>
                    </tr>
                  </thead>

                --}%%{-- BULK_REMOVE
                <g:if test="${editable}">
                  <tr>
                    <td rowspan="2"><input type="checkbox" name="checkall" onClick="javascript:$('.bulkcheck').attr('checked', true);"/></td>
                    <td colspan="2"><button class="ui button" type="submit" value="Go" name="BatchEdit">${message(code:'default.button.apply_batch.label')}</button></td>
                    <td>

                        <semui:datepicker label="title.show.history.date" id="bulk_start_date" name="bulk_start_date" value="${params.bulk_start_date}" />
                       - <input type="checkbox" id="clear_start_date" name="clear_start_date"/> (${message(code:'title.edit.tipp.clear')})

                        <div class="field">
                            <label>${message(code:'tipp.volume')}</label>
                            <semui:simpleHiddenValue id="bulk_start_volume" name="bulk_start_volume"/>
                            - <input type="checkbox" name="clear_start_volume"/> (${message(code:'title.edit.tipp.clear')})
                        </div>

                        <div class="field">
                            <label>${message(code:'tipp.issue')}</label>
                            <semui:simpleHiddenValue id="bulk_start_issue" name="bulk_start_issue"/>
                            - <input type="checkbox" name="clear_start_issue"/> (${message(code:'title.edit.tipp.clear')})
                        </div>
                    </td>
                    <td>

                        <semui:datepicker label="title.show.history.date" id="bulk_end_date" name="bulk_end_date" value="${params.bulk_end_date}" />
                       - <input type="checkbox" id="clear_end_date" name="clear_end_date"/> (${message(code:'title.edit.tipp.clear')})

                        <br/>

                        <div class="field">
                            <label>${message(code:'tipp.volume')}</label>
                            <semui:simpleHiddenValue id="bulk_end_volume" name="bulk_end_volume"/>
                            - <input type="checkbox" name="clear_end_volume"/> (${message(code:'title.edit.tipp.clear')})
                        </div>

                        <div class="field">
                            <label>${message(code:'tipp.issue')}</label>
                            <semui:simpleHiddenValue id="bulk_end_issue" name="bulk_end_issue"/>
                            - <input type="checkbox" name="clear_end_issue"/> (${message(code:'title.edit.tipp.clear')})
                        </div>

                    </td>
                    <td>
                        <div class="field">
                            <label>&nbsp;</label>
                            <semui:simpleHiddenValue id="bulk_coverage_depth" name="bulk_coverage_depth"/>
                            - <input type="checkbox" name="clear_coverage_depth"/> (${message(code:'title.edit.tipp.clear')})
                        </div>
                    </td>
                    <td/>
                  </tr>
                  <tr>
                    <td colspan="6">
                        <div class="field">
                            <label>${message(code:'title.edit.tipp.bulk_notes_change')}</label>
                            <semui:simpleHiddenValue id="bulk_coverage_note" name="bulk_coverage_note"/>
                            - <input type="checkbox" name="clear_coverage_note"/> (${message(code:'title.edit.tipp.clear')})
                        </div>
                        <div class="field">
                            <label>${message(code:'title.edit.tipp.bulk_platform_change')}</label>
                            <semui:simpleHiddenValue id="bulk_hostPlatformURL" name="bulk_hostPlatformURL"/>
                            - <input type="checkbox" name="clear_hostPlatformURL"/> (${message(code:'title.edit.tipp.clear')})
                        </div>
                    </td>
                  </tr>
                </g:if>
                --}%%{--
  
                <g:each in="${ti.tipps}" var="t">
                  <tr>
                    --}%%{--<td rowspan="2"><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${t.id}" class="bulkcheck"/></g:if></td> BULK_REMOVE --}%%{--
                    <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                    <td>
                        <div class="la-flexbox">
                            <i class="icon gift scale la-list-icon"></i>
                            <g:link controller="package" action="show" id="${t.pkg.id}">${t.pkg.name}</g:link>
                        </div>
                    </td>
  
                    <td>${message(code:'title.show.history.date')}: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${t.startDate}"/><br/>
                    ${message(code:'tipp.volume')}: ${t.startVolume}<br/>
                    ${message(code:'tipp.issue')}: ${t.startIssue}</td>
                    <td>${message(code:'title.show.history.date')}: <g:formatDate format="${message(code:'default.date.format.notime')}" date="${t.endDate}"/><br/>
                    ${message(code:'tipp.volume')}: ${t.endVolume}<br/>
                    ${message(code:'tipp.issue')}: ${t.endIssue}</td>
                    <td>${t.coverageDepth}</td>
                    <td><g:link controller="tipp" action="show" id="${t.id}">${message(code:'title.edit.tipp.show')}</g:link></td>
                  </tr>
                  <tr>
                    <td colspan="6">${message(code:'tipp.coverageNote')}: ${t.coverageNote?:"${message(code:'title.edit.tipp.no_note', default: 'No coverage note')}"}<br/>
                                    ${message(code:'tipp.hostPlatformURL')}: ${t.hostPlatformURL?:"${message(code:'title.edit.tipp.no_url', default: 'No Host Platform URL')}"}</td>
                  </tr>
                </g:each>
              </table>
            --}%%{--</g:form> BULK_REMOVE--}%

            %{--NEW VIEW FOR TIPPS--}%

  <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
      <thead>
      <tr>
          <th></th>
          <th>${message(code:'package')}</th>
          <th class="two wide">${message(code:'tipp.coverage')}</th>
          <th class="two wide">${message(code:'tipp.access')}</th>
          <th class="two wide">${message(code:'tipp.coverageDepth', default:'Coverage Depth')}</th>
      </tr>
      <tr>
          <th colspan="2" rowspan="2"></th>
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

      <g:set var="counter" value="${1}" />
      <g:each in="${ti.tipps.sort{it?.pkg?.name}}" var="t">
          <g:set var="hasCoverageNote" value="${t.coverageNote?.length() > 0}" />
          <tr>
              <td ${hasCoverageNote==true?'rowspan="2"':''}>${counter++}</td>
              <td>
                      <div class="la-flexbox">
                          <i class="icon gift scale la-list-icon"></i>
                          <g:link controller="package" action="show" id="${t?.pkg?.id}">${t?.pkg?.name}</g:link>
                      </div>
                  <br>
                  <g:link controller="tipp" action="show" id="${t.id}">${message(code:'platform.show.full_tipp', default:'Full TIPP Details')}</g:link>
              &nbsp;&nbsp;&nbsp;
                  <g:each in="${com.k_int.kbplus.ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                          var="gokbAPI">
                      <g:if test="${t?.gokbId}">
                          <a target="_blank" href="${gokbAPI.baseUrl ? gokbAPI.baseUrl + '/gokb/resource/show/' + t?.gokbId : '#'}"><i
                                  title="${gokbAPI.name} Link" class="external alternate icon"></i></a>
                      </g:if>
                  </g:each>
                  <br>

                  <g:each in="${t.title.ids.sort{it.identifier.ns.ns}}" var="id">
                      <g:if test="${id.identifier.ns.ns == 'originediturl'}">
                          <span class="ui small teal image label">
                              ${id.identifier.ns.ns}: <div class="detail"><a href="${id.identifier.value}">${message(code:'package.show.openLink', default:'Open Link')}</a></div>
                          </span>
                          <span class="ui small teal image label">
                              ${id.identifier.ns.ns}: <div class="detail"><a href="${id.identifier.value.toString().replace("resource/show", "public/packageContent")}">${message(code:'package.show.openLink', default:'Open Link')}</a></div>
                          </span>
                      </g:if>
                      <g:else>
                          <span class="ui small teal image label">
                              ${id.identifier.ns.ns}: <div class="detail">${id.identifier.value}</div>
                          </span>
                      </g:else>
                  </g:each>


                  <div class="ui list">
                      <div class="item"  title="${t.availabilityStatusExplanation}"><b>${message(code:'default.access.label', default:'Access')}:</b> ${t.availabilityStatus?.getI10n('value')}</div>
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



            <br><br>

  <r:script language="JavaScript">

    $(function(){
      <g:if test="${editable}">

      $("#addOrgSelect").select2({
        placeholder: "Search for an org...",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
        },
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
          url: "<g:createLink controller='ajax' action='lookup'/>",
          dataType: 'json',
          data: function (term, page) {
              return {
                  q: term, // search term
                  page_limit: 10,
                  baseClass:'com.k_int.kbplus.Org'
              };
          },
          results: function (data, page) {
            return {results: data.values};
          }
        }
      });

      $("#orgRoleSelect").select2({
        placeholder: "Search for an role...",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note', default:'Please enter 1 or more character')}";
        },
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
          url: "<g:createLink controller='ajax' action='lookup'/>",
          dataType: 'json',
          data: function (term, page) {
              return {
                  q: term, // search term
                  page_limit: 10,
                  baseClass:'com.k_int.kbplus.RefdataValue'
              };
          },
          results: function (data, page) {
            return {results: data.values};
          }
        }
      });
  </g:if>
      });

      function validateAddOrgForm() {
        var org_name=$("#addOrgSelect").val();
        var role=$("#orgRoleSelect").val();

        if( !org_name || !role ){
          return false;
        }
        return true;
      }
  </r:script>

  </body>
</html>
