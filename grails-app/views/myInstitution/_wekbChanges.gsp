<laser:serviceInjection />

<ui:msg class="info" noClose="true">
    In der <strong>we:kb</strong> wurden in den vergangenen <strong>${wekbChanges.query.days}</strong> Tagen
    (seit dem <strong>${wekbChanges.query.changedSince}</strong>) folgende Ressourcen neu angelegt, bzw. geändert.
    <br/><br/>
    <i class="university icon"></i>
    <a href="#" class="wekbFlyoutTrigger">${wekbChanges.org.count} ${message(code: 'default.ProviderAgency.label')}</a>
    &nbsp;&nbsp;(Neu: ${wekbChanges.org.created.size()}, Geändert: ${wekbChanges.org.updated.size()})
    <br/>
    <i class="cloud icon"></i>
    <a href="#" class="wekbFlyoutTrigger">${wekbChanges.platform.count} ${message(code: 'platform.plural')}</a>
    &nbsp;&nbsp;(Neu: ${wekbChanges.platform.created.size()}, Geändert: ${wekbChanges.platform.updated.size()})
    <br/>
    <i class="gift icon"></i>
    <a href="#" class="wekbFlyoutTrigger">${wekbChanges.package.count} ${message(code: 'package.plural')}</a>
    &nbsp;&nbsp;(Neu: ${wekbChanges.package.created.size()}, Geändert: ${wekbChanges.package.updated.size()})
</ui:msg>

<div id="wekbFlyout" class="ui ten wide flyout" style="padding:50px 0 10px 0;overflow:scroll">

    <div id="wekbChanges-org" style="margin:2em 1em">
        <p class="ui header left floated">
            <i class="icon university"></i> ${message(code: 'default.ProviderAgency.label')}
        </p>

        <div class="filter" style="float:right">
            <div class="ui button mini" data-filter="created">Neu: ${wekbChanges.org.created.size()}</div>
            <div class="ui button mini" data-filter="updated">Geändert: ${wekbChanges.org.updated.size()}</div>
            <div class="ui button mini" data-filter="all">Alle: ${wekbChanges.org.count}</div>
        </div>

        <table class="ui single line table compact">
            <thead>
                <tr style="display: none">
                    <th class="one wide"></th>
                    <th class="one wide"></th>
                    <th class="ten wide"></th>
                    <th class="four wide"></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${wekbChanges.org.all}" var="obj" status="i">
                <tr data-filter="${obj.uuid in wekbChanges.org.created ? 'created' : 'updated'}">
                    <td> ${i+1} </td>
                    <td> <ui:wekbIconLink type="platform" gokbId="${obj.uuid}" /> </td>
                    <td>
                        <g:link controller="org" actione="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                        <g:if test="${obj.uuid in wekbChanges.org.created}"><span class="ui yellow mini label">NEU</span></g:if> </td>
                    <td> ${obj.dateCreatedDisplay} </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div id="wekbChanges-platform" style="margin:2em 1em">
        <p class="ui header left floated">
            <i class="icon cloud"></i> ${message(code: 'platform.plural')}
        </p>

        <div class="filter" style="float:right">
            <div class="ui button mini" data-filter="created">Neu: ${wekbChanges.platform.created.size()}</div>
            <div class="ui button mini" data-filter="updated">Geändert: ${wekbChanges.platform.updated.size()}</div>
            <div class="ui button mini" data-filter="all">Alle: ${wekbChanges.platform.count}</div>
        </div>

        <table class="ui single line table compact">
            <thead>
                <tr style="display: none">
                    <th class="one wide"></th>
                    <th class="one wide"></th>
                    <th class="ten wide"></th>
                    <th class="four wide"></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${wekbChanges.platform.all}" var="obj" status="i">
                <tr data-filter="${obj.uuid in wekbChanges.platform.created ? 'created' : 'updated'}">
                    <td> ${i+1} </td>
                    <td> <ui:wekbIconLink type="platform" gokbId="${obj.uuid}" /> </td>
                    <td>
                        <g:link controller="platform" actione="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                        <g:if test="${obj.uuid in wekbChanges.platform.created}"><span class="ui yellow mini label">NEU</span></g:if>
                    </td>
                    <td> ${obj.dateCreatedDisplay} </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div id="wekbChanges-package" style="margin:2em 1em">
        <p class="ui header left floated">
            <i class="icon gift"></i> ${message(code: 'package.plural')}
        </p>

        <div class="filter" style="float:right">
            <div class="ui button mini" data-filter="created">Neu: ${wekbChanges.package.created.size()}</div>
            <div class="ui button mini" data-filter="updated">Geändert: ${wekbChanges.package.updated.size()}</div>
            <div class="ui button mini" data-filter="all">Alle: ${wekbChanges.package.count}</div>
        </div>

        <table class="ui single line table compact">
            <thead>
                <tr style="display: none">
                    <th class="one wide"></th>
                    <th class="one wide"></th>
                    <th class="ten wide"></th>
                    <th class="four wide"></th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${wekbChanges.package.all}" var="obj" status="i">
                <tr data-filter="${obj.uuid in wekbChanges.package.created ? 'created' : 'updated'}">
                    <td> ${i+1} </td>
                    <td> <ui:wekbIconLink type="package" gokbId="${obj.uuid}" /> </td>
                    <td>
                        <g:link controller="package" actione="show" target="_blank" params="${[id:obj.globalUID]}">${obj.name}</g:link>
                        <g:if test="${obj.uuid in wekbChanges.package.created}"><span class="ui yellow mini label">NEU</span></g:if>
                    </td>
                    <td> ${obj.dateCreatedDisplay} </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <br/>
    <br/>
    <br/>
    <br/>

</div>

    %{--    <div class="ui list">--}%
    %{--        <div class="item">--}%
    %{--            <i class="university icon"></i>--}%
    %{--            <div class="content">--}%
    %{--                <div class="header">${message(code: 'default.ProviderAgency.label')}: ${wekbChanges.org.warning.result_count_total}</div>--}%
    %{--                <div class="description">--}%
    %{--                    <g:each in="${wekbChanges.org.warning.result}" var="org">--}%
    %{--                        <g:if test="${org.lastUpdatedDisplay == org.dateCreatedDisplay}">[NEU]</g:if>--}%
    %{--                        <ui:wekbIconLink type="org" gokbId="${org.gokbId}" /> ${org.name} - ${org.lastUpdatedDisplay} - ${org.dateCreatedDisplay}--}%
    %{--                        <br />--}%
    %{--                        <br />--}%
    %{--                    </g:each>--}%
    %{--                </div>--}%
    %{--            </div>--}%
    %{--        </div>--}%
    %{--        <div class="item">--}%
    %{--            <i class="cloud icon"></i>--}%
    %{--            <div class="content">--}%
    %{--                <div class="header">${message(code: 'platform.plural')}: ${wekbChanges.platform.warning.result_count_total}</div>--}%
    %{--                <div class="description">--}%
    %{--                    <g:each in="${wekbChanges.platform.warning.result}" var="platform">--}%
    %{--                        <g:if test="${platform.lastUpdatedDisplay == platform.dateCreatedDisplay}">[NEU]</g:if>--}%
    %{--                        <ui:wekbIconLink type="platform" gokbId="${platform.gokbId}" /> ${platform.name} - ${platform.lastUpdatedDisplay} - ${platform.dateCreatedDisplay}--}%
    %{--                        <br />--}%
    %{--                        <br />--}%
    %{--                    </g:each>--}%
    %{--                </div>--}%
    %{--            </div>--}%
    %{--        </div>--}%
    %{--        <div class="item">--}%
    %{--            <i class="gift icon"></i>--}%
    %{--            <div class="content">--}%
    %{--                <div class="header">${message(code: 'package.plural')}: ${wekbChanges.package.warning.result_count_total}</div>--}%
    %{--                <div class="description">--}%
    %{--                    <g:each in="${wekbChanges.package.warning.result}" var="pkg">--}%
    %{--                        <g:if test="${pkg.lastUpdatedDisplay == pkg.dateCreatedDisplay}">[NEU]</g:if>--}%
    %{--                        <ui:wekbIconLink type="package" gokbId="${pkg.gokbId}" /> ${pkg.name} - ${pkg.lastUpdatedDisplay} - ${pkg.dateCreatedDisplay}--}%
    %{--                        <br />--}%
    %{--                        <br />--}%
    %{--                    </g:each>--}%
    %{--                </div>--}%
    %{--            </div>--}%
    %{--        </div>--}%
    %{--    </div>--}%


<laser:script file="${this.getGroovyPageFileName()}">

    $('a.wekbFlyoutTrigger').on ('click', function(e) {
        e.preventDefault();

        $('#wekbFlyout').flyout ({
            onShow: function (e) {
                JSPC.app.dashboardWekbFlyout('#wekbChanges-org', 'created')
                JSPC.app.dashboardWekbFlyout('#wekbChanges-platform', 'created')
                JSPC.app.dashboardWekbFlyout('#wekbChanges-package', 'created')
            },
            onHide: function (e) {
            },
            onHidden: function (e) {
            }
        }).flyout('toggle');
    });

    JSPC.app.dashboardWekbFlyout = function(wrapper, filter) {
        $(wrapper + ' .filter .button[data-filter]').addClass('secondary')
        $(wrapper + ' .filter .button[data-filter=' + filter + ']').removeClass('secondary')

        if (filter == 'all') {
            $(wrapper + ' tr[data-filter]').show()
        } else {
            $(wrapper + ' tr[data-filter]').hide()
            $(wrapper + ' tr[data-filter=' + filter + ']').show()
        }
    }

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