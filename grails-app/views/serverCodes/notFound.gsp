<laser:htmlStart message="serverCode.notFound.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <div class="ui segment piled">
        <div class="content">
            <div>
                <span class="ui orange label huge">${status}</span>
            </div>

            <h2 class="ui header">
                ${message(code: 'serverCode.notFound.message1')}
            </h2>

            <div>
                    <p>${customMessage ?: message(code: 'serverCode.notFound.message2')}</p>

                    <g:if test="${alternatives}">
                        <g:if test="${alternatives.size() == 1}">
                            ${message(code: 'serverCode.notFound.message3')}
                        </g:if>
                        <g:else>
                            ${message(code: 'serverCode.notFound.message4')}
                        </g:else>
                        <br/>
                        <div class="ui selection list">
                            <g:each in="${alternatives}" var="alt">
                                <div class="item">
                                    <a href="${alt.key}">${alt.value ?: alt.key}</a>
                                </div>
                            </g:each>
                        </div>
                    </g:if>
                    <br />

                    <p>
                        <button class="ui button" onclick="JSPC.helper.goBack()">${message(code: 'default.button.back')}</button>
                    </p>
            </div>

        </div>
    </div>

<laser:htmlEnd />
