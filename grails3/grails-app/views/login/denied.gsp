<head>
<meta name="layout" content="laser">
<title><g:message code="springSecurity.denied.title" /></title>
</head>

<body>
    <semui:messages data="${flash}" />

    <div class='body'>
        <div>
            <div class='errors'>
                <h3 class="ui header"><g:message code="springSecurity.denied.message" /></h3>
                <p>
                    <button class="ui button" onclick="JSPC.helper.goBack()">Zurück</button>
                </p>
            </div>
        </div>
    </div>
</body>
