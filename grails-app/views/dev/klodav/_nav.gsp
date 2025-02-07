<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ui.AttrIcon;" %>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="klodav" id="icons" class="item${view == 'icons' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="klodav" id="attrIcons" class="item${view == 'attrIcons' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New AttrIcons</g:link>
    <g:link controller="dev" action="klodav" id="buttons" class="item${view == 'buttons' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Buttons</g:link>
    <g:link controller="dev" action="klodav" id="markdown" class="item${view == 'markdown' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} orange"></i> Markdown</g:link>
    <g:link controller="dev" action="klodav" id="security" class="item${view == 'security' ? ' active' : ''}"><i class="${Icon.SIG.NEW_OBJECT} red"></i> Security</g:link>
</nav>