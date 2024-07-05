<laser:htmlStart message="serverCode.unavailable.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'serverCode.unavailable.message1')}"
                             subheader="${message(code: 'serverCode.unavailable.message2')}">

    </laser:serverCodeMessage>

<laser:htmlEnd />

