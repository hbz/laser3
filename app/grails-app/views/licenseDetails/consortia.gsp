<%@ page import="com.k_int.kbplus.License" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'license', default: 'License')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:messages data="${flash}" />

    <h1 class="ui header">${license?.reference}</h1>

    <g:render template="nav"/>


<div>
<g:if test="${consortia}">
<h3 class="ui header"> Institutions for ${consortia.name} consortia </h3>
<br><p> The following list displays all members of ${consortia.name} consortia. To create child licenses
    select the desired checkboxes and click 'Create child licenses'</p><br>
<g:form action="generateSlaveLicenses" controller="licenseDetails" method="POST">
<input type="hidden" name="baselicense" value="${license.id}"/>
<input type="hidden" name="id" value="${id}"/>
<table class="ui celled table">
<thead>
    <tr>
        <th>Organisation</th>
        <th>Contains  License Copy </th>
        <th>Create Child License</th>
    </tr>
</thead>
<tbody>
    <g:each in="${consortiaInstsWithStatus}" var="pair">
        <tr>
            <td>${pair.getKey().name}</td>
            <td><g:refdataValue cat="YNO" val="${pair.getValue()}" /></td>
            <td><g:if test="${editable}"><input type="checkbox" name="_create.${pair.getKey().id}" value="true"/>
                    </g:if></td>
        </tr>
    </g:each>
</tbody>
</table>
<dl>
<dt>License name: <input type="text" name="lic_name"
    value="Child license for ${license?.reference}"/></dt>
<dd><input type="submit" class="ui primary button" value="Create child licenses"/></dd>
</dl>
</g:form>
</g:if>
</div>
</body>
</html>
