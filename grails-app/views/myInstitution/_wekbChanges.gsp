<%@ page import="de.laser.remote.ApiSource" %>
<laser:serviceInjection />

<g:if test="${tmplView == 'info' && (wekbChanges.org.count + wekbChanges.platform.count + wekbChanges.package.count) > 0}">

    <div class="ui accordion" style="margin: 1em 0">
        <div class="title" style="padding:0 1em;">
            <a href="#" class="ui label"><i class="icon blue la-gokb"></i>&nbsp;Es gibt Änderungen in der We:kb</a>
        </div>
        <div class="content">

    <ui:msg class="info" noClose="true">
        Folgende Ressourcen wurden in den vergangenen <strong>${wekbChanges.query.days}</strong> Tagen
        (seit dem <strong>${wekbChanges.query.changedSince}</strong>) geändert, bzw. neu angelegt.

        <div class="ui four column compact grid">
            <div class="column">
                <i class="university icon"></i> ${wekbChanges.org.count} ${message(code: 'default.ProviderAgency.label')} <br/>
                <i class="cloud icon"></i> ${wekbChanges.platform.count} ${message(code: 'platform.plural')} <br/>
                <i class="gift icon"></i> ${wekbChanges.package.count} ${message(code: 'package.plural')}
            </div>
            <div class="column">
                Neu: ${wekbChanges.org.created.size()}, Geändert: ${wekbChanges.org.updated.size()}, Meine Objekte: ${wekbChanges.org.my.size()} <br/>
                Neu: ${wekbChanges.platform.created.size()}, Geändert: ${wekbChanges.platform.updated.size()}, Meine Objekte: ${wekbChanges.platform.my.size()} <br/>
                Neu: ${wekbChanges.package.created.size()}, Geändert: ${wekbChanges.package.updated.size()}, Meine Objekte: ${wekbChanges.package.my.size()}
            </div>
            <div class="column">
            </div>
            <div class="column">
                <a href="#" class="wekbFlyoutTrigger" data-preset="created,all,all">Neue Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-preset="all,all,my">Meine Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-preset="all,all,all">Alle Änderungen anzeigen</a>
            </div>
        </div>
    </ui:msg>

        </div>
    </div>

    <div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }
        JSPC.app.dashboard.wekbChanges = {
            filter: { filter: 'all', laser: 'all', my: 'all' }
        }

        $('a.wekbFlyoutTrigger').on ('click', function(e) {
            e.preventDefault()
            let preset = $(this).attr ('data-preset').split(',')
            if ($('#wekbFlyout').children().length > 0) {
                JSPC.app.dashboard.wekbChanges.flyout (preset[0], preset[1], preset[2])
                $('#wekbFlyout').flyout ('show')
            }
            else {
                $('#globalLoadingIndicator').show()

                $.ajax ({
                    url: "<g:createLink controller="ajaxHtml" action="wekbChangesFlyout"/>"
                }).done (function (response) {
                    $('#globalLoadingIndicator').hide()

                    $('#wekbFlyout').html (response)
                    JSPC.app.dashboard.wekbChanges.flyout (preset[0], preset[1], preset[2])

                    $('#wekbFlyout').flyout ('show')
                    r2d2.initDynamicUiStuff ('#wekbFlyout')
                    r2d2.initDynamicXEditableStuff ('#wekbFlyout')
                })
            }
        });

        JSPC.app.dashboard.wekbChanges.flyout  = function(filter, laser, my) {
            if (! filter) { filter = JSPC.app.dashboard.wekbChanges.filter.filter }     else { JSPC.app.dashboard.wekbChanges.filter.filter = filter }
            if (! laser)  { laser    = JSPC.app.dashboard.wekbChanges.filter.laser }    else { JSPC.app.dashboard.wekbChanges.filter.laser = laser }
            if (! my)     { my     = JSPC.app.dashboard.wekbChanges.filter.my }         else { JSPC.app.dashboard.wekbChanges.filter.my = my }

            $('#wekbFlyout .filterWrapper .button').removeClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-filter=' + filter + ']').addClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-laser=' + laser + ']').addClass ('active')
            $('#wekbFlyout .filterWrapper .button[data-my=' + my + ']').addClass ('active')

            let rows = '#wekbFlyout .dataWrapper .row'
            if (filter == 'all' && laser == 'all' && my == 'all') {
                $(rows).show()
            }
            else {
                let xf = '', xl = '', xm = ''
                if (filter != 'all') { xf = '[data-xf=' + filter + ']' }
                if (laser != 'all')  { xl = '[data-xl=' + laser + ']' }
                if (my != 'all')     { xm = '[data-xm=' + my + ']' }

                $(rows).hide()
                $(rows + xf + xl + xm).show()
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

    <g:set var="wekb_count_all"     value="${wekbChanges.org.count + wekbChanges.platform.count + wekbChanges.package.count}" />
    <g:set var="wekb_count_laser"   value="${wekbChanges.org.laserCount + wekbChanges.platform.laserCount + wekbChanges.package.laserCount}" />
    <g:set var="wekb_count_my"      value="${wekbChanges.org.my.size() + wekbChanges.platform.my.size() + wekbChanges.package.my.size()}" />
    <g:set var="wekb_count_created" value="${wekbChanges.org.created.size() + wekbChanges.platform.created.size() + wekbChanges.package.created.size()}" />
    <g:set var="wekb_count_updated" value="${wekbChanges.org.updated.size() + wekbChanges.platform.updated.size() + wekbChanges.package.updated.size()}" />

    <div class="filterWrapper" style="margin:2em 1em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbChanges.query.days}</strong> Tage: <strong>${wekb_count_all}</strong> Datensätze.<br />
            Letzter Datenabgleich: <strong>${wekbChanges.query.call}</strong>
            <span style="float:right">
                <a href="${apiSource.baseUrl}" target="_blank"><i class="icon large la-gokb"></i></a>
            </span>
        </p>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-filter="created">Neue Objekte: ${wekb_count_created}</span>
            <span class="ui button mini" data-filter="updated">Geänderte Objekte: ${wekb_count_updated}</span>
            <span class="ui button mini" data-filter="all">Alle</span>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-laser="laser">In Laser vorhanden: ${wekb_count_laser}</span>
            <span class="ui button mini" data-laser="notlaser">Nicht in Laser vorhanden: ${wekb_count_all - wekb_count_laser}</span>
            <span class="ui button mini" data-laser="all">Alle</span>
        </div>
        <div class="filter" style="margin:0 2em 0.5em; text-align:right;">
            <span class="ui button mini" data-my="my">Meine Objekte: ${wekb_count_my}</span>
            <span class="ui button mini" data-my="notmy">Nicht meine Objekte: ${wekb_count_all - wekb_count_my}</span>
            <span class="ui button mini" data-my="all">Alle</span>
        </div>
    </div>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" style="margin:2em 1em">
            <p class="ui header">
                <i class="icon grey ${cfg[3]}" style="vertical-align:bottom"></i> ${message(code: "${cfg[2]}")}
            </p>

            <div class="ui vertically divided very compact grid">
                <g:each in="${cfg[1].all}" var="obj" status="i">
                    <div class="three column row"
                         data-xf="${obj.uuid in cfg[1].created ? 'created' : 'updated'}"
                         data-xl="${obj.globalUID ? 'laser' : 'notlaser'}"
                         data-xm="${obj.uuid in cfg[1].my ? 'my' : 'notmy'}"
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
            JSPC.app.dashboard.wekbChanges.flyout ($(this).attr('data-filter'), $(this).attr('data-laser'), $(this).attr('data-my'))
        });
    </laser:script>
</g:if>