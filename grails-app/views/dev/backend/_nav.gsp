<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>

<nav class="ui secondary menu">
    <g:link controller="dev" action="backend" id="resources" class="item${view == 'resources' ? ' active' : ''}">Resources/Weppapp</g:link>
    <g:link controller="dev" action="backend" class="item${view == 'index' ? ' active' : ''}">Helper & Utils</g:link>
</nav>