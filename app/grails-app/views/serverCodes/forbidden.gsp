<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap"/>
        <title>${message(code:'laser', default:'LAS:eR')} - Forbidden</title>
    </head>
    <body>
        <laser:flash data="${flash}" />

        <div class="container">
            <p>${message(code:'serverCode.forbidden.message')}</p>
        </div>
    </body>
</html>
