<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} - ${message(code: 'serverCode.notFound.message1')}</title>
</head>

<body>
<semui:messages data="${flash}"/>


<semui:card>
    <div class="content">
        <h1 class="ui header"><semui:headerIcon/>404</h1>

        <h3 class="ui header">${message(code: 'serverCode.notFound.message1')}</h3>

        <g:if test="${!flash.error}">
            <div>
                <p>${message(code: 'serverCode.notFound.message2')}</p>
                <br/>

                <p>
                    <button class="ui button"
                            onclick="javascript:window.history.back()">${message(code: 'default.button.back')}</button>
                </p>
            </div>
        </g:if>
    </div>
</semui:card>

</body>
</html>
