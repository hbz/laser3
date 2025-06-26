<%@ page import="de.laser.system.SystemMessage; de.laser.system.SystemMessageCondition" %>
%{-- model: systemMessages --}%

<g:if test="${type == SystemMessage.TYPE_GLOBAL}">
    <g:set var="globalMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_GLOBAL)}" />
</g:if>
<g:if test="${type == SystemMessage.TYPE_DASHBOARD}">
    <g:set var="dashboardMessages" value="${SystemMessage.getActiveMessages(SystemMessage.TYPE_DASHBOARD).findAll{ it.isDisplayed() }}" />
</g:if>

<g:if test="${globalMessages}">
    <div id="globalMessages" class="ui message warning system-message-wrapper">
        <div class="ui top attached label">
            <i class="icon exclamation circular inverted red"></i>
            <g:if test="${globalMessages.size() > 1}">
                <strong>${message(code: 'systemMessage.plural')}: ${globalMessages.size()}</strong>
            </g:if>
            <g:else>
                <strong>${message(code: 'systemMessage.label')}</strong>
            </g:else>
        </div>

        <g:each in="${globalMessages}" var="message" status="i">
            <div class="system-message">
                <ui:renderContentAsMarkdown>${message.getLocalizedContent()}</ui:renderContentAsMarkdown>
            </div>
        </g:each>
    </div>
</g:if>

<g:if test="${dashboardMessages}">
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

<g:if test="${globalMessages || dashboardMessages}">
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



