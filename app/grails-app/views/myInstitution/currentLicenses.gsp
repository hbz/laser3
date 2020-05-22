<%@ page import="de.laser.interfaces.CalculatedType;de.laser.helper.RDStore;de.laser.helper.RDConstants;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.RefdataCategory" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code:'license.current')}</title>
  </head>
  <body>

  <laser:serviceInjection />

  <semui:breadcrumbs>
      <semui:crumb message="license.current" class="active" />
  </semui:breadcrumbs>

  <semui:controlButtons>
      <semui:exportDropdown>
          <g:if test="${filterSet || defaultSet}">
              <semui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </semui:exportDropdownItem>
          </g:if>
          <g:else>
              <semui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </semui:exportDropdownItem>
          </g:else>
      </semui:exportDropdown>

      <g:render template="actions" />

  </semui:controlButtons>

  <semui:messages data="${flash}" />

  <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'license.current')}
      <semui:totalNumber total="${licenseCount}"/>
  </h1>


  <g:render template="/templates/filter/javascript" />


  <semui:filter showFilterButton="true" class="license-searches">
      <form class="ui form">
          <div class="four fields">
              <div class="field">
                  <label for="keyword-search"><g:message code="default.search.text"/>
                      <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.license')}">
                          <i class="question circle icon"></i>
                      </span>
                  </label>
                  <input type="text" id="keyword-search" name="keyword-search" placeholder="${message(code:'default.search.ph')}" value="${params['keyword-search']?:''}" />
              </div>
              <g:if test="${'licensingConsortium' in licenseFilterTable}">
                  <div class="field">
                      <label for="consortium"><g:message code="consortium"/></label>
                      <select id="consortium" name="consortium" multiple="" class="ui search selection fluid dropdown">
                          <option value=""><g:message code="default.select.choose.label"/></option>
                          <g:each in="${orgs.consortia}" var="consortium">
                              <option <%=(params.list('consortium').contains(consortium.id.toString())) ? 'selected="selected"' : ''%> value="${consortium.id}">${consortium.name}</option>
                          </g:each>
                      </select>
                  </div>
              </g:if>
              <div class="field">
                  <semui:datepicker label="license.valid_on" id="validOn" name="validOn" placeholder="default.date.label" value="${validOn}" />
              </div>
              <g:render template="../templates/properties/genericFilter" model="[propList: propList]"/>
          </div>
          <div class="four fields">
              <div class="field">
                  <label for="licensor"><g:message code="license.licensor.label"/></label>
                  <select id="licensor" name="licensor" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${orgs.licensors}" var="licensor">
                          <option <%=(params.list('licensor').contains(licensor.id.toString())) ? 'selected="selected"' : ''%> value="${licensor.id}">${licensor.name}</option>
                      </g:each>
                  </select>
              </div>
              <div class="field">
                  <label for="categorisation"><g:message code="license.categorisation.label"/></label>
                  <select id="categorisation" name="categorisation" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_CATEGORY)}" var="categorisation">
                          <option <%=(params.list('categorisation').contains(categorisation.id.toString())) ? 'selected="selected"' : ''%> value="${categorisation.id}">${categorisation.getI10n("value")}</option>
                      </g:each>
                  </select>
              </div>
              <div class="field">
                  <label for="subKind"><g:message code="license.subscription.kind.label"/></label>
                  <select id="subKind" name="subKind" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                          <option <%=(params.list('subKind').contains(subKind.id.toString())) ? 'selected="selected"' : ''%> value="${subKind.id}">${subKind.getI10n("value")}</option>
                      </g:each>
                  </select>
              </div>
              <div class="field la-field-right-aligned">
                  <a href="${request.forwardURI}" class="ui reset primary primary button">${message(code:'default.button.reset.label')}</a>
                  <input type="hidden" name="filterSet" value="true">
                  <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
              </div>
          </div>
      </form>
  </semui:filter>

  <div class="license-results la-clear-before">
      <g:if test="${licenses}">
          <table class="ui sortable celled la-table table">
              <thead>
                  <tr>
                      <th rowspan="2"><g:message code="sidewide.number"/></th>
                      <g:sortableColumn rowspan="2" params="${params}" property="reference" title="${message(code:'license.slash.name')}" />
                      <g:if test="${'memberLicenses' in licenseFilterTable}">
                          <th rowspan="2">
                              <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.incoming.childs',args:[message(code:'consortium.superOrgType')])}" data-position="top center">
                                  <i class="users large icon"></i>
                              </span>
                          </th>
                      </g:if>
                      <th rowspan="2"><g:message code="license.licensor.label"/></th>
                      <g:if test="${'licensingConsortium' in licenseFilterTable}">
                          <th rowspan="2"><g:message code="consortium"/></th>
                      </g:if>
                      <g:sortableColumn params="${params}" property="startDate" title="${message(code:'license.start_date')}" />
                      <g:if test="${'action' in licenseFilterTable}">
                          <th rowspan="2" class="la-action-info"><g:message code="default.actions.label"/></th>
                      </g:if>
                  </tr>
                  <tr>
                      <g:sortableColumn params="${params}" property="endDate" title="${message(code:'license.end_date')}" />
                  </tr>
              </thead>
              <tbody>
                  <g:each in="${licenses}" var="l" status="jj">
                      <tr>
                          <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                          <td>
                              <g:link action="show" class="la-main-object" controller="license" id="${l.id}">
                                  ${l.reference ?: message(code:'missingLicenseReference')}
                              </g:link>
                              <%--<g:if test="${l.subscriptions && ( l.subscriptions.size() > 0 )}">--%>
                                  <g:each in="${l.subscriptions}" var="sub">
                                      <g:if test="${sub.status.id == RDStore.SUBSCRIPTION_CURRENT.id}">
                                          <g:if test="${institution.id in sub.orgRelations.org.id || accessService.checkPerm("ORG_CONSORTIUM")}">
                                              <div class="la-flexbox">
                                                  <i class="icon clipboard outline outline la-list-icon"></i>
                                                  <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link><br/>
                                              </div>
                                          </g:if>
                                      </g:if>
                                  </g:each>
                              <%--</g:if>--%>
                              <%--<g:else>
                                  <br>${message(code:'myinst.currentLicenses.no_subs')}
                              </g:else>--%>
                          </td>
                          <g:if test="${'memberLicenses' in licenseFilterTable}">
                              <td>
                                  <g:each in="${l.derivedLicenses}" var="lChild">

                                      <g:link controller="license" action="show" id="${lChild.id}">
                                          ${lChild}
                                      </g:link>
                                      <br/>

                                  </g:each>
                              </td>
                          </g:if>
                          <td>
                              <g:if test="${l.licensor}">
                                  <g:link controller="organisation" action="show" id="${l.licensor.id}">${l.licensor.name}</g:link>
                              </g:if>
                          </td>
                          <g:if test="${'licensingConsortium' in licenseFilterTable}">
                              <td>${l.getLicensingConsortium().name}</td>
                          </g:if>
                          <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.startDate}"/><br><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.endDate}"/></td>
                          <g:if test="${'action' in licenseFilterTable}">
                              <td class="x">
                                  <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.copy.tooltip')}">
                                      <g:link controller="myInstitution" action="copyLicense" params="${[id:l.id]}" class="ui icon button">
                                          <i class="copy icon"></i>
                                      </g:link>
                                  </span>
                              </td>
                        </g:if>
                    </tr>
                  </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br><strong><g:message code="filter.result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
            </g:if>
            <g:else>
                <br><strong><g:message code="result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
            </g:else>
        </g:else>
    </div>

      <semui:paginate action="currentLicenses" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${licenseCount}" />

      <semui:debugInfo>
          <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
      </semui:debugInfo>

  </body>
</html>
