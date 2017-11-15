<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</title>
  </head>

  <body>

    <div>
      <ul class="breadcrumb">
        <li> <g:link controller="home" action="index">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
        <li> <g:link controller="dataManager" action="index">${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</g:link> <span class="divider">/</span> </li>
        <li> ${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title Management')} </li>
      </ul>
    </div>

    <g:if test="${flash.message}">
      <div>
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </div>
    </g:if>

    <g:if test="${flash.error}">
      <div>
        <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
      </div>
    </g:if>

    <div>
      <h2>${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title Management')} : ${titleInstanceTotal} ${message(code:'datamanager.deletedTitleManagement.del_titles', default:'Deleted Titles')}</h2>
    </div>

    <div>

      <table class="ui celled striped table">
        <thead>
          <tr>
            <g:sortableColumn property="title" title="${message(code: 'title.label', default: 'Title')}" />
            <th></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${titleInstanceList}" var="titleInstance">
            <tr>
              <td>${fieldValue(bean: titleInstance, field: "title")}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <div class="pagination">
        <bootstrap:paginate  action="deletedTitleManagement" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${titleInstanceTotal}" />
      </div>
    </div>


  </body>
</html>
