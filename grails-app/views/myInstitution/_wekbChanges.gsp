<%@ page import="de.laser.remote.ApiSource" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info' && (wekbChanges.org.count + wekbChanges.platform.count + wekbChanges.package.count) > 0}">

    <div class="ui accordion" style="margin: 1em 0">
        <div class="title" style="padding:0 1em;">
            <a href="#" class="ui label"><i class="icon blue la-gokb"></i>&nbsp;Es gibt Änderungen in der We:kb</a>
        </div>
        <div class="content">

%{--                Folgende Ressourcen wurden in den vergangenen <strong>${wekbChanges.query.days}</strong> Tagen--}%
%{--        (seit dem <strong>${wekbChanges.query.changedSince}</strong>) geändert, bzw. neu angelegt.--}%

            <div class="ui three column grid">

                <div class="column">
                    <div class="ui fluid card">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="org,all">${message(code: 'default.ProviderAgency.label')}%{--: ${wekbChanges.org.count}--}%</a>
                                <div class="right floated meta"><i class="icon university"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="org,created">Neu: ${wekbChanges.org.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="org,updated">Geändert: ${wekbChanges.org.updated.size()}</a> <br/>
%{--                                </div>--}%
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="org,my"><i class="icon star"></i>${wekbChanges.org.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="org,favorites"><i class="icon bookmark"></i>${wekbChanges.org.my.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div>

                <div class="column">
                    <div class="ui fluid card">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">${message(code: 'platform.plural')}%{--: ${wekbChanges.platform.count}--}%</a>
                                <div class="right floated meta"><i class="icon cloud"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,created">Neu: ${wekbChanges.platform.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,updated">Geändert: ${wekbChanges.platform.updated.size()}</a> <br/>
%{--                                </div>--}%
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,my"><i class="icon star"></i>${wekbChanges.platform.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,favorites"><i class="icon bookmark"></i>${wekbChanges.platform.favorites.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div>

                <div class="column">
                    <div class="ui fluid card">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">${message(code: 'package.plural')}%{--: ${wekbChanges.package.count}--}%</a>
                                <div class="right floated meta"><i class="icon gift"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,created">Neu: ${wekbChanges.package.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,updated">Geändert: ${wekbChanges.package.updated.size()}</a> <br/>
%{--                                </div>--}%
%{--                                <div style="width:50%;min-width:130px;float:left">--}%
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,my"><i class="icon star"></i>${wekbChanges.package.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,favorites"><i class="icon bookmark"></i>${wekbChanges.package.favorites.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </div>

    <div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }

        JSPC.app.dashboard.wekbChanges = {
            preset: {
                obj: 'all', filter: 'all'
            }
        }

        $('a.wekb-flyout-trigger').on ('click', function(e) {
            e.preventDefault()
            let preset = $(this).attr ('data-preset').split(',')
            JSPC.app.dashboard.wekbChanges.preset.obj = preset[0]
            JSPC.app.dashboard.wekbChanges.preset.filter = preset[1]

            $('#globalLoadingIndicator').show()

            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="wekbChangesFlyout"/>"
            }).done (function (response) {
                $('#globalLoadingIndicator').hide()

                $('#wekbFlyout').html (response)
                JSPC.app.dashboard.wekbChanges.flyout (preset[0], preset[1])

                $('#wekbFlyout').flyout ('show')
                r2d2.initDynamicUiStuff ('#wekbFlyout')
                r2d2.initDynamicXEditableStuff ('#wekbFlyout')
            })

        });

        JSPC.app.dashboard.wekbChanges.flyout  = function(obj, filter) {
            console.log(obj,filter)

            if (! obj)    { obj    = JSPC.app.dashboard.wekbChanges.preset.obj }     else { JSPC.app.dashboard.wekbChanges.preset.obj = obj }
            if (! filter) { filter = JSPC.app.dashboard.wekbChanges.preset.filter }  else { JSPC.app.dashboard.wekbChanges.preset.filter = filter }

            $('#wekbFlyout .filterWrapper .button').removeClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-obj=' + obj + ']').addClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-filter=' + filter + ']').addClass ('active')

            let cats = '#wekbFlyout .dataWrapper'
            if (obj == 'all') {
                $(cats).show()
            }
            else {
                $(cats).hide()
                $('#wekbFlyout .dataWrapper[data-obj=' + obj + ']').show()
            }

            let rows = '#wekbFlyout .dataWrapper .row'
            if (filter == 'all') {
                $(rows).show()
            }
            else {
                $(rows).hide()
                if (filter == 'created') {
                    $(rows + '[data-f1=created]').show()
                }
                else if (filter == 'updated') {
                    $(rows + '[data-f1=updated]').show()
                }
                else if (filter == 'my') {
                    $(rows + '[data-f2=true]').show()
                }
                else if (filter == 'favorites') {
                    $(rows + '[data-f3=true]').show()
                }
            }
        }
        </laser:script>

</g:if>

<g:if test="${tmplView == 'details'}">

    <%
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        tmplConfig = [
                ['org',      wekbChanges.org,        'default.ProviderAgency.label', 'university',   'menu.my.providers'],
                ['platform', wekbChanges.platform,   'platform.plural',              'cloud',        'menu.my.platforms'],
                ['package',  wekbChanges.package,    'package.plural',               'gift',         'menu.my.packages']
        ]
    %>

    <g:set var="wekb_count_all"       value="${wekbChanges.org.count + wekbChanges.platform.count + wekbChanges.package.count}" />
    <g:set var="wekb_count_laser"     value="${wekbChanges.org.laserCount + wekbChanges.platform.laserCount + wekbChanges.package.laserCount}" />
    <g:set var="wekb_count_my"        value="${wekbChanges.org.my.size() + wekbChanges.platform.my.size() + wekbChanges.package.my.size()}" />
    <g:set var="wekb_count_favorites" value="${wekbChanges.org.favorites.size() + wekbChanges.platform.favorites.size() + wekbChanges.package.favorites.size()}" /> %{--TODO--}%
    <g:set var="wekb_count_created"   value="${wekbChanges.org.created.size() + wekbChanges.platform.created.size() + wekbChanges.package.created.size()}" />
    <g:set var="wekb_count_updated"   value="${wekbChanges.org.updated.size() + wekbChanges.platform.updated.size() + wekbChanges.package.updated.size()}" />

    <div class="filterWrapper" style="margin:2em 1em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbChanges.query.days}</strong> Tage: <strong>${wekb_count_all}</strong> Datensätze.<br />
            Letzter Datenabgleich: <strong>${wekbChanges.query.call}</strong>
            <span style="float:right">
                <a href="${apiSource.baseUrl}" target="_blank"><i class="icon large la-gokb"></i></a>
            </span>
        </p>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-obj="org">${message(code: 'default.ProviderAgency.label')}: ${wekbChanges.org.count}</span>
            <span class="ui button mini" data-obj="platform">${message(code: 'platform.plural')}: ${wekbChanges.platform.count}</span>
            <span class="ui button mini" data-obj="package">${message(code: 'package.plural')}: ${wekbChanges.package.count}</span>
            <span class="ui button mini" data-obj="all">Alle</span>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-filter="created">Neue Objekte: ${wekb_count_created}</span>
            <span class="ui button mini" data-filter="updated">Geänderte Objekte: ${wekb_count_updated}</span>
            <span class="ui button mini" data-filter="my"><i class="icon star"></i> ${wekb_count_my}</span>
            <span class="ui button mini" data-filter="favorites"><i class="icon bookmark"></i> ${wekb_count_favorites}</span>
            <span class="ui button mini" data-filter="all">Alle</span>
        </div>
    </div>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" data-obj="${cfg[0]}" style="margin:2em 1em">
            <p class="ui header">
                <i class="icon grey ${cfg[3]}" style="vertical-align:bottom"></i> ${message(code: "${cfg[2]}")}
            </p>

            <div class="ui vertically divided very compact grid">
                <g:each in="${cfg[1].all}" var="obj" status="i">
                    <div class="three column row"
                         data-f1="${obj.uuid in cfg[1].created ? 'created' : 'updated'}"
                         data-f2="${obj.uuid in cfg[1].my ? 'true' : 'false'}"
                         data-f3="${obj.globalUID ? 'true' : 'false'}" %{--TODO--}%
                    >
                        <div class="column one wide center aligned">${i+1}</div>
                        <div class="column ten wide">
                            <ui:wekbIconLink type="${cfg[0]}" gokbId="${obj.uuid}" />
                            <g:if test="${obj.globalUID}">
                                <g:link controller="${cfg[0]}" action="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                            </g:if>
                            <g:else>
                                ${obj.name}
                            </g:else>
                            <g:if test="${obj.uuid in cfg[1].created}"><span class="ui yellow mini label">NEU</span></g:if>
                        </div>
                        <div class="column one wide center aligned">
                            <g:if test="${obj.uuid in cfg[1].my}">
                                <span class="la-popup-tooltip la-delay" data-content="${message(code: "${cfg[4]}")}">
                                    <i class="icon yellow star"></i>
                                </span>
                            </g:if>
                        </div>
                        <div class="column four wide center aligned">${obj.dateCreatedDisplay}</div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wekbFlyout .filterWrapper .button').on ('click', function(e) {
            JSPC.app.dashboard.wekbChanges.flyout ($(this).attr('data-obj'), $(this).attr('data-filter'))
        });
    </laser:script>
</g:if>
