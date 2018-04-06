<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>


        <h1 class="ui header"><semui:headerIcon /><g:message code="globalDataSync.newTracker" args="[item.name,item.identifier,item.source.name]" /></h1>

      <semui:messages data="${flash}" />


    <div class="ui segment">
      <h1 class="ui header"><g:message code="globalDataSync.selectlocalPackage"/></h1>
      %{--<p>....to be updated by this remote package tracker</p>--}%
      <g:form action="buildMergeTracker" id="${params.id}" method="get">
        <input type="hidden" name="synctype" value="existing"/>
        <fieldset>
          <dl>
            <dt><g:message code="globalDataSync.localPackagetoSync" args="[item.name]"/></dt>
            <dd><g:simpleReferenceTypedown name="localPkg" baseClass="com.k_int.kbplus.Package" style="width:550px;"/></dd>
          </dl>
          <input type="submit" class="ui button"/>
        </fieldset>
      </g:form>
    </div>

  </body>
</html>
