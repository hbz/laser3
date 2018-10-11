<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
  </head>
  <body>

    <h1 class="ui left aligned icon header"><semui:headerIcon />Subscription Offered - Manual Upload</h1>

    <semui:messages data="${flash}" />

    <semui:errors bean="${packageInstance}" />

        <g:form action="so" method="post" enctype="multipart/form-data" class="ui form">

            Updload File: <input type="file" id="soFile" name="soFile"/><br/>

            Doc Style: <select name="docstyle">
              <option value="csv" selected>Comma Separated</option>
              <option value="tsv">Tab Separated</option>
            </select></br>

            Override Character Set Test: <input type="checkbox" name="OverrideCharset" checked="false"/>

            <button type="submit" class="ui button">Upload SO</button>
        </g:form>

        <g:if test="${new_pkg_id && new_sub_id}">
          <g:link controller="subscriptionDetails" action="index" id="${new_sub_id}">Created subscription ${new_sub_id}</g:link><br/>
          <g:link controller="packageDetails" action="show" id="${new_pkg_id}">Created package ${new_pkg_id}</g:link>
        </g:if>

  </body>
</html>
