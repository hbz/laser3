<%@ page import="de.laser.Package" %>

      <ui:breadcrumbs>
          <ui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
          <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
      </ui:breadcrumbs>

      <ui:modeSwitch controller="package" action="${params.action}" params="${params}" />

      <ui:controlButtons>
          <ui:exportDropdown>
              <ui:exportDropdownItem>
                  <g:if test="${filterSet}">
                      <g:link class="item js-open-confirm-modal"
                              data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                              data-confirm-term-how="ok" controller="package" action="${actionName}"
                              params="${params + [format: 'csv']}">
                          <g:message code="default.button.exports.csv"/>
                      </g:link>
                  </g:if>
                  <g:else>
                      <g:link class="item" action="${actionName}" params="${params + [format: 'csv']}">CSV Export</g:link>
                  </g:else>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <a class="item" data-ui="modal" href="#individuallyExportTippsModal">Click Me Excel Export</a>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:if test="${filterSet}">
                      <g:link class="item js-open-confirm-modal"
                              data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                              data-confirm-term-how="ok" controller="package" action="${actionName}"
                              params="${params + [exportXLSX: true]}">
                          <g:message code="default.button.exports.xls"/>
                      </g:link>
                  </g:if>
                  <g:else>
                      <g:link class="item" action="${actionName}" params="${params + [exportXLSX: true]}">
                          <g:message code="default.button.exports.xls"/>
                      </g:link>
                  </g:else>
              </ui:exportDropdownItem>
              <ui:exportDropdownItem>
                  <g:if test="${filterSet}">
                      <g:link class="item js-open-confirm-modal"
                              data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                              data-confirm-term-how="ok" controller="package" action="${actionName}"
                              params="${params + [exportKBart: true]}">
                          KBART Export
                      </g:link>
                  </g:if>
                  <g:else>
                      <g:link class="item" action="${actionName}"
                              params="${params + [exportKBart: true]}">KBART Export</g:link>
                  </g:else>
              </ui:exportDropdownItem>
          </ui:exportDropdown>
          <laser:render template="actions" />
      </ui:controlButtons>


          <ui:h1HeaderWithIcon>
              <g:if test="${editable}"><span id="packageNameEdit"
                        class="xEditableValue"
                        data-type="textarea"
                        data-pk="${packageInstance.class.name}:${packageInstance.id}"
                        data-name="name"
                        data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
              <g:else>${packageInstance.name}</g:else>
          </ui:h1HeaderWithIcon>

  <laser:render template="nav"/>


  <ui:messages data="${flash}" />

  <ui:errors bean="${packageInstance}" />

  <div class="row">
      <div class="column">
          <laser:render template="/templates/filter/tipp_ieFilter"/>
      </div>
  </div>

  <div class="row">
      <div class="column">

          <div class="ui blue large label"><g:message code="title.plural"/>: <div class="detail">${num_tipp_rows}</div>
          </div>

          <laser:render template="/templates/tipps/table"
                    model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
      </div>
  </div>

  <g:if test="${titlesList}">
      <ui:paginate action="${actionName}" controller="package" params="${params}"
                      next="${message(code: 'default.paginate.next')}" prev="${message(code: 'default.paginate.prev')}"
                      maxsteps="${max}" total="${num_tipp_rows}"/>
  </g:if>

  <laser:render template="/templates/export/individuallyExportTippsModal" model="[modalID: 'individuallyExportTippsModal']" />

    <laser:script file="${this.getGroovyPageFileName()}">
      JSPC.app.selectAll = function () {
        $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);
      }

      JSPC.app.confirmSubmit = function () {
        if ( $('#bulkOperationSelect').val() === 'remove' ) {
          var agree=confirm("${message(code:'default.continue.confirm')}");
          if (agree)
            return true ;
          else
            return false ;
        }
      }
    </laser:script>

