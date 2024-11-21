<%@ page import="de.laser.utils.LocaleUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'menu.user.manual')}" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon text="${message(code: 'menu.user.manual')}" type="help"/>

<div class="ui secondary pointing menu">
    <g:each in="${content}" var="cc">
        <g:link controller="public" action="manual" id="${cc.key}" class="item ${cc.key == topic ? 'active' : ''}">
            ${LocaleUtils.getCurrentLang() == 'de' ? cc.value[0] : cc.value[1]}
        </g:link>
    </g:each>

    <g:link controller="public" action="faq" class="item right floated"><icon:arrow /> ${message(code:'menu.user.faq')}</g:link>
</div>

<div class="ui segment la-markdown">
    <ui:renderMarkdown manual="${topic}" />
</div>

<g:render template="markdownScript" />

<laser:htmlEnd />

