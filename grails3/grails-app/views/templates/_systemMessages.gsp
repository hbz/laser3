<!-- systemMessages -->
<g:if test="${systemMessages}">
    <i class="icon bell outline large circular inverted red"></i>
    <g:if test="${systemMessages.size() > 1}">
        <strong style="font-size:1.2em">Systemmeldungen:&nbsp; ${systemMessages.size()}</strong>
    </g:if>
    <g:else>
        <strong style="font-size:1.2em">Systemmeldung</strong>
    </g:else>

    <g:each in="${systemMessages}" var="message" status="i">
        <div style="padding-top:1em; text-align:center">
            <% println message.getLocalizedContent() %>
        </div>
    </g:each>
</g:if>
<!-- systemMessages -->
