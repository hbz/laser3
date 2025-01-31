<%@ page import="org.grails.web.util.GrailsApplicationAttributes" %>
<!doctype html>

<html>

<head>
    <meta charset="utf-8">
    <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
    <meta name="viewport" content="initial-scale = 1.0">

    <asset:stylesheet src="laser.css"/>%{-- dont move --}%

    <laser:javascript src="base.js"/>%{-- dont move --}%
    <script data-type="fixed">
        <g:render template="/templates/jspc/jspc.js" />%{-- g:render; dont move --}%
    </script>

    <g:layoutHead/>

    <g:render template="/layouts/favicon" />
</head>

    <body class="public">

        <ui:skipLink />%{-- skip to main content - for screenreader --}%

        <g:layoutBody/>%{-- body here --}%
    
        <div id="Footer">
            <div class="clearfix"></div>
        </div>

        <laser:javascript src="laser.js"/>%{-- dont move --}%

        <laser:scriptBlock/>%{-- dont move --}%

    </body>
</html>
