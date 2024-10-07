<%@ page import="de.laser.utils.AppUtils; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart text="${message(code: 'faq')}" serviceInjection="true"/>

<ui:h1HeaderWithIcon text="${message(code: 'faq')}" type="help"/>

<br />

<div class="ui top attached menu">
    <g:each in="${content}" var="cc">
        <g:link controller="public" action="faq" id="${cc}" class="item ${cc == topic ? 'active' : ''}">${cc}</g:link>
    </g:each>
</div>

<div class="ui bottom attached segment">
    <ui:renderMarkdown faq="${topic}" />
</div>

<laser:htmlEnd />

