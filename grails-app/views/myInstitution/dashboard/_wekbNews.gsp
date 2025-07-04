<%@ page import="de.laser.wekb.Vendor; de.laser.License; de.laser.Subscription; de.laser.CustomerTypeService; de.laser.OrgSetting; de.laser.auth.User; de.laser.Org; de.laser.utils.DateUtils;  de.laser.storage.RDStore; de.laser.Task; de.laser.system.SystemActivityProfiler; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.remote.Wekb" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info'}">

        <div class="ui fluid card">

            <div class="ui top attached label">
                <div class="header">${message(code: 'marker.WEKB_CHANGES')}</div>
            </div>

            <div class="content">

                <g:if test="${wekbNews && wekbNews.counts.all > 0}">
                    <table class="ui basic compact table" style="border: none">
                        <tbody>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="provider,all">
                                    <i class="${Icon.PROVIDER} la-list-icon"></i>${message(code: 'provider.label')}%{--: ${wekbNews.provider.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.created.size()}">
                                    <a href="#" class="ui label green wekb-flyout-trigger" data-preset="provider,created">
                                        <i class="${Icon.CMD.ADD}"></i>${wekbNews.provider.created.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.countUpdated}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="provider,updated">
                                        <i class="${Icon.CMD.EDIT} blue"></i>${wekbNews.provider.countUpdated}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.deleted.size()}">
                                    <a href="#" class="ui label red wekb-flyout-trigger" data-preset="provider,deleted">
                                        <i class="${Icon.CMD.REMOVE}"></i>${wekbNews.provider.deleted.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.my.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="provider,my">
                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i>${wekbNews.provider.my.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.provider.marker.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="provider,marker">
                                        <i class="${Icon.MARKER} purple"></i>${wekbNews.provider.marker.size()}
                                    </a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="vendor,all">
                                    <i class="${Icon.VENDOR} la-list-icon"></i>${message(code: 'vendor.plural')}%{--: ${wekbNews.vendor.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.created.size()}">
                                    <a href="#" class="ui label green wekb-flyout-trigger" data-preset="vendor,created">
                                        <i class="${Icon.CMD.ADD}"></i>${wekbNews.vendor.created.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.countUpdated}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="vendor,updated">
                                        <i class="${Icon.CMD.EDIT} blue"></i>${wekbNews.vendor.countUpdated}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.deleted.size()}">
                                    <a href="#" class="ui label red wekb-flyout-trigger" data-preset="vendor,deleted">
                                        <i class="${Icon.CMD.REMOVE}"></i>${wekbNews.vendor.deleted.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.my.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="vendor,my">
                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i>${wekbNews.vendor.my.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.vendor.marker.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="vendor,marker">
                                        <i class="${Icon.MARKER} purple"></i>${wekbNews.vendor.marker.size()}
                                    </a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="platform,all">
                                    <i class="${Icon.PLATFORM} la-list-icon"></i>${message(code: 'platform.plural')}%{--: ${wekbNews.platform.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.created.size()}">
                                    <a href="#" class="ui label green wekb-flyout-trigger" data-preset="platform,created">
                                        <i class="${Icon.CMD.ADD}"></i>${wekbNews.platform.created.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.countUpdated}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="platform,updated">
                                        <i class="${Icon.CMD.EDIT} blue"></i>${wekbNews.platform.countUpdated}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.deleted.size()}">
                                    <a href="#" class="ui label red wekb-flyout-trigger" data-preset="platform,deleted">
                                        <i class="${Icon.CMD.REMOVE}"></i>${wekbNews.platform.deleted.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.my.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="platform,my">
                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i>${wekbNews.platform.my.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.platform.marker.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="platform,marker">
                                        <i class="${Icon.MARKER} purple"></i>${wekbNews.platform.marker.size()}
                                    </a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                            <td>
                                <a href="#" class="wekb-flyout-trigger" data-preset="package,all">
                                    <i class="${Icon.PACKAGE} la-list-icon"></i>${message(code: 'package.plural')}%{--: ${wekbNews.package.count}--}%
                                </a>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.created.size()}">
                                    <a href="#" class="ui label green wekb-flyout-trigger" data-preset="package,created">
                                        <i class="${Icon.CMD.ADD}"></i>${wekbNews.package.created.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.countUpdated}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="package,updated">
                                        <i class="${Icon.CMD.EDIT} blue"></i>${wekbNews.package.countUpdated}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.deleted.size()}">
                                    <a href="#" class="ui label red wekb-flyout-trigger" data-preset="package,deleted">
                                        <i class="${Icon.CMD.REMOVE}"></i>${wekbNews.package.deleted.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.my.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="package,my">
                                        <i class="${Icon.SIG.MY_OBJECT} yellow"></i>${wekbNews.package.my.size()}
                                    </a>
                                </g:if>
                            </td>
                            <td>
                                <g:if test="${wekbNews.package.marker.size()}">
                                    <a href="#" class="ui label wekb-flyout-trigger" data-preset="package,marker">
                                        <i class="${Icon.MARKER} purple"></i>${wekbNews.package.marker.size()}
                                    </a>
                                </g:if>
                            </td>
                        </tr>
                        <tr style="border-bottom: none">
                        </tr>
                        </tbody>
                    </table>
%{--<%--}%
%{--    // TEST--}%
%{--    wekbNews.provider.created = wekbNews.provider.all.collect{ it.uuid }--}%
%{--    wekbNews.vendor.created = wekbNews.vendor.all.collect{ it.uuid }--}%
%{--%>--}%
                    <g:set var="ccProvider" value="${wekbNews.provider.created}" />
                    <g:set var="ccVendor" value="${wekbNews.vendor.created}" />

                    <g:if test="${ccProvider.size() || ccVendor.size()}">
                        <ui:msg class="info">
                            <g:if test="${ccProvider.size()}">
                                <div>
                                    <i class="${Icon.PROVIDER} la-list-icon"></i><strong>Neue Anbieter:</strong>
                                </div>
                                <g:each in="${ccProvider}" var="p1" status="i">
                                    <g:set var="p2" value="${wekbNews.provider.all.find{ it.uuid == p1 }}" />
                                    <g:if test="${i > 0}">, </g:if>
                                    <g:if test="${p2.id}">
                                        <g:link controller="provider" action="show" id="${p2.id}" target="_blank">${p2.name}</g:link>
                                    </g:if>
                                    <g:else>
                                        ${p2.name}
                                    </g:else>
                                </g:each>
                            </g:if>
                            <g:if test="${ccVendor.size()}">
                                <g:if test="${ccProvider.size()}"><br /><br /></g:if>
                                <div>
                                    <i class="${Icon.VENDOR} la-list-icon"></i><strong>Neue Library Supplier:</strong>
                                </div>
                                <g:each in="${ccVendor}" var="v1" status="i">
                                    <g:set var="v2" value="${wekbNews.vendor.all.find{ it.uuid == v1 }}" />
                                    <g:if test="${i > 0}">, </g:if>
                                    <g:if test="${v2.id}">
                                        <g:link controller="vendor" action="show" id="${v2.id}" target="_blank">${v2.name}</g:link>
                                    </g:if>
                                    <g:else>
                                        ${v2.name}
                                    </g:else>
                                </g:each>
                            </g:if>
                        </ui:msg>
                    </g:if>
                </g:if>
                <g:else>
                    <p>
                        ${message(code: 'dashboard.card.wekbNews.noData')}
                    </p>
                </g:else>
            </div>
        </div>

    <div id="wekbFlyout" class="ui very wide flyout"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

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

<g:if test="${tmplView == 'details' && !wekbNews}">
    <p style="margin:2em">
        ${message(code: 'dashboard.card.wekbNews.noData')}
    </p>
</g:if>
<g:elseif test="${tmplView == 'details' && wekbNews}">

    <%
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
                <a href="${Wekb.getURL()}" target="_blank"><i class="${Icon.WEKB} large"></i></a>
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
                                <icon:placeholder />
                            </g:else>
                            <g:if test="${isObjMarker}">
                                <ui:markerIcon type="WEKB_CHANGES" color="purple" />
                            </g:if>
                            <g:else>
                                <icon:placeholder />
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
</g:elseif>
