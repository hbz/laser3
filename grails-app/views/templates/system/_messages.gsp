<%@ page import="de.laser.system.SystemMessage" %>
%{-- model: systemMessages --}%

<g:set var="globalMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_GLOBAL)}" />
<g:set var="dashboardMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_DASHBOARD)}" />

<g:if test="${globalMessages && type == SystemMessage.TYPE_GLOBAL}">
    <div id="globalMessages" class="ui message warning system-message-wrapper">
        <div class="ui top attached label">
            <i class="icon exclamation circular inverted red"></i>
            <g:if test="${globalMessages.size() > 1}">
                <strong>Systemmeldungen: ${globalMessages.size()}</strong>
            </g:if>
            <g:else>
                <strong>Systemmeldung</strong>
            </g:else>
        </div>

        <g:each in="${globalMessages}" var="message" status="i">
            <div class="system-message">
                <ui:renderContentAsMarkdown>${message.getLocalizedContent()}</ui:renderContentAsMarkdown>
            </div>
        </g:each>
    </div>
</g:if>

<g:if test="${dashboardMessages && type == SystemMessage.TYPE_DASHBOARD}">
    <div id="dashboardMessages" class="ui message info system-message-wrapper">
        <div class="ui top attached label">
            <i class="icon exclamation circular inverted teal"></i>
            <g:if test="${dashboardMessages.size() > 1}">
                <strong>Aktuelle Hinweise: ${dashboardMessages.size()}</strong>
            </g:if>
            <g:else>
                <strong>Aktueller Hinweis</strong>
            </g:else>
        </div>

        <g:each in="${dashboardMessages}" var="message" status="i">
            <div class="system-message">
                <ui:renderContentAsMarkdown>${message.getLocalizedContent()}</ui:renderContentAsMarkdown>
            </div>
        </g:each>
    </div>
</g:if>

<g:if test="${(globalMessages && type == SystemMessage.TYPE_GLOBAL) || (dashboardMessages && type == SystemMessage.TYPE_DASHBOARD)}">
    <style>
        .system-message-wrapper {
            padding: 2em 3em;
            font-size: 1.1em;
        }
        .system-message-wrapper .top.label strong {
            font-size:1.2em;
        }
        .system-message-wrapper .system-message {
            padding-top: 1em;
        }
        .system-message-wrapper .system-message ul,
        .system-message-wrapper .system-message ol {
            text-align: left;
        }

        #globalMessages .top.label {
            background-color: #c9ba9b;
        }
        #globalMessages .system-message{
            text-align: center;
        }

        #dashboardMessages .top.label {
            background-color: #a9d5de;
        }
    </style>
</g:if>



