<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.title', default:'Institutional Dash')} :: ${institution?.name}</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb text="${institution?.name}" class="active" />
    </semui:breadcrumbs>

    <div class="container home-page">
      <div class="well">
        <h1>${institution.name} - Dashboard</h1>
        <ul class="inline">
          <li><h5>${message(code:'myinst.view', default:'View')}:</h5></li>
          <li><g:link controller="myInstitutions" 
                                       action="currentLicenses" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'license.plural', default:'Licenses')}</g:link></li>
          <li><g:link controller="myInstitutions" 
                                       action="currentSubscriptions" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'subscription.plural', default:'Subscriptions')}</g:link></li>
          <li><g:link controller="myInstitutions" 
                                       action="currentTitles" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'title.plural', default:'Titles')}</g:link></li>
          <li><h5>${message(code:'myinst.renewals', default:'Renewals')}:</h5></li>
          <li><g:link controller="myInstitutions" 
                                       action="renewalsSearch" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.gen_renewals', default:'Generate Renewals Worksheet')}</g:link></li>
          <li><g:link controller="myInstitutions" 
                                       action="renewalsUpload" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.imp_renew', default:'Import Renewals')}</g:link></li>
          <g:if test="${grailsApplication.config.feature_finance}">
            <li><g:link controller="myInstitutions"
                                       action="finance"
                                       params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.finance', default:'Finance')}</g:link></li>
          </g:if>
          
          <li><h5>${message(code:'default.special.label', default:'Special')}:</h5></li>
          <li><g:link controller="myInstitutions" 
                                       action="addressbook" 
                                       params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.addressbook', default:'Addressbook')}</g:link></li>
          <li><g:link controller="myInstitutions" 
                                       action="managePrivateProperties"
                                       params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.manage_private_properties')}</g:link></li>
        </ul>
      </div>
    </div>

    <semui:messages data="${flash}" />

    <div class="container home-page">
      <div class="row">
        <div class="span4">
            <table class="table table-bordered dashboard-widget">
              <thead>
                <th>
                  <h5 class="pull-left">${message(code:'myinst.todo.label', default:'To Do')}</h5>
                  <img src="${resource(dir: 'images', file: 'icon_todo.png')}" alt="To-Dos" class="pull-right" />
                </th>
              </thead>
              <tbody>
              <g:each in="${todos}" var="todo">
                <tr>
                  <td>
                    <div class="pull-left icon">
                      <img src="${resource(dir: 'images', file: 'icon_todo.png')}" alt="To-Dos" /><br/>
                      <span class="badge badge-warning">${todo.num_changes}</span>
                    </div>
                    <div class="pull-right message">
                      <p>
                        <g:if test="${todo.item_with_changes instanceof com.k_int.kbplus.Subscription}">
                          <g:link controller="subscriptionDetails" action="index" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                        </g:if>
                        <g:else>
                          <g:link controller="licenseDetails" action="index" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                        </g:else>
                      </p>
                      <p>${message(code:'myinst.change_from', default:'Changes between')} <g:formatDate date="${todo.earliest}" formatName="default.date.format"/></span> ${message(code:'myinst.change_to', default:'and')} <g:formatDate date="${todo.latest}" formatName="default.date.format"/></p>
                    </div>
                  </td>
                </tr>
              </g:each>
                <tr>
                  <td>
                    <g:link action="todo" params="${[shortcode:params.shortcode]}" class="btn btn-primary pull-right">${message(code:'myinst.todo.submit.label', default:'View To Do List')}</g:link>
                  </td>
                </tr>
              </tbody>
            </table>
        </div>
        <div class="span4">
            <table class="table table-bordered dashboard-widget">
              <thead>
                <th>
                  <h5 class="pull-left">${message(code:'announcement.plural', default:'Announcements')}</h5>
                  <img src="${resource(dir: 'images', file: 'icon_announce.png')}" alt="To-Dos" class="pull-right" />
                </th>
              </thead>
              <tbody>
              <g:each in="${recentAnnouncements}" var="ra">
                <tr>
                  <td>
                    <div class="pull-left icon">
                      <img src="${resource(dir: 'images', file: 'icon_announce.png')}" alt="Annoucement" />
                    </div>
                    <div class="pull-right message">
                      <g:set var="ann_nws" value="${ra.title.replaceAll(' ','')}" />
                      <p><strong>${message(code:"announcement.${ann_nws}", default:"${ra.title}")}</strong></p>
                      <div>
                        <span class="widget-content">${ra.content}</span>
                        <div class="see-more"><a href="">[ ${message(code:'default.button.see_more.label', default:'See More')} ]</a></div>
                      </div> 
                      <p>${message(code:'myinst.ann.posted_by', default:'Posted by')} <em><g:link controller="userDetails" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link></em><div> ${message(code:'myinst.ann.posted_on', default:'on')} <g:formatDate date="${ra.dateCreated}" formatName="default.date.format"/></div></p>
                    </div>
                  </td>
                </tr>
              </g:each>
                <tr>
                  <td>
                     <g:link action="announcements" params="${[shortcode:params.shortcode]}" class="btn btn-primary pull-right">${message(code:'myinst.ann.view.label', default:'View All Announcements')}</g:link>
                  </td>
                </tr>
              </tbody>
            </table>
        </div>
        <g:if test="${grailsApplication.config.ZenDeskBaseURL}">
        <div class="span4">
           <table class="table table-bordered dashboard-widget">
              <thead>
                <th>
                  <h5 class="pull-left">${message(code:'myinst.dash.forum.label', default:'Latest Discussions')}</h5>
                  <img src="${resource(dir: 'images', file: 'icon_discuss.png')}" alt="Discussions" class="pull-right" />
                </th>
              </thead>
              <tbody>
            <g:if test="${forumActivity}">
                <g:each in="${forumActivity}" var="fa">
                  <tr>
                    <td>
                      <div class="pull-left icon">
                        <img src="${resource(dir: 'images', file: 'icon_discuss.png')}" alt="Discussion" />
                      </div>
                      <div class="pull-right message">
                        <p><strong>${fa.title}</strong></p>
                        <p>
                        <g:if test="${fa.result_type=='topic'}">
                          <g:formatDate date="${fa.updated_at}"  formatName="default.date.format"/>
                          <a href="${grailsApplication.config.ZenDeskBaseURL}/entries/${fa.id}">View Topic</a>
                          <a href="${grailsApplication.config.ZenDeskBaseURL}/entries/${fa.id}" title="View Topic (new Window)" target="_blank"><i class="icon-share-alt"></i></a>
                        </g:if>
                        <g:else>
                          <a href="${fa.url}">View ${fa.result_type}</a>
                        </g:else>
                        </p>
                      </div>
                    </td>
                  </tr>
                </g:each>
            </g:if>
            <g:else>
            <tr>
              <td>
                <p>${message(code:'myinst.dash.forum.noActivity', default:'Recent forum activity not available. Please retry later.')}</p>
              </td>
            </tr>
            </g:else>
            <tr>
              <td>
                <g:if test="${!grailsApplication.config.ZenDeskBaseURL.equals('https://projectname.zendesk.com')}">
                  <a href="${grailsApplication.config.ZenDeskBaseURL}/forums" class="btn btn-primary pull-right">${message(code:'myinst.dash.forum.visit', default:'Visit Discussion Forum')}</a>
                </g:if>
                <g:else>
                  <span class="btn btn-primary pull-right disabled">${message(code:'myinst.dash.forum.visit', default:'Visit Discussion Forum')}</span>
                </g:else>
              </td>
            </tr>
          </tbody>
          </table>
        </div>
        </g:if>
      </div>
    </div>

    <r:script>
      $(document).ready(function() {

        $(".widget-content").dotdotdot({
           height: 50,
           after: ".see-more",
           callback: function(isTruncated, orgContent) {
             if(isTruncated) {
               $(this).parent().find('.see-more').show();
             }
           }
         });

         $('.see-more').click(function(e) {

           if ($(this).text() == "[ ${message(code:'default.button.see_more.label', default:'See More')} ]") {
             e.preventDefault();
             $(this).parent().find('.widget-content').trigger('destroy');
             $(this).html("<a href=\"\">[ ${message(code:'default.button.see_less.label', default:'See Less')} ]</a>");

           } else {
             e.preventDefault();
             $(this).parent().find('.widget-content').dotdotdot({
               height: 50,
               after: ".see-more",
               callback: function(isTruncated, orgContent) {
                 if(isTruncated) {
                   $(this).parent().find('.see-more').show();
                 }
               }
             });
             $(this).html("<a href=\"\">[ ${message(code:'default.button.see_more.label', default:'See More')} ]</a>");
           }



           // e.preventDefault();
           // $(this).parent().find('.widget-content').trigger('destroy');
           // $(this).hide();
         });
      });
    </r:script>

  </body>
</html>
