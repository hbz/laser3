<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.addLicense.label', default:'Data import explorer')}</title>
    </head>

    <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.datamanager.dash" controller="dataManager" action="index"/>
        <semui:crumb message="menu.datamanager.ann" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h2 class="ui header">${message(code:'announcement.create.label', default:'Create announcement')}</h2>

    <semui:form>
        <g:form action="createAnnouncement" class="ui form">
            ${message(code:'announcement.subject.label', default:'Subject')}: <input type="text" name="subjectTxt" value="${params.as}" /><br/>
            <textarea name="annTxt">${params.at}</textarea>
            <br />
            <input type="submit" class="ui primary button" value="${message(code:'announcement.create.button.label', default:'Create Announcement...')}" />
        </g:form>
    </semui:form>


      <h2 class="ui header">${message(code:'announcement.previous.label', default:'previous announcements')}</h2>

      <table class="ui table">
          <tbody>
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
          </tbody>
      </table>

    <r:script language="JavaScript">
      $(document).ready(function() {
        $(".ann-content ul").wrap("<div class='collapse'/>");
        $(".collapse").before("<div class='ui primary mini button toggle' style='margin-bottom:5px;' type='button'>${message(code:'default.button.show.label')}</div>");
        $('.toggle').click(function(){
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
