<%@ page import="de.laser.Package" %>
<laser:htmlStart message="package.show.nav.current" />

<ui:breadcrumbs>
    <ui:crumb controller="package" action="index" text="${message(code: 'package.show.all')}"/>
    <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
</ui:breadcrumbs>

<ui:modeSwitch controller="package" action="show" params="${params}"/>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="package" action="current"
                        params="${params + [format: 'csv']}">
                    <g:message code="default.button.exports.csv"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="current" params="${params + [format: 'csv']}">CSV Export</g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <a class="item" data-ui="modal" href="#individuallyExportTippsModal">Click Me Excel Export</a>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="package" action="current"
                        params="${params + [exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="current" params="${params + [exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="package" action="current"
                        params="${params + [exportKBart: true]}">
                    KBART Export
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="current"
                        params="${params + [exportKBart: true]}">KBART Export</g:link>
            </g:else>
        </ui:exportDropdownItem>
    <%--<ui:exportDropdownItem>
        <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
    </ui:exportDropdownItem>
    <ui:exportDropdownItem>
        <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
    </ui:exportDropdownItem>--%>
    </ui:exportDropdown>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${packageInstance.name}" />

<laser:render template="nav"/>


<%--<sec:ifAnyGranted roles="ROLE_ADMIN">
    <laser:render template="/templates/pendingChanges"
              model="${['pendingChanges': pendingChanges, 'flash': flash, 'model': packageInstance]}"/>
</sec:ifAnyGranted>--%>


<ui:messages data="${flash}"/>

<ui:errors bean="${packageInstance}"/>
<div class="ui grid">
    <div class="row">
        <div class="column">
            <laser:render template="/templates/filter/tipp_ieFilter"/>
        </div>
    </div>

    <div class="row">
        <div class="column">

            <div class="ui blue large label"><g:message code="title.plural"/>: <div
                    class="detail">${num_tipp_rows}</div>
            </div>

            <laser:render template="/templates/tipps/table"
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

     //$('#select-all').is( ':checked' )? $('.bulkcheck').attr('checked', false) : $('.bulkcheck').attr('checked', true);
   }

   JSPC.app.confirmSubmit = function () {
     if ( $('#bulkOperationSelect').val() === 'remove' ) {
       var agree=confirm("${message(code: 'default.continue.confirm')}");
          if (agree)
            return true ;
          else
            return false ;
        }
      }
</laser:script>

<laser:htmlEnd />
