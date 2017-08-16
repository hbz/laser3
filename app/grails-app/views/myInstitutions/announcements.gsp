<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.addLicense.label', default:'Data import explorer')}</title>
  </head>

  <body>

  <laser:breadcrumbs>
    <laser:crumb controller="myInstitutions" action="announcements" params="${[shortcode:params.shortcode]}" text="${institution.name}" message="menu.datamanager.ann" />
  </laser:breadcrumbs>


    <div class="container home-page">
            <table class="table">
              <g:each in="${recentAnnouncements}" var="ra">
                <tr>
                  <td><strong>${ra.title}</strong> <br/>
                  ${ra.content} <span class="pull-right">${message(code:'announcement.posted_by.label', default:'posted by')} <em><g:link controller="userDetails" action="pub" id="${ra.user?.id}">${ra.user?.displayName}</g:link></em> ${message(code:'default.on', default:'on')} <g:formatDate date="${ra.dateCreated}" formatName="default.date.format.notime"/></span></td>
                </tr>
              </g:each>
            </table>

      <div class="pagination" style="text-align:center">
        <g:if test="${recentAnnouncements!=null}" >
          <bootstrap:paginate  action="announcements" controller="myInstitutions" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="10" total="${num_announcements}" />
        </g:if>
      </div>


    </div>


  </body>
</html>
