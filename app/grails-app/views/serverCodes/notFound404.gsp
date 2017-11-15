<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} - ${message(code:'serverCode.notFound.message1')}</title>
    </head>
    <body>
        <semui:messages data="${flash}" />

        <div class="container">
            <laser:card>
                <h1>404</h1>
                <h3>${message(code:'serverCode.notFound.message1')}</h3>

                <g:if test="${! flash.error}">
                    <div class="container">
                        <p>${message(code:'serverCode.notFound.message2')}</p>
                    </div>
                </g:if>
            </laser:card>
        </div>
    </body>
</html>
