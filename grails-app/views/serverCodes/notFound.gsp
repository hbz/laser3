<laser:htmlStart message="serverCode.notFound.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'serverCode.notFound.message1')}"
                             subheader="${customMessage ?: message(code: 'serverCode.notFound.message2')}">

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

    </laser:serverCodeMessage>

<laser:htmlEnd />
