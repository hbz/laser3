<%@ page import="de.laser.storage.RDStore; de.laser.system.SystemMessage; de.laser.jobs.HeartbeatJob" %>

<laser:htmlStart message="menu.admin.systemMessage" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.systemMessage" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.systemMessage" />

<div class="la-float-right">
    <input type="submit" class="ui button" value="${message(code: 'admin.systemMessage.create')}" data-ui="modal" data-href="#modalCreateSystemMessage" />
</div>

<br />
<br />

<div class="ui info message">
    <i class="ui icon hand point right"></i> ${message(code: 'admin.systemMessage.info', args: [HeartbeatJob.HEARTBEAT_IN_SECONDS])}
</div>

<ui:messages data="${flash}" />

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code: 'default.content.label')}</th>
            <th>${message(code: 'default.type.label')}</th>
            <th>${message(code: 'default.activated.label')}</th>
            <th>${message(code: 'default.lastUpdated.label')}</th>
            <th class="la-action-info">${message(code:'default.actions.label')}</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${systemMessages}" var="msg">
        <tr style="vertical-align: top">
            <td>
                <div class="ui attached segment">
                    <strong>${message(code: 'default.german.label')}</strong><br />
                    <ui:xEditable owner="${msg}" field="content_de" type="textarea"/>
                </div>
                <div class="ui attached segment">
                    <strong>${message(code: 'default.english.label')}</strong><br />
                    <ui:xEditable owner="${msg}" field="content_en" type="textarea"/>
                </div>
            </td>
            <td>
                <g:if test="${SystemMessage.TYPE_ATTENTION == msg.type}">
                    <span class="ui label yellow">Systemmeldung</span>
                </g:if>
                <g:if test="${SystemMessage.TYPE_STARTPAGE_NEWS == msg.type}">
                    <span class="ui label blue">Startseite</span>
                </g:if>
            </td>
            <td>
                <ui:xEditableBoolean owner="${msg}" field="isActive"/>
            </td>
            <td>
                <g:formatDate date="${msg.lastUpdated}" format="${message(code: 'default.date.format.noZ')}"/>
            </td>
            <td class="x">
                <g:link controller="admin" action="deleteSystemMessage" id="${msg.id}" class="ui negative icon button la-modern-button"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                    <i class="trash alternate outline icon"></i>
                </g:link>
            </td>
        </tr>
        </g:each>
    </tbody>
</table>

<ui:modal id="modalCreateSystemMessage" message="admin.systemMessage.create">
    <g:form class="ui form" url="[controller: 'admin', action: 'systemMessages', params: [create: true]]" method="post">

        <fieldset>
            <div class="field">
                <label for="content_de">${message(code: 'default.content.label')} (${message(code: 'default.german.label')})</label>
                <textarea name="content_de" id="content_de"></textarea>
            </div>

            <div class="field">
                <label for="content_en">${message(code: 'default.content.label')} (${message(code: 'default.english.label')})</label>
                <textarea name="content_en" id="content_en"></textarea>
            </div>

            <div class="field">
                <label>${message(code: 'default.type.label')}</label>
                <g:select from="${[[SystemMessage.TYPE_ATTENTION, 'Systemmeldung'], [SystemMessage.TYPE_STARTPAGE_NEWS, 'Startseite']]}"
                          optionKey="${{it[0]}}"
                          optionValue="${{it[1]}}"
                          name="type"
                          class="ui fluid search dropdown"/>
            </div>
        </fieldset>
    </g:form>
</ui:modal>

<laser:htmlEnd />
