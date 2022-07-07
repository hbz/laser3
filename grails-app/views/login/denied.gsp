<laser:htmlStart message="springSecurity.denied.title" />

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

<laser:htmlEnd />
