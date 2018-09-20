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

    <h1 class="ui header"><semui:headerIcon />${institution?.name} - ${message(code:'menu.institutions.financeImport', default: 'Finance Import')}</h1>

        <g:if test="${loaderResult==null}">
          ${message(code:'myinst.financeImport.headline', default:'Bulk load cost item records')}
          <a href="${resource(dir: 'swagger/docs', file: 'bulk_load_cost_item_records_template_01.csv')}" download="template_bulk_load_cost_item_records.csv">
            <p>${message(code:'myinst.financeImport.template', default:'Template for bulk import.')}</p>
          </a>
         <table class="ui celled striped table la-table">
           <thead>
             <tr>
                <%-- <th>tsv column name</th>
                <th>Description</th>
                <th>maps to</th> --%>
                <th>${message(code:'myinst.financeImport.tsvColumnName', default:'tsv column name')}</th>
                <th>${message(code:'myinst.financeImport.description', default:'Description')}</th>
                <th>${message(code:'myinst.financeImport.mapsTo', default:'maps to')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${grailsApplication.config.financialImportTSVLoaderMappings.cols}" var="mpg">
                <tr>
                  <%-- <td>${mpg.colname}</td>
                  <td>${mpg.desc} --%>
                  <td>${message(code:"myinst.financeImport.${mpg.colname}", default:"${mpg.colname}")}</td>
                  <td>${message(code:"myinst.financeImport.description.${mpg.colname}", default:"${mpg.desc}")}
                    <g:if test="${mpg.type=='vocab'}">
                        <br/>Must be one of : <ul>
                          <g:each in="${mpg.mapping}" var="m,k">
                            <li>${m}</li>
                          </g:each>
                        </ul>
                      </g:if>
                  </td>
                  <td></td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <g:form action="financeImport" method="post" enctype="multipart/form-data" params="${[shortcode:params.shortcode]}">
            <dl>
              <div class="field">
                <dt>${message(code:'myinst.financeImport.upload', default:'Upload TSV File')}</dt>
                <dd>
                  <input type="file" name="tsvfile" />
                </dd>
              </div>
              <div class="field">
                <%-- <dt>Dry Run</dt> --%>
                <dt>${message(code:'myinst.financeImport.dryrun', default:'Dry Run')}</dt>
                <dd>
                  <input class="ui button" type="checkbox" name="dryRun" checked value="Y" />
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
