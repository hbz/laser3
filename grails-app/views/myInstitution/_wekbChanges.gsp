<laser:serviceInjection />

<g:if test="${tmplView == 'info'}">

    <ui:msg class="info" noClose="true">
        Folgende Ressourcen wurden in den vergangenen <strong>${wekbChanges.query.days}</strong> Tagen
        (seit dem <strong>${wekbChanges.query.changedSince}</strong>) in der <strong>we:kb</strong> geändert, bzw. neu angelegt.

        <div class="ui four column compact grid">
            <div class="column">
                <i class="university icon"></i> ${wekbChanges.org.count} ${message(code: 'default.ProviderAgency.label')} <br/>
                <i class="cloud icon"></i> ${wekbChanges.platform.count} ${message(code: 'platform.plural')} <br/>
                <i class="gift icon"></i> ${wekbChanges.package.count} ${message(code: 'package.plural')}
            </div>
            <div class="column">
                Neu: ${wekbChanges.org.created.size()}, Geändert: ${wekbChanges.org.updated.size()} <br/>
                Neu: ${wekbChanges.platform.created.size()}, Geändert: ${wekbChanges.platform.updated.size()} <br/>
                Neu: ${wekbChanges.package.created.size()}, Geändert: ${wekbChanges.package.updated.size()}
            </div>
            <div class="column">
            </div>
            <div class="column">
                <a href="#" class="wekbFlyoutTrigger" data-filter="created">Neue Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-filter="updated">Geänderte Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-filter="all">Alle Änderungen anzeigen</a>
            </div>
        </div>
    </ui:msg>

    <div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        if (!JSPC.app.dashboard) { JSPC.app.dashboard = {} }

        JSPC.app.dashboard.wekbChanges = {
            filter: { filter: 'all', cat: 'all' }
        }

        $('a.wekbFlyoutTrigger').on ('click', function(e) {
            e.preventDefault()
            let filter = $(this).attr ('data-filter')

            if ($('#wekbFlyout').children().length > 0) {
                JSPC.app.dashboard.wekbChanges.flyout (filter, 'all')
                $('#wekbFlyout').flyout ('show')
            }
            else {
                $('#globalLoadingIndicator').show()

                $.ajax ({
                    url: "<g:createLink controller="ajaxHtml" action="wekbChangesFlyout"/>",
                    data: {
                        filter: filter
                    }
                }).done (function (response) {
                    $('#globalLoadingIndicator').hide()

                    $('#wekbFlyout').html (response)
                    JSPC.app.dashboard.wekbChanges.flyout (filter, 'all')

                    $('#wekbFlyout').flyout ('show')
                    r2d2.initDynamicUiStuff ('#wekbFlyout')
                    r2d2.initDynamicXEditableStuff ('#wekbFlyout')
                })
            }
        });

        JSPC.app.dashboard.wekbChanges.flyout  = function(filter, cat) {
            if (! filter) { filter = JSPC.app.dashboard.wekbChanges.filter.filter } else { JSPC.app.dashboard.wekbChanges.filter.filter = filter }
            if (! cat)    { cat    = JSPC.app.dashboard.wekbChanges.filter.cat }    else { JSPC.app.dashboard.wekbChanges.filter.cat = cat }

            $('#wekbFlyout .filterWrapper .button').removeClass ('red')
            $('#wekbFlyout .filterWrapper .button[data-filter=' + filter + ']').addClass ('red')
            $('#wekbFlyout .filterWrapper .button[data-cat=' + cat + ']').addClass ('red')

            let rows = '#wekbFlyout .dataWrapper .row'
            if (filter == 'all' && cat == 'all') {
                $(rows).show()
            }
            else {
                $(rows).hide()

                if (filter != 'all' && cat != 'all') { $(rows + '[data-fc=' + filter + '-' + cat + ']').show() }
                if (filter != 'all' && cat == 'all') { $(rows + '[data-fc^=' + filter + '-]').show() }
                if (filter == 'all' && cat != 'all') { $(rows + '[data-fc$=-' + cat + ']').show() }
            }
        }
    </laser:script>
</g:if>

<g:if test="${tmplView == 'details'}">

    <%
        tmplConfig = [
               ['org',      wekbChanges.org,        'default.ProviderAgency.label', 'university'],
               ['platform', wekbChanges.platform,   'platform.plural',              'cloud'],
               ['package',  wekbChanges.package,    'package.plural',               'gift']
        ]
    %>

    <div style="display:inline-block; font-weight:bold; color:white; background-color:red; padding:1em 2em; float: right">DEMO</div>

    <g:set var="wekb_count_all" value="${wekbChanges.org.count + wekbChanges.platform.count + wekbChanges.package.count}" />
    <g:set var="wekb_count_created" value="${wekbChanges.org.created.size() + wekbChanges.platform.created.size() + wekbChanges.package.created.size()}" />
    <g:set var="wekb_count_updated" value="${wekbChanges.org.updated.size() + wekbChanges.platform.updated.size() + wekbChanges.package.updated.size()}" />
    <g:set var="wekb_count_laser" value="${wekbChanges.org.laserCount + wekbChanges.platform.laserCount + wekbChanges.package.laserCount}" />

    <div class="filterWrapper" style="margin:2em 1em">
        <p style="margin:0 2em 2em">
            Änderungen der letzten <strong>${wekbChanges.query.days}</strong> Tage. Letzter Datenabgleich: <strong>${wekbChanges.query.call}</strong>
        </p>
        <div class="filter" style="margin:0 2em 0.5em;">
            <span class="ui button mini" data-filter="created">Neue Objekte: ${wekb_count_created}</span>
            <span class="ui button mini" data-filter="updated">Geänderte Objekte: ${wekb_count_updated}</span>
            <span class="ui button mini" data-filter="all">Alle: ${wekb_count_all}</span>
        </div>
        <div class="filter" style="margin:0 2em 0.5em;">
            <span class="ui button mini" data-cat="laser">In Laser vorhanden: ${wekb_count_laser}</span>
            <span class="ui button mini" data-cat="nonlaser">Nicht in Laser vorhanden: ${wekb_count_all - wekb_count_laser}</span>
            <span class="ui button mini" data-cat="all">Alle: ${wekb_count_all}</span>
        </div>
    </div>

    <g:each in="${tmplConfig}" var="cfg">
        <div class="dataWrapper" style="margin:2em 1em">
            <p class="ui header">
                <i class="icon grey ${cfg[3]}" style="vertical-align:bottom"></i> ${message(code: "${cfg[2]}")}
            </p>

            <div class="ui vertically divided very compact grid">
                <g:each in="${cfg[1].all}" var="obj" status="i">
                    <div class="three column row" data-fc="${obj.uuid in cfg[1].created ? 'created' : 'updated'}-${obj.globalUID ? 'laser' : 'nonlaser'}">
                         <div class="column one wide center aligned">${i+1}</div>
                         <div class="column eleven wide">
                            <ui:wekbIconLink type="${cfg[0]}" gokbId="${obj.uuid}" />
                            <g:if test="${obj.globalUID}">
                                <g:link controller="${cfg[2]}" action="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                            </g:if>
                            <g:else>
                                ${obj.name}
                            </g:else>
                            <g:if test="${obj.uuid in cfg[1].created}"><span class="ui yellow mini label">NEU</span></g:if>
                        </div>
                        <div class="column four wide center aligned">${obj.dateCreatedDisplay}</div>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wekbFlyout .filterWrapper .button').on ('click', function(e) {
            JSPC.app.dashboard.wekbChanges.flyout ($(this).attr('data-filter'), $(this).attr('data-cat'))
        });
    </laser:script>
</g:if>