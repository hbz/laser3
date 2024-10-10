<laser:htmlStart message="statusCode.forbidden.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'statusCode.forbidden.message1')}"
                             subheader="${message(code: 'statusCode.forbidden.message2')}">

    </laser:serverCodeMessage>

<laser:htmlEnd />