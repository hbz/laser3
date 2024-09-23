<%@ page import="de.laser.system.SystemMessage" %>
%{-- model: systemMessages --}%

<g:set var="activeSystemMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION)}" />
<g:if test="${activeSystemMessages}">

    <div id="systemMessages" class="ui message warning">
        <div class="ui top attached label">
            <i class="icon exclamation circular inverted red"></i>
            <g:if test="${activeSystemMessages.size() > 1}">
                <strong>Systemmeldungen: ${activeSystemMessages.size()}</strong>
            </g:if>
            <g:else>
                <strong>Systemmeldung</strong>
            </g:else>
        </div>

        <g:each in="${activeSystemMessages}" var="message" status="i">
            <div class="sysMessage">
                <ui:renderContentAsMarkdown>${message.getLocalizedContent()}</ui:renderContentAsMarkdown>
            </div>
        </g:each>
    </div>

    <style>
        #systemMessages {
            padding: 2em 3em;
            font-size: 1.1em;
        }
        #systemMessages .top.label {
            background-color: #c9ba9b;
        }
        #systemMessages .top.label strong {
            font-size:1.2em;
        }
        #systemMessages .sysMessage {
            padding-top: 1em;
            text-align: center;
        }
        #systemMessages .sysMessage ul, #systemMessages .sysMessage ol {
            text-align: left;
        }
    </style>
</g:if>



