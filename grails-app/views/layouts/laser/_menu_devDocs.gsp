<%@ page import="de.laser.ui.Icon" %>
<laser:serviceInjection />

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.devDocs.short')}
        <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
%{--        <ui:link addItemAttributes="true" controller="dev" action="index">Dashboard</ui:link>--}%
%{--        <div class="divider"></div>--}%
        <ui:link addItemAttributes="true" controller="dev" action="frontend">Frontend</ui:link>
        <ui:link addItemAttributes="true" controller="dev" action="backend">Backend</ui:link>
        <ui:link addItemAttributes="true" controller="dev" action="klodav">klodav</ui:link>
    </div>
</div>