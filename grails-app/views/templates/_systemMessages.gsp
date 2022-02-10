<!-- systemMessages -->
<g:if test="${systemMessages}">
    <span style="border-bottom: 2px solid orange; padding: 0 2em 0.3em;">
    <g:if test="${systemMessages.size() > 1}">
        <strong>Systemmeldungen:&nbsp; ${systemMessages.size()}</strong>
    </g:if>
    <g:else>
        <strong>Systemmeldung</strong>
    </g:else>
    </span>

    <g:each in="${systemMessages}" var="message" status="i">
        <div style="padding-top:1em">
            <% println message.getLocalizedContent() %>
        </div>
    </g:each>
</g:if>
<!-- systemMessages -->
