<%--
  Created by IntelliJ IDEA.
  User: ioannis
  Date: 15/05/2014
  Time: 15:00
--%>

<%@ page import="com.k_int.kbplus.TitleInstance" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstance.label', default: 'Title Instance')}"/>
    <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>

<div>
  <ul class="breadcrumb">
    <li> <g:link controller="home" action="index">Home</g:link> <span class="divider">/</span> </li>
    <li> <g:link controller="titleDetails" action="show" id="${ti?.id}">Title ${ti?.title}</g:link> </li>

    <li class="dropdown pull-right">

    <g:if test="${editable}">
      <li class="pull-right"><span class="badge badge-warning">Editable</span>&nbsp;</li>
    </g:if>
  </ul>
</div>

<div>
    <div class="span12">


  <h1 class="ui header">${ti.title}</h1>

    <g:render template="nav" />
    
    <g:if test="${flash.message}">
        <div><bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert></div>
    </g:if>

    <g:if test="${flash.error}">
        <div><bootstrap:alert class="alert-error">${flash.error}</bootstrap:alert></div>
    </g:if>

    <g:if test="${availability?.size() > 0}">

      <div class="container alert-warn">
        <table class="ui celled table">
          <thead>
            <tr>
              <th rowspan="2">IE</th>
              <th>Subscribing Institution</th>
              <th>Status</th>
              <th>Coverage Start</th>
              <th>Coverage End</th>
            </tr>
            <tr>
              <th colspan="4">License properties</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${availability}" var="a">
              <tr>
                <td rowspan="2">${a.id}</a></td>
                <td>${a.subscription?.subscriber?.name}</a></td>
                <td>${a.status?.value}</a></td>
                <td>Start Date : <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${a.startDate}"/><br/>
                    Start Volume : ${a.startVolume}<br/>
                    Start Issue : ${a.startIssue}</td>
                <td>End Date : <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${a.endDate}"/><br/>
                    End Volume ${a.endVolume}<br/>
                    End Issue : ${a.endIssue}</td>
              </tr>
              <tr>
                <td colspan="4">
                  <ul>
                    <g:each in="${a.subscription?.owner?.customProperties}" var="lp">
                      <g:if test="${lp.type?.name.startsWith('ILL')}">
                        <li>${lp.type?.name} value:${lp.value}</li>
                      </g:if>
                    </g:each>
                  </ul>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </g:if>
  </div>
  </div>
  </div>

</body>
</html>
