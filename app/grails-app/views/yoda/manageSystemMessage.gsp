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

<br />
<h2 class="ui left floated aligned header la-clear-before">${message(code: "menu.admin.systemMessage")}</h2>

<semui:messages data="${flash}" />

<div class="la-float-right">
    <input type="submit" class="ui button" value="Neue Nachricht erstellen" data-semui="modal" data-href="#modalCreateSystemMessage" />
</div>

<br />
<br />

<table class="ui celled la-table table">
    <thead>
    <tr>
        <th>Nachricht</th>
        <th>Typ</th>
        <th>Aktiv</th>
        <th>Letzte Änderung</th>
        <th class="la-action-info">${message(code:'default.actions.label')}</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${systemMessages}" var="msg">
        <tr>
            <td>
                <semui:xEditable owner="${msg}" field="content" type="textarea"/>
            </td>
            <td>
                <g:if test="${SystemMessage.TYPE_OVERLAY == msg.type}">Systemmeldung</g:if>
                <g:if test="${SystemMessage.TYPE_STARTPAGE_NEWS == msg.type}">Startseite</g:if>
            </td>
            <td>
                <semui:xEditableBoolean owner="${msg}" field="isActive"/>
            </td>
            <td>
                <g:formatDate date="${msg.lastUpdated}" format="${message(code: 'default.date.format.noZ')}"/>
            </td>
            <td class="x">
                <g:link controller="yoda" action="deleteSystemMessage" id="${msg.id}" class="ui negative icon button">
                    <i class="trash alternate icon"></i>
                </g:link>
            </td>
        </tr>
    </g:each>
    </tbody>
</table>

<semui:modal id="modalCreateSystemMessage" text="Neue Nachricht erstellen">

    <g:form class="ui form" url="[controller: 'yoda', action: 'manageSystemMessage', params: [create: true]]" method="post">

        <fieldset>
            <div class="field">
                <label>Nachricht</label>
                <textarea name="content" ></textarea>
            </div>

            <div class="field">
                <label>Typ</label>
                <g:select from="${[[SystemMessage.TYPE_OVERLAY, 'Systemmeldung'], [SystemMessage.TYPE_STARTPAGE_NEWS, 'Startseite']]}"
                          optionKey="${{it[0]}}"
                          optionValue="${{it[1]}}"
                          name="type"
                          class="ui fluid search dropdown"/>
            </div>
        </fieldset>
    </g:form>

</semui:modal>


</body>
</html>
