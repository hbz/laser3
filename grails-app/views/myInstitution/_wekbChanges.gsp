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
                <a href="#" class="wekbFlyoutTrigger" data-filter="created">Neu angelegte Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-filter="updated">Nur geänderte Objekte anzeigen</a> <br/>
                <a href="#" class="wekbFlyoutTrigger" data-filter="all">Alle Änderungen anzeigen</a>
            </div>
        </div>
    </ui:msg>

    <div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll"></div>

    <laser:script file="${this.getGroovyPageFileName()}">

        $('a.wekbFlyoutTrigger').on ('click', function(e) {
            e.preventDefault();

            let filter = $(this).attr ('data-filter')
            $.ajax ({
                url: "<g:createLink controller="ajaxHtml" action="wekbChangesFlyout"/>",
                data: {
                    filter: filter
                }
            }).done (function (response) {
                $('#wekbFlyout').html (response)



                $('#wekbFlyout').flyout ('show')
                r2d2.initDynamicUiStuff ('#wekbFlyout')
                r2d2.initDynamicXEditableStuff ('#wekbFlyout')

                JSPC.app.dashboardWekbFlyout ('#wekbChanges-org', filter)
                JSPC.app.dashboardWekbFlyout ('#wekbChanges-platform', filter)
                JSPC.app.dashboardWekbFlyout ('#wekbChanges-package', filter)
            })
        });

        JSPC.app.dashboardWekbFlyout = function(wrapper, filter) {
            $(wrapper + ' .filter .button[data-filter]').addClass ('secondary')
            $(wrapper + ' .filter .button[data-filter=' + filter + ']').removeClass ('secondary')

            if (filter == 'all') {
                $(wrapper + ' tr[data-filter]').show()
            } else {
                $(wrapper + ' tr[data-filter]').hide()
                $(wrapper + ' tr[data-filter=' + filter + ']').show()
            }
        }
    </laser:script>
</g:if>

<g:if test="${tmplView == 'details'}">

    <%
        tmplConfig = [
               ['wekbChanges-org',      wekbChanges.org,        'org',      'default.ProviderAgency.label', 'university'],
               ['wekbChanges-platform', wekbChanges.platform,   'platform', 'platform.plural',              'cloud'],
               ['wekbChanges-package',  wekbChanges.package,    'package',  'package.plural',               'gift']
        ]
    %>

    <div style="display:inline-block; font-weight:bold; color:white; background-color:red; padding:1em 2em">DEMO</div>

    <g:each in="${tmplConfig}" var="cfg">
        <div id="${cfg[0]}" style="margin:2em 1em">
            <p class="ui header left floated">
                <i class="icon ${cfg[4]}"></i> ${message(code: "${cfg[3]}")}
            </p>

            <div class="filter" style="float:right">
                <div class="ui button mini" data-filter="created">Neu: ${cfg[1].created.size()}</div>
                <div class="ui button mini" data-filter="updated">Geändert: ${cfg[1].updated.size()}</div>
                <div class="ui button mini" data-filter="all">Alle: ${cfg[1].count}</div>
            </div>

            <table class="ui very basic table compact">
                <thead>
                <tr style="display: none">
                    <th class="two wide"></th>
                    <th class="eleven wide"></th>
                    <th class="three wide"></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${cfg[1].all}" var="obj" status="i">
                    <tr data-filter="${obj.uuid in cfg[1].created ? 'created' : 'updated'}">
                        <td> ${i+1} </td>
                        <td>
                            <ui:wekbIconLink type="${cfg[2]}" gokbId="${obj.uuid}" />
                            <g:if test="${obj.globalUID}">
                                <g:link controller="${cfg[2]}" action="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                            </g:if>
                            <g:else>
                                ${obj.name}
                            </g:else>
                            <g:if test="${obj.uuid in cfg[1].created}"><span class="ui yellow mini label">NEU</span></g:if>
                        </td>
                        <td> ${obj.dateCreatedDisplay} </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:each>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#wekbChanges-org .filter .button[data-filter]').on ('click', function(e) {
            JSPC.app.dashboardWekbFlyout('#wekbChanges-org', $(this).attr('data-filter'))
        });
        $('#wekbChanges-platform .filter .button[data-filter]').on ('click', function(e) {
            JSPC.app.dashboardWekbFlyout('#wekbChanges-platform', $(this).attr('data-filter'))
        });
        $('#wekbChanges-package .filter .button[data-filter]').on ('click', function(e) {
            JSPC.app.dashboardWekbFlyout('#wekbChanges-package', $(this).attr('data-filter'))
        });
    </laser:script>
</g:if>