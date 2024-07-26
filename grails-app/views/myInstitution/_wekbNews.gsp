<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.remote.ApiSource" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info' && wekbNews.counts.all > 0}">

    <div id="wekb-menu" style="margin:1em 0 2em 0">
        <div style="margin:1em 0;padding:0 1em; text-align:right">
            <div class="ui large labels">
                <a href="#" id="wekb-menu-trigger" class="ui label"><i class="${Icon.WEKB} blue"></i>&nbsp;We:kb-News</a>
                    <a href="#" class="ui icon label la-popup-tooltip wekb-flyout-trigger" data-preset="all,my"
                       data-content="${message(code: 'menu.my')}" data-position="top right">
                            <i class="${Icon.SIG.MY_OBJECT} yellow"></i> ${wekbNews.counts.my}
                    </a>
                    <a href="#" class="ui icon label la-popup-tooltip wekb-flyout-trigger" data-preset="all,marker"
                       data-content="${message(code: 'marker.WEKB_CHANGES')}" data-position="top right">
                            <i class="${Icon.MARKER} purple"></i> ${wekbNews.counts.marker}
                    </a>
            </div>
        </div>
        <div id="wekb-menu-content" style="display:none">

%{--                Folgende Ressourcen wurden in den vergangenen <strong>${wekbNews.query.days}</strong> Tagen--}%
%{--        (seit dem <strong>${wekbNews.query.changedSince}</strong>) geändert, bzw. neu angelegt.--}%

            <div class="ui four column grid">

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">${message(code: 'provider.label')}%{--: ${wekbNews.provider.count}--}%</a>
                                <div class="right floated meta"><i class="${Icon.PROVIDER}"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,created">Neu: ${wekbNews.provider.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,updated">Geändert: ${wekbNews.provider.countUpdated}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,deleted">Gelöscht: ${wekbNews.provider.deleted.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.provider.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="provider,marker"><i class="${Icon.MARKER}"></i>${wekbNews.provider.marker.size()}</a> <br/>
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">${message(code: 'vendor.plural')}%{--: ${wekbNews.vendor.count}--}%</a>
                                <div class="right floated meta"><i class="${Icon.VENDOR}"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,created">Neu: ${wekbNews.vendor.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,updated">Geändert: ${wekbNews.vendor.countUpdated}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,deleted">Gelöscht: ${wekbNews.vendor.deleted.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.vendor.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="vendor,marker"><i class="${Icon.MARKER}"></i>${wekbNews.vendor.marker.size()}</a> <br/>
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">${message(code: 'platform.plural')}%{--: ${wekbNews.platform.count}--}%</a>
                                <div class="right floated meta"><i class="${Icon.PLATFORM}"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,created">Neu: ${wekbNews.platform.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,updated">Geändert: ${wekbNews.platform.countUpdated}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,deleted">Gelöscht: ${wekbNews.platform.deleted.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.platform.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="platform,marker"><i class="${Icon.MARKER}"></i>${wekbNews.platform.marker.size()}</a> <br/>
                            </div>
                        </div>
                    </div>
                </div><!-- .column -->

                <div class="column">
                    <div class="ui fluid card" style="margin-bottom:0">
                        <div class="content">
                            <div class="header">
                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">${message(code: 'package.plural')}%{--: ${wekbNews.package.count}--}%</a>
                                <div class="right floated meta"><i class="${Icon.PACKAGE}"></i></div>
                            </div>
                        </div>
                        <div class="content">
                            <div class="description">
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,created">Neu: ${wekbNews.package.created.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,updated">Geändert: ${wekbNews.package.countUpdated}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,deleted">Gelöscht: ${wekbNews.package.deleted.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,my"><i class="${Icon.SIG.MY_OBJECT}"></i>${wekbNews.package.my.size()}</a> <br/>
                                    <a href="#" class="wekb-flyout-trigger" data-preset="package,marker"><i class="${Icon.MARKER}"></i>${wekbNews.package.marker.size()}</a> <br/>
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

        JSPC.app.dashboard.wekbNews = {
            preset: {
                obj: 'all', filter: 'all'
            },
            dataLoaded: false
        }

        $('a.wekb-flyout-trigger').on ('click', function(e) {
            e.preventDefault()
            let preset = $(this).attr ('data-preset').split(',')
            JSPC.app.dashboard.wekbNews.preset.obj = preset[0]
            JSPC.app.dashboard.wekbNews.preset.filter = preset[1]

            $('#globalLoadingIndicator').show()

            if (JSPC.app.dashboard.wekbNews.dataLoaded) {
                JSPC.app.dashboard.wekbNews.flyout (preset[0], preset[1])

                $('#wekbFlyout').flyout('show')
                $('#globalLoadingIndicator').hide()
            }
            else {
                $.ajax ({
                    url: "<g:createLink controller="ajaxHtml" action="wekbNewsFlyout"/>"
                }).done (function (response) {
                    JSPC.app.dashboard.wekbNews.dataLoaded = true

                    $('#wekbFlyout').html (response)
                    JSPC.app.dashboard.wekbNews.flyout (preset[0], preset[1])

                    $('#wekbFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#wekbFlyout')
                    r2d2.initDynamicXEditableStuff ('#wekbFlyout')
                })
            }

        });

        JSPC.app.dashboard.wekbNews.flyout  = function(obj, filter) {
            console.log(obj,filter)

            if (! obj)    { obj    = JSPC.app.dashboard.wekbNews.preset.obj }     else { JSPC.app.dashboard.wekbNews.preset.obj = obj }
            if (! filter) { filter = JSPC.app.dashboard.wekbNews.preset.filter }  else { JSPC.app.dashboard.wekbNews.preset.filter = filter }

            $('#wekbFlyout .filterWrapper .button').removeClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-obj=' + obj + ']').addClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-filter=' + filter + ']').addClass ('active')

            let cats = '#wekbFlyout .dataWrapper'
            if (obj == 'all') {
                $(cats).removeClass('hidden')
            }
            else {
                $(cats).addClass('hidden')
                $('#wekbFlyout .dataWrapper[data-obj=' + obj + ']').removeClass('hidden')
            }

            let rows = '#wekbFlyout .dataWrapper .row'
            if (filter == 'all') {
                $(rows).removeClass('hidden')
            }
            else {
                $(rows).addClass('hidden')
                if (filter == 'created') {
                    $(rows + '[data-f1=created]').removeClass('hidden')
                }
                else if (filter == 'updated') {
                    $(rows + '[data-f1=updated]').removeClass('hidden')
                }
                else if (filter == 'deleted') {
                    $(rows + '[data-f1=deleted]').removeClass('hidden')
                }
                else if (filter == 'my') {
                    $(rows + '[data-f2=true]').removeClass('hidden')
                }
                else if (filter == 'marker') {
                    $(rows + '[data-f3=true]').removeClass('hidden')
                }
            }
        }
        </laser:script>

</g:if>

<g:if test="${tmplView == 'details'}">

    <%
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)

        tmplConfig = [
                ['provider', wekbNews.provider,   'provider.label',     "${Icon.PROVIDER}",    'menu.my.providers'],
                ['vendor',   wekbNews.vendor,     'vendor.plural',      "${Icon.VENDOR}",      'menu.my.vendors'],
                ['platform', wekbNews.platform,   'platform.plural',    "${Icon.PLATFORM}",    'menu.my.platforms'],
                ['package',  wekbNews.package,    'package.plural',     "${Icon.PACKAGE}",     'menu.my.packages']
        ]
    %>

    <div class="filterWrapper" style="margin:2em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbNews.query.days}</strong> Tage: <strong>${wekbNews.counts.all}</strong> Datensätze.<br />
            Letzter Datenabgleich: <strong>${wekbNews.query.call}</strong>
            <span style="float:right">
                <a href="${apiSource.baseUrl}" target="_blank"><i class="${Icon.WEKB} large"></i></a>
            </span>
        </p>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <div class="ui buttons mini">
                <span class="${Btn.SIMPLE}" data-obj="provider">${message(code: 'provider.label')}: ${wekbNews.provider.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="vendor">${message(code: 'vendor.plural')}: ${wekbNews.vendor.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="platform">${message(code: 'platform.plural')}: ${wekbNews.platform.count}</span>
                <span class="${Btn.SIMPLE}" data-obj="package">${message(code: 'package.plural')}: ${wekbNews.package.count}</span>
                <span class="${Btn.SIMPLE_TOOLTIP} la-long-tooltip" data-obj="all"
                      data-content="Alle anzeigen: ${message(code: 'provider.label')}, ${message(code: 'vendor.plural')}, ${message(code: 'platform.plural')}, ${message(code: 'package.plural')}">Alle</span>
            </div>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <div class="ui buttons mini">
                <span class="${Btn.SIMPLE}" data-filter="created">Neue Objekte: ${wekbNews.counts.created}</span>
                <span class="${Btn.SIMPLE}" data-filter="updated">Geänderte Objekte: ${wekbNews.counts.updated}</span>
                <span class="${Btn.SIMPLE}" data-filter="deleted">Gelöschte Objekte: ${wekbNews.counts.deleted}</span>
                <span class="${Btn.SIMPLE}" data-filter="my"><i class="${Icon.SIG.MY_OBJECT}"></i> ${wekbNews.counts.my}</span>
                <span class="${Btn.SIMPLE}" data-filter="marker"><i class="${Icon.MARKER}"></i> ${wekbNews.counts.marker}</span>
                <span class="${Btn.SIMPLE_TOOLTIP} la-long-tooltip" data-filter="all"
                      data-content="Alle anzeigen: Neue Objekte, Geänderte Objekte, Gelöschte Objekte, Meine Objekte, Meine Beobachtungsliste">Alle</span>
            </div>
        </div>
    </div>

    <style>
        .filterWrapper > .filter .button {
            color: #54575b;
            background-color: #d3dae3;
        }
        .filterWrapper > .filter .button:hover {
            background-color: #c3cad3;
        }
        .filterWrapper > .filter .button.active {
            color: #ffffff;
            background-color: #004678;
        }
        .dataWrapper .grid .row .column .label {
            margin-left: 1em;
        }
    </style>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" data-obj="${cfg[0]}" style="margin:2em">
            <div class="ui header">
                <i class="icon circular grey ${cfg[3]}"></i>
                <div class="content"> ${message(code: "${cfg[2]}")} </div>
            </div>
            <div class="ui vertically divided very compact grid" style="margin-top: 1.5em;">
                <g:each in="${cfg[1].all}" var="obj">
                    <g:set var="isObjCreated" value="${obj.uuid in cfg[1].created}" /> %{-- uuid --}%
                    <g:set var="isObjDeleted" value="${obj.uuid in cfg[1].deleted}" /> %{-- uuid --}%
                    <g:set var="isObjMy" value="${obj.id in cfg[1].my}" />  %{-- id --}%
                    <g:set var="isObjMarker" value="${obj.id in cfg[1].marker}" /> %{-- id --}%

                    <div class="row"
                         data-f1="${isObjCreated ? 'created' : (isObjDeleted ? 'deleted' : 'updated')}"
                         data-f2="${isObjMy ? 'true' : 'false'}"
                         data-f3="${isObjMarker ? 'true' : 'false'}"
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
                            <g:if test="${isObjCreated}"><span class="ui green mini label">NEU</span></g:if>
                            <g:if test="${isObjDeleted}"><span class="ui red mini label">Gelöscht</span></g:if>
                        </div>
                        <div class="column two wide center aligned">
                            <g:if test="${isObjMy}">
                                <ui:myXIcon tooltip="${message(code: "${cfg[4]}")}" color="yellow"/>
                            </g:if>
                            <g:else>
                                <i class="icon fake"></i>
                            </g:else>
                            <g:if test="${isObjMarker}">
                                <ui:markerIcon type="WEKB_CHANGES" color="purple" />
                            </g:if>
                            <g:else>
                                <i class="icon fake"></i>
                            </g:else>
                        </div>
                        <div class="column four wide center aligned">${obj.lastUpdatedDisplay}</div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wekbFlyout .filterWrapper .button').on ('click', function(e) {
            JSPC.app.dashboard.wekbNews.flyout ($(this).attr('data-obj'), $(this).attr('data-filter'))
        });
    </laser:script>
</g:if>
