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

        <h1 class="ui header"><semui:headerIcon />${institution.name}</h1>

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
            <g:if test="${editable}">
                <div class="pull-right">
                    <g:link action="changes" class="ui button">${message(code:'myinst.todo.submit.label', default:'View To Do List')}</g:link>
                </div>
            </g:if>

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${todos}" var="todo">
                    <div class="item">
                        <div class="icon">
                            <i class="alarm outline icon"></i>
                            <span class="ui label yellow">${todo.num_changes}</span>
                        </div>
                        <div class="message">
                            <p>
                                <g:if test="${todo.item_with_changes instanceof com.k_int.kbplus.Subscription}">
                                    <g:link controller="subscriptionDetails" action="index" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="licenseDetails" action="show" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
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
            <g:if test="${editable}">
                <div class="pull-right">
                    <g:link action="announcements" class="ui button">${message(code:'myinst.ann.view.label', default:'View All Announcements')}</g:link>
                </div>
            </g:if>

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

        <div class="ui bottom attached active tab segment" data-tab="third">
            <g:if test="${editable}">
                <div class="pull-right">
                    <input type="submit" class="ui button" value="${message(code:'task.create.new')}" data-semui="modal" href="#modalCreateTask" />
                </div>
            </g:if>

            <div class="ui relaxed list" style="clear:both;padding-top:1rem;">
                <g:each in="${tasks.sort{ a,b -> b.endDate.compareTo(a.endDate) }}" var="tsk">
                    <div class="item">
                        <div class="ui internally celled grid">
                            <div class="row">
                                <div class="three wide column">

                                    <strong>Fällig:</strong>
                                    <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk?.endDate}"/>
                                    <br />
                                    <br />
                                    <strong>Status:</strong>
                                    <semui:xEditableRefData config="Task Status" owner="${tsk}" field="status" />

                                </div><!-- .column -->
                                <div class="thirteen wide column">

                                    <div class="header" style="margin:0 0 1em 0">
                                        <a onclick="taskedit(${tsk?.id});">${tsk?.title}</a>
                                    </div>
                                    <div class="description"style="margin:0 0 1em 0">
                                        <g:if test="${tsk.description}">
                                            <span><em>${tsk.description}</em></span> <br />
                                        </g:if>
                                    </div>
                                    <div>
                                        <strong>Betrifft:</strong>
                                        <g:if test="${tsk.getObjects()}">
                                            <g:each in="${tsk.getObjects()}" var="tskObj">
                                                <div class="item">
                                                    <g:link controller="${tskObj.controller}" action="show" params="${[id:tskObj.object?.id]}">${tskObj.object}</g:link>
                                                    &nbsp; (${message(code: 'task.' + tskObj.controller)})
                                                </div>
                                            </g:each>
                                        </g:if>
                                        <g:else>
                                            ${message(code: 'task.general')}
                                        </g:else>
                                    </div>

                                </div><!-- .column -->
                            </div><!-- .row -->
                        </div><!-- .grid -->
                    </div>
                </g:each>
            </div>
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
