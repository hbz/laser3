<%@ page import="de.laser.Subscription; de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

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

    <div>
        <g:message code="myinst.financeImport.manual.p1"/>
        <img class="ui mini image" alt="Abbildung_Fragezeichen_Icon.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Fragezeichen_Icon.png')}"/>
    </div>
    <div>
        <g:message code="myinst.financeImport.manual.p2"/>
        <ul>
            <li><g:message code="myinst.financeImport.manual.li1"/></li>
            <li><g:message code="myinst.financeImport.manual.li2"/></li>
            <li><g:message code="myinst.financeImport.manual.li3"/></li>
        </ul>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_1.png')}"><img class="ui large image" alt="financeupload_1.png" src="${resource(dir: 'media', file: 'finance/financeupload_1.png')}"/></a>
    </div>
    <hr/>
    <div>
        <ul>
            <li><g:message code="myinst.financeImport.manual.li4"/></li>
        </ul>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_2.png')}"><img class="ui large image" alt="financeupload_2.png" src="${resource(dir: 'media', file: 'finance/financeupload_2.png')}"/></a>
    </div>
    <hr/>
    <div>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li5"/></li>
            <li><g:message code="myinst.financeImport.manual.li6"/></li>
            <li><g:message code="myinst.financeImport.manual.li7"/></li>
        </ol>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_3.png')}"><img class="ui large image" alt="financeupload_3.png" src="${resource(dir: 'media', file: 'finance/financeupload_3.png')}"/></a>
    </div>
    <hr/>
    <div>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li8"/></li>
            <li><g:message code="myinst.financeImport.manual.li9"/></li>
        </ol>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_4.png')}"><img class="ui large image" alt="financeupload_4.png" src="${resource(dir: 'media', file: 'finance/financeupload_4.png')}"/></a>
    </div>
    <hr/>
    <div>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li10"/></li>
            <li><g:message code="myinst.financeImport.manual.li11"/></li>
        </ol>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_5.png')}"><img class="ui large image" alt="financeupload_5.png" src="${resource(dir: 'media', file: 'finance/financeupload_5.png')}"/></a>
    </div>
    <hr/>
    <div>
        <g:message code="myinst.financeImport.manual.p3"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li12"/></li>
            <li><g:message code="myinst.financeImport.manual.li13"/></li>
        </ol>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"><img class="ui large image" alt="financeupload_6.png" src="${resource(dir: 'media', file: 'finance/financeupload_6.png')}"/></a>
    </div>
    <hr/>
    <div>
        <ul>
            <li><g:message code="myinst.financeImport.manual.li17"/></li>
        </ul>
        <a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"><img class="ui large image" alt="financeupload_7.png" src="${resource(dir: 'media', file: 'finance/financeupload_7.png')}"/></a>
        <g:message code="myinst.financeImport.manual.p4"/>
    </div>
          <g:if test="${params.id}">
              <g:link action="generateFinanceImportWorksheet" params="${[id:params.id]}">
                  <p>${message(code:'myinst.financeImport.subscription.template')}</p>
              </g:link>
          </g:if>
          <g:else>
              <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template.csv')}" download="template_bulk_load_cost_item_records.csv">
                  <p>${message(code:'myinst.financeImport.template')}</p>
              </a>
          </g:else>


          <g:uploadForm action="processFinanceImport" method="POST">
              <g:if test="${params.id}">
                  <g:hiddenField name="subId" value="${params.id}"/>
              </g:if>
              <label for="tsvFile">${message(code:'myinst.financeImport.upload')}</label>
              <input class="ui input" type="file" name="tsvFile" id="tsvFile" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"/>
              <input class="${Btn.SIMPLE}" type="submit" value="${message(code:"myinst.financeImport.upload")}"/>
          </g:uploadForm>

    <ui:modal id="fullsizeImage" hideSubmitButton="true" modalSize="large">
        <img class="ui fluid image" src="#" alt="fullsize image"/>
    </ui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.previewImage').click(function() {
            $('#fullsizeImage img').attr('src', $(this).attr('data-src'));
            $('#fullsizeImage').modal('show');
        });
    </laser:script>

<laser:htmlEnd />
