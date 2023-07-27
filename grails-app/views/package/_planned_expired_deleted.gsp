<%@ page import="de.laser.Package" %>

      <ui:breadcrumbs>
          <ui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
          <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
      </ui:breadcrumbs>

      <ui:modeSwitch controller="package" action="${params.action}" params="${params}" />

      <ui:controlButtons>
          <ui:exportDropdown>
              <%--
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
              --%>
              <g:if test="${num_tipp_rows < 1000000}">
                  <ui:exportDropdownItem>
                      <a class="item" data-ui="modal" href="#individuallyExportTippsModal">Export</a>
                  </ui:exportDropdownItem>
              </g:if>
              <g:else>
                  <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
              </g:else>
              <%--
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
              --%>
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
      </ui:controlButtons>


          <ui:h1HeaderWithIcon>
              <g:if test="${editable}"><span id="packageNameEdit"
                        class="xEditableValue"
                        data-type="textarea"
                        data-pk="${packageInstance.class.name}:${packageInstance.id}"
                        data-name="name"
                        data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${packageInstance.name}</span></g:if>
              <g:else>${packageInstance.name}</g:else>
              <g:if test="${isMyPkg}">
                  <laser:render template="/templates/iconObjectIsMine"/>
              </g:if>
          </ui:h1HeaderWithIcon>

  <laser:render template="nav"/>


  <ui:messages data="${flash}" />

  <ui:errors bean="${packageInstance}" />

<div class="ui grid">
    <div class="row">
        <div class="column">
            <laser:render template="/templates/filter/tipp_ieFilter"/>
        </div>
    </div>

    <div class="row">
        <div class="eight wide column">
            <h3 class="ui icon header la-clear-before la-noMargin-top"><span
                    class="ui circular  label">${num_tipp_rows}</span> <g:message code="title.filter.result"/></h3>
        </div>

    </div>
</div>
<div id="downloadWrapper"></div>
<%
    Map<String, String>
    sortFieldMap = ['sortname': message(code: 'title.label')]
    if (journalsOnly) {
        sortFieldMap['startDate'] = message(code: 'default.from')
        sortFieldMap['endDate'] = message(code: 'default.to')
    } else {
        sortFieldMap['dateFirstInPrint'] = message(code: 'tipp.dateFirstInPrint')
        sortFieldMap['dateFirstOnline'] = message(code: 'tipp.dateFirstOnline')
    }
%>
<div class="ui form">
    <div class="three wide fields">
        <div class="field">
            <ui:sortingDropdown noSelection="${message(code:'default.select.choose.label')}" from="${sortFieldMap}" sort="${params.sort}" order="${params.order}"/>
        </div>
    </div>
</div>
<div class="ui grid">
    <div class="row">
        <div class="column">
            <laser:render template="/templates/tipps/table_accordion"
                          model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
        </div>
    </div>
</div>

<g:if test="${titlesList}">
    <ui:paginate action="current" controller="package" params="${params}"
                 max="${max}" total="${num_tipp_rows}"/>
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

    $('.kbartExport').click(function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();
        $.ajax({
            url: "<g:createLink action="current" params="${params + [exportKBart: true]}"/>",
            type: 'POST',
            contentType: false
        }).done(function(response){
            $("#downloadWrapper").html(response);
            $('#globalLoadingIndicator').hide();
        });
    });
    </laser:script>

