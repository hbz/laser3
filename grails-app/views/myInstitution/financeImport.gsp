<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<laser:htmlStart message="myinst.financeImport.pageTitle" />

  <ui:breadcrumbs>
      <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
    <ui:crumb message="menu.institutions.financeImport" class="active"/>
  </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.institutions.financeImport" type="finance" />

    <ui:messages data="${flash}" />

    <div class="ui flyout" id="help-content" style="padding:50px 0 10px 0;overflow:scroll">
        <h1 class="ui header">
            <g:message code="myinst.financeImport.headline"/>
        </h1>
        <div class="content">
            <table class="ui la-ignore-fixed compact table">
                <thead>
                    <tr>
                        <%-- <th>tsv column name</th>
                        <th>Description</th>
                        <th>maps to</th> --%>
                        <th>${message(code:'myinst.financeImport.tsvColumnName')}</th>
                        <%--<th>${message(code:'myinst.financeImport.descriptionColumnName')}</th>--%>
                        <th>${message(code:'myinst.financeImport.necessaryFormat')}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${mappingCols}" var="mpg">
                        <%
                            List args = []
                            boolean mandatory = false
                            switch(mpg) {
                                case 'status': List<RefdataValue> costItemStatus = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)
                                    costItemStatus.remove(RDStore.COST_ITEM_DELETED)
                                    args.addAll(costItemStatus.collect { it -> it.getI10n('value') })
                                    break
                                case 'currency': mandatory = true
                                    break
                                case 'element': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT).collect { it -> it.getI10n('value') })
                                    break
                                case 'elementSign': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION).collect { it -> it.getI10n('value') })
                                    break
                                    //as of December 3rd '20, Micha said that no reverse charge should be made possible by tax type in order to avoid confusion with users of the interface
                                case 'taxType': List<RefdataValue> taxTypes = RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE)
                                    taxTypes.remove(RDStore.TAX_TYPE_REVERSE_CHARGE)
                                    args.addAll(taxTypes.collect { it -> it.getI10n('value') })
                                    break
                                case 'taxRate': args.addAll([0,5,7,16,19])
                                    break
                            }
                        %>
                        <tr <g:if test="${mandatory}">class="negative"</g:if>>
                            <td>${message(code:"myinst.financeImport.${mpg}")}<g:if test="${mandatory}"><span style="color: #BB1600">*</span></g:if></td>
                            <%--<td>${message(code:"myinst.financeImport.description.${mpg}") ?: ''}</td>--%>
                            <td>${message(code:"myinst.financeImport.format.${mpg}",args:[raw("<ul><li>${args.join('</li><li>')}</li></ul>")]) ?: ''}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </div>

    <p>
        <g:message code="myinst.financeImport.manual.p1"/>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p2"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li1"/></li>
            <li><g:message code="myinst.financeImport.manual.li2"/></li>
            <li><g:message code="myinst.financeImport.manual.li3"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_01_03.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_01_03.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li4"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_01_04.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_01_04.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li5"/></li>
            <li><g:message code="myinst.financeImport.manual.li6"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_01_06.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_01_06.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li7"/></li>
            <li><g:message code="myinst.financeImport.manual.li8"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_01_08.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_01_08.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li9"/></li>
            <li><g:message code="myinst.financeImport.manual.li10"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_01_10.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_01_10.png"/></a></p></li>
        </ol>
    </p>
    <p>
        <g:message code="myinst.financeImport.manual.p3"/>
        <ol>
            <li><g:message code="myinst.financeImport.manual.li11"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_02_01.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_02_01.png"/></a></p></li>
            %{--<li><g:message code="myinst.financeImport.manual.li12"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_02_02.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_02_02.png"/></a></p></li>
            <li><g:message code="myinst.financeImport.manual.li13"/></li>
            <li><g:message code="myinst.financeImport.manual.li14"/></li>
            <li><g:message code="myinst.financeImport.manual.li15"/></li>--}%
            <li><g:message code="myinst.financeImport.manual.li16"/><p><a href="#" class="previewImage" data-src="/assets/manuals/Abbildung_Punkt_02_02a.png"><g:img class="ui small image" file="manuals/Abbildung_Punkt_02_02a.png"/></a></p></li>
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
              <input class="ui button" type="submit" value="${message(code:"myinst.financeImport.upload")}"/>
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
