<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} - Forbidden</title>
    </head>
    <body>
        <semui:messages data="${flash}" />

        <div class="ui grid">
            <div class="twelve wide column">

                <p>${message(code:'serverCode.forbidden.message')}</p>

            </div><!-- .twelve -->
        </div><!-- .grid -->
    </body>
</html>
