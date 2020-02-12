
<%@ page import="com.k_int.kbplus.TitleInstancePackagePlatform" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'titleInstancePackagePlatform.label')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
  </head>
  <body>

      <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="default.show.label" args="[entityName]" /></h1>
      <semui:messages data="${flash}" />

        <div class="ui grid">

      <div class="twelve wide column">

          <div class="inline-lists">

              <g:if test="${titleInstancePackagePlatformInstance?.pkg}">
                  <dl>
                      <dt><g:message code="package.label" /></dt>
                      <dd><g:link controller="package" action="show" id="${titleInstancePackagePlatformInstance?.pkg?.id}">${titleInstancePackagePlatformInstance?.pkg?.name} (id: ${titleInstancePackagePlatformInstance?.pkg?.identifier})</g:link></dd>
                  </dl>
              </g:if>

              <g:if test="${titleInstancePackagePlatformInstance?.platform}">
                  <dl>
                      <dt><g:message code="platform.label" /></dt>
                      <dd><g:link controller="platform" action="show" id="${titleInstancePackagePlatformInstance?.platform?.id}">${titleInstancePackagePlatformInstance?.platform?.name}</g:link></dd>
                  </dl>
              </g:if>

              <g:if test="${titleInstancePackagePlatformInstance?.title}">
                  <dl>
                      <dt><g:message code="title.label" /></dt>
                      <dd><g:link controller="title" action="show" id="${titleInstancePackagePlatformInstance?.title?.id}">${titleInstancePackagePlatformInstance?.title?.title}</g:link></dd>
                  </dl>
              </g:if>

              <g:if test="${titleInstancePackagePlatformInstance?.hostPlatformURL}">
                  <dl>
                      <dt><g:message code="tipp.hostPlatformURL" default="Host Platform URL" /></dt>
                      <dd><a href="${titleInstancePackagePlatformInstance?.hostPlatformURL}" target="new"><g:fieldValue bean="${titleInstancePackagePlatformInstance}" field="hostPlatformURL"/></a></dd>
                  </dl>
              </g:if>

              <dl>
                  <g:each in="${titleInstancePackagePlatformInstance?.coverages}" var="covStmt">
                      <dt><g:message code="tipp.startDate" /></dt>
                      <dd><g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.startDate}" /></dd>
                      <dt><g:message code="tipp.startVolume" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="startVolume"/></dd>
                      <dt><g:message code="tipp.startIssue" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="startIssue"/></dd>
                      <dt><g:message code="tipp.endDate" /></dt>
                      <dd><g:formatDate format="${message(code:'default.date.format.notime')}" date="${covStmt.endDate}" /></dd>
                      <dt><g:message code="tipp.endVolume" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="endVolume"/></dd>
                      <dt><g:message code="tipp.endIssue" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="endIssue"/></dd>
                      <dt><g:message code="tipp.embargo" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="embargo"/></dd>
                      <dt><g:message code="tipp.coverageDepth" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="coverageDepth"/></dd>
                      <dt><g:message code="tipp.coverageNote" /></dt>
                      <dd><g:fieldValue bean="${covStmt}" field="coverageNote"/></dd>
                  </g:each>
              </dl>

          </div>

        <g:form>
          <sec:ifAnyGranted roles="ROLE_ADMIN">
          <g:hiddenField name="id" value="${titleInstancePackagePlatformInstance?.id}" />
          
          <div class="ui form-actions">
            <g:link class="ui button" action="edit" id="${titleInstancePackagePlatformInstance?.id}">
              <i class="write icon"></i>
              <g:message code="default.button.edit.label" />
            </g:link>
            <button class="ui negative button" type="submit" name="_action_delete">
              <i class="trash alternate icon"></i>
              <g:message code="default.button.delete.label" />
            </button>
          </div>
           </sec:ifAnyGranted>
        </g:form>

      </div><!-- .twelve -->

      <aside class="four wide column">
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
      </aside><!-- .four -->

    </div><!-- .grid -->
  </body>
</html>
