<%@ page import="de.laser.storage.PropertyStore; de.laser.properties.LicenseProperty; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.RefdataValue;de.laser.Links;de.laser.Org" %>
<laser:serviceInjection />

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
                              ${message(code:'license.details.incoming.childs')}
                          </th>
                      </g:if>

                      <g:if test="${'provider' in licenseFilterTable}">
                          <th rowspan="2"><g:message code="provider.label"/></th>
                      </g:if>
                      <g:if test="${'vendor' in licenseFilterTable}">
                          <th rowspan="2"><g:message code="vendor.label"/></th>
                      </g:if>
                      <g:if test="${'licensingConsortium' in licenseFilterTable}">
                          <th rowspan="2"><g:message code="consortium"/></th>
                      </g:if>
                      <g:if test="${'processing' in licenseFilterTable}">
                          <th rowspan="2" class="center aligned">
                              <span class="la-popup-tooltip" data-content="${message(code:'license.processing')}" data-position="top right">
                                  <i class="${Icon.ATTR.LICENSE_PROCESSING} large"></i>
                              </span>
                          </th>
                      </g:if>
                      <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate" title="${message(code:'license.start_date')}" />
                      <g:if test="${'action' in licenseFilterTable}">
                          <th rowspan="2" class="center aligned">
                              <ui:optionsIcon />
                          </th>
                      </g:if>
                  </tr>
                  <tr>
                      <g:sortableColumn class="la-smaller-table-head" params="${params}" property="endDate" title="${message(code:'license.end_date')}" />
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
                                  <g:if test="${l._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}">
                                      <i class="icon users la-list-icon la-popup-tooltip" data-content="${tooltip}"></i>
                                  </g:if>
                                  ${l.reference ?: message(code:'missingLicenseReference')}
                              </g:link>
                              <g:each in="${allLinkedSubscriptions.get(l)}" var="sub">
                                  <div class="la-flexbox la-minor-object">
                                      <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                      <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link><br />
                                  </div>
                              </g:each>
                          </th>
                          <g:if test="${'memberLicenses' in licenseFilterTable}">
                              <td>
                                  <g:each in="${l.derivedLicenses}" var="lChild">
                                      <div class="la-flexbox">
                                          <g:if test="${l.derivedLicenses.size() > 1}">
                                              <i class="${Icon.LICENSE} la-list-icon"></i>
                                          </g:if>
                                          <g:link controller="license" action="show" id="${lChild.id}">${lChild}</g:link>
                                      </div>
                                  </g:each>
                              </td>
                          </g:if>
                        <g:if test="${'provider' in licenseFilterTable}">
                          <td>
                              <g:set var="providers" value="${l.getProviders()}"/>
                              <g:each in="${providers}" var="provider">
                                  <g:link controller="provider" action="show" id="${provider.id}">
                                      ${fieldValue(bean: provider, field: "name")}
                                      <g:if test="${provider.sortname}">
                                          (${fieldValue(bean: provider, field: "sortname")})
                                      </g:if>
                                  </g:link>
                                  <br>
                              </g:each>
                          </td>
                        </g:if>
                        <g:if test="${'vendor' in licenseFilterTable}">
                          <td>
                              <g:set var="vendors" value="${l.getVendors()}"/>
                              <g:each in="${vendors}" var="vendor">
                                  <g:link controller="vendor" action="show" id="${vendor.id}">
                                      ${fieldValue(bean: vendor, field: "name")}
                                      <g:if test="${vendor.sortname}">
                                          (${fieldValue(bean: vendor, field: "sortname")})
                                      </g:if>
                                  </g:link>
                                  <br>
                              </g:each>
                          </td>
                        </g:if>
                          <g:if test="${'licensingConsortium' in licenseFilterTable}">
                              <td>${l.getLicensingConsortium()?.name}</td>
                          </g:if>
                          <g:if test="${'processing' in licenseFilterTable}">
                              <td>
                                  <% LicenseProperty processingProp = LicenseProperty.findByOwnerAndType(l, PropertyStore.LIC_PROCESSING) %>
                                  <g:if test="${processingProp}">
                                      <g:if test="${processingProp.refValue == RDStore.INVOICE_PROCESSING_CONSORTIUM}">
                                          <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${processingProp.getValueInI10n()}"><i class="${Icon.AUTH.ORG_CONSORTIUM}"></i></span>
                                      </g:if>
                                      <g:elseif test="${processingProp.refValue == RDStore.INVOICE_PROCESSING_PROVIDER}">
                                          <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${processingProp.getValueInI10n()}"><i class="${Icon.PROVIDER}"></i></span>
                                      </g:elseif>
                                      <g:elseif test="${processingProp.refValue == RDStore.INVOICE_PROCESSING_PROVIDER_OR_VENDOR}">
                                          <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${processingProp.getValueInI10n()}"><i class="${Icon.PROVIDER}"></i> / <i class="${Icon.VENDOR}"></i></span>
                                      </g:elseif>
                                      <g:elseif test="${processingProp.refValue == RDStore.INVOICE_PROCESSING_VENDOR}">
                                          <span class="la-long-tooltip la-popup-tooltip" data-position="right center" data-content="${processingProp.getValueInI10n()}"><i class="${Icon.VENDOR}"></i></span>
                                      </g:elseif>
                                  </g:if>
                              </td>
                          </g:if>
                          <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.startDate}"/><br />
                              <span class="la-secondHeaderRow" data-label="${message(code:'license.end_date')}:">
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.endDate}"/>
                              </span>
                          </td>
                          <g:if test="${'action' in licenseFilterTable}">
                              <td class="x">
                              <g:if test="${(contextCustomerType == CustomerTypeService.ORG_INST_PRO && l._getCalculatedType() == License.TYPE_LOCAL) || (customerTypeService.isConsortium( contextCustomerType ) && l._getCalculatedType() == License.TYPE_CONSORTIAL)}">
                                  <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'license.details.copy.tooltip')}">
                                      <g:link controller="license" action="copyLicense" params="${[sourceObjectId: genericOIDService.getOID(l), copyObject: true]}" class="${Btn.MODERN.SIMPLE}">
                                          <i class="${Icon.CMD.COPY}"></i>
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
      <input type="submit" class="${Btn.SIMPLE}" value="${message(code:'menu.my.comp_lic')}" />
  </g:if>

</g:form>

<ui:paginate action="currentLicenses" controller="myInstitution" params="${params}" max="${max}" total="${licenseCount}" />
