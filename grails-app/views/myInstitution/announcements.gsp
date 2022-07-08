<laser:htmlStart message="announcement.plural" />

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="announcement.plural" class="active" />
        </semui:breadcrumbs>

        <semui:h1HeaderWithIcon message="announcement.plural" />

        <br />
        <br />
        <g:message code="profile.dashboardItemsTimeWindow" args="${itemsTimeWindow}" />

        <br />

            <table class="ui table la-js-responsive-table la-table compact">
              <g:each in="${recentAnnouncements}" var="ra">
                <tr>
                  <td>
                      <strong>${ra.title}</strong> <br />
                    <% print ra.content; /* avoid auto encodeAsHTML() */ %>
                    <span class="la-float-right">${message(code:'announcement.posted_by.label')}
                    <em><g:link controller="user" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link></em>
                    ${message(code:'default.on')}
                    <g:formatDate date="${ra.dateCreated}" formatName="default.date.format.notime"/></span>
                  </td>
                </tr>
              </g:each>
            </table>

  <laser:htmlEnd />
