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
    <g:if test="${currentAnnouncement}">
        <h2 class="ui left floated aligned header la-clear-before">${message(code:'announcement.update.label')}</h2>
    </g:if>
    <g:else>
        <h2 class="ui left floated aligned header la-clear-before">${message(code:'announcement.create.label')}</h2>
    </g:else>

    <semui:form>
        <g:form action="createSystemAnnouncement" class="ui form">
            <input type="hidden" name="saId" value="${currentAnnouncement?.id}">
            <div class="field">
                <label for="saTitle">${message(code:'announcement.subject.label')}</label>
                <input type="text" id="saTitle" name="saTitle" value="${currentAnnouncement?.title}" />
            </div>
            <div class="field">
                <label for="saContent">${message(code:'announcement.content.label')}</label>
                <textarea id="saContent" name="saContent">${currentAnnouncement?.content}</textarea>
            </div>
            <div class="ui field">
                <label for="saPreview">${message(code:'announcement.preview.label')}</label>
                <textarea id="saPreview" name="saPreview" readonly="readonly">${currentAnnouncement?.getCleanTitle()}

${currentAnnouncement?.getCleanContent()}
                </textarea>
            </div>

            <script>
                updateSysAnnPreview = function() {
                    $('#saPreview').text(
                        $('form #saTitle').val().replace(/<.*?>/gm,"") + '\n\n' +
                        $('form #saContent').val().replace(/<.*?>/gm,"")
                    )
                }
                $('form #saTitle').on('change', function(){
                    updateSysAnnPreview()
                })
                $('form #saContent').on('change', function(){
                    updateSysAnnPreview()
                })
            </script>

            <div class="field">
                <g:if test="${currentAnnouncement}">
                    <g:link controller="admin" action="systemAnnouncements" role="button" class="ui button">Formular zurücksetzen</g:link>
                    <input type="submit" class="ui button" value="${message(code:'default.button.save_changes')}" />
                </g:if>
                <g:else>
                    <input type="submit" class="ui button" value="${message(code:'announcement.create.button.label')}" />
                </g:else>
            </div>
        </g:form>
    </semui:form>

    <br />
    <h3 class="ui  header la-clear-before">${message(code:'announcement.previous.label')}</h3>

    <div>
        <g:each in="${announcements}" var="sa">
            <div class="ui segment">
                <h4 class="ui header"><% print sa.title; /* avoid auto encodeAsHTML() */ %></h4>
                <g:if test="${sa.isPublished}">
                    <div class="ui green ribbon label">${message(code:'announcement.published')}</div>
                </g:if>

                <div class="ui divider"></div>
                <div class="content">
                    <% print sa.content; /* avoid auto encodeAsHTML() */ %>
                </div>
                <div class="ui divider"></div>

                ${message(code:'default.lastUpdated.label')}:
                <g:formatDate date="${sa.lastUpdated}" format="${message(code:'default.date.format.noZ')}"/>
                -
                ${message(code:'default.dateCreated.label')}:
                <g:formatDate date="${sa.dateCreated}" format="${message(code:'default.date.format.noZ')}"/>
                -
                ${message(code:'default.from')}:
                <g:link controller="user" action="show" id="${sa.user?.id}">${(sa.user?.displayName)?:sa.user}</g:link>
                <br />
                <g:if test="${sa.lastPublishingDate}">
                    Zuletzt veröffentlicht: <g:formatDate date="${sa.lastPublishingDate}" format="${message(code:'default.date.format.noZ')}"/>
                    <br />
                </g:if>

                <div>
                    <g:if test="${sa.isPublished}">
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'undo']" role="button" class="ui button">${message(code:'default.publish_undo.label')}</g:link>
                    </g:if>
                    <g:else>
                        <br />
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'delete']" role="button" class="ui negative icon button"><i aria-hidden="true" class="trash alternate icon"></i></g:link>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'edit']" role="button" class="ui icon button"><i aria-hidden="true" class="edit icon"></i></g:link>
                        <g:link controller="admin" action="systemAnnouncements" id="${sa.id}" params="[cmd:'publish']" role="button" class="ui button">${message(code:'default.publish.label')}</g:link>
                    </g:else>
                </div>
            </div>
        </g:each>
    </div>

  </body>
</html>
