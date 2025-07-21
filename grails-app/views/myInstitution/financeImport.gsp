<%@ page import="de.laser.ExportClickMeService; de.laser.Subscription; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<laser:htmlStart message="${pageTitle}" />

  <ui:breadcrumbs>
      <g:if test="${params.id}">
          <ui:crumb controller="sub" action="show" id="${params.id}" text="${Subscription.get(params.id).name}"/>
      </g:if>
      <g:else>
          <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
      </g:else>
      <ui:crumb message="${pageTitle}" class="active"/>
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="${pageTitle}" type="finance" />

    <ui:messages data="${flash}" />

<div class="ui segment la-markdown">
    <div>
        <g:message code="myinst.financeImport.manual.p1"/>
        <img class="ui mini spaced image la-js-questionMark" alt="Abbildung_Fragezeichen_Icon.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Fragezeichen_Icon.png')}"/>
    </div>
    <div class="ui styled fluid accordion">
        <div class="title">
            <i class="dropdown icon"></i>
            <g:message code="myinst.subscriptionImport.csvManual.header"/>
        </div>
        <div class="content">
            <ui:renderMarkdown manual="fileImport" />
        </div>
    </div>
</div>
<div class="ui segment">

    <g:if test="${params.id}">
        <g:link class="${Btn.ICON.SIMPLE} xls" style="margin-bottom: 1em" action="generateFinanceImportWorksheet" params="${[id:params.id, format: ExportClickMeService.FORMAT.XLS.toString()]}">
            <i class="${Icon.CMD.DOWNLOAD}"></i><g:message code="myinst.financeImport.subscription.template"/>
        </g:link>
        <g:link class="${Btn.ICON.SIMPLE} csv" style="margin-bottom: 1em" action="generateFinanceImportWorksheet" params="${[id:params.id, format: ExportClickMeService.FORMAT.CSV.toString()]}">
            <i class="${Icon.CMD.DOWNLOAD}"></i><g:message code="myinst.financeImport.subscription.template"/>
        </g:link>
    </g:if>
    <g:else>
        <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template.xlsx')}" download="template_bulk_load_cost_item_records.xlsx" class="${Btn.ICON.SIMPLE} xls" style="margin-bottom: 1em">
            <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.financeImport.template"/>
        </a>
        <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template.csv')}" download="template_bulk_load_cost_item_records.csv" class="${Btn.ICON.SIMPLE} csv" style="margin-bottom: 1em">
            <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.financeImport.template"/>
        </a>
    </g:else>

    <g:render template="/templates/genericFileImportForm" model="[processAction: 'processFinanceImport', subId: params.id]"/>
</div>

<g:render template="/public/markdownScript" />
<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });
</laser:script>

<laser:htmlEnd />
