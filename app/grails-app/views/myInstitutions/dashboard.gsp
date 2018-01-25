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

    <div class="home-page">
        <div class="ui">
            <h1 class="ui header">${institution.name}</h1>

            <div class="ui equal width grid">
                <div class="row">

                    <div class="column">
                        <!--<h5 class="ui header">${message(code:'myinst.view', default:'View')}</h5>-->
                        <div class="ui divided relaxed list">
                            <div class="item"><g:link controller="myInstitutions"
                                        action="currentLicenses"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.myLics')}</g:link></div>
                            <div class="item"><g:link controller="myInstitutions"
                                        action="currentSubscriptions"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.mySubs')}</g:link></div>
                            <div class="item"><g:link controller="myInstitutions"
                                        action="currentTitles"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.myTitles')}</g:link></div>
                        </div>
                    </div>

                    <div class="column">
                        <!--<h5 class="ui header">${message(code:'myinst.renewals', default:'Renewals')}</h5>-->
                        <div class="ui divided relaxed list">
                            <div class="item"><g:link controller="myInstitutions"
                                        action="renewalsSearch"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.gen_renewals', default:'Generate Renewals Worksheet')}</g:link></div>
                            <div class="item"><g:link controller="myInstitutions"
                                        action="renewalsUpload"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.imp_renew', default:'Import Renewals')}</g:link></div>
                            <g:if test="${grailsApplication.config.feature_finance}">
                                <div class="item"><g:link controller="myInstitutions"
                                        action="finance" params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.finance', default:'Finance')}</g:link></div>
                            </g:if>

                        </div>
                    </div>

                    <div class="column">
                        <!--<h5 class="ui header">${message(code:'default.special.label', default:'Special')}</h5>-->
                        <div class="ui divided relaxed list">
                            <div class="item"><g:link controller="myInstitutions"
                                        action="tasks"
                                        params="${[shortcode:params.shortcode]}">${message(code:'task.plural', default:'Tasks')}</g:link></div>
                            <div class="item"><g:link controller="myInstitutions"
                                        action="addressbook"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.addressbook', default:'Addressbook')}</g:link></div>
                            <div class="item"><g:link controller="myInstitutions"
                                        action="managePrivateProperties"
                                        params="${[shortcode:params.shortcode]}">${message(code:'menu.institutions.manage_props')}</g:link></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <semui:messages data="${flash}" />

    <br />

    <div class="ui top attached tabular menu">
        <a class="item" data-tab="first">
            <i class="alarm outline icon large"></i>
            ${message(code:'myinst.todo.label', default:'To Do')}
        </a>
        <a class="item" data-tab="second" id="jsFallbackAnnouncements">
            <i class="warning circle icon large"></i>
            ${message(code:'announcement.plural', default:'Announcements')}
        </a>
        <a class="active item" data-tab="third">
            <i class="checked calendar icon large"></i>
            ${message(code:'myinst.dash.task.label')}
        </a>
    </div>

    <div class="ui bottom attached tab segment" data-tab="first">
        <div class="pull-right">
            <g:link action="changes" params="${[shortcode:params.shortcode]}" class="ui button">${message(code:'myinst.todo.submit.label', default:'View To Do List')}</g:link>
        </div>

        <div class="ui relaxed divided list">
            <g:each in="${todos}" var="todo">
                <div class="item">
                    <div class="icon">
                        <i class="alarm outline icon"></i>
                        <span class="badge badge-warning">${todo.num_changes}</span>
                    </div>
                    <div class="message">
                        <p>
                            <g:if test="${todo.item_with_changes instanceof com.k_int.kbplus.Subscription}">
                                <g:link controller="subscriptionDetails" action="index" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="licenseDetails" action="index" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                            </g:else>
                        </p>
                        <p>
                            ${message(code:'myinst.change_from', default:'Changes between')}
                            <g:formatDate date="${todo.earliest}" formatName="default.date.format"/>
                            ${message(code:'myinst.change_to', default:'and')}
                            <g:formatDate date="${todo.latest}" formatName="default.date.format"/>
                        </p>
                    </div>
                </div>
            </g:each>
        </div>
    </div>

    <div class="ui bottom attached tab segment" data-tab="second">
        <div class="pull-right">
            <g:link action="announcements" params="${[shortcode:params.shortcode]}" class="ui button">${message(code:'myinst.ann.view.label', default:'View All Announcements')}</g:link>
        </div>

        <div class="ui relaxed divided list">
            <g:each in="${recentAnnouncements}" var="ra">
                <div class="item">
                    <div class="icon">
                        <i class="warning circle icon"></i>
                    </div>
                    <div class="message">
                        <g:set var="ann_nws" value="${ra.title.replaceAll(' ','')}" />
                        <p>
                            <strong>${message(code:"announcement.${ann_nws}", default:"${ra.title}")}</strong>
                        </p>
                        <div class="widget-content">${ra.content}</div>
                        <p>
                            ${message(code:'myinst.ann.posted_by', default:'Posted by')}
                            <em><g:link controller="userDetails" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link></em>
                            <br/>
                            ${message(code:'myinst.ann.posted_on', default:'on')}
                            <g:formatDate date="${ra.dateCreated}" formatName="default.date.format"/>
                        </p>
                    </div>
                </div>
            </g:each>
        </div>
    </div>

    <div class="ui bottom attached active tab segment" data-tab="third">
        <div class="pull-right">
            <input type="submit" class="ui button" id="jsFallbackTasks" value="${message(code:'task.create.new')}" data-semui="modal" href="#modalCreateTask" />
        </div>

        <div class="ui relaxed divided list">
            <g:each in="${tasks}" var="tsk">
                <div class="item">
                    <strong><g:link controller="task" action="show" params="${[id:tsk.id]}">${tsk.title}</g:link></strong> <br />
                    <g:if test="${tsk.description}">
                        <span><em>${tsk.description}</em></span> <br />
                    </g:if>
                    <span>
                        <strong>${tsk.status?.getI10n('value')}</strong>
                        / fällig am
                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk?.endDate}"/>
                    </span>
                </div>
            </g:each>
        </div>
    </div>

  <g:render template="/templates/tasks/modal" />

  <!--div class="modal hide fade" id="modalTasks"></div-->

        <% /*
        <g:if test="${grailsApplication.config.ZenDeskBaseURL}">
        <div class="five wide column">
           <table class="ui table dashboard-widget">
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
        </div><!-- .five -->
        </g:if>
        */ %>

    <r:script>
        $(document).ready( function(){
            $('.tabular.menu .item').tab()

            $('#jsFallbackAnnouncements').click( function(){
                $('.item .widget-content').readmore({
                    speed: 250,
                    collapsedHeight: 25,
                    startOpen: false,
                    moreLink: '<a href="#">[ ${message(code:'default.button.show.label')} ]</a>',
                    lessLink: '<a href="#">[ ${message(code:'default.button.hide.label')} ]</a>'
                })
            })

            $('#jsFallbackTasks').click( function(){
                $('form#create_task .datepicker').calendar({
                        type: 'date',
                        firstDayOfWeek: 1,
                        monthFirst: false,
                        formatter: {
                            date: function (date, settings) {
                                if (!date) return '';
                                var day = date.getDate();
                                if (day<10) day="0"+day;
                                var month = date.getMonth() + 1;
                                if (month<10) month="0"+month;
                                var year = date.getFullYear();

                                if ('dd.mm.yyyy' == gspDateFormat) {
                                    console.log('dd.mm.yyyy');
                                    return day + '.' + month + '.' + year;
                                }
                                else if ('yyyy-mm-dd' == gspDateFormat) {
                                    console.log('yyyy-mm-dd');
                                    return year + '-' + month + '-' + day;
                                }
                            }
                        }
                    });
            })
        })
    </r:script>

    <style>
        .item .widget-content {
            overflow: hidden;
        }
    </style>

  </body>
</html>
