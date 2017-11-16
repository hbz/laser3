<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>


        <h1 class="ui header">Track ${item.name}(${item.identifier}) from ${item.source.name}</h1>

      <semui:messages data="${flash}" />


    <div class="container well">
      <h1 class="ui header">Select local package</h1>
      <p>....to be updated by this remote package tracker</p>
      <g:form action="buildMergeTracker" id="${params.id}" method="get">
        <input type="hidden" name="synctype" value="existing"/>
        <fieldset>
          <dl>
            <dt>Local Package To Sync with Remote Package</dt>
            <dd><g:simpleReferenceTypedown name="localPkg" baseClass="com.k_int.kbplus.Package" style="width:550px;"/></dd>
          </dl>
          <input type="submit"/>
        </fieldset>
      </g:form>
    </div>

  </body>
</html>
