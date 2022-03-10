<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'menu.datamanager.ann')}</title>
    </head>

    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.datamanager.dash" controller="dataManager" action="index"/>
        <semui:crumb message="menu.datamanager.ann" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h1 class="ui left floated aligned header la-clear-before">${message(code:'announcement.create.label')}</h1>

    <semui:form>
        <g:form action="createAnnouncement" class="ui form">
            <div class="field">
                <label>${message(code:'announcement.subject.label')}</label>
                <input type="text" name="subjectTxt" value="${params.as}" />
            </div>
            <div class="field">
                <label></label>
                <textarea name="annTxt">${params.at}</textarea>
            </div>
            <div class="field">
                <label>&nbsp;</label>
                <input type="submit" class="ui button" value="${message(code:'announcement.create.button.label')}" />
            </div>
        </g:form>
    </semui:form>


      <h2 class="ui  header la-clear-before">${message(code:'announcement.previous.label')}</h2>

      <div class="ui divided relaxed list">
        <g:each in="${recentAnnouncements}" var="ra">
          <div class="item">
            <strong>${ra.title}</strong> <br />
            <div class="ann-content">
              <% print ra.content; /* avoid auto encodeAsHTML() */ %>
            </div>
              <br />
            <g:if test="${ra.user != null}">
              ${message(code:'announcement.posted_by.label')}
                <em><g:link controller="user" action="show" id="${ra.user?.id}">${(ra.user?.displayName)?:'Unknown'}</g:link></em>
                <br />
                ${message(code:'default.on')} <g:formatDate date="${ra.dateCreated}" format="${message(code:'default.date.format')}"/>
            </g:if>
            <g:else>
                ${message(code:'announcement.posted_auto.label')}
                <br />
                <g:formatDate date="${ra.dateCreated}" format="${message(code:'default.date.format')}"/>
            </g:else>
          </div>
        </g:each>
      </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('.ann-content').readmore({
            speed: 250,
            collapsedHeight: 21,
            startOpen: false,
            moreLink: '<a href="#">[ ${message(code:'default.button.show.label')} ]</a>',
            lessLink: '<a href="#">[ ${message(code:'default.button.hide.label')} ]</a>'
        })
    </laser:script>

    <style>
        .ann-content {
            overflow: hidden;
            line-height: 20px;
        }
    </style>
  </body>
</html>
