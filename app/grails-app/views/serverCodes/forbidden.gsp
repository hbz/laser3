<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} - Forbidden</title>
    </head>
    <body>
        <semui:messages data="${flash}" />

        <div>
            <p>${message(code:'serverCode.forbidden.message')}</p>
        </div>
    </body>
</html>
