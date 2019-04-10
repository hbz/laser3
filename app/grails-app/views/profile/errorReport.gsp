<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.user.errorReport')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.help" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.user.errorReport')}</h1>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="profile" action="errorOverview" message="profile.errorOverview.label" />
    <semui:subNavItem controller="profile" action="errorReport" message="profile.errorReport.label" />
</semui:subNav>

<g:if test="${'ok'.equalsIgnoreCase(sendingStatus)}">
    <semui:msg class="positive" text="Ihr Fehlerbericht wurde übertragen." />
</g:if>
<g:if test="${'fail'.equalsIgnoreCase(sendingStatus)}">
    <semui:msg class="negative" text="Ihr Fehlerbericht konnte leider nicht übertragen werden. Beachten Sie bitte die Pflichtfelder." />
</g:if>

<div class="ui warning message">
    <div class="header">Informieren Sie uns, wenn Sie einen Fehler entdeckt haben.</div>

    <p>
        <br />
        Formulieren Sie bitte kurz ...
    </p>
    <ul class="ui list">
        <li>das beobachtete Verhalten</li>
        <li>das erwartete Verhalten</li>
        <li>den genauen Kontext</li>
    </ul>
    <p>Vielen Dank!</p>
</div>

<semui:form>
    <form method="post">
        <div class="ui form">

            <div class="required field">
                <label for="title">Titel</label>
                <input  id="title" name="title" type="text" value="${title}"/>
            </div>

            <div class="field">
                <label>Beobachtetes Verhalten</label>
                <textarea name="described">${described}</textarea>
            </div>
            <div class="field">
                <label>Erwartetes Verhalten</label>
                <textarea name="expected">${expected}</textarea>
            </div>
            <div class="field">
                <label>Kontext, z. B. die betroffene URL in der Adressleiste des Browses</label>
                <textarea name="info">${info}</textarea>
            </div>

            <div class="field">
                <input type="submit" name="sendErrorReport" class="ui button" value="Absenden">
            </div>

        </div>
    </form>
</semui:form>

<r:script>
    $('.ui.form').form({
        on: 'blur',
        fields: {
            title: 'empty'
        }
    })
</r:script>
</body>
</html>
