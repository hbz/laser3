<!-- systemMessages -->
<g:if test="${systemMessages}">
   <i class="icon bell large red"></i>
    <g:if test="${systemMessages.size() > 1}">
        <strong>Systemmeldungen:&nbsp; ${systemMessages.size()}</strong>
    </g:if>
    <g:else>
        <strong>Systemmeldung</strong>
    </g:else>

    <g:each in="${systemMessages}" var="message" status="i">
        <div style="padding-top:1em">
            <% println message.getLocalizedContent() %>
        </div>
    </g:each>
</g:if>
<!-- systemMessages -->
