<%@ page import="de.laser.helper.RDStore; de.laser.domain.SystemMessage" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code: 'menu.admin.systemMessage')}</title>
</head>
<body>
<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.admin.systemMessage" class="active"/>
</semui:breadcrumbs>

<h2 class="ui header">${message(code: "menu.admin.systemMessage")}</h2>

<semui:messages data="${flash}" />

<div class="la-float-right">
    <input type="submit" class="ui button" value="Neue System Nachricht erstellen" data-semui="modal" data-href="#modalCreateSystemMessage" />
</div>

<table class="ui celled la-table table">
    <thead>
    <tr>
        <th>Nachricht</th>
        <th>Aktiv</th>
        <th>Erstellt</th>
        <th>Letzte Änderung</th>
        <th class="la-action-info">${message(code:'default.actions.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${systemMessages}" var="msg">
        <tr>
            <td>${msg.content}</td>
            <td><semui:xEditableBoolean owner="${msg}" field="isActive"/></td>
            <td><g:formatDate date="${msg.dateCreated}"
                              format="${message(code: 'default.date.format.noZ')}"/></td>
            <td><g:formatDate date="${msg.lastUpdated}"
                              format="${message(code: 'default.date.format.noZ')}"/></td>
            <td class="x">
                        <g:if test="${true}">
                            <g:link controller="yoda" action="deleteSystemMessage" id="${msg.id}"
                                    class="ui negative icon button"><i
                                    class="trash alternate icon"></i></g:link>
                        </g:if>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>

<semui:modal id="modalCreateSystemMessage" text="Neue System Nachricht erstellen">

    <g:form class="ui form" url="[controller: 'yoda', action: 'manageSystemMessage', params: [create: true]]" method="post">

        <fieldset>
            <div class="field">
                <label>Text</label>
                <input type="text" name="content" />
            </div>

            <div class="field">
                <label>Typ</label>
                <g:select name="type" from="${SystemMessage.getTypes()}" class="ui fluid search dropdown"/>
            </div>
        </fieldset>
    </g:form>

</semui:modal>


</body>
</html>
