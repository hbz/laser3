<laser:htmlStart message="serverCode.forbidden.message1" />

    <br />

    <ui:messages data="${flash}"/>

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'serverCode.forbidden.message1')}"
                             subheader="${message(code: 'serverCode.forbidden.message2')}">

    </laser:serverCodeMessage>

<laser:htmlEnd />