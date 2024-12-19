<laser:htmlStart message="statusCode.notFound.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'statusCode.notFound.message1')}"
                             subheader="${customMessage ?: message(code: 'statusCode.notFound.message2')}">

        <g:if test="${alternatives}">
            <g:if test="${alternatives.size() == 1}">
                ${message(code: 'statusCode.notFound.message3')}
            </g:if>
            <g:else>
                ${message(code: 'statusCode.notFound.message4')}
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
