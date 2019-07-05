<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.user.errorReport')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.help" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.user.errorReport')}</h1>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="profile" action="errorOverview" message="profile.errorOverview.label" />
    <semui:subNavItem controller="profile" action="errorReport" message="profile.errorReport.label" />
</semui:subNav>

<div class="ui grid">
    <div class="sixteen wide column">

        <table class="ui celled la-table table">
            <thead>
                <tr>
                    <th>Status</th>
                    <th class="header"><g:message code="ticket.created.label" default="Created" /></th>
                    <th class="header"><g:message code="ticket.title.label" default="Title" /></th>
                    <th class="header"><g:message code="ticket.author.label" default="Author" /></th>
                    <th class="la-action-info">${message(code:'default.actions')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tickets}" var="ticket">
                    <tr class="ticket-row-${ticket.id}">
                        <td>
                            <g:if test="${ticket.status.value == 'New'}">
                                <div class="ui label">${ticket.status.getI10n('value')}</div>
                            </g:if>
                            <g:if test="${ticket.status.value == 'Open'}">
                                <div class="ui blue label">${ticket.status.getI10n('value')}</div>
                            </g:if>
                            <g:if test="${ticket.status.value == 'In Progress'}">
                                <div class="ui yellow label">${ticket.status.getI10n('value')}</div>
                            </g:if>
                            <g:if test="${ticket.status.value == 'Done'}">
                                <div class="ui olive label">${ticket.status.getI10n('value')}</div>
                            </g:if>
                            <g:if test="${ticket.status.value == 'Deferred'}">
                                <div class="ui grey label">${ticket.status.getI10n('value')}</div>
                            </g:if>
                        </td>
                        <td>
                            <g:formatDate date="${ticket.dateCreated}" format="${message(code: 'default.date.format.notime')}"/>
                        </td>

                        <td>
                            ${fieldValue(bean: ticket, field: "title")}
                        </td>
                        <td>
                            <g:if test="${editable}">
                                ${fieldValue(bean: ticket, field: "author")}
                            </g:if>
                            <g:else>
                                <i class="user icon"></i>
                            </g:else>
                        </td>

                        <td class="x">
                            <button class="ui icon button" data-target="ticket-content-${ticket.id}">
                                <i class="info icon"></i>
                            </button>
                        </td>
                    </tr>

                    <tr class="ticket-content-${ticket.id}" style="display:none">
                        <td colspan="5">
                            <h4 class="ui header">${ticket.title}</h4>

                            <div class="ui relaxed list">
                                <div class="item">
                                    <div class="header">Status</div>
                                    <semui:xEditableRefData owner="${ticket}" field="status" config="Ticket.Status"/>
                                </div>

                                <g:if test="${editable}">
                                    <div class="item">
                                        <div class="header">Jira</div>
                                        <semui:xEditable owner="${ticket}" field="jiraReference"/>
                                        <g:if test="${ticket.jiraReference}">
                                            &nbsp;
                                            <a href="${ticket.jiraReference}" target="_blank">
                                                <i class="external alternate icon"></i>
                                            </a>
                                        </g:if>
                                    </div>
                                </g:if>

                                <div class="item">
                                    ${ticket.described}
                                </div>
                                <div class="item">
                                    ${ticket.expected}
                                </div>
                                <div class="item">
                                    ${ticket.info}
                                </div>
                                <div class="item">
                                    <div class="header">Zuletzt bearbeitet</div>
                                    <g:formatDate date="${ticket.lastUpdated}" format="${message(code: 'default.date.format.noZ')}"/>
                                </div>

                                <g:if test="${editable}">
                                    <div class="item">
                                        <div class="header">Meta</div>
                                        ${ticket.meta}
                                    </div>
                                </g:if>
                            </div>

                        </td>
                    </tr>

                </g:each>
            </tbody>
        </table>

        <r:script>
            $(function(){
                $('tr[class*=ticket-row] .button').click( function(){
                    $('.' + $(this).attr('data-target')).toggle()
                })
            })
        </r:script>
    </div>
</div>

</body>
</html>
