<%@ page import="de.laser.Package" %>
<laser:htmlStart message="package.show.nav.current" />

<ui:breadcrumbs>
    <ui:crumb controller="package" action="index" text="${message(code: 'package.show.all')}"/>
    <ui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
</ui:breadcrumbs>

<ui:modeSwitch controller="package" action="show" params="${params}"/>

<ui:controlButtons>
    <ui:exportDropdown>
        <%--
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
        --%>
        <ui:exportDropdownItem>
            <%--
            <g:if test="${filterSet}">
                <g:link class="item kbartExport js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok"
                        params="${params + [exportKBart: true]}">
                    KBART Export
                </g:link>
            </g:if>
            <g:else>
                --%>
            <%-- enabling LAS:eR confirms for AJAX links requires additional engineering --%>
                <g:link class="item kbartExport"
                        params="${params + [exportKBart: true]}">KBART Export</g:link>
            <%--</g:else>--%>
        </ui:exportDropdownItem>
    <%--<ui:exportDropdownItem>
        <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
    </ui:exportDropdownItem>
    <ui:exportDropdownItem>
        <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
    </ui:exportDropdownItem>--%>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${packageInstance.name}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyPkg: isMyPkg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<ui:messages data="${flash}"/>

<ui:errors bean="${packageInstance}"/>

<laser:render template="/templates/filter/tipp_ieFilter"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <span class="ui circular label">${num_tipp_rows}</span> <g:message code="title.filter.result"/>
</h3>

<div id="downloadWrapper"></div>
<g:if test="${titlesList}">
    <div class="ui form">
        <div class="two wide fields">
            <div class="field">
                <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 2, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
            </div>
            <div class="field la-field-noLabel">
                <button class="ui button la-js-closeAll-showMore right floated ">${message(code: "accordion.button.closeAll")}</button>
            </div>
        </div>
    </div>

    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/table_accordion" model="[tipps: titlesList, showPackage: false, showPlattform: true]"/>
            </div>
        </div>
    </div>

    <div class="ui clearing segment la-segmentNotVisable">
        <button class="ui button la-js-closeAll-showMore right floated">${message(code: "accordion.button.closeAll")}</button>
    </div>

    <ui:paginate action="current" controller="package" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>


<laser:render template="/templates/export/individuallyExportTippsModal" model="[modalID: 'individuallyExportTippsModal']" />

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.selectAll = function () {
      $('#select-all').is( ":checked")? $('.bulkcheck').prop('checked', true) : $('.bulkcheck').prop('checked', false);

     //$('#select-all').is( ':checked' )? $('.bulkcheck').attr('checked', false) : $('.bulkcheck').attr('checked', true);
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

<laser:htmlEnd />
