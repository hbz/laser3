<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.institutions.help')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.help" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"><semui:headerIcon />${message(code: 'menu.institutions.help')}</h1>

<div class="ui styled accordion">
    <div class="active title">
        <i class="dropdown icon"></i>
        Die Icons werden nicht richtig angezeigt
    </div>
    <div class="active content">
        <div class="ui relaxed divided list">
            <p>Bitte den Browser Cache in den Einstellungen Ihres Browsers löschen:</p>
            <div class="item">
                <i class="large internet explorer middle aligned icon"></i>
                <div class="content">
                    <div class="description">Internet Explorer</div>
                    <a href="https://support.microsoft.com/de-de/help/508440" class="header" target="_blank">https://support.microsoft.com/de-de/help/508440</a>
                </div>
            </div>
            <div class="item">
                <i class="large chrome middle aligned icon"></i>
                <div class="content">
                    <div class="description">Google Chrome</div>
                    <a href="https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008" class="header" target="_blank">https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008</a>
                </div>
            </div>
            <div class="item">
                <i class="large firefox middle aligned icon"></i>
                <div class="content">
                    <div class="description">Firefox</div>
                    <a href="https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren" target="_blank" class="header">https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren</a>
                </div>
            </div>
        </div>
    </div>

</div>

</body>
</html>
