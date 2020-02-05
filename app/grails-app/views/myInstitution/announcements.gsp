<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser')} : ${message(code:'menu.datamanager.ann')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.datamanager.ann" class="active" />
        </semui:breadcrumbs>
        <br>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            ${message(code:'menu.datamanager.ann')}
        </h1>

        <g:message code="profile.dashboardItemsTimeWindow"
             default="You see events from the last {0} days."
             args="${itemsTimeWindow}" />

        <br />

            <table class="ui table la-table la-table-small">
              <g:each in="${recentAnnouncements}" var="ra">
                <tr>
                  <td>
                      <strong>${ra.title}</strong> <br/>
                    <% print ra.content; /* avoid auto encodeAsHTML() */ %>
                    <span class="la-float-right">${message(code:'announcement.posted_by.label', default:'posted by')}
                    <em><g:link controller="user" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link></em>
                    ${message(code:'default.on', default:'on')}
                    <g:formatDate date="${ra.dateCreated}" formatName="default.date.format.notime"/></span>
                  </td>
                </tr>
              </g:each>
            </table>

  </body>
</html>
