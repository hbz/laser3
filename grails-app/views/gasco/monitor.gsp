<%@ page import="de.laser.storage.BeanStore; de.laser.addressbook.PersonRole; de.laser.addressbook.Contact; de.laser.wekb.ProviderRole; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.Subscription; de.laser.storage.PropertyStore; de.laser.Org; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants;" %>

<laser:htmlStart message="menu.public.gasco_monitor" description="${message(code:'metaDescription.gasco')}" publicLayout="gasco">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<g:render template="/layouts/gasco/nav" />

<h1 class="ui header">
    <g:message code="${message(code: 'menu.public.gasco_monitor')}: ${message(code: 'gasco.licenceSearch')}" />
</h1>

<div class="ui grid">
    <div class="sixteen wide column">
        <div class="ui segment">
            <g:form controller="gasco" action="monitor" method="get" class="ui small form">

                <div class="field">
                    <label for="search">${message(code: 'default.search.label')}</label>

                    <div class="ui input">
                        <input type="text" id="search" name="q" placeholder="${message(code: 'default.search.ph')}" value="${params.q}"/>
                    </div>
                </div>
                <div class="field">
                    <fieldset id="subscritionKind">
                        <legend>${message(code: 'myinst.currentSubscriptions.subscription_kind')}</legend>
                        <div class="inline fields la-filter-inline">

                            <%
                                List subkinds = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)
                                subkinds -= RDStore.SUBSCRIPTION_KIND_LOCAL
                            %>

                            <g:each in="${subkinds}" var="subKind">
                                    <g:if test="${subKind.value == RDStore.SUBSCRIPTION_KIND_NATIONAL.value}">
                                        <div class="inline field js-nationallicence">
                                    </g:if>
                                    <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_ALLIANCE.value}">
                                        <div class="inline field js-alliancelicence">
                                    </g:elseif>
                                    <g:elseif test="${subKind.value == RDStore.SUBSCRIPTION_KIND_CONSORTIAL.value}">
                                        <div class="inline field js-consortiallicence">
                                    </g:elseif>
                                    <g:else>
                                        <div class="inline field">
                                    </g:else>
                                        <div class="ui checkbox">
                                            <label for="checkSubType-${subKind.id}">${subKind.getI10n('value')}</label>
                                            <input id="checkSubType-${subKind.id}" name="subKinds" type="checkbox" value="${subKind.id}"
                                                <g:if test="${Params.getLongList(params, 'subKinds').contains(subKind.id)}"> checked="" </g:if>
                                                <g:if test="${initQuery}"> checked="" </g:if>
                                                   tabindex="0">
                                        </div>
                                    </div>
                            </g:each>

                        </div>
                    </fieldset>
                </div>
                <div class="field" id="js-consotial-authority">
                    <fieldset>
                        <label for="consortia" id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</label>

                        <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown clearable "
                            optionKey="${{ Org.class.name + ':' + it.id }}"
                            optionValue="${{ it.getName() }}"
                            name="consortia" noSelection="${['' : message(code:'default.select.choose.label')]}" value="${params.consortia}"/>
                    </fieldset>

                </div>

                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                    <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.search.label')}">
                </div>

            </g:form>
        </div>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.toggleFilterPart = function () {
            if ($('.js-consortiallicence input').prop('checked')) {
                $('#js-consotial-authority .dropdown').removeClass('disabled')
                $('#js-consotial-authority select').removeAttr('disabled')
            } else {
                $('#js-consotial-authority .dropdown').addClass('disabled')
                $('#js-consotial-authority select').attr('disabled', 'disabled')
            }
        }
        JSPC.app.toggleTableHeading = function () {
            if ($('.js-nationallicence input').prop('checked') || $('.js-alliancelicence input').prop('checked')) {
                $('#js-negotiator-header').show()
                $('#js-consortium-header').hide()
            } else {
                $('#js-negotiator-header').hide()
                $('#js-consortium-header').show()
            }
        }
        JSPC.app.toggleFilterPart()
        $('.js-nationallicence').on('click', JSPC.app.toggleFilterPart)
        $('.js-alliancelicence').on('click', JSPC.app.toggleFilterPart)
        $('.js-consortiallicence').on('click', JSPC.app.toggleFilterPart)
        JSPC.app.toggleTableHeading()
        $('.ui primary button').on('click', JSPC.app.toggleTableHeading)

</laser:script>

<g:if test="${subscriptions}">
<br><br>
<table class="ui striped table">
    <thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th>${message(code:'gasco.table.product')}</th>
        <th>${message(code:'gasco.table.provider')}</th>
        <th>
            <span id="js-consortium-header">${message(code:'gasco.table.consortium')}</span>
            <span id="js-negotiator-header">${message(code:'gasco.table.negotiator')}</span>
        </th>
        <th> </th>
    </tr>
    </thead>
    <tbody>
    <g:set var="GASCO_INFORMATION_LINK" value="${PropertyStore.SUB_PROP_GASCO_INFORMATION_LINK}" />
    <g:set var="GASCO_ANZEIGENAME" value="${PropertyStore.SUB_PROP_GASCO_DISPLAY_NAME}" />
    <g:set var="GASCO_VERHANDLERNAME" value="${PropertyStore.SUB_PROP_GASCO_NEGOTIATOR_NAME}" />
    <%
        List flyoutCheckList = Subscription.executeQuery(
                'select distinct(s.instanceOf.id), count(*) from Subscription s group by s.instanceOf.id order by s.instanceOf.id'
        ).collect{ it[0] }
    %>
        <g:each in="${subscriptions}" var="sub" status="i">
            <g:set var="gasco_infolink" value="${sub.propertySet.find{ it.type == GASCO_INFORMATION_LINK}?.urlValue}" />
            <g:set var="gasco_anzeigename" value="${sub.propertySet.find{ it.type == GASCO_ANZEIGENAME}?.stringValue}" />
            <g:set var="gasco_verhandlername" value="${sub.propertySet.find{ it.type == GASCO_VERHANDLERNAME}?.stringValue}" />
            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:if test="${gasco_infolink}">
                        <span class="la-popup-tooltip" data-position="right center" data-content="Diese URL aufrufen:  ${gasco_infolink}">
                            <a class="la-break-all" href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                        </span>
                    </g:if>
                    <g:else>
                        ${gasco_anzeigename ?: sub}
                    </g:else>

                    <g:each in="${sub.packages}" var="subPkg" status="j">
                        <div class="la-flexbox">
                            <i class="${Icon.PACKAGE} la-list-icon"></i>
                            <g:link controller="gasco" action="monitorDetails" id="${subPkg.id}">${subPkg.pkg}</g:link>
                        </div>
                    </g:each>
                </td>
                <td>
                    <g:each in="${ProviderRole.findAllBySubscription(sub)}" var="role">
                        ${role.provider.name}<br />
                    </g:each>
                </td>
                <td>
                    ${gasco_verhandlername ?: sub.getConsortium()?.name}
                    <br />
                    <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, sub.getConsortium())}" var="personRole">
                        <g:set var="person" value="${personRole.getPrs()}" />
                        <g:if test="${person.isPublic}">
                        <div class="ui list">
                            <div class="item">
                                <div class="content">
                                    <div class="header">
                                        ${person?.getFirst_name()} ${person?.getLast_name()}
                                    </div>
                                    <g:each in="${Contact.findAllByPrsAndContentType( person, RDStore.CCT_URL )}" var="prsContact">
                                        <div class="description">
                                            <i class="${Icon.SYM.URL} la-list-icon"></i>
                                            <span class="la-popup-tooltip" data-position="right center" data-content="Diese URL aufrufen:  ${prsContact?.content}">
                                                <a class="la-break-all" href="${prsContact?.content}" target="_blank">Webseite</a>
                                            </span>
                                        </div>
                                    </g:each>
                                    <g:each in="${Contact.findAllByPrsAndContentType( person, RDStore.CCT_EMAIL )}" var="prsContact">
                                        <div class="description js-copyTriggerParent">
                                            <i class="${Icon.SYM.EMAIL} la-list-icon js-copyTrigger la-js-copyTriggerIcon la-popup-tooltip" data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}"></i>
                                            <span class="la-popup-tooltip" data-position="right center" data-content="Mail senden an ${person?.getFirst_name()} ${person?.getLast_name()}">
                                                <a class="la-break-all js-copyTopic" href="mailto:${prsContact?.content}" >${prsContact?.content}</a>
                                            </span>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                        </g:if>
                    </g:each>
                </td>
                <td class="center aligned">
                    <g:if test="${flyoutCheckList.contains(sub.id)}">
                        <g:link class="${Btn.MODERN.SIMPLE} flyoutLink" controller="gasco" action="monitorData" data-key="${sub.id}">
                            <i class="${Icon.UI.INFO}"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <span data-position="top right" class="la-popup-tooltip" data-content="Leider stehen keine Informationen zur Verfügung. Bitte wenden Sie sich an die Konsortialstelle.">
                            <i class="icon grey minus circle"></i>
                        </span>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </tbody>
</table>

<div id="gascoFlyout" class="ui very wide flyout">
    <div class="ui header">
        <i class="${Icon.UI.INFO}"></i>
        <div class="content"></div>
    </div>
    <div class="content">
        <h3><g:message code="gasco.flyout.infoHeader"/></h3>
        <div class="info"></div>
        <div class="filter" style="margin:0 0 1em 0; text-align:right;">
            <div class="ui buttons mini">
                <span class="${Btn.SIMPLE_TOOLTIP}" data-content="Vergrößern" onclick="JSPC.app.gasco.ui.zoomIn()">+</span>
                <span class="${Btn.SIMPLE_TOOLTIP}" data-content="Verkleinern" onclick="JSPC.app.gasco.ui.zoomOut()">-</span>
                <span class="${Btn.SIMPLE_TOOLTIP}" data-content="Labels ein-/ausblenden" data-filter="label" onclick="JSPC.app.gasco.ui.toggleLabel()">Labels</span>
                <span class="${Btn.SIMPLE_TOOLTIP}" data-content="Legende ein-/ausblenden" data-filter="legend" onclick="JSPC.app.gasco.ui.toggleLegend()">Legende</span>
            </div>
        </div>
        <div class="charts"></div>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.gasco = {
        defaultConfig: {
            tooltip: {
                trigger: 'item'
            },
            legend: {
                orient: 'vertical', left: '20px', bottom: '30px', z: 1
            },
            series: [
                {
                    type: 'pie',
                    radius: [0, '75%'],
                    center: ['55%', '50%'],
                    minAngle: 1,
                    minShowLabelAngle: 1,
                    encode: {
                        id: 'id', itemName: 'name', value: 'value'
                    },
                    data: []
                }
            ]
        },
        current: {
            config: {
                showLabel: true,
                showLegend: true,
                radius: 75
            },
            charts: {}
        },
        ui: {
            $flyout: $('#gascoFlyout'),
            $title:  $('#gascoFlyout > .header > .content'),
            $info:   $('#gascoFlyout > .content > .info'),
            $infoHeader: $('#gascoFlyout > .content > h3'),
            $filter: $('#gascoFlyout > .content > .filter'),
            $charts: $('#gascoFlyout > .content > .charts'),

            toggleLabel: function() {
                JSPC.app.gasco.current.config.showLabel = !(JSPC.app.gasco.current.config.showLabel);
                JSPC.app.gasco.ui.commit();
            },
            toggleLegend: function() {
                JSPC.app.gasco.current.config.showLegend = !(JSPC.app.gasco.current.config.showLegend);
                JSPC.app.gasco.ui.commit();
            },
            zoomIn: function (){
                let r = JSPC.app.gasco.current.config.radius;
                if (r < 90) {
                    JSPC.app.gasco.current.config.radius = r + 5;
                    JSPC.app.gasco.ui.commit();
                }
            },
            zoomOut: function (){
                let r = JSPC.app.gasco.current.config.radius;
                if (r > 30) {
                    JSPC.app.gasco.current.config.radius = r - 5;
                    JSPC.app.gasco.ui.commit();
                }
            },

            commit: function() {
                for (const cc of Object.values(JSPC.app.gasco.current.charts)) {
                    cc.setOption({ legend: {show: JSPC.app.gasco.current.config.showLegend} })
                    cc.getModel().getSeries().forEach( function(s) {
                        s.option.label.show = JSPC.app.gasco.current.config.showLabel;
                        s.option.radius[1] = JSPC.app.gasco.current.config.radius + '%';
                    })
                    cc.resize();
                }

                let $label = JSPC.app.gasco.ui.$filter.find('*[data-filter=label]');
                JSPC.app.gasco.current.config.showLabel ? $label.addClass('active') : $label.removeClass('active');
                let $legend = JSPC.app.gasco.ui.$filter.find('*[data-filter=legend]');
                JSPC.app.gasco.current.config.showLegend ? $legend.addClass('active') : $legend.removeClass('active');
            }
        }
    };

    $('a.flyoutLink').on ('click', function(e) {
        e.preventDefault();
        $('html').css ('cursor', 'auto');
        $(this).removeClass ('blue');

        JSPC.app.gasco.ui.$flyout.flyout ({
            onHide: function (e) {
                $('a.flyoutLink').addClass ('blue');
            },
            onHidden: function (e) {
                JSPC.app.gasco.ui.$charts.empty(); %{-- after animation --}%
                JSPC.app.gasco.current.charts = {};
            }
        });

        $.ajax ({
            url: $(this).attr ('href'),
            dataType: 'json',
            data: {
                key: $(this).attr ('data-key')
            }
        }).done (function (data) {
            JSPC.app.gasco.ui.$title.html (data.title);
            JSPC.app.gasco.ui.$info.html (data.info);
            if(data.info !== null) {
                JSPC.app.gasco.ui.$infoHeader.show ();
            }
            else {
                JSPC.app.gasco.ui.$infoHeader.hide ();
            }

            data.data.forEach (function (dd) {
                JSPC.app.gasco.ui.$charts.append ('<p class="ui header chartHeader">' + dd.title + '</p>');
                JSPC.app.gasco.ui.$charts.append ('<div class="chartWrapper" id="chartWrapper-' + dd.key + '"></div>');

                let chartCfg = Object.assign ({}, JSPC.app.gasco.defaultConfig);
                chartCfg.series[0].data = dd.data;

                let echart = echarts.init ( $('#chartWrapper-' + dd.key)[0] );
                echart.setOption (chartCfg);
                JSPC.app.gasco.current.charts[dd.key] = echart;

                $(window).resize(function () {
                    JSPC.app.gasco.current.charts[dd.key].resize();
                });
            });
            JSPC.app.gasco.ui.commit();
            JSPC.app.gasco.ui.$flyout.flyout ('show');
        })
    });

%{--        tooltip.init('#gascoFlyout');--}%
</laser:script>

</g:if>%{-- {subscriptions} --}%

<style>
#gascoFlyout .filter .button {
    color: #54575b;
    background-color: #d3dae3;
}
#gascoFlyout .filter .button:hover {
    background-color: #c3cad3;
}
#gascoFlyout .filter .button.active {
    color: #ffffff;
    background-color: #004678;
}
#gascoFlyout .chartHeader {
    text-align: center;
}
#gascoFlyout .chartWrapper {
    width: 100%;
    height: 450px; /* todo */
    margin: 20px 0;
}
.ui.table thead tr:first-child>th {
    top: 48px!important;
}
<sec:ifAnyGranted roles="ROLE_USER">
    .ui.table thead tr:first-child>th {
        top: 90px!important;
    }
</sec:ifAnyGranted>
</style>

<laser:htmlEnd />