<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Finance Import</title>
  </head>

  <body>
  <semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb message="menu.institutions.financeImport" class="active"/>
  </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.financeImport')}</h1>

        <g:if test="${loaderResult==null}">
          ${message(code:'myinst.financeImport.headline', default:'Bulk load cost item records')}
          <%-- continue here: make the template and make then test processes --%>
          <a href="${resource(dir: 'resources/downloadFile', file: 'bulk_load_cost_item_records_template_02.csv')}" download="template_bulk_load_cost_item_records.csv">
            <p>${message(code:'myinst.financeImport.template', default:'Template for bulk import.')}</p>
          </a>
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
                <tr>
                  <td>${message(code:"myinst.financeImport.${mpg}")}</td>
                  <td>${message(code:"myinst.financeImport.description.${mpg}") ?: ''}</td>
                  <td>${message(code:"myinst.financeImport.format.${mpg}") ?: ''}</td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <g:form action="processFinanceImport" method="post" enctype="multipart/form-data" params="${[shortcode:params.shortcode]}">
            <dl>
              <div class="field">
                <dt>${message(code:'myinst.financeImport.upload', default:'Upload TSV File')}</dt>
                <dd>
                  <input type="file" name="tsvFile" />
                </dd>
              </div>
              <div class="field">
                <%-- <dt>Dry Run</dt> --%>
                <dt>${message(code:'myinst.financeImport.dryrun', default:'Dry Run')}</dt>
                <dd>
                  <input class="ui button" type="checkbox" name="dryRun" checked value="true" />
                </dd>
              </div>
              <button class="ui button" name="load" type="submit" value="Go">${message(code:"myinst.financeImport.upload", default:'Upload...')}</button>
            </dl>
          </g:form>
        </g:if>


      <g:if test="${loaderResult}">
        <table class="ui celled striped table la-table">
          <thead>
            <tr>
              <th></th>
              <g:each in="${loaderResult.columns}" var="c">
                <th>${c}</th>
              </g:each>
            </tr>
          </thead>
          <tbody>
            <g:each in="${loaderResult.log}" var="logEntry">
              <tr>
                <td rowspan="2">${logEntry.rownum}</td>
                <g:each in="${logEntry.rawValues}" var="v">
                  <td>${v}</td>
                </g:each>
              </tr>
              <tr ${logEntry.error?'style="background-color:red;"':''}>
                <td colspan="${loaderResult.columns.size()}">
                  ${logEntry.error?'Row ERROR':'Row OK'}
                  <ul>
                    <g:each in="${logEntry.messages}" var="m">
                      <li>${m}</li>
                    </g:each>
                  </ul>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </g:if>

  </body>
</html>
