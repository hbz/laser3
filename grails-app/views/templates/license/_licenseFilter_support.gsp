<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.CustomerTypeService; de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.RefdataValue;de.laser.Links;de.laser.Org" %>
<laser:serviceInjection />

<ui:filter>
      <form class="ui form">
          <div class="four fields">
              <div class="field">
                  <label for="keyword-search"><g:message code="default.search.text"/>
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip" data-content="${message(code:'default.search.tooltip.license')}">
                          <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                  </label>
                  <input type="text" id="keyword-search" name="keyword-search" placeholder="${message(code:'default.search.ph')}" value="${params['keyword-search']?:''}" />
              </div>
              <div class="field">
                  <ui:datepicker label="license.valid_on" id="validOn" name="validOn" placeholder="default.date.label" value="${validOn}" />
              </div>
              <div class="field">
                  <label for="categorisation"><g:message code="license.categorisation.label"/></label>
                  <select id="categorisation" name="categorisation" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_CATEGORY)}" var="categorisation">
                          <option <%=Params.getLongList(params, 'categorisation').contains(categorisation.id) ? 'selected="selected"' : ''%> value="${categorisation.id}">${categorisation.getI10n("value")}</option>
                      </g:each>
                  </select>
              </div>
              <div class="field">
                  <label for="status">${message(code: 'license.status.label')}</label>
                  <ui:select class="ui dropdown clearable" name="status"
                             from="${ RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_STATUS) }"
                             optionKey="id"
                             optionValue="value"
                             value="${params.status}"
                             noSelection="${['' : message(code:'default.select.choose.label')]}"/>
              </div>
          </div>
          <div class="four fields">
              <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

              <div class="field">
                  <label for="subKind"><g:message code="license.subscription.kind.label"/></label>
                  <select id="subKind" name="subKind" multiple="" class="ui search selection fluid dropdown">
                      <option value=""><g:message code="default.select.choose.label"/></option>
                      <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                          <option <%=Params.getLongList(params, 'subKind').contains(subKind.id) ? 'selected="selected"' : ''%> value="${subKind.id}">${subKind.getI10n("value")}</option>
                      </g:each>
                  </select>
              </div>
              <div class="field">
                  <label for="subStatus">${message(code: 'subscription.status.label')}</label>
                  <ui:select class="ui dropdown clearable" name="subStatus"
                                from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                optionKey="id"
                                optionValue="value"
                                value="${params.subStatus}"
                                noSelection="${['' : message(code:'default.select.choose.label')]}"/>
              </div>
          </div>
          <div class="field la-field-right-aligned">
              <g:link action="currentLicenses" params="[resetFilter:true]" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</g:link>
              <input type="hidden" name="filterSet" value="true">
              <input type="submit" name="filterSubmit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}">
          </div>
      </form>
</ui:filter>

