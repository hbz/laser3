<%@ page import="com.k_int.kbplus.Package;com.k_int.kbplus.RefdataCategory;org.springframework.web.servlet.support.RequestContextUtils" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'package', default: 'Package')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'package', default:'Package Details')}</title>
    </head>
    <body>
    <g:set var="locale" value="${RequestContextUtils.getLocale(request)}" />

    <semui:modeSwitch controller="packageDetails" action="show" params="${params}"/>

    <semui:breadcrumbs>
        <semui:crumb controller="packageDetails" action="index" message="package.show.all" />
        <semui:crumb class="active" text="${packageInstance.name}" />
    </semui:breadcrumbs>

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

    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_PACKAGE_EDITOR">
        <g:render template="/templates/pendingChanges" model="${['pendingChanges': pendingChanges, 'flash':flash, 'model':packageInstance]}"/>
    </sec:ifAnyGranted>


      <h1 class="ui header"><semui:headerIcon />
        <g:if test="${params.asAt}">${message(code:'package.show.asAt', args:[params.asAt])}</g:if>
          <g:if test="${editable}"><span id="packageNameEdit"
                    class="xEditableValue"
                    data-type="textarea"
                    data-pk="${packageInstance.class.name}:${packageInstance.id}"
                    data-name="name"
                    data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
          <g:else>${packageInstance.name}</g:else>
      </h1>

    <g:render template="nav" />

    <semui:meta>
        <div class="inline-lists">

            <dl>
                <dt><g:message code="package.globalUID.label" default="Global UID" /></dt>
                <dd> <g:fieldValue bean="${packageInstance}" field="globalUID"/> </dd>

                %{--<dt>${message(code: 'package.show.persistent_id')}</dt>
                <dd>uri://laser/${grailsApplication.config.laserSystemId}/package/${packageInstance?.id}</dd>--}%

                <dt>${message(code: 'package.show.other_ids')}</dt>
                <dd>
                    <table class="ui celled la-table la-table-small table ignore-floatThead">
                        <thead>
                        <tr>
                            <th>${message(code: 'component.id.label')}</th>
                            <th>${message(code: 'identifier.namespace.label')}</th>
                            <th>${message(code: 'identifier.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${packageInstance.ids.sort{it.identifier.ns.ns}}" var="io">
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
                        <semui:formAddIdentifier owner="${packageInstance}" />
                    </g:if>

                </dd>
            </dl>

        </div>
    </semui:meta>

 <semui:messages data="${flash}" />

 <semui:errors bean="${packageInstance}" />

    <div class="ui grid">



            <% /*
            <g:if test="${forum_url != null}">
                <a href="${forum_url}"> | Discuss this package in forums</a> <a href="${forum_url}" title="Discuss this package in forums (new Window)" target="_blank"><i class="icon-share-alt"></i></a>
            </g:if>
            */ %>


        <div class="twelve wide column">
            <g:hiddenField name="version" value="${packageInstance?.version}" />
            <div class="la-inline-lists">
                <div class="ui two cards">
                    <div class="ui card la-time-card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'package.show.start_date')}</dt>
                                <dd>
                                    <semui:xEditable owner="${packageInstance}" field="startDate" type="date"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.show.end_date')}</dt>
                                <dd>
                                    <semui:xEditable owner="${packageInstance}" field="endDate" type="date"/>
                                </dd>
                            </dl>
                        </div>
                    </div>
                    <div class="ui card">
                        <div class="content">
                            <dl>
                                <dt>${message(code: 'package.show.status')}</dt>
                                <dd>${packageInstance.packageStatus?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.list_status')}</dt>
                                <dd>
                                    <semui:xEditableRefData owner="${packageInstance}" field="packageListStatus" config="${RefdataCategory.PKG_LIST_STAT}"/>
                                </dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'package.scope')}</dt>
                                <dd>
                                    <semui:xEditableRefData owner="${packageInstance}" field="packageScope" config="${RefdataCategory.PKG_SCOPE}"/>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'package.show.pkg_name')}</dt>
                            <dd> <semui:xEditable owner="${packageInstance}" field="name"/></dd>



                            <% /*
                            <dt>${message(code: 'license.is_public')}</dt>
                            <dd>
                                <semui:xEditableRefData owner="${packageInstance}" field="isPublic" config='YN'/>
                            </dd>


                            <dt><g:message code="license" default="License"/></dt>
                            <dd>
                                <semui:xEditableRefData owner="${packageInstance}" field="license" config="Licenses"/>
                            </dd>
                            */ %>
                        </dl>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <dl>

                            <dt>${message(code: 'package.show.vendor_url')}</dt>
                            <dd>
                                <semui:xEditable owner="${packageInstance}" field="vendorURL" />
                            </dd>
                        </dl>

                        <g:render template="/templates/links/orgLinksAsList"
                                  model="${[roleLinks: visibleOrgs,
                                            roleObject: packageInstance,
                                            roleRespValue: 'Specific package editor',
                                            editmode: editable,
                                            tmplButtonText: 'Anbieter hinzufügen',
                                            tmplmodalID:'osel_add_modal'
                                  ]}" />

                        <g:render template="/templates/links/orgLinksModal"
                              model="${[linkType:packageInstance?.class?.name,
                                        parent: packageInstance.class.name+':'+packageInstance.id,
                                        property: 'orgs',
                                        recip_prop: 'pkg',
                                        tmplRole: com.k_int.kbplus.RefdataValue.getByValueAndCategory('Content Provider', 'Organisational Role'),
                                        tmplText:'Anbieter hinzufügen',
                                        tmplID:'ContentProvider',
                                        tmplmodalID:'osel_add_modal'
                              ]}" />
<%--
                        <g:render template="/templates/links/orgLinksAsListAddPrsModal"
                                  model="[roleLinks: visibleOrgs,
                                          'package': packageInstance,
                                          parent: packageInstance.class.name + ':' + packageInstance.id,
                                          role: modalPrsLinkRole.class.name + ':' + modalPrsLinkRole.id]"/>
--%>
                    </div>
                </div>
                <div class="ui card">
                    <div class="content">
                        <dl>
                            <dt>${message(code: 'package.breakable')}</dt>
                            <dd>
                                <semui:xEditableRefData owner="${packageInstance}" field="breakable" config="${RefdataCategory.PKG_BREAKABLE}"/>
                            </dd>
                        </dl>
                        <dl>

                            <dt>${message(code: 'package.consistent')}</dt>
                            <dd>
                                <semui:xEditableRefData owner="${packageInstance}" field="consistent" config="${RefdataCategory.PKG_CONSISTENT}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt>${message(code: 'package.fixed')}</dt>
                            <dd>
                                <semui:xEditableRefData owner="${packageInstance}" field="fixed" config="${RefdataCategory.PKG_FIXED}"/>
                            </dd>
                        </dl>

                        <g:if test="${statsWibid && packageIdentifier}">
                            <dl>
                               <dt>Paketnutzung</dt>
                               <dd>
                                    <laser:statsLink class="ui basic negative"
                                                     base="${grailsApplication.config.statsApiUrl}"
                                                     module="statistics"
                                                     controller="default"
                                                     action="select"
                                                     params="[mode:usageMode,
                                                              packages:packageInstance.getIdentifierByType('isil').value,
                                                              institutions:statsWibid
                                                     ]"
                                                     title="Springe zu Statistik im Nationalen Statistikserver">
                                        <i class="chart bar outline icon"></i>
                                    </laser:statsLink>
                                </dd>
                            </dl>
                        </g:if>

                    </div>
                </div>
            </div>
        </div><!-- .twelve -->


        <aside class="four wide column">

            <semui:card message="package.show.addToSub" class="notes">
                <div class="content">
                <g:if test="${(subscriptionList != null) && (subscriptionList?.size() > 0)}">

                    <g:form controller="packageDetails" action="addToSub" id="${packageInstance.id}" class="ui form">

                        <select class="ui dropdown" name="subid">
                            <g:each in="${subscriptionList}" var="s">
                                <option value="${s.sub.id}">${s.sub.name ?: "unnamed subscription ${s.sub.id}"} - ${s.org.name}</option>
                            </g:each>
                        </select>

                        <br/>
                        <br/>
                        <div class="ui checkbox">
                            <label>${message(code:'package.show.addEnt', default:'Create Entitlements in Subscription')}</label>
                            <input type="checkbox" id="addEntitlementsCheckbox" name="addEntitlements" value="true" class="hidden"/>
                        </div>

                        <input class="ui button" id="add_to_sub_submit_id" type="submit" value="${message(code:'default.button.submit.label')}"/>

                    </g:form>
                </g:if>
                <g:else>
                  ${message(code: 'package.show.no_subs')}
                </g:else>
                </div>
            </semui:card>

          <g:render template="/templates/tasks/card" model="${[ownobj:packageInstance, owntp:'pkg']}" />
          <g:render template="/templates/documents/card" model="${[ownobj:packageInstance, owntp:'pkg']}" />
          <g:render template="/templates/notes/card"  model="${[ownobj:packageInstance, owntp:'pkg']}" />
        </aside><!-- .four -->

    </div><!-- .grid -->


    <% /* TODO: DO NOT REMOVE

    <div>
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

       <semui:filter>
          <g:form action="show" params="${params}" method="get" class="ui form">
            <input type="hidden" name="sort" value="${params.sort}">
            <input type="hidden" name="order" value="${params.order}">
            <input type="hidden" name="mode" value="${params.mode}">
            <div class="two fields">
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
              <g:if test="${params.mode!='advanced'}">
                  <div class="field">
                        <semui:datepicker label="package.show.atDate" name="asAt" value="${params.asAt}" />
                  </div>
              </g:if>
                <div class="field">
                    <label>&nbsp;</label>
                    <input type="submit" class="ui secondary button" value="${message(code:'package.compare.filter.submit.label', default:'Filter Results')}" />
                </div>
            </div>

          </g:form>
       </semui:filter>
          <g:form action="packageBatchUpdate" params="${[id:packageInstance?.id]}">

            <g:hiddenField name="filter" value="${params.filter}"/>
            <g:hiddenField name="coverageNoteFilter" value="${params.coverageNoteFilter}"/>
            <g:hiddenField name="startsBefore" value="${params.startsBefore}"/>
            <g:hiddenField name="endsAfter" value="${params.endsAfter}"/>
            <g:hiddenField name="sort" value="${params.sort}"/>
            <g:hiddenField name="order" value="${params.order}"/>
            <g:hiddenField name="offset" value="${params.offset}"/>
            <g:hiddenField name="max" value="${params.max}"/>
            <table class="ui celled la-table table">
            <thead>
            <tr class="no-background">

              <th>
                <g:if test="${editable}"><input type="checkbox" id="select-all" name="chkall" onClick="javascript:selectAll();"/></g:if>
              </th>

              <th colspan="7">
                <g:if test="${editable}">
                  <select id="bulkOperationSelect" name="bulkOperation" class="input-xxlarge">
                    <option value="edit">${message(code:'package.show.batch.edit.label', default:'Batch Edit Selected Rows Using the following values')}</option>
                    <option value="remove">${message(code:'package.show.batch.remove.label', default:'Batch Remove Selected Rows')}</option>
                  </select>
                  <br/>
                  <table class="ui celled la-table table">
                    <tr>
                        <td>
                            <semui:datepicker label="subscription.details.coverageStartDate" name="bulk_start_date" value="${params.bulk_start_date}" />

                            <i>${message(code:'default.or', default:'or')}</i>
                            <input type="checkbox" name="clear_start_date" />
                            ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.startVolume', default:'Start Volume')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_start_volume" name="bulk_start_volume" />
                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_start_volume"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                             <div class="field">
                                 <label>${message(code:'tipp.startIssue', default:'Start Issue')}</label>
                                 <div>
                                    <semui:simpleHiddenValue id="bulk_start_issue" name="bulk_start_issue"/>
                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_start_issue"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                 </div>
                             </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <semui:datepicker label="subscription.details.coverageEndDate" name="bulk_end_date" value="${params.bulk_end_date}" />

                                <i>${message(code:'default.or', default:'or')}</i>
                                <input type="checkbox" name="clear_end_date"/>
                                ${message(code:'package.show.checkToClear', default:'Check to clear')}
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.endVolume', default:'End Volume')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_end_volume" name="bulk_end_volume"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_end_volume"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.endIssue', default:'End Issue')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_end_issue" name="bulk_end_issue"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_end_issue"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.hostPlatformURL', default:'Host Platform URL')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_hostPlatformURL" name="bulk_hostPlatformURL"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_hostPlatformURL"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.coverageNote', default:'Coverage Note')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_coverage_note" name="bulk_coverage_note"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_coverage_note"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.embargo', default:'Embargo')}</label>
                                <div>
                                    <semui:simpleHiddenValue id="bulk_embargo" name="bulk_embargo"/>
                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_embargo"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                    </tr>
                    <g:if test="${params.mode=='advanced'}">
                      <tr>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.delayedOA', default:'Delayed OA')}</label>
                                <div>
                                    <g:simpleHiddenRefdata id="bulk_delayedOA" name="bulk_delayedOA" refdataCategory="TitleInstancePackagePlatform.DelayedOA"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_delayedOA"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.hybridOA', default:'Hybrid OA')}</label>
                                <div>
                                    <g:simpleHiddenRefdata id="bulk_hybridOA" name="bulk_hybridOA" refdataCategory="TitleInstancePackagePlatform.HybridOA"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_hybridOA"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                        <td>
                            <div class="field">
                                <label>${message(code:'tipp.paymentType', default:'Payment')}</label>
                                <div>
                                    <g:simpleHiddenRefdata id="bulk_payment" name="bulk_payment" refdataCategory="TitleInstancePackagePlatform.PaymentType"/>

                                    <i>${message(code:'default.or', default:'or')}</i>
                                    <input type="checkbox" name="clear_payment"/>
                                    ${message(code:'package.show.checkToClear', default:'Check to clear')}
                                </div>
                            </div>
                        </td>
                      </tr>
                    </g:if>
                  </table>
                  <button name="BatchSelectedBtn" value="on" onClick="return confirmSubmit()" class="ui button">${message(code:'default.button.apply_batch.label')} (${message(code:'default.selected.label')})</button>
                  <button name="BatchAllBtn" value="on" onClick="return confirmSubmit()" class="ui button">${message(code:'default.button.apply_batch.label')} (${message(code:'package.show.batch.allInFL', default:'All in filtered list')})</button>
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
                   <strong>${t.title.title}</strong>
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
                     <br/> ${message(code:'subscription.details.record_status', default:'Record Status')}: <semui:xEditableRefData owner="${t}" field="status" config='TIPP Status'/>
                     <br/> ${message(code:'tipp.accessStartDate', default:'Access Start')}: <semui:xEditable owner="${t}" type="date" field="accessStartDate" />
                     <br/> ${message(code:'tipp.accessEndDate', default:'Access End')}: <semui:xEditable owner="${t}" type="date" field="accessEndDate" />
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
                   <semui:xEditableRefData owner="${t}" field="hybridOA" config='TitleInstancePackagePlatform.HybridOA'/>
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
                  ${message(code:'default.date.label', default:'Date')}: <semui:xEditable owner="${t}" type="date" field="startDate" /><br/>
                  ${message(code:'tipp.volume', default:'Volume')}: <semui:xEditable owner="${t}" field="startVolume" /><br/>
                  ${message(code:'tipp.issue', default:'Issue')}: <semui:xEditable owner="${t}" field="startIssue" />
                </td>

                <td style="white-space: nowrap"> 
                   ${message(code:'default.date.label', default:'Date')}: <semui:xEditable owner="${t}" type="date" field="endDate" /><br/>
                   ${message(code:'tipp.volume', default:'Volume')}: <semui:xEditable owner="${t}" field="endVolume" /><br/>
                   ${message(code:'tipp.issue', default:'Issue')}: <semui:xEditable owner="${t}" field="endIssue" />
                </td>
              </tr>

              <g:if test="${hasCoverageNote==true || params.mode=='advanced'}">
               <tr>
                  <td colspan="8">coverageNote: ${t.coverageNote}
                  <g:if test="${params.mode=='advanced'}">
                    <br/> ${message(code:'tipp.hostPlatformURL', default:'Host Platform URL')}: <semui:xEditable owner="${t}" field="hostPlatformURL" />
                    <br/> ${message(code:'tipp.delayedOA', default:'Delayed OA')}: <semui:xEditableRefData owner="${t}" field="delayedOA" config='TitleInstancePackagePlatform.DelayedOA'/> &nbsp;
                    ${message(code:'tipp.hybridOA', default:'Hybrid OA')}: <semui:xEditableRefData owner="${t}" field="hybridOA" config='TitleInstancePackagePlatform.HybridOA'/> &nbsp;
                    ${message(code:'tipp.paymentType', default:'Payment')}: <semui:xEditableRefData owner="${t}" field="payment" config='TitleInstancePackagePlatform.PaymentType'/> &nbsp;
                  </g:if>
                  </td>
                </tr>
              </g:if>

            </g:each>
            </tbody>
            </table>
          </g:form>
          

          <g:if test="${titlesList}" >
            <semui:paginate  action="show" controller="packageDetails" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_tipp_rows}" />
          </g:if>

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
            <button type="submit" class="ui button">${message(code:'package.show.title.add.submit', default:'Add Title...')}</button>
          </fieldset>
        </g:form>


        </g:if>

      </div>


    <g:render template="enhanced_select" contextPath="../templates" />
    <g:render template="orgLinksModal" 
              contextPath="../templates" 
              model="${[linkType:packageInstance?.class?.name,roleLinks:visibleOrgs,parent:packageInstance.class.name+':'+packageInstance.id,property:'orgs',recip_prop:'pkg']}" />

    <r:script language="JavaScript">
      $(function(){
        $.fn.editable.defaults.mode = 'inline';
        $('.xEditableValue').editable();
      });
      function selectAll() {
          $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
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
    */ %>

    </body>
</html>
