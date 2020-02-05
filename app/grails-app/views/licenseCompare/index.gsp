<%@ page import="com.k_int.kbplus.License" contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser')} : ${message(code:'menu.my.comp_lic')}</title>
    </head>
    <body>
        <semui:breadcrumbs>
            <semui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />
        	<semui:crumb class="active" message="menu.my.comp_lic" />
		</semui:breadcrumbs>
        <br>
		<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.my.comp_lic')}</h1>
		<g:render template="selectionForm" />
	</body>
</html>
