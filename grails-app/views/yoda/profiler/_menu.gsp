
<g:if test="${hideWrapper != true}">
    <nav class="ui secondary stackable menu" style="margin-bottom:2em">
</g:if>
        <g:link controller="yoda" action="profilerLoadtime" class="item${actionName == 'profilerLoadtime' ? ' active' : ''}">Ladezeiten</g:link>
        <g:link controller="yoda" action="profilerTimeline" class="item${actionName == 'profilerTimeline' ? ' active' : ''}">Seitenaufrufe</g:link>
        <g:link controller="yoda" action="profilerActivity" class="item${actionName == 'profilerActivity' ? ' active' : ''}">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="profilerCurrent"  class="item${actionName == 'profilerCurrent'  ? ' active' : ''}">?</g:link>
<g:if test="${hideWrapper != true}">
    </nav>
</g:if>
