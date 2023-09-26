<%@ page import="de.laser.CustomerTypeService; de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.RefdataValue;de.laser.Links;de.laser.Org" %>
<laser:serviceInjection />

  <ui:filter>
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
                  <ui:datepicker label="license.valid_on" id="validOn" name="validOn" placeholder="default.date.label" value="${validOn}" />
              </div>
              <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
          </div>
          <div class="three fields">
              <div class="field">
                  <label for="status">${message(code: 'license.status.label')}</label>
                  <ui:select class="ui dropdown" name="status"
                                from="${ RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS) }"
                                optionKey="id"
                                optionValue="value"
                                value="${params.status}"
                                noSelection="${['' : message(code:'default.select.choose.label')]}"/>
              </div>
            <g:if test="${'providerAgency' in licenseFilterTable}">
              <div class="field">
                  <label for="licensor"><g:message code="default.ProviderAgency.singular"/></label>
                  <select id="licensor" name="licensor" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${orgs.licensors}" var="licensor">
                          <option <%=(params.list('licensor').contains(licensor.id.toString())) ? 'selected="selected"' : ''%> value="${licensor.id}">${licensor.name}</option>
                      </g:each>
                  </select>
              </div>
            </g:if>
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
                  <ui:select class="ui dropdown" name="subStatus"
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
              <div class="field"></div>
          </div>
          <div class="three fields">
          <%-- TODO [ticket=2276] provisoric, name check is in order to prevent id mismatch --%>
              <g:if test="${contextService.getOrg().isCustomerType_Inst_Pro() || institution.globalUID == Org.findByName('LAS:eR Backoffice').globalUID}">
                  <div class="field">
                      <fieldset id="licenseType">
                          <div class="inline fields la-filter-inline">
                              <div class="inline field">
                                  <div class="ui checkbox">
                                      <label for="checkLicType-${RDStore.OR_LICENSEE}"><g:message code="license.filter.local"/></label>
                                      <input id="checkLicType-${RDStore.OR_LICENSEE}" name="licTypes" type="checkbox" value="${RDStore.OR_LICENSEE.id.toString()}"
                                          <g:if test="${params.list('licTypes').contains(RDStore.OR_LICENSEE.id.toString())}"> checked="" </g:if>
                                             tabindex="0">
                                  </div>
                              </div>
                              <div class="inline field">
                                  <div class="ui checkbox">
                                      <label for="checkLicType-${RDStore.OR_LICENSEE_CONS}"><g:message code="license.filter.member"/></label>
                                      <input id="checkLicType-${RDStore.OR_LICENSEE_CONS}" name="licTypes" type="checkbox" value="${RDStore.OR_LICENSEE_CONS.id.toString()}"
                                          <g:if test="${params.list('licTypes').contains(RDStore.OR_LICENSEE_CONS.id.toString())}"> checked="" </g:if>
                                             tabindex="0">
                                  </div>
                              </div>
                          </div>
                      </fieldset>
                  </div>
              </g:if>
              <g:else>
                  <div class="field"></div>
              </g:else>
              <div class="field"></div>
              <div class="field la-field-right-aligned">
                  <g:link action="currentLicenses" params="[resetFilter:true]" class="ui reset secondary button">${message(code:'default.button.reset.label')}</g:link>
                  <input type="hidden" name="filterSet" value="true">
                  <input type="submit" name="filterSubmit" class="ui primary button" value="${message(code:'default.button.filter.label')}">
              </div>
          </div>
      </form>
  </ui:filter>

