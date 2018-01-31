<%@ page import="com.k_int.kbplus.Package" %>
<%@ page import="com.k_int.kbplus.Package;com.k_int.kbplus.RefdataCategory" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'Title Instance')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>
  <body>

      <semui:breadcrumbs>
          <semui:crumb class="active" text="${message(code:'title.title.label')}: ${ti.title}" />
      </semui:breadcrumbs>

        <h1 class="ui header">
            <semui:editableLabel editable="${editable}" />
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

        <semui:meta>
            <div class="inline-lists">
                <g:if test="${ti.globalUID}">
                    <dl>
                        <dt><g:message code="title.globalUID.label" default="Global UID" /></dt>
                        <dd>
                            <g:fieldValue bean="${ti}" field="globalUID"/>
                        </dd>
                    </dl>
                </g:if>

                <dl>
                    <dt>${message(code: 'title.identifiers.label')}</dt>
                    <dd>
                    <table class="ui celled table">
                        <thead>
                        <tr>
                            <th>${message(code:'title.edit.component_id.label')}</th>
                            <th>${message(code:'title.edit.namespace.label')}</th>
                            <th>${message(code:'identifier.label')}</th>
                            <th>${message(code:'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${ti.ids}" var="io">
                            <tr>
                                <td>${io.id}</td>
                                <td>${io.identifier.ns.ns}</td>
                                <td>${io.identifier.value}</td>
                                <td><g:if test="${editable}"><g:link controller="ajax" action="deleteThrough" params='${[contextOid:"${ti.class.name}:${ti.id}",contextProperty:"ids",targetOid:"${io.class.name}:${io.id}"]}'>${message(code:'title.edit.identifier.delete')}</g:link></g:if></td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>

                    <g:if test="${editable}">
                        <semui:formAddIdentifier owner="${ti}" buttonText="${message(code:'title.edit.identifier.select.add')}"
                                                 uniqueCheck="yes" uniqueWarningText="${message(code:'title.edit.duplicate.warn.list')}">
                            ${message(code:'identifier.select.text', args:['eISSN:2190-9180'])}
                        </semui:formAddIdentifier>
                    </g:if>
                    </dd>
                </dl>

            </div>
        </semui:meta>

        <semui:messages data="${flash}" />

        <div class="ui grid">

            <div class="twelve wide column">

              <h3 class="ui header">${message(code:'default.status.label')}:
                <semui:xEditableRefData owner="${ti}" field="status" config='${RefdataCategory.TI_STATUS}'/>
              </h3>

            </div><!-- .eight -->

            <div class="twelve wide column">

              <g:each in="${duplicates}" var="entry">
                  <bootstrap:alert class="alert-info">
                      ${message(code:'title.edit.duplicate.warn', args:[entry.key])}:
                      <ul>
                          <g:each in ="${entry.value}" var="dup_title">
                              <li><g:link controller='titleDetails' action='show' id="${dup_title.id}">${dup_title.title}</g:link></li>
                          </g:each>
                      </ul>
                  </bootstrap:alert>
              </g:each>

            </div><!-- .eight -->
        </div><!-- .grid -->

          <h3 class="ui header">${message(code:'title.edit.orglink')}</h3>

            <table class="ui celled table">
              <thead>
                <tr>
                  <th>${message(code:'title.edit.component_id.label')}</th>
                  <th>${message(code:'template.orgLinks.name')}</th>
                  <th>${message(code:'template.orgLinks.role')}</th>
                  <th>${message(code:'title.edit.orglink.from')}</th>
                  <th>${message(code:'title.edit.orglink.to')}</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${ti.orgs}" var="org">
                  <tr>
                    <td>${org.org.id}</td>
                    <td><g:link controller="organisations" action="show" id="${org.org.id}">${org.org.name}</g:link></td>
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

            <g:render template="orgLinks" contextPath="../templates" model="${[roleLinks:ti?.orgs,editmode:editable]}" />

            <g:render template="orgLinksModal"
                contextPath="../templates"
                model="${[linkType:ti?.class?.name,roleLinks:ti?.orgs,parent:ti.class.name+':'+ti.id,property:'orgLinks',recip_prop:'title']}" />

            <h3 class="ui header">${message(code: 'title.show.history.label')}</h3>

            <table class="ui extra table">
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
                          <g:link controller="titleDetails" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br/>
                        </g:if>
                      </g:each>
                    </td>
                    <td>
                      <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='to'}">
                          <g:link controller="titleDetails" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br/>
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

            <h3 class="ui header">${message(code:'title.edit.tipp')}</h3>

              <table class="ui extra table">
                  <thead>
                  <tr>
                      <th>${message(code:'tipp.startDate')}</th><th>${message(code:'tipp.startVolume')}</th><th>${message(code:'tipp.startIssue')}</th>
                      <th>${message(code:'tipp.endDate')}</th><th>${message(code:'tipp.endVolume')}</th><th>${message(code:'tipp.endIssue')}</th><th>${message(code:'tipp.coverageDepth')}</th>
                      <th>${message(code:'tipp.platform')}</th><th>${message(code:'tipp.package')}</th><th>${message(code:'title.edit.actions.label')}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <g:each in="${ti.tipps}" var="t">
                      <tr>
                          <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${t.startDate}"/></td>
                          <td>${t.startVolume}</td>
                          <td>${t.startIssue}</td>
                          <td><g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${t.endDate}"/></td>
                          <td>${t.endVolume}</td>
                          <td>${t.endIssue}</td>
                          <td>${t.coverageDepth}</td>
                          <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                          <td><g:link controller="packageDetails" action="show" id="${t.pkg.id}">${t.pkg.name}</g:link></td>
                          <td><g:link controller="tipp" action="show" id="${t.id}">${message(code:'title.edit.tipp.show', default:'Full TIPP record')}</g:link></td>
                      </tr>
                  </g:each>
                  </tbody>
              </table>

            <g:form id="${params.id}" controller="titleDetails" action="batchUpdate" class="ui form">
              <table class="ui extra table">
                  <thead>
                    <tr>
                      <th rowspan="2"></th>
                      <th>${message(code:'tipp.platform')}</th><th>${message(code:'tipp.package')}</th>
                      <th>${message(code:'tipp.start')}</th>
                      <th>${message(code:'tipp.end')}</th>
                      <th>${message(code:'tipp.coverageDepth')}</th>
                      <th>${message(code:'title.edit.actions.label')}</th>
                    </tr>
                    <tr>
                      <th colspan="6">${message(code:'tipp.coverageNote')}</th>
                    </tr>
                  </thead>
                <g:if test="${editable}">
                  <tr>
                    <td rowspan="2"><input type="checkbox" name="checkall" onClick="javascript:$('.bulkcheck').attr('checked', true);"/></td>
                    <td colspan="2"><button class="ui button" type="submit" value="Go" name="BatchEdit">${message(code:'title.edit.tipp.clear')}</button></td>
                    <td>

                        <semui:datepicker label="title.show.history.date" name="bulk_start_date" value="${params.bulk_start_date}" />
                       - <input type="checkbox" name="clear_start_date"/> (${message(code:'title.edit.tipp.clear')})

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

                        <semui:datepicker label="title.show.history.date" name="bulk_end_date" value="${params.bulk_end_date}" />
                       - <input type="checkbox" name="clear_end_date"/> (${message(code:'title.edit.tipp.clear')})

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
  
                <g:each in="${ti.tipps}" var="t">
                  <tr>
                    <td rowspan="2"><g:if test="${editable}"><input type="checkbox" name="_bulkflag.${t.id}" class="bulkcheck"/></g:if></td>
                    <td><g:link controller="platform" action="show" id="${t.platform.id}">${t.platform.name}</g:link></td>
                    <td><g:link controller="packageDetails" action="show" id="${t.pkg.id}">${t.pkg.name}</g:link></td>
  
                    <td>${message(code:'title.show.history.date')}: <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${t.startDate}"/><br/>
                    ${message(code:'tipp.volume')}: ${t.startVolume}<br/>
                    ${message(code:'tipp.issue')}: ${t.startIssue}</td>
                    <td>${message(code:'title.show.history.date')}: <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${t.endDate}"/><br/>
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
            </g:form>

  <r:script language="JavaScript">

    $(function(){
      <g:if test="${editable}">

      $("#addOrgSelect").select2({
        placeholder: "Search for an org...",
        minimumInputLength: 1,
        formatInputTooShort: function () {
            return "${message(code:'select2.minChars.note', default:'Pleaser enter 1 or more character')}";
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
            return "${message(code:'select2.minChars.note', default:'Pleaser enter 1 or more character')}";
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
