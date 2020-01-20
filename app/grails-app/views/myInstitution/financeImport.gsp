<%@ page import="de.laser.helper.RDConstants; com.k_int.kbplus.RefdataCategory" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code:'myinst.financeImport.pageTitle')}</title>
  </head>

  <body>
  <semui:breadcrumbs>
    <semui:crumb message="menu.institutions.financeImport" class="active"/>
  </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.institutions.financeImport')}</h1>
       <semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.financeImport.headline" />

          <g:if test="${params.id}">
              <g:link action="generateFinanceImportWorksheet" params="${[id:params.id]}">
                  <p>${message(code:'myinst.financeImport.template', default:'Template for bulk import.')}</p>
              </g:link>
          </g:if>
          <g:else>
              <a href="${resource(dir: 'resources/downloadFile', file: 'bulk_load_cost_item_records_template_02.csv')}" download="template_bulk_load_cost_item_records.csv">
                  <p>${message(code:'myinst.financeImport.template', default:'Template for bulk import.')}</p>
              </a>
          </g:else>
         <table class="ui celled striped table la-table">
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
                        case 'taxType': args.addAll(RefdataCategory.getAllRefdataValues('TaxType').collect { it -> it.getI10n('value') })
                            break
                        case 'taxRate': args.addAll([0,7,19])
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

          <g:form action="processFinanceImport" method="post" enctype="multipart/form-data">
            <dl>
              <div class="field">
                <dt>${message(code:'myinst.financeImport.upload', default:'Upload TSV File')}</dt>
                <dd>
                  <input type="file" name="tsvFile" />
                </dd>
              </div>
              <%-- <div class="field">
                <dt>Dry Run</dt>
                <dt>${message(code:'myinst.financeImport.dryrun', default:'Dry Run')}</dt>
                <dd>
                  <input class="ui button" type="checkbox" name="dryRun" checked value="true" />
                </dd>
              </div> --%>
              <button class="ui button" name="load" type="submit" value="Go">${message(code:"myinst.financeImport.upload", default:'Upload...')}</button>
            </dl>
          </g:form>

  </body>
</html>
