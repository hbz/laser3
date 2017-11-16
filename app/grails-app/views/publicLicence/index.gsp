<!doctype html>



<html>

  <head>
    <meta name="layout" content="pubbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} Public Licenses</title>
    <r:require module='annotations' />
  </head>


  <body class="public">

  <g:render template="public_navbar" contextPath="/templates" model="['active': 'publicLicense']"/>


  <div>
      <h1 class="ui header">Public Licenses</h1>
  </div>


  <div>


    <div id="resultsarea">
      <table class="ui celled striped table">
        <thead>
          <tr style="white-space: nowrap">
          <g:sortableColumn property="reference" title="Reference" />
          </tr>
        </thead>
        <tbody>
          <g:each in="${licenses}" var="lic">
            <tr>
              
      <td> <g:link action="show" id="${lic.id}">${lic.reference}</g:link></td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>
    <div class="paginateButtons" style="text-align:center">
    <span><g:paginate max="${max}" controller="${controller}" action="index" params="${params}" next="Next" prev="Prev" total="${licenses.totalCount}" /></span></div>
     
  </div>



  </body>

</html>
