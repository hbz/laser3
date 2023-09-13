<%@ page import="de.laser.remote.ApiSource" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info' && wekbChanges.counts.all > 0}">

    <div id="wekb-menu" style="margin:1em 0 2em 0">
        <div style="margin:1em 0;padding:0 1em; text-align:right">
            <div class="ui large labels">
                <a href="#" id="wekb-menu-trigger" class="ui label"><i class="icon blue la-gokb"></i>&nbsp;We:kb-News</a>
                <g:if test="${wekbChanges.counts.my > 0}">
                    <a href="#" class="ui icon label la-popup-tooltip la-delay wekb-flyout-trigger" data-preset="all,my"
                       data-content="${message(code: 'menu.my')}" data-position="top right">
                            <i class="icon yellow star"></i> ${wekbChanges.counts.my}
                    </a>
                </g:if>
                <g:if test="${wekbChanges.counts.marker > 0}">
                    <a href="#" class="ui icon label la-popup-tooltip la-delay wekb-flyout-trigger" data-preset="all,marker"
                       data-content="${message(code: 'marker.WEKB_CHANGES')}" data-position="top right">
                            <i class="icon purple bookmark"></i> ${wekbChanges.counts.marker}
                    </a>
                </g:if>
            </div>
        </div>
        <div id="wekb-menu-content" style="display:none">

%{--                Folgende Ressourcen wurden in den vergangenen <strong>${wekbChanges.query.days}</strong> Tagen--}%
%{--        (seit dem <strong>${wekbChanges.query.changedSince}</strong>) geändert, bzw. neu angelegt.--}%

            <div class="ui three column grid">

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
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
                                    <a href="#" class="wekb-flyout-trigger" data-preset="org,marker"><i class="icon bookmark"></i>${wekbChanges.org.marker.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
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
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,marker"><i class="icon bookmark"></i>${wekbChanges.platform.marker.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
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
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,marker"><i class="icon bookmark"></i>${wekbChanges.package.marker.size()}</a> <br/>
%{--                                </div>--}%
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

            </div><!-- .column -->

        </div>
    </div>

    <div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('#wekb-menu-trigger').on ('click', function () {
            $('#wekb-menu-content').slideToggle (200);
        });

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }

        JSPC.app.dashboard.wekbChanges = {
            preset: {
                obj: 'all', filter: 'all'
            },
            dataLoaded: false
        }

        $('a.wekb-flyout-trigger').on ('click', function(e) {
            e.preventDefault()
            let preset = $(this).attr ('data-preset').split(',')
            JSPC.app.dashboard.wekbChanges.preset.obj = preset[0]
            JSPC.app.dashboard.wekbChanges.preset.filter = preset[1]

            $('#globalLoadingIndicator').show()

            if (JSPC.app.dashboard.wekbChanges.dataLoaded) {
                JSPC.app.dashboard.wekbChanges.flyout (preset[0], preset[1])

                $('#wekbFlyout').flyout('show')
                $('#globalLoadingIndicator').hide()
            }
            else {
                $.ajax ({
                    url: "<g:createLink controller="ajaxHtml" action="wekbChangesFlyout"/>"
                }).done (function (response) {
                    JSPC.app.dashboard.wekbChanges.dataLoaded = true

                    $('#wekbFlyout').html (response)
                    JSPC.app.dashboard.wekbChanges.flyout (preset[0], preset[1])

                    $('#wekbFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#wekbFlyout')
                    r2d2.initDynamicXEditableStuff ('#wekbFlyout')
                })
            }

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
                else if (filter == 'marker') {
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

    <div class="filterWrapper" style="margin:2em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbChanges.query.days}</strong> Tage: <strong>${wekbChanges.counts.all}</strong> Datensätze.<br />
            Letzter Datenabgleich: <strong>${wekbChanges.query.call}</strong>
            <span style="float:right">
                <a href="${apiSource.baseUrl}" target="_blank"><i class="icon large la-gokb"></i></a>
            </span>
        </p>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-obj="org">${message(code: 'default.ProviderAgency.label')}: ${wekbChanges.org.count}</span>
            <span class="ui button mini" data-obj="platform">${message(code: 'platform.plural')}: ${wekbChanges.platform.count}</span>
            <span class="ui button mini" data-obj="package">${message(code: 'package.plural')}: ${wekbChanges.package.count}</span>
            <span class="ui button mini la-popup-tooltip la-long-tooltip la-delay" data-obj="all"
                  data-content="Alle anzeigen: ${message(code: 'default.ProviderAgency.label')}, ${message(code: 'platform.plural')}, ${message(code: 'package.plural')}">Alle</span>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-filter="created">Neue Objekte: ${wekbChanges.counts.created}</span>
            <span class="ui button mini" data-filter="updated">Geänderte Objekte: ${wekbChanges.counts.updated}</span>
            <span class="ui button mini" data-filter="my"><i class="icon star"></i> ${wekbChanges.counts.my}</span>
            <span class="ui button mini" data-filter="marker"><i class="icon bookmark"></i> ${wekbChanges.counts.marker}</span>
            <span class="ui button mini la-popup-tooltip la-long-tooltip la-delay" data-filter="all"
                  data-content="Alle anzeigen: Neue Objekte, Geänderte Objekte, Meine Objekte, Meine Beobachtungsliste">Alle</span>
        </div>
    </div>

    <style>
        .filterWrapper > .filter > .button.active { background-color: #1b1c1d; }
    </style>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" data-obj="${cfg[0]}" style="margin:2em">
            <p class="ui header">
                <i class="icon grey ${cfg[3]}" style="vertical-align:bottom"></i> ${message(code: "${cfg[2]}")}
            </p>

            <div class="ui vertically divided very compact grid" style="margin-top: 1.5em;">
                <g:each in="${cfg[1].all}" var="obj">
                    <div class="row"
                         data-f1="${obj.uuid in cfg[1].created ? 'created' : 'updated'}"
                         data-f2="${obj.uuid in cfg[1].my ? 'true' : 'false'}"
                         data-f3="${obj.uuid in cfg[1].marker ? 'true' : 'false'}"
                    >
                        <div class="column one wide center aligned">
                            <ui:wekbIconLink type="${cfg[0]}" gokbId="${obj.uuid}" />
                        </div>
                        <div class="column nine wide">
                            <g:if test="${obj.globalUID}">
                                <g:link controller="${cfg[0]}" action="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                            </g:if>
                            <g:else>
                                ${obj.name}
                            </g:else>
                            <g:if test="${obj.uuid in cfg[1].created}"><span class="ui green mini label">NEU</span></g:if>
                        </div>
                        <div class="column two wide center aligned">
                            <g:if test="${obj.uuid in cfg[1].my}">
                                <ui:myXIcon tooltip="${message(code: "${cfg[4]}")}" color="yellow"/>
                            </g:if>
                            <g:else>
                                <i class="icon fake"></i>
                            </g:else>
                            <g:if test="${obj.uuid in cfg[1].marker}">
                                <ui:markerIcon type="WEKB_CHANGES" color="purple" />
                            </g:if>
                            <g:else>
                                <i class="icon fake"></i>
                            </g:else>
                        </div>
                        <div class="column four wide center aligned">${obj.lastUpdatedDisplay}</div>
%{--                        <div class="column four wide center aligned">${obj.dateCreatedDisplay}</div>--}%
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
