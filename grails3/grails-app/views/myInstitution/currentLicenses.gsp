<%@ page import="de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.helper.RDStore;de.laser.helper.RDConstants;de.laser.RefdataValue;de.laser.Links" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
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
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[exportPDF:true]}">${message(code:'default.button.exports.pdf')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </semui:exportDropdownItem>
          </g:if>
          <g:else>
              <semui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[exportPDF:true]}">${message(code:'default.button.exports.pdf')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </semui:exportDropdownItem>
              <semui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </semui:exportDropdownItem>
          </g:else>
      </semui:exportDropdown>

      <g:render template="actions" />

  </semui:controlButtons>

  <h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon />${message(code:'license.current')}
      <semui:totalNumber total="${licenseCount}"/>
  </h1>

  <semui:messages data="${flash}" />

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
              <g:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
          </div>
          <div class="three fields">
              <div class="field">
                  <label for="status">${message(code: 'license.status.label')}</label>
                  <laser:select class="ui dropdown" name="status"
                                from="${ RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS) }"
                                optionKey="id"
                                optionValue="value"
                                value="${params.status}"
                                noSelection="${['' : message(code:'default.select.choose.label')]}"/>
              </div>
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
          </div>
          <div class="three fields">
              <div class="field">
                  <label for="subStatus">${message(code: 'subscription.status.label')}</label>
                  <laser:select class="ui dropdown" name="subStatus"
                                from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                optionKey="id"
                                optionValue="value"
                                value="${params.subStatus}"
                                noSelection="${['' : message(code:'default.select.choose.label')]}"/>
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
                  <g:link action="currentLicenses" params="[resetFilter:true]" class="ui reset primary primary button">${message(code:'default.button.reset.label')}</g:link>
                  <input type="hidden" name="filterSet" value="true">
                  <input type="submit" name="filterSubmit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
              </div>
          </div>
      </form>
  </semui:filter>

  <g:form action="compareLicenses" controller="compare" method="post">

  <div class="license-results la-clear-before">
      <g:if test="${licenses}">
          <table class="ui sortable celled la-js-responsive-table la-table table">
              <thead>
                  <tr>
                      <g:if test="${compare}">
                          <th rowspan="2" class="center aligned">
                              <g:message code="default.compare.submit.label"/>
                          </th>
                      </g:if>
                      <th rowspan="2"><g:message code="sidewide.number"/></th>
                      <g:sortableColumn rowspan="2" params="${params}" property="reference" title="${message(code:'license.slash.name')}" />
                      <g:if test="${'memberLicenses' in licenseFilterTable}">
                          <th rowspan="2">
                              <span class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.incoming.childs')}" data-position="top right">
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
                          <g:if test="${compare}">
                              <td class="center aligned">
                                  <g:checkBox id="selectedObjects_${l.id}" name="selectedObjects" value="${l.id}" checked="false"/>
                              </td>
                          </g:if>
                          <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                          <th scope="row" class="la-th-column">
                              <g:link action="show" class="la-main-object" controller="license" id="${l.id}">
                                  ${l.reference ?: message(code:'missingLicenseReference')}
                              </g:link>
                              <g:each in="${allLinkedSubscriptions.get(l)}" var="sub">
                                  <div class="la-flexbox la-minor-object">
                                      <i class="icon clipboard outline la-list-icon"></i>
                                      <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link><br />
                                  </div>
                              </g:each>
                          </th>
                          <g:if test="${'memberLicenses' in licenseFilterTable}">
                              <td>
                                  <g:each in="${l.derivedLicenses}" var="lChild">
                                      <g:link controller="license" action="show" id="${lChild.id}">
                                          <p>${lChild}</p>
                                      </g:link>
                                  </g:each>
                              </td>
                          </g:if>
                          <td>
                              <g:set var="licensor" value="${l.getLicensor()}"/>
                              <g:if test="${licensor}">
                                  <g:link controller="organisation" action="show" id="${licensor.id}">
                                      ${fieldValue(bean: licensor, field: "name")}
                                      <g:if test="${licensor.shortname}">
                                          <br />
                                          (${fieldValue(bean: licensor, field: "shortname")})
                                      </g:if>
                                  </g:link>
                              </g:if>
                          </td>
                          <g:if test="${'licensingConsortium' in licenseFilterTable}">
                              <td>${l.getLicensingConsortium()?.name}</td>
                          </g:if>
                          <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.startDate}"/><br />
                              <span class="la-secondHeaderRow" data-label="${message(code:'license.end_date')}:">
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.endDate}"/>
                              </span>
                          </td>
                          <g:if test="${'action' in licenseFilterTable}">
                              <td class="x">
                              <g:if test="${(contextCustomerType == "ORG_INST" && l._getCalculatedType() == License.TYPE_LOCAL) || (contextCustomerType == "ORG_CONSORTIUM" && l._getCalculatedType() == License.TYPE_CONSORTIAL)}">
                                  <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'license.details.copy.tooltip')}">
                                      <g:link controller="license" action="copyLicense" params="${[sourceObjectId: genericOIDService.getOID(l), copyObject: true]}" class="ui icon button blue la-modern-button">
                                          <i class="copy icon"></i>
                                      </g:link>
                                  </span>
                              </g:if>
                              </td>
                        </g:if>
                    </tr>
                  </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <strong><g:message code="filter.result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
            </g:if>
            <g:else>
                <strong><g:message code="result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
            </g:else>
        </g:else>
    </div>

  <g:if test="${licenses && compare}">
      <input type="submit" class="ui button" value="${message(code:'menu.my.comp_lic')}" />
  </g:if>

  </g:form>
      <semui:paginate action="currentLicenses" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${licenseCount}" />
      <semui:debugInfo>
          <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
      </semui:debugInfo>
  </body>
</html>
