<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'package.label', default: 'Package')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="row-fluid">
      
      <div class="span2">
        <div class="well">
          <ul class="nav nav-list">
            <li class="nav-header">${entityName}</li>
            <li>
              <g:link class="list" action="list">
                <i class="icon-list"></i>
                <g:message code="default.list.label" args="[entityName]" />
              </g:link>
            </li>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
            <li>
              <g:link class="create" action="create">
                <i class="icon-plus"></i>
                <g:message code="default.create.label" args="[entityName]" />
              </g:link>
            </li>
            </sec:ifAnyGranted>
          </ul>
        </div>
      </div>
      
      <div class="span10">

          <h1 class="ui header">${message(code:'package.label', default:'Package')} : ${packageInstance?.name} (${packageInstance?.contentProvider?.name})</h1>


        <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>

          <div class="inline-lists">
        
          <g:if test="${packageInstance?.identifier}">
              <dl>
                <dt><g:message code="package.identifier.label" default="Identifier" /></dt>
                <dd><g:fieldValue bean="${packageInstance}" field="identifier"/></dd>
              </dl>
          </g:if>

          <g:if test="${packageInstance?.contentProvider}">
              <dl>
                 <dt><g:message code="package.content_provider" default="Content Provider" /></dt>
                <dd><g:link controller="org" action="show" id="${packageInstance.contentProvider.id}"><g:fieldValue bean="${packageInstance}" field="contentProvider.name"/></g:link></dd>
              </dl>
          </g:if>
        
          <g:if test="${packageInstance?.name}">
              <dl>
                  <dt><g:message code="package.name.label" default="Name" /></dt>
                  <dd><g:fieldValue bean="${packageInstance}" field="name"/></dd>
              </dl>           
          </g:if>
              
          </div>
        
            <h6>${message(code:'package.show.byPlatform', default:'Availability of titles in this package by platform')}</h6>
      
          <g:set var="counter" value="${1}" />
          <table class="ui celled striped table">
            <tr>
              <th rowspan="2" style="width: 2%;">#</th>
              <th rowspan="2" style="width: 20%;">${message(code:'title.label', default:'Title')}</th>
              <th rowspan="2" style="width: 10%;">ISSN</th>
              <th rowspan="2" style="width: 10%;">eISSN</th>
              <th colspan="${platforms.size()}">${message(code:'platform.label', default:'Provided by platform')}</th>
            </tr>
            <tr>
              <g:each in="${platforms}" var="p">
                <th><g:link controller="platform" action="show" id="${p.id}">${p.name}</g:link></th>
              </g:each>
            </tr>
            <g:each in="${titles}" var="t">
              <tr>
                <td>${counter++}</td>
                <td style="text-align:left;"><g:link controller="titleInstance" action="show" id="${t.title.id}">${t.title.title}</g:link>&nbsp;</td>
                <td style="white-space:nowrap;">${t?.title?.getIdentifierValue('ISSN')}</td>
                <td style="white-space:nowrap;">${t?.title?.getIdentifierValue('eISSN')}</td>
                <g:each in="${crosstab[t.position]}" var="tipp">
                  <g:if test="${tipp}">
                    <td style="white-space:nowrap;">
                      ${message(code:'default.from', default:'from')}: <g:formatDate format="dd MMM yyyy" date="${tipp.startDate}"/> 
                        <g:if test="${tipp.startVolume}"> / ${message(code:'tipp.volume' ,default:'volume')}: ${tipp.startVolume} </g:if>
                        <g:if test="${tipp.startIssue}"> / ${message(code:'tipp.issue', default:'issue')}: ${tipp.startIssue} </g:if> <br/>
                      ${message(code:'default.to', default:'to')}:  <g:formatDate format="dd MMM yyyy" date="${tipp.endDate}"/> 
                        <g:if test="${tipp.endVolume}"> / ${message(code:'tipp.volume' ,default:'volume')}: ${tipp.endVolume}</g:if>
                        <g:if test="${tipp.endIssue}"> / ${message(code:'tipp.issue', default:'issue')}: ${tipp.endIssue}</g:if> <br/>
                      ${message(code:'tipp.coverageDepth', default:'coverage Depth')}: ${tipp.coverageDepth}</br>
                      <g:link controller="titleInstancePackagePlatform" action="show" id="${tipp.id}">${message(code:'platform.show.full_tipp', default:'Full TIPP Details')}</g:link>
                  </g:if>
                  <g:else>
                    <td></td>
                  </g:else>
                  </td>
                </g:each>
              </tr>
            </g:each>
          </table>


        <g:form>
          <sec:ifAnyGranted roles="ROLE_ADMIN">
          <g:hiddenField name="id" value="${packageInstance?.id}" />
          <div class="form-actions">
            <g:link class="ui button" action="edit" id="${packageInstance?.id}">
              <i class="icon-pencil"></i>
              <g:message code="default.button.edit.label" default="Edit" />
            </g:link>
            <button class="ui negative button" type="submit" name="_action_delete">
              <i class="icon-trash icon-white"></i>
              <g:message code="default.button.delete.label" default="Delete" />
            </button>
          </div>
          </sec:ifAnyGranted>
        </g:form>

      </div>

    </div>
  </body>
</html>
