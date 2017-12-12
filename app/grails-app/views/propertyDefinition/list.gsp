<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'propertyDefinition.label', default: 'PropertyDefinition')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div>


            <h1 class="ui header"><g:message code="default.list.label" args="[entityName]"/></h1>


    <semui:messages data="${flash}" />

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
                                class="ui tiny button">Edit</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <semui:paginate total="${propertyDefinitionTotal}"/>

</div>
</body>
</html>