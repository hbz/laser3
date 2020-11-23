<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Datenbereinigung</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Datenbereinigung" class="active"/>
</semui:breadcrumbs>

    <h2 class="ui header">Ersetzen des UserSetting DASHBOARD_REMINDER_PERIOD</h2>
    <semui:messages data="${flash}"/>
    <br />
    <br />
    <p>Es wird das UserSetting DASHBOARD_REMINDER_PERIOD durch die neuen feineren Erinnerungseinstellungen für jeden Benutzer in der DB ersetzt.</p>
    <p>Aktion wurde durchgeführt für <strong>${users? users?.size() : '<NULL>'}</strong> Benutzer.</p>
</body>
</html>
