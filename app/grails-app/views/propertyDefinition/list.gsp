<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'propertyDefinition.label', default: 'PropertyDefinition')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="container">

        <div class="page-header">
            <h1><g:message code="default.list.label" args="[entityName]"/></h1>
        </div>

        <g:if test="${flash.message}">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>

        <table class="ui celled striped table">
            <thead>
            <tr>

              <g:sortableColumn property="name" title="Name" />
              <g:sortableColumn property="descr" title="Description" />
              <g:sortableColumn property="type" title="Type" />
                <th class="header"> Occurrences Count</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${propDefInstanceList}" var="propDefInstance">
                <tr>

                    <td>${fieldValue(bean: propDefInstance, field: "name")}</td>

                    <td>${fieldValue(bean: propDefInstance, field: "descr")}</td>

                    <td>${fieldValue(bean: propDefInstance, field: "type")}</td>
      <g:set var="num_lcp" value="${propDefInstance.countOccurrences('com.k_int.kbplus.LicenseCustomProperty','com.k_int.kbplus.SystemAdminCustomProperty','com.k_int.kbplus.OrgCustomProperty')}" />

                    <td> ${num_lcp} </td>
                    <td class="link">
                        <g:link action="edit" id="${propDefInstance.id}"
                                class="ui tiny button">Edit &raquo;</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="pagination">
            <bootstrap:paginate total="${propertyDefinitionTotal}"/>
        </div>

</div>
</body>
</html>