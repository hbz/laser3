<%@ page import="de.laser.utils.LocaleUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'menu.user.manual')}" />

<ui:h1HeaderWithIcon text="${message(code: 'menu.user.manual')}" type="help"/>

<br />

<div class="ui top attached menu">
    <g:each in="${content}" var="cc">
        <g:link controller="public" action="manual" id="${cc.key}" class="item ${cc.key == topic ? 'active' : ''}">
            ${LocaleUtils.getCurrentLang() == 'de' ? cc.value[0] : cc.value[1]}
        </g:link>
    </g:each>
</div>

<div class="ui bottom attached segment la-markdown">
    <ui:renderMarkdown manual="${topic}" />
</div>

<laser:htmlEnd />

