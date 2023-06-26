<%@ page import="de.laser.Org" %>

<laser:htmlStart text="${user.display}" />

      <laser:render template="breadcrumb" model="${[ params:params ]}"/>

      <ui:h1HeaderWithIcon text="${user.username} : ${user.displayName ?: 'No username'}" />

      <ui:messages data="${flash}" />

      <h2 class="ui header">${message(code:'user.affiliation.plural')}</h2>

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <th>${message(code:'user.id')}</th>
            <th>${message(code:'user.org')}</th>
            <th>${message(code:'default.role.label')}</th>
          </tr>
        </thead>
        <tbody>
            <tr>
              <td>${user.id}</td>
              <td>${user.formalOrg?.name}</td>
              <td>${message(code:"cv.roles.${user.formalRole?.authority}")}</td>
            </tr>
        </tbody>
      </table>

      <h2 class="ui header">${message(code:'user.role.plural')}</h2>

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <th>${message(code:'default.role.label')}</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${user.roles}" var="rl">
            <tr>
              <td>${rl.role.authority}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

<laser:htmlEnd />
