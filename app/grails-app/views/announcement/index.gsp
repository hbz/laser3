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
    <h2 class="ui left floated aligned header la-clear-before">${message(code:'announcement.create.label', default:'Create announcement')}</h2>

    <semui:form>
        <g:form action="createSystemAnnouncement" class="ui form">
            <div class="field">
                <label>${message(code:'announcement.subject.label', default:'Subject')}</label>
                <input type="text" name="subjectTxt" value="${params.as}" />
            </div>
            <div class="field">
                <label></label>
                <textarea name="annTxt">${params.at}</textarea>
            </div>
            <div class="field">
                <label>&nbsp;</label>
                <input type="submit" class="ui button" value="${message(code:'announcement.create.button.label', default:'Create Announcement...')}" />
            </div>
        </g:form>
    </semui:form>


    <h3 class="ui  header la-clear-before">${message(code:'announcement.previous.label')}</h3>

    <div>
        <g:each in="${recentAnnouncements}" var="ra">
            <div class="ui segment">
                <h4 class="ui header">${ra.title}</h4>
                <div class="ui divider"></div>
                <div class="content">
                    <% print ra.content; /* avoid auto encodeAsHTML() */ %>
                </div>
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

  </body>
</html>
