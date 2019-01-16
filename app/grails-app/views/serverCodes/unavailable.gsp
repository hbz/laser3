<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} - Service Unavailable</title>
</head>

<body>
<semui:messages data="${flash}"/>
<semui:card>
    <div class="content">
        <h3 class="ui header">
            <i class="icon meh outline"></i>
            Service Unavailable
        </h3>

        <g:if test="${!flash.error}">
            <div>
                <p>Die angefragte Funktion steht zur Zeit nicht zur Verfügung. Bitte versuchen Sie es später erneut.</p>
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

