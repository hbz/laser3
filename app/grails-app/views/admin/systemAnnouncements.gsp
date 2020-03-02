<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'menu.admin.announcements')}</title>
    </head>

    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.announcements" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />
    <br>
    <h2 class="ui left floated aligned header la-clear-before">${message(code:'announcement.create.label')}</h2>

    <semui:form>
        <g:form action="createSystemAnnouncement" class="ui form">
            <input type="hidden" name="saId" value="${currentAnnouncement?.id}">
            <div class="field">
                <label for="saTitle">${message(code:'announcement.subject.label')}</label>
                <input type="text" id="saTitle" name="saTitle" value="${currentAnnouncement?.title}" />
            </div>
            <div class="field">
                <label for="saContent"></label>
                <textarea id="saContent" name="saContent">${currentAnnouncement?.content}</textarea>
            </div>
            <div class="field">
                <g:if test="${currentAnnouncement}">
                    <g:link controller="admin" action="systemAnnouncements" class="ui button">Formular zurücksetzen</g:link>
                    <input type="submit" class="ui button" value="${message(code:'default.button.save_changes')}" />
                </g:if>
                <g:else>
                    <input type="submit" class="ui button" value="${message(code:'announcement.create.button.label')}" />
                </g:else>
            </div>
        </g:form>
    </semui:form>

    <br />
    <br />
    <h3 class="ui  header la-clear-before">${message(code:'announcement.previous.label')}</h3>

    <div>
        <g:each in="${announcements}" var="sa">
            <div class="ui segment">
                <h4 class="ui header">${sa.title}</h4>
                <div class="ui divider"></div>
                <div class="content">
                    <% print sa.content; /* avoid auto encodeAsHTML() */ %>
                </div>

                <br />
                Erstellt am: <g:formatDate date="${sa.dateCreated}" format="${message(code:'default.date.format')}"/>
                <br />
                Von: <em><g:link controller="user" action="show" id="${sa.user?.id}">${(sa.user?.displayName)?:'Unknown'}</g:link></em>
                <br />
                <g:if test="${sa.lastPublishingDate}">
                    <br />
                    Zuletzt veröffentlicht am: <g:formatDate date="${sa.lastPublishingDate}" format="${message(code:'default.date.format')}"/>
                    <br />
                </g:if>

                <div>
                    <g:if test="${sa.isPublished}">
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'undo']" class="ui negative button">${message(code:'default.publish_undo.label')}</g:link>
                    </g:if>
                    <g:else>
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'publish']" class="ui positive button">${message(code:'default.publish.label')}</g:link>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'delete']" class="ui button">${message(code:'default.button.delete.label')}</g:link>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'edit']" class="ui button">${message(code:'default.button.edit.label')}</g:link>
                    </g:else>
                </div>
            </div>
        </g:each>
    </div>

  </body>
</html>
