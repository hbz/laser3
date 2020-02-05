<%@ page import="com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.auth.Role; com.k_int.kbplus.auth.UserOrg; com.k_int.kbplus.UserSettings" %>
<%@ page import="static de.laser.helper.RDStore.*" %>
<%@ page import="static com.k_int.kbplus.UserSettings.KEYS.*" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${message(code: 'profile', default: 'LAS:eR User Profile')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="profile.bc.profile" class="active"/>
</semui:breadcrumbs>


<br>
<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'profile', default: 'LAS:eR User Profile')}</h1>

<semui:messages data="${flash}" />



</body>
</html>
