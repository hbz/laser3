<%@ page import="de.laser.utils.AppUtils; de.laser.config.ConfigMapper; org.grails.web.util.GrailsApplicationAttributes" %>
<!doctype html>

<g:set var="currentServer" scope="page" value="${AppUtils.getCurrentServer()}" />
<html>

<head>
    <meta charset="utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="viewport" content="initial-scale = 1.0">
    <g:if test="${AppUtils.getCurrentServer() == AppUtils.PROD && ConfigMapper.getGoogleSiteVerificationToken()}">
        <meta name="google-site-verification" content="${ConfigMapper.getGoogleSiteVerificationToken()}" />
    </g:if>

    <asset:stylesheet src="laser.css"/>%{-- dont move --}%

    <laser:javascript src="base.js"/>%{-- dont move --}%
    <script data-type="fixed">
        <g:render template="/templates/jspc/jspc.js" />%{-- g:render; dont move --}%
    </script>

    <g:layoutHead/>

    <g:render template="/layouts/favicon" />
</head>

    <body class="public ${controllerName}_${actionName}">
        <ui:skipLink />%{-- skip to main content - for screenreader --}%

        <laser:render template="/templates/system/serverIndicator" />

        <div class="landingpage"> <!-- TODO - check/fix css -->
            <g:if test="${!(controllerName == 'gasco')}">
                    <laser:render template="/layouts/publicMenu" />
            </g:if>
            <main class="ui main container">
                <g:layoutBody/>%{-- body here --}%
            </main>
            <g:if test="${!(controllerName == 'gasco')}">
                <laser:render template="/layouts/footer" />
            </g:if>

            <laser:javascript src="laser.js"/>%{-- dont move --}%

            <laser:scriptBlock/>%{-- dont move --}%
        </div>

    </body>
</html>
