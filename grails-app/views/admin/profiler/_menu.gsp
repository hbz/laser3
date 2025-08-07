
<g:if test="${hideWrapper != true}">
    <nav class="ui secondary stackable menu" style="margin-bottom:2em">
</g:if>
        <g:link controller="admin" action="profilerLoadtime" class="item${actionName == 'profilerLoadtime' ? ' active' : ''}">Ladezeiten</g:link>
        <g:link controller="admin" action="profilerTimeline" class="item${actionName == 'profilerTimeline' ? ' active' : ''}">Seitenaufrufe</g:link>
        <g:link controller="admin" action="profilerActivity" class="item${actionName == 'profilerActivity' ? ' active' : ''}">Nutzerzahlen</g:link>
        <sec:ifAnyGranted roles="ROLE_YODA">
            <g:link controller="admin" action="profilerLive"  class="item${actionName == 'profilerLive'  ? ' active' : ''}">Live</g:link>
        </sec:ifAnyGranted>
<g:if test="${hideWrapper != true}">
    </nav>
</g:if>
