<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.addLicense.label', default:'Data import explorer')}</title>
  </head>

  <body>

    <laser:breadcrumbs>
      <laser:crumb message="menu.datamanager.dash" controller="dataManager" action="index"/>
      <laser:crumb message="menu.datamanager.ann" class="active"/>
    </laser:breadcrumbs>

    <laser:flash data="${flash}" />

    <div class="container">
      <h2>${message(code:'announcement.create.label', default:'Create announcement')}</h2>
      <g:form action="createAnnouncement">
        ${message(code:'announcement.subject.label', default:'Subject')}: <input type="text" name="subjectTxt" class="span12" value="${params.as}"/><br/>
        <textarea name="annTxt" class="span12">${params.at}</textarea><br/>
        <input type="submit" class="btn btn-primary" value="${message(code:'announcement.create.button.label', default:'Create Announcement...')}"/>
      </g:form>
    </div>

    <div class="container">
      <h2>${message(code:'announcement.previous.label', default:'previous announcements')}</h2>
      <table class="table">
        <g:each in="${recentAnnouncements}" var="ra">
          <tr>
            <td><strong>${ra.title}</strong> <br/>
            <div class="ann-content">
              ${ra.content}
            </div>
            <g:if test="${ra.user != null}">
              <span class="pull-right">${message(code:'announcement.posted_by.label', default:'posted by')} <em><g:link controller="userDetails" action="show" id="${ra.user?.id}">${(ra.user?.displayName)?:'Unknown'}</g:link></em> ${message(code:'default.on', default:'on')} <g:formatDate date="${ra.dateCreated}" format="${message(code:'default.date.format')}"/></span>
            </g:if>
            <g:else>
              <span class="pull-right">${message(code:'announcement.posted_auto.label', default:'posted automatically on')} <g:formatDate date="${ra.dateCreated}" format="${message(code:'default.date.format')}"/></span>
            </g:else>
          </tr>
        </g:each>
      </table>
    </div>
    <r:script language="JavaScript">
      $(document).ready(function() {
        $(".ann-content ul").wrap("<div class='collapse'/>");
        $(".collapse").before("<div class='btn btn-primary toggle' style='margin-bottom:5px;' type='button'>${message(code:'default.button.show.label')}</div>");
        $('.toggle').click(function(){
          console.log("Toggled!");
          if ( $(this).next().hasClass('in') ){
            $(this).text("${message(code:'default.button.show.label')}")
          }else{
            $(this).text("${message(code:'default.button.hide.label')}")
          }
          $(this).next().collapse('toggle');
        });
      });
    </r:script>
  </body>
</html>
