<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>

    <div class="container">
      <div class="page-header">
        <h1><g:message code="globalDataSync.label" /></h1>
      </div>
      <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
      </g:if>
    </div>

    <div class="container" style="text-align:center">
      <g:form action="index" method="get" class="form-inline">
        <label>${message(code: 'globalDataSync.search.text')}: </label> <input type="text" name="q" placeholder="${message(code: 'globalDataSync.search.ph')}" value="${params.q?.encodeAsHTML()}"  />
        <input type="submit" class="btn btn-primary" value="${message(code: 'default.button.search.label')}" />
      </g:form><br/>
    </div>

    <div class="container">
        
      <g:if test="${items != null}">
        <div class="container" style="text-align:center">
          ${message(code:'globalDataSync.pagination.text', args: [offset,(offset + max),globalItemTotal])}
        </div>
      </g:if>
      <table class="table table-bordered table-striped">
        <thead>
          <tr>
            <g:sortableColumn property="identifier"      title="${message(code: 'package.identifier.label'     )}" />
            <g:sortableColumn property="name"            title="${message(code: 'package.name.label'           )}" />
            <g:sortableColumn property="desc"            title="${message(code: 'package.description.label'    )}" />
            <g:sortableColumn property="source.name"     title="${message(code: 'package.source.label'         )}" />
            <g:sortableColumn property="type"            title="${message(code: 'package.type.label'           )}" />
            <g:sortableColumn property="kbplusCompliant" title="${message(code: 'package.kbplusCompliant.label')}" />
            <th>${message(code: 'globalDataSync.actions.label')}</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${items}" var="item">
            <tr>
              <td> <a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "identifier")}</a><br/>
                   <g:message code="globalDataSync.updated.brackets" args="[formatDate(date: item.ts, formatName: 'default.date.format.notime')]" /></td>
              <td> <a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "name")}</a></td>
              <td> <a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "desc")}</a></td>
              <td> <a href="${item.source.uri}?verb=getRecord&amp;identifier=${item.identifier}&amp;metadataPrefix=${item.source.fullPrefix}">
                     ${item.source.name}</a></td>
              <td> <a href="${item.source.baseUrl}search/index?qbe=g:1packages">${item.displayRectype}</a></td>
              <td>${item.kbplusCompliant?.getI10n('value')}</td>
              <td><g:link action="newCleanTracker" controller="globalDataSync" id="${item.id}" class="btn btn-success">${message(code: 'globalDataSync.track_new')}</g:link>
                  <g:link action="selectLocalPackage" controller="globalDataSync" id="${item.id}" class="btn btn-success">${message(code: 'globalDataSync.track_merge')}</g:link>
              </td>
            </tr>
            <g:each in="${item.trackers}" var="tracker">
              <tr>
                <td colspan="6">
                  -> ${message(code: 'globalDataSync.using_id')}
                  <g:if test="${tracker.localOid != null}">
                    <g:if test="${tracker.localOid.startsWith('com.k_int.kbplus.Package')}">
                      <g:link controller="packageDetails" action="show" id="${tracker.localOid.split(':')[1]}">
                        ${tracker.name ?: message(code: 'globalDataSync.noname')}</g:link>
                      <g:if test="${tracker.name == null}">
                        <g:set var="confirm" value="${message(code: 'globalDataSync.cancel.confirm.noname')}" />
                      </g:if>
                      <g:else>
                        <g:set var="confirm" value="${message(code: 'globalDataSync.cancel.confirm', args: [tracker.name])}" />
                      </g:else>
                      <g:link controller="globalDataSync" action="cancelTracking" class="btn btn-danger"
                              params="[trackerId: tracker.id, itemName: fieldValue(bean: item, field: 'name')]"
                              onclick="return confirm('${confirm}')">
                        <g:message code="globalDataSync.cancel" />
                      </g:link>
                    </g:if>
                  </g:if>
                  <g:else>No tracker local oid</g:else>
                </td>
              </tr>
            </g:each>
          </g:each>
        </tbody>
      </table>
      <div class="pagination">
        <bootstrap:paginate  action="index" controller="globalDataSync" params="${params}" next="Next" prev="Prev" max="${max}" total="${globalItemTotal}" />
      </div>
    </div>
  </body>
</html>
