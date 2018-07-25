<%@ page import="com.k_int.kbplus.Package" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title><g:message code="default.edit.label" args="[entityName ?: message(code:'title.label')]" /></title>
  </head>
  <body>

    <h1 class="ui header"><semui:headerIcon />${message(code:'title.findTitleMatches.label', default:'New Title - Step 1')}</h1>

    <semui:messages data="${flash}" />

    <p>${message(code:'title.findTitleMatches.note')}</p>

            <semui:simpleForm controller="titleDetails" action="findTitleMatches" method="get" message="title.findTitleMatches.proposed">
              <input type="text" name="proposedTitle" value="${params.proposedTitle}" />
              <label>&nbsp;</label>
              <div class="fields">
                  <div class="field">
                    <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.filterreset.label')}</a>
                    </div>
                  <div class="field">
                    <input type="submit" value="${message(code:'default.button.filter.label', default:'Filter')}" class="ui button">
                  </div>
              </div>
            </semui:simpleForm>

            <br/>

            <g:if test="${titleMatches != null}">
              <g:if test="${titleMatches.size()>0}">
                <table class="ui celled la-table table">
                  <thead>
                    <tr>
                      <th>${message(code:'title.label', default:'Title')}</th>
                      <th>${message(code:'indentifier.plural', default:'Identifiers')}</th>
                      <th>${message(code:'org.plural', default:'Orgs')}</th>
                      <th>${message(code:'default.key.label', default:'Key')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <g:each in="${titleMatches}" var="titleInstance">
                      <tr>
                        <td>${titleInstance.title} <g:link controller="titleDetails" action="show" id="${titleInstance.id}">(${message(code:'default.button.edit.label', default:'Edit')})</g:link></td>
                        <td><ul><g:each in="${titleInstance.ids.sort{it.identifier.ns.ns}}" var="id"><li>${id.identifier.ns.ns}: ${id.identifier.value}</li></g:each></ul></td>
                        <td>
                          <ul>
                            <g:each in="${titleInstance.orgs}" var="org">
                              <li>${org.org.name} (${org.roleType?.value?:''})</li>
                            </g:each>
                          </ul>
                        </td>
                        <td>${titleInstance.keyTitle}</td>
                      </tr>
                    </g:each>
                  </tbody>
                </table>
                <bootstrap:alert class="alert-info">
                  ${message(code:'title.findTitleMatches.match', args:[params.proposedTitle])}
                </bootstrap:alert>
                <g:link controller="titleDetails" action="createTitle" class="ui negative button" params="${[title:params.proposedTitle, typ: 'Journal']}">${message(code:'title.findTitleMatches.create_for_journal', default:'Create New Journal Title for')} <em>"${params.proposedTitle}"</em></g:link>
                <g:link controller="titleDetails" action="createTitle" class="ui negative button" params="${[title:params.proposedTitle, typ: 'Ebook']}">${message(code:'title.findTitleMatches.create_for_ebook', default:'Create New eBook Title for')} <em>"${params.proposedTitle}"</em></g:link>
                <g:link controller="titleDetails" action="createTitle" class="ui negative button" params="${[title:params.proposedTitle, typ: 'Database']}">${message(code:'title.findTitleMatches.create_for_database', default:'Create New Database Title for')} <em>"${params.proposedTitle}"</em></g:link>
              </g:if>
              <g:else>
                <bootstrap:alert class="alert-info">${message(code:'title.findTitleMatches.no_match', args:[params.proposedTitle])}</bootstrap:alert>
                <g:link controller="titleDetails" action="createTitle" class="ui positive button" params="${[title:params.proposedTitle, typ: 'Journal']}">${message(code:'title.findTitleMatches.create_for_journal', default:'Create New Journal Title for')} <em>"${params.proposedTitle}"</em></g:link>
                <g:link controller="titleDetails" action="createTitle" class="ui positive button" params="${[title:params.proposedTitle, typ: 'Ebook']}">${message(code:'title.findTitleMatches.create_for_ebook', default:'Create New eBook Title for')} <em>"${params.proposedTitle}"</em></g:link>
                <g:link controller="titleDetails" action="createTitle" class="ui positive button" params="${[title:params.proposedTitle, typ: 'Database']}">${message(code:'title.findTitleMatches.create_for_database', default:'Create New Database Title for')} <em>"${params.proposedTitle}"</em></g:link>
              </g:else>


            </g:if>
            <g:else>
            </g:else>

  </body>
</html>
