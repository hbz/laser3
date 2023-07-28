<laser:htmlStart message="serverCode.unkown.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <div class="ui segment piled">
        <div class="content">
            <div>
                <span class="ui orange label huge">42</span>
            </div>

            <h2 class="ui header">
                ${message(code: 'serverCode.unkown.message1')}
            </h2>

            <div>
                    <p>${message(code: 'serverCode.unkown.message2')}</p>
                    <br />

                    <p>
                        <button class="ui button la-js-dont-hide-button" onclick="JSPC.helper.goBack()">${message(code: 'default.button.back')}</button>
                    </p>
            </div>

        </div>
    </div>

<laser:htmlEnd />

