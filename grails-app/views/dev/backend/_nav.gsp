<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>

<nav class="ui secondary menu">
    <g:link controller="dev" action="backend" id="index" class="item${view == 'index' ? ' active' : ''}">Helper & Utils</g:link>
    <g:link controller="dev" action="backend" id="various" class="item${view == 'various' ? ' active' : ''}">Various</g:link>
</nav>