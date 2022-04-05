<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'myinst.financeImport.pageTitle')}</title>
  </head>

  <body>
  <semui:breadcrumbs>
    <semui:crumb message="menu.institutions.financeImport" class="active"/>
  </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.institutions.financeImport')}</h1>

    <semui:messages data="${flash}" />

       <semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.financeImport.headline" />

          <g:if test="${params.id}">
              <g:link action="generateFinanceImportWorksheet" params="${[id:params.id]}">
                  <p>${message(code:'myinst.financeImport.template')}</p>
              </g:link>
          </g:if>
          <g:else>
              <a href="${resource(dir: 'files', file: 'bulk_load_cost_item_records_template_02.csv')}" download="template_bulk_load_cost_item_records.csv">
                  <p>${message(code:'myinst.financeImport.template')}</p>
              </a>
          </g:else>
         <table class="ui celled striped table la-js-responsive-table la-table">
           <thead>
             <tr>
                <%-- <th>tsv column name</th>
                <th>Description</th>
                <th>maps to</th> --%>
                <th>${message(code:'myinst.financeImport.tsvColumnName')}</th>
                <th>${message(code:'myinst.financeImport.descriptionColumnName')}</th>
                <th>${message(code:'myinst.financeImport.necessaryFormat')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${mappingCols}" var="mpg">
                <%
                    List args = []
                    switch(mpg) {
                        case 'status': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS).collect { it -> it.getI10n('value') })
                            break
                        case 'element': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT).collect { it -> it.getI10n('value') })
                            break
                        case 'elementSign': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION).collect { it -> it.getI10n('value') })
                            break
                        //as of December 3rd '20, Micha said that no reverse charge should be made possible by tax type in order to avoid confusion with users of the interface
                        case 'taxType': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.TAX_TYPE).minus(RDStore.TAX_REVERSE_CHARGE).collect { it -> it.getI10n('value') })
                            break
                        case 'taxRate': args.addAll([0,5,7,16,19])
                            break
                    }
                %>
                <tr>
                    <td>${message(code:"myinst.financeImport.${mpg}")}</td>
                    <td>${message(code:"myinst.financeImport.description.${mpg}") ?: ''}</td>
                    <td>${message(code:"myinst.financeImport.format.${mpg}",args:[raw("<ul><li>${args.join('</li><li>')}</li></ul>")]) ?: ''}</td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <g:uploadForm action="processFinanceImport" method="POST">
              <label for="tsvFile">${message(code:'myinst.financeImport.upload')}</label>
              <input class="ui input" type="file" name="tsvFile" id="tsvFile"/>
              <input class="ui button" type="submit" value="${message(code:"myinst.financeImport.upload")}"/>
          </g:uploadForm>

  </body>
</html>
