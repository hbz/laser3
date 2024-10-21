<%@ page import="de.laser.ui.Btn; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<laser:htmlStart message="myinst.financeImport.pageTitle" />

  <ui:breadcrumbs>
      <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.financeImport" class="active"/>
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.institutions.financeImport" type="finance" />

    <ui:messages data="${flash}" />

    <p>
        <g:message code="myinst.financeImport.manual.p1"/>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p2"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li1"/></li>
            <li><g:message code="myinst.financeImport.manual.li2"/></li>
            <li><g:message code="myinst.financeImport.manual.li3"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_03.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_03.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_03.png')}"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li4"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_04.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_04.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_04.png')}"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li5"/></li>
            <li><g:message code="myinst.financeImport.manual.li6"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_06.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_06.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_06.png')}"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li7"/></li>
            <li><g:message code="myinst.financeImport.manual.li8"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_08.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_08.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_08.png')}"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li9"/></li>
            <li><g:message code="myinst.financeImport.manual.li10"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_10.png')}"><img class="ui small image" alt="Abbildung_Punkt_01_10.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_01_10.png')}"/></a></p></li>
        </ol>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p3"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li11"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_02_01.png')}"><img class="ui small image" alt="Abbildung_Punkt_02_01.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_02_01.png')}"/></a></p></li>
            %{--<li><g:message code="myinst.financeImport.manual.li12"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_02_02.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_02_02.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li13"/></li>
            <li><g:message code="myinst.financeImport.manual.li14"/></li>
            <li><g:message code="myinst.financeImport.manual.li15"/></li>--}%
            <li><g:message code="myinst.financeImport.manual.li16"/><p><a href="#" class="previewImage" data-src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_02_02a.png')}"><img class="ui small image" alt="Abbildung_Punkt_02_02a.png" src="${resource(dir: 'media', file: 'finance/Abbildung_Punkt_02_02a.png')}"/></a></p></li>
        </ol>
        <g:message code="myinst.financeImport.manual.p4"/>
    </p>
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
              <input class="ui input" type="file" name="tsvFile" id="tsvFile" accept=".txt,.tsv,.csv,text/tab-separated-values"/>
              <input class="${Btn.SIMPLE}" type="submit" value="${message(code:"myinst.financeImport.upload")}"/>
          </g:uploadForm>

    <ui:modal id="fullsizeImage" hideSubmitButton="true">
        <img class="ui image" src="#" alt="fullsize image"/>
    </ui:modal>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.previewImage').click(function() {
            $('#fullsizeImage img').attr('src', $(this).attr('data-src'));
            $('#fullsizeImage').modal('show');
        });
    </laser:script>

<laser:htmlEnd />
