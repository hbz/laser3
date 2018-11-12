<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.abstract_domain.AbstractProperty" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.title', default:'Institutional Dash')}</title>
    </head>
    <body>

        <laser:serviceInjection />

        <semui:breadcrumbs>
            <semui:crumb text="${institution?.getDesignation()}" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${institution.name}</h1>

        <div class="ui equal width grid">
            <div class="row">

                <div class="column">
                    <div class="ui divided relaxed list">
                        <div class="item">
                            <g:link controller="myInstitution" action="currentSubscriptions">${message(code:'menu.institutions.mySubs')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="currentLicenses">${message(code:'menu.institutions.myLics')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="currentTitles">${message(code:'menu.institutions.myTitles')}</g:link>
                        </div>
                    </div>
                </div>

                <div class="column">
                    <div class="ui divided relaxed list">
                        <div class="item">
                            <g:link controller="myInstitution" action="changes">${message(code: 'myinst.todo.label', default: 'To Do')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="announcements">${message(code: 'announcement.plural', default: 'Announcements')}</g:link>
                        </div>

                        <g:if test="${grailsApplication.config.feature_finance}">
                            <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="finance" message="menu.institutions.finance" />
                        </g:if>
                    </div>
                </div>

                <div class="column">
                    <div class="ui divided relaxed list">
                        <div class="item">
                            <g:link controller="myInstitution" action="tasks">${message(code:'task.plural', default:'Tasks')}</g:link>
                        </div>
                        <div class="item">
                            <g:link controller="myInstitution" action="addressbook">${message(code:'menu.institutions.myAddressbook')}</g:link>
                        </div>

                        <semui:securedMainNavItem affiliation="INST_EDITOR" controller="myInstitution" action="managePrivateProperties" message="menu.institutions.manage_props" />
                    </div>
                </div>
            </div>
        </div>

        <semui:messages data="${flash}" />

        <br />

    <g:set var="US_DASHBOARD_TAB" value="${user.getSetting(UserSettings.KEYS.DASHBOARD_TAB, RefdataValue.getByValueAndCategory('Due Dates', 'User.Settings.Dashboard.Tab'))}" />

    <div class="ui secondary pointing tabular menu">
            <a class="${US_DASHBOARD_TAB.getValue().value == 'Changes' ? 'active item':'item'}" data-tab="first">
                <i class="clock outline icon large"></i>
                <%
                    def countChanges = 0
                    changes.collect { c ->
                        countChanges += c[1]
                    }
                %>
                ${countChanges}
                ${message(code:'myinst.todo.label', default:'To Do')}
            </a>
            <a class="${US_DASHBOARD_TAB.getValue().value=='Announcements' ? 'active item':'item'}" data-tab="second" id="jsFallbackAnnouncements">
                <i class="warning circle icon large"></i>
                ${recentAnnouncements.size()}
                ${message(code:'announcement.plural', default:'Announcements')}
            </a>
            <a class="${US_DASHBOARD_TAB.getValue().value=='Tasks' ? 'active item':'item'}" data-tab="third">
                <i class="checked calendar icon large"></i>
                ${tasks.size()}
                ${message(code:'myinst.dash.task.label')}
            </a>
            <a class="${US_DASHBOARD_TAB.getValue().value=='Due Dates' ? 'active item':'item'}" data-tab="forth">
                <i class="checked alarm end icon large"></i>
                ${message(code:'myinst.dash.due_dates.label')}
            </a>
        </div>

        <div class="ui bottom attached tab segment" data-tab="first" style="border-top: 1px solid #d4d4d5; ">
            <g:if test="${editable}">
                <div class="pull-right">
                    <g:link action="changes" class="ui button">${message(code:'myinst.todo.submit.label', default:'View To Do List')}</g:link>
                </div>
            </g:if>

            <div>
                ${message(code: 'profile.dashboardReminderPeriod')}:
                ${user.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}
            </div>

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${changes}" var="changeSet">
                    <g:set var="change" value="${changeSet[0]}" />
                    <div class="item">

                       <div class="ui internally celled grid">
                           <div class="row">
                               <div class="three wide column">
                                   <a class="ui green circular label">${changeSet[1]}</a>
                                </div><!-- .column -->
                                <div class="thirteen wide column">

                                <g:if test="${change instanceof com.k_int.kbplus.Subscription}">
                                    <strong>${message(code:'subscription')}</strong>
                                    <br />
                                    <g:link controller="subscriptionDetails" action="changes" id="${change.id}">${change.toString()}</g:link>
                                </g:if>
                                <g:if test="${change instanceof com.k_int.kbplus.License}">
                                    <strong>${message(code:'license')}</strong>
                                    <br />
                                    <g:link controller="licenseDetails" action="changes" id="${change.id}">${change.toString()}</g:link>
                                </g:if>
                           </div><!-- .column -->
                           </div><!-- .row -->
                       </div><!-- .grid -->

                    </div>
                    <%--
                    <div class="item">
                        <div class="icon">
                            <i class="alarm outline icon"></i>
                            <div class="ui yellow circular label">${change.num_changes}</div>
                        </div>
                        <div class="message">
                            <p>
                                <g:if test="${change.item_with_changes instanceof com.k_int.kbplus.Subscription}">
                                    <g:link controller="subscriptionDetails" action="changes" id="${change.item_with_changes.id}">${change.item_with_changes.toString()}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="licenseDetails" action="changes" id="${change.item_with_changes.id}">${change.item_with_changes.toString()}</g:link>
                                </g:else>
                            </p>
                            <p>
                                ${message(code:'myinst.change_from', default:'Changes between')}
                                <g:formatDate date="${change.earliest}" formatName="default.date.format"/>
                                ${message(code:'myinst.change_to', default:'and')}
                                <g:formatDate date="${change.latest}" formatName="default.date.format"/>
                            </p>
                        </div>
                    </div>
                    --%>
                </g:each>
            </div>
        </div>

        <div class="ui bottom attached tab segment" data-tab="second" style="border-top: 1px solid #d4d4d5; ">
            <g:if test="${editable}">
                <div class="pull-right">
                    <g:link action="announcements" class="ui button">${message(code:'myinst.ann.view.label', default:'View All Announcements')}</g:link>
                </div>
            </g:if>

            <div>
                ${message(code: 'profile.dashboardReminderPeriod')}:
                ${user.getSettingsValue(com.k_int.kbplus.UserSettings.KEYS.DASHBOARD_REMINDER_PERIOD, 14)}
            </div>

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${recentAnnouncements}" var="ra">
                    <div class="item">

                        <div class="ui internally celled grid">
                            <div class="row">
                                <div class="three wide column">

                                    <strong>${message(code:'myinst.ann.posted_by', default:'Posted by')}</strong>
                                    <g:link controller="userDetails" action="show" id="${ra.user?.id}">${ra.user?.displayName}</g:link>
                                    <br /><br />
                                    <g:formatDate date="${ra.dateCreated}" formatName="default.date.format.noZ"/>

                                </div><!-- .column -->
                                <div class="thirteen wide column">

                                    <div class="header" style="margin:0 0 1em 0">
                                        <g:set var="ann_nws" value="${ra.title?.replaceAll(' ', '')}"/>
                                        ${message(code:"announcement.${ann_nws}", default:"${ra.title}")}
                                    </div>
                                    <div>
                                        <div class="widget-content"><% print ra.content; /* avoid auto encodeAsHTML() */ %></div>
                                    </div>

                                </div><!-- .column -->
                            </div><!-- .row -->
                        </div><!-- .grid -->

                    </div>
                </g:each>
            </div>
        </div>

        <div class="ui bottom attached tab" data-tab="third">

            <g:if test="${editable}">
                <div class="ui right aligned grid">
                    <div class="right floated right aligned sixteen wide column">
                        <input type="submit" class="ui button" value="${message(code:'task.create.new')}" data-semui="modal" href="#modalCreateTask" />
                    </div>
                </div>
            </g:if>

            <div class="ui cards">
                <g:each in="${tasks}" var="tsk">
                    <div class="ui card">

                        <div class="ui label">
                            <div class="right floated author">
                                Status:
                                <span>
                                <semui:xEditableRefData config="Task Status" owner="${tsk}" field="status" />
                                </span>
                            </div>
                        </div>

                        <div class="content">
                            <div class="meta">
                                <div class="">Fällig: <strong><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk?.endDate}"/></strong></div>
                            </div>
                            <a class="header" onclick="taskedit(${tsk?.id});">${tsk?.title}</a>

                            <div class="description">
                                <g:if test="${tsk.description}">
                                    <span><em>${tsk.description}</em></span> <br />
                                </g:if>
                            </div>
                        </div>
                        <div class="extra content">
                            <g:if test="${tsk.getObjects()}">
                                <g:each in="${tsk.getObjects()}" var="tskObj">
                                    <div class="item">
                                        <span data-tooltip="${message(code: 'task.' + tskObj.controller)}" data-position="left center" data-variation="tiny">
                                            <g:if test="${tskObj.controller == 'organisations'}">
                                                <i class="university icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('subscription')}">
                                                <i class="folder open icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('package')}">
                                                <i class="gift icon"></i>
                                            </g:if>
                                            <g:if test="${tskObj.controller.contains('license')}">
                                                <i class="book icon"></i>
                                            </g:if>
                                        </span>
                                        <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object?.id]}">${tskObj.object}</g:link>
                                    </div>
                                </g:each>
                            </g:if>
                            <g:else>
                                <i class="checked calendar icon"></i>
                                ${message(code: 'task.general')}
                            </g:else>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>
    <div class="ui bottom attached active tab segment" data-tab="forth" style="border-top: 1px solid #d4d4d5; ">
        Sie werden <b>${dashboardReminderPeriod} Tage</b> vor Fälligkeit an anstehende Termine erinnert.

        <hr>
        <table class="ui celled table">
            <thead>
            <tr>
                <th>${message(code:'myinst.dash.due_dates.attribute.label')}</th>
                <th>${message(code:'myinst.dash.due_date.date.label')}</th>
                <th>${message(code:'myinst.dash.due_dates.name.label')}</th>
            </tr></thead>
            <tbody>
            <g:each in="${dueObjects}" var="obj">
                <tr>
                    <td>
                        <g:if test="${obj instanceof Subscription || obj instanceof License}">
                            <g:if test="${obj.manualCancellationDate}">
                                ${message(code:'myinst.dash.due_date.noticePeriod.label')}
                                <br>
                            </g:if>
                            ${message(code:'myinst.dash.due_date.enddate.label')}
                        </g:if>
                        <g:elseif test="${obj instanceof Task}">
                            ${message(code:'myinst.dash.due_date.task.label')}
                        </g:elseif>
                        <g:elseif test="${obj instanceof AbstractProperty}">
                            <i class="icon tags la-list-icon"></i>
                            ${obj.type?.name}
                        </g:elseif>
                        <g:else>
                            Not implemented yet!
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${obj instanceof Subscription || obj instanceof License}">
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.manualCancellationDate}"/>
                            <br>
                        </g:if>
                        <g:if test="${obj instanceof AbstractProperty}">
                            <g:if test="${obj.dateValue}">
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.dateValue}"/>
                            </g:if>
                        </g:if>
                        <g:else>
                            <g:if test="${obj.endDate}">
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${obj.endDate}"/>
                            </g:if>
                        </g:else>
                    </td>
                    <td>
                        <div class="la-flexbox">
                            <g:if test="${obj instanceof Subscription}">
                                <i class="icon folder open la-list-icon"></i>
                                <g:link controller="subscriptionDetails" action="show" id="${obj.id}">${obj.name}</g:link>
                            </g:if>
                            <g:elseif test="${obj instanceof License}">
                                <i class="icon balance scale la-list-icon"></i>
                                <g:link controller="licenseDetails" action="show" id="${obj.id}">${obj.name}</g:link>
                            </g:elseif>
                            <g:elseif test="${obj instanceof Task}">
                                <span data-position="top right" data-tooltip="Aufgabe">
                                    <i class="icon tasks la-list-icon"></i>
                                </span>
                                <a class="header" onclick="taskedit(${obj?.id});">${obj?.title}</a>
                                &nbsp(Status: ${obj.status?.getI10n("value")})
                            </g:elseif>
                            <g:elseif test="${obj instanceof AbstractProperty}">
                                <g:if test="${obj.owner instanceof Person}">
                                    <i class="icon address book la-list-icon"></i>
                                    <g:link controller="person" action="show" id="${obj.owner.id}">${obj.owner?.first_name}&nbsp${obj.owner?.last_name}</g:link>
                                </g:if>
                                <g:elseif test="${obj.owner instanceof Subscription}">
                                    <i class="icon folder open la-list-icon"></i>
                                    <g:link controller="subscriptionDetails" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>
                                </g:elseif>
                                <g:elseif test="${obj.owner instanceof License}">
                                    <i class="icon balance scale la-list-icon"></i>
                                    <g:link controller="licenseDetails" action="show" id="${obj.owner?.id}">${obj.owner?.reference}</g:link>
                                </g:elseif>
                                <g:elseif test="${obj.owner instanceof Org}">
                                    <i class="icon university la-list-icon"></i>
                                    <g:link controller="organisations" action="show" id="${obj.owner?.id}">${obj.owner?.name}</g:link>
                                </g:elseif>
                                <g:else>
                                    ${obj.owner?.name}
                                </g:else>
                            </g:elseif>
                            <g:else>
                                Not implemented yet!
                            </g:else>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
   </div>


    <g:render template="/templates/tasks/modal_create" />

    <g:javascript>
        function taskedit(id) {

            $.ajax({
                url: '<g:createLink controller="ajax" action="TaskEdit"/>?id='+id,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalEditTask").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({
                        onVisible: function () {
                            r2d2.initDynamicSemuiStuff('#modalEditTask');
                            r2d2.initDynamicXEditableStuff('#modalEditTask');

                            ajaxPostFunc()
                        }
                    }).modal('show');
                }
            });
        }
    </g:javascript>
        <r:script>
            $(document).ready( function(){
                $('.tabular.menu .item').tab()

                $('#jsFallbackAnnouncements').click( function(){
                    $('.item .widget-content').readmore({
                        speed: 250,
                        collapsedHeight: 21,
                        startOpen: false,
                        moreLink: '<a href="#">[ ${message(code:'default.button.show.label')} ]</a>',
                        lessLink: '<a href="#">[ ${message(code:'default.button.hide.label')} ]</a>'
                    })
                });
                $('.xEditableManyToOne').editable({
                }).on('hidden', function() {
                        location.reload();
                 });


            })
        </r:script>

        <style>
            .item .widget-content {
                overflow: hidden;
                line-height: 20px;
            }
        </style>

    </body>
</html>
