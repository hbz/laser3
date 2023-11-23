<%@ page import="de.laser.CustomerTypeService; de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.RefdataValue;de.laser.Links;de.laser.Org" %>

<laser:htmlStart message="license.current" serviceInjection="true" />

  <ui:breadcrumbs>
      <ui:crumb message="license.current" class="active" />
  </ui:breadcrumbs>

  <ui:controlButtons>
      <ui:exportDropdown>
          <ui:exportDropdownItem>
              <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
          </ui:exportDropdownItem>
      <%--
          <g:if test="${filterSet || defaultSet}">
              <ui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" target="_blank" params="${params+[exportPDF:true]}">${message(code:'default.button.exports.pdf')}</g:link>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:link class="item js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                          data-confirm-term-how="ok" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </ui:exportDropdownItem>
          </g:if>
          <g:else>
              <ui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" target="_blank" params="${params+[exportPDF:true]}">${message(code:'default.button.exports.pdf')}</g:link>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:link class="item" action="currentLicenses" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
              </ui:exportDropdownItem>
          </g:else>
      --%>
      </ui:exportDropdown>

      <laser:render template="${customerTypeService.getActionsTemplatePath()}" />

  </ui:controlButtons>

  <ui:h1HeaderWithIcon message="license.current" total="${licenseCount}" floated="true" />

  <ui:messages data="${flash}" />

    <laser:render template="${customerTypeService.getLicenseFilterTemplatePath()}"/>

    <laser:render template="/templates/license/licenseTable"/>

  <ui:debugInfo>
      <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
  </ui:debugInfo>
  <laser:render template="export/individuallyExportModalLics" model="[modalID: 'individuallyExportModal']" />
<laser:htmlEnd />
