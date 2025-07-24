
<nav class="ui secondary menu">
    <g:link controller="gasco" action="about" class="item${actionName == 'about' ? ' active' : ''}">Über uns</g:link>
    <g:link controller="gasco" action="monitor" class="item${actionName in ['monitor', 'monitorDetails'] ? ' active' : ''}">GASCO-Monitor</g:link>
    <g:link controller="gasco" action="members" class="item${actionName == 'members' ? ' active' : ''}">GASCO-Mitglieder</g:link>
</nav>