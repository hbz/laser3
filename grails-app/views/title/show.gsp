<%@ page import="de.laser.remote.ApiSource; de.laser.Package; de.laser.RefdataCategory; de.laser.titles.BookInstance" %>
<laser:htmlStart message="title.details" />

      <semui:breadcrumbs>
          <semui:crumb controller="title" action="list" message="menu.public.all_titles" />
          <semui:crumb class="active" text="${message(code:'default.title.label')}: ${ti.title}" />
      </semui:breadcrumbs>

        <semui:h1HeaderWithIcon type="${ti.printTitleType()}">
            ${ti.title}
            <g:if test="${ti.status?.value && ti.status.value != 'Current'}">
                <span class="badge badge-error" style="vertical-align:middle;">${ti.status.getI10n('value')}</span>
            </g:if>
        </semui:h1HeaderWithIcon>

        <laser:render template="nav" />

        <laser:render template="/templates/meta/identifier" model="${[object: ti, editable: editable]}" />

        <semui:messages data="${flash}" />

        <div class="ui grid">

            <div class="sixteen wide column">

                <semui:form>
                    <!-- START TEMPLATE -->
                    <laser:render template="/templates/title_long"
                              model="${[ie: null, tipp: ti,
                                        showPackage: true, showPlattform: true, showCompact: false, showEmptyFields: true]}"/>
                    <!-- END TEMPLATE -->
                </semui:form>
            </div>

            <div class="sixteen wide column">
                <semui:form>
                  <g:each in="${duplicates}" var="entry">

                          ${message(code:'title.edit.duplicate.warn', args:[entry.key])}:
                          <ul>
                              <g:each in ="${entry.value}" var="dup_title">
                                  <li><g:link controller='title' action='show' id="${dup_title.id}">${dup_title.title}</g:link></li>
                              </g:each>
                          </ul>

                  </g:each>
                </semui:form>
            </div>

        </div><!-- .grid -->

          <h3 class="ui header"><g:message code="title.edit.orglink"/></h3>

            <table class="ui celled la-js-responsive-table la-table table ">
              <thead>
                <tr>
                  <th><g:message code="template.orgLinks.name"/></th>
                  <th><g:message code="default.role.label"/></th>
                  <th><g:message code="default.from"/></th>
                  <th><g:message code="default.to"/></th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${ti.orgs}" var="org">
                  <tr>
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

            <h3 class="ui header">${message(code: 'title.show.history.label')}</h3>

            <table class="ui celled la-js-responsive-table la-table table">
              <thead>
                <tr>
                  <th>${message(code: 'default.date.label')}</th>
                  <th>${message(code: 'default.from')}</th>
                  <th>${message(code: 'default.to')}</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${titleHistory}" var="th">
                  <tr>
                    <td><g:formatDate date="${th.eventDate}" formatName="default.date.format.notime"/></td>
                    <td>
                      <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='from'}">
                          <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br />
                        </g:if>
                      </g:each>
                    </td>
                    <td>
                      <g:each in="${th.participants}" var="p">
                        <g:if test="${p.participantRole=='to'}">
                          <g:link controller="title" action="show" id="${p.participant.id}"><span style="<g:if test="${p.participant.id == ti.id}">font-weight:bold</g:if>">${p.participant.title}</span></g:link><br />
                        </g:if>
                      </g:each>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>
            <g:if test="${ti.getIdentifierValue('originediturl') != null}">
              <span class="la-float-right">
                ${message(code: 'title.show.gokb')} <a href="${ti.getIdentifierValue('originediturl')}">we:kb</a>.
              </span>
            </g:if>

  <h3 class="ui icon header la-clear-before la-noMargin-top"><g:message code="title.edit.tipp"/>
  <semui:totalNumber total="${ti.tipps.size()}"/>
  </h3>

            %{--NEW VIEW FOR TIPPS--}%

  <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
      <thead>
      <tr>
          <th></th>
          <th><g:message code="package"/></th>
          <th class="two wide">${message(code: 'tipp.coverage')}</th>
          <th class="two wide">${message(code: 'tipp.access')}</th>
      </tr>
      <tr>
          <th colspan="2" rowspan="2"></th>
          <th><g:message code="default.from"/></th>
          <th><g:message code="default.from"/></th>
      </tr>
      <tr>
          <th><g:message code="default.to"/></th>
          <th><g:message code="default.to"/></th>
      </tr>
      </thead>
      <tbody>

      <g:set var="counter" value="${1}" />
      <g:each in="${ti.tipps.sort{it?.pkg?.name}}" var="t">
          <tr>
              <td>${counter++}</td>
              <td>


                  <div class="la-inline-flexbox la-popup-tooltip la-delay">
                      <i class="icon gift scale la-list-icon"></i>
                      <g:link controller="package" action="show" id="${t?.pkg?.id}">${t?.pkg?.name}</g:link>
                  </div>
                  <g:if test="${t.hostPlatformURL}">
                      <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                         data-content="${t.platform.name}"
                         href="${t.hostPlatformURL.contains('http') ? t.hostPlatformURL :'http://'+t.hostPlatformURL}"
                         target="_blank">
                          <i class="cloud icon"></i>
                      </a><br />
                  </g:if>

                  <g:each in="${t?.title?.ids?.sort{it?.ns?.ns}}" var="id">
                      <span class="ui small basic image label">
                          ${id.ns.ns}: <div class="detail">${id.value}</div>
                      </span>
                  </g:each>
                  <div class="la-icon-list">
                     %{-- <g:if test="${t.availabilityStatus?.getI10n('value')}">
                          <div class="item">
                              <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.access.label')}"></i>
                              <div class="content">
                                  ${t.availabilityStatus?.getI10n('value')}
                              </div>
                          </div>
                      </g:if>--}%

                      <div class="item">
                          <i class="grey key icon la-popup-tooltip la-delay" data-content="${message(code: 'default.status.label')}"></i>
                          <div class="content">
                              <semui:xEditableRefData owner="${t}" field="status" config="${de.laser.storage.RDConstants.TIPP_STATUS}"/>
                          </div>
                      </div>

                      <g:if test="${t?.platform.name}">
                          <div class="item">
                              <i class="grey icon cloud la-popup-tooltip la-delay" data-content="${message(code:'tipp.tooltip.changePlattform')}"></i>
                              <div class="content">
                                <g:if test="${t?.platform.name}">
                                    <g:link controller="platform" action="show"
                                          id="${t?.platform.id}">
                                      ${t?.platform.name}
                                    </g:link>
                                </g:if>
                                <g:else>
                                    ${message(code: 'default.unknown')}
                                </g:else>
                              </div>
                          </div>
                      </g:if>
                      <g:if test="${t?.id}">
                          <div class="la-title">${message(code: 'default.details.label')}</div>
                          <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                  data-content="${message(code: 'laser')}"
                                  target="_blank"
                                  controller="tipp" action="show"
                                  id="${t.id}">
                              <i class="book icon"></i>
                          </g:link>
                      </g:if>

                      <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"
                              var="gokbAPI">
                          <g:if test="${t?.gokbId}">
                              <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                                 data-content="${message(code: 'wekb')}"
                                 href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/tippContent/?id=' + t?.gokbId : '#'}"
                                 target="_blank"><i class="la-gokb  icon"></i>
                              </a>
                          </g:if>
                      </g:each>
                  </div>
              </td>

              <td>
                  <g:each in="${t.coverages}" var="covStmt">
                      <p>
                          <!-- von -->
                          <g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.startDate}"/><br />
                          <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.startVolume')}"></i>${covStmt.startVolume}<br />
                          <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.startIssue')}"></i>${covStmt.startIssue}<br />
                          <semui:dateDevider/>
                          <!-- bis -->
                          <g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.endDate}"/><br />
                          <i class="grey fitted la-books icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.endVolume')}"></i>${covStmt.endVolume}<br />
                          <i class="grey fitted la-notebook icon la-popup-tooltip la-delay" data-content="${message(code:'tipp.endIssue')}"></i>${covStmt.endIssue}<br />
                          <i class="grey icon quote right la-popup-tooltip la-delay" data-content="${message(code: 'default.note.label')}"></i>${covStmt.coverageNote}<br />
                          <i class="grey icon file alternate right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.coverageDepth')}"></i>${covStmt.coverageDepth}<br />
                          <i class="grey icon hand paper right la-popup-tooltip la-delay" data-content="${message(code: 'tipp.embargo')}"></i>${covStmt.embargo}<br />
                      </p>
                  </g:each>
              </td>
              <td>
                  <!-- von -->
                  <semui:xEditable owner="${t}" type="date" field="accessStartDate" />
                  <semui:dateDevider/>
                  <!-- bis -->
                  <semui:xEditable owner="${t}" type="date" field="accessEndDate" />
              </td>
          </tr>

      </g:each>

      </tbody>

  </table>

            <br /><br />

<laser:htmlEnd />
