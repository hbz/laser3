<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="klodav" id="icons" class="item${view == 'icons' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="klodav" id="iconsAttr" class="item${view == 'iconsAttr' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons (ATTR)</g:link>
    <g:link controller="dev" action="klodav" id="buttons" class="item${view == 'buttons' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Buttons</g:link>
    <g:link controller="dev" action="klodav" id="markdown" class="item${view == 'markdown' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} orange"></i> Markdown</g:link>
    <g:link controller="dev" action="klodav" id="security" class="item${view == 'security' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} red"></i> Security</g:link>
</nav>