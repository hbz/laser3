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

        <div class="ui secondary pointing tabular menu">
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

        <div class="ui bottom attached tab segment" data-tab="first" style="border-top: 1px solid #d4d4d5; ">
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
                            <div class="ui yellow circular label">${todo.num_changes}</div>
                        </div>
                        <div class="message">
                            <p>
                                <g:if test="${todo.item_with_changes instanceof com.k_int.kbplus.Subscription}">
                                    <g:link controller="subscriptionDetails" action="changes" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="licenseDetails" action="changes" id="${todo.item_with_changes.id}">${todo.item_with_changes.toString()}</g:link>
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

        <div class="ui bottom attached tab segment" data-tab="second" style="border-top: 1px solid #d4d4d5; ">
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

        <div class="ui bottom attached active tab " data-tab="third">

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
                            </a>
                        </div>
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
