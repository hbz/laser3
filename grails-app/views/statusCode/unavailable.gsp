<laser:htmlStart message="statusCode.unavailable.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'statusCode.unavailable.message1')}"
                             subheader="${message(code: 'statusCode.unavailable.message2')}">

    </laser:serverCodeMessage>

<laser:htmlEnd />

