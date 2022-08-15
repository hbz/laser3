<laser:htmlStart message="datamanager.deletedTitleManagement.label" />

    <ui:breadcrumbs>
      <ui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager')}" />
      <ui:crumb text="${message(code:'datamanager.deletedTitleManagement.label')}" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="datamanager.deletedTitleManagement.label" total="${titleInstanceTotal}" />

    <ui:messages data="${flash}" />

      <table class="ui sortable celled la-js-responsive-table la-table compact table">
        <thead>
          <tr>
            <g:sortableColumn property="title" title="${message(code: 'title.label')}" />
          </tr>
        </thead>
        <tbody>
          <g:each in="${titleList}" var="titleInstance">
            <tr>
              <td>${fieldValue(bean: titleInstance, field: "title")}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <ui:paginate action="deletedTitles" controller="dataManager" params="${params}" max="${max}" total="${titleInstanceTotal}" />

<laser:htmlEnd />
