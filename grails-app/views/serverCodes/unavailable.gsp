<laser:htmlStart message="serverCode.unavailable.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <div class="ui segment piled">
        <div class="content">
            <div>
                <span class="ui orange label huge">${status}</span>
            </div>

            <h2 class="ui header">
                ${message(code: 'serverCode.unavailable.message1')}
            </h2>

            <div>
                    <p>${message(code: 'serverCode.unavailable.message2')}</p>
                    <br />

                    <p>
                        <button class="ui button la-js-dont-hide-button" onclick="JSPC.helper.goBack()">${message(code: 'default.button.back')}</button>
                    </p>
            </div>

        </div>
    </div>

<laser:htmlEnd />

