<%@ page import="com.k_int.kbplus.SystemMessage" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'menu.admin.systemMessage', default: 'System Message')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.systemMessage" class="active"/>
</semui:breadcrumbs>

<h2 class="ui header">${message(code: "menu.admin.systemMessage")}</h2>

<semui:messages data="${flash}" />

<div class="pull-right">
    <input type="submit" class="ui button" value="Neue System Nachricht erstellen" data-semui="modal" href="#modalCreateSystemMessage" />
</div>

<table class="ui celled la-table table">
    <thead>
    <tr>
        <th>Nachricht</th>
        <th>Anzeigen?</th>
        <th>Org</th>
        <th>Erstellt am</th>
        <th>Letzte Änderung am</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${systemMessages}" var="m">
        <tr>
            <td>${m?.text}</td>
            <td><semui:xEditable owner="${m}" field="showNow"/></td>
            <td>${m.org?.name}</td>
            <td><g:formatDate date="${m?.dateCreated}"
                              format="${message(code: 'default.date.format')}"/></td>
            <td><g:formatDate date="${m?.lastUpdated}"
                              format="${message(code: 'default.date.format')}"/></td>
            <td class="x">
                        <g:if test="${true}">
                            <g:link controller="yoda" action="deleteSystemMessage" id="${m?.id}"
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
                <input type="text" name="text" />
            </div>

            <div class="field">
                <label>Organisation</label>
                <g:select name="org"
                          from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')}"
                          optionKey="id"
                          optionValue="name"
                          class="ui fluid search dropdown"/>
            </div>

            <div class="field">
                <label>System Nachricht Anzeigen</label>
                <g:checkBox name="showNow"/>
            </div>

        </fieldset>
    </g:form>

</semui:modal>


</body>
</html>
