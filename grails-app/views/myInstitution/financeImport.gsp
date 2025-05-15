<%@ page import="de.laser.Subscription; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

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
    <ui:renderMarkdown manual="fileImport" />
</div>
<div class="ui segment">

    <g:if test="${params.id}">
        <g:link class="${Btn.ICON.SIMPLE}" style="margin-bottom: 1em" action="generateFinanceImportWorksheet" params="${[id:params.id]}">
            <i class="${Icon.CMD.DOWNLOAD}"></i><g:message code="myinst.financeImport.subscription.template"/>
        </g:link>
    </g:if>
    <g:else>
        <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template.csv')}" download="template_bulk_load_cost_item_records.csv" class="${Btn.ICON.SIMPLE}" style="margin-bottom: 1em">
            <i class="${Icon.CMD.DOWNLOAD}"></i> <g:message code="myinst.financeImport.template"/>
        </a>
    </g:else>

    <g:uploadForm action="processFinanceImport" method="post">
        <ui:msg class="warning" header="Achtung" text="" message="myinst.subscriptionImport.attention" showIcon="true" hideClose="true" />

        <g:if test="${params.id}">
            <g:hiddenField name="subId" value="${params.id}"/>
        </g:if>

        <div class="field">
            <div class="two fields">
                <div class="ui action input">
                    <input type="text" readonly="readonly" class="ui input"
                           placeholder="${message(code: 'myinst.subscriptionImport.uploadCSV')}">

                    <input type="file" name="tsvFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                           style="display: none;">
                    <div class="${Btn.ICON.SIMPLE}">
                        <i class="${Icon.CMD.ATTACHMENT}"></i>
                    </div>
                </div>

                <button class="${Btn.SIMPLE}" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.upload"/></button>
            </div>
        </div>
    </g:uploadForm>
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
