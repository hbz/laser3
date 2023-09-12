<%@ page import="de.laser.Subscription; de.laser.storage.PropertyStore; de.laser.Org; de.laser.PersonRole; de.laser.OrgRole; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Contact; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants;" %>

<laser:htmlStart message="gasco.title">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

    <ui:h1HeaderWithIcon text="${message(code: 'menu.public.gasco_monitor')}: ${message(code: 'gasco.licenceSearch')}" />

    <div class="ui grid">
        <div class="eleven wide column">
            <div class="ui la-search segment">
                <g:form action="gasco" controller="public" method="get" class="ui small form">

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
                                                    <g:if test="${params.list('subKinds').contains(subKind.id.toString())}"> checked="" </g:if>
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

                            <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                optionKey="${{ Org.class.name + ':' + it.id }}"
                                optionValue="${{ it.getName() }}"
                                name="consortia" noSelection="${['' : message(code:'default.select.choose.label')]}" value="${params.consortia}"/>
                        </fieldset>

                    </div>

                    <div class="field la-field-right-aligned">
                        <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                        <input type="submit" class="ui primary button" value="${message(code:'default.button.search.label')}">
                    </div>

                </g:form>
            </div>
        </div>
        <div class="five wide column">
            <img class="ui fluid image" alt="Logo GASCO" src="${resource(dir: 'images', file: 'gasco/logo-small.jpg')}"/>
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

    <table class="ui celled la-js-responsive-table la-table table">
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
                            <span class="la-popup-tooltip la-delay" data-position="right center" data-content="Diese URL aufrufen:  ${gasco_infolink}">
                                <a class="la-break-all" href="${gasco_infolink}" target="_blank">${gasco_anzeigename ?: sub}</a>
                            </span>
                        </g:if>
                        <g:else>
                            ${gasco_anzeigename ?: sub}
                        </g:else>

                        <g:each in="${sub.packages}" var="subPkg" status="j">
                            <div class="la-flexbox">
                                <i class="icon gift la-list-icon"></i>
                                <g:link controller="public" action="gascoDetailsIssueEntitlements" id="${subPkg.id}">${subPkg.pkg}</g:link>
                            </div>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${OrgRole.findAllBySubAndRoleType(sub, RDStore.OR_PROVIDER)}" var="role">
                            ${role.org?.name}<br />
                        </g:each>
                    </td>
                    <td>
                        ${gasco_verhandlername ?: sub.getConsortia()?.name}
                        <br />
                        <g:each in ="${PersonRole.findAllByFunctionTypeAndOrg(RDStore.PRS_FUNC_GASCO_CONTACT, sub.getConsortia())}" var="personRole">
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
                                                <i class="icon globe la-list-icon"></i>
                                                <span class="la-popup-tooltip la-delay " data-position="right center" data-content="Diese URL aufrufen:  ${prsContact?.content}">
                                                    <a class="la-break-all" href="${prsContact?.content}" target="_blank">Webseite</a>
                                                </span>
                                            </div>
                                        </g:each>
                                        <g:each in="${Contact.findAllByPrsAndContentType( person, RDStore.CCT_EMAIL )}" var="prsContact">
                                            <div class="description js-copyTriggerParent">
                                                <i class="ui icon envelope outline la-list-icon js-copyTrigger"></i>
                                                <span class="la-popup-tooltip la-delay" data-position="right center " data-content="Mail senden an ${person?.getFirst_name()} ${person?.getLast_name()}">
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
                            <g:link class="flyoutLink ui icon button blue la-modern-button" controller="public" action="gascoFlyout" data-key="${sub.id}">
                                <i class="icon info"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <span data-position="top right" class="la-popup-tooltip la-delay" data-content="Leider stehen keine Informationen zur Verfügung. Bitte wenden Sie sich an die Konsortialstelle.">
                                <i class="icon grey minus circle"></i>
                            </span>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <div id="gascoFlyout" class="ui eight wide flyout" style="padding:50px 0 10px 0;overflow:scroll">
        <div class="ui header">
            <i class="info icon"></i>
            <div class="content"></div>
        </div>
        <div class="content">
            <div class="filter" style="margin:0 0 1em 0; text-align:right;">
                <span class="ui button mini" onclick="JSPC.app.gasco.ui.zoomIn()">+</span>
                <span class="ui button mini" onclick="JSPC.app.gasco.ui.zoomOut()">-</span>
                <span class="ui button mini" onclick="JSPC.app.gasco.ui.toggleLabel()">#</span>
                <span class="ui button mini" onclick="JSPC.app.gasco.ui.toggleLegend()">#</span>
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
                $charts: $('#gascoFlyout > .content > .charts'),

                toggleLabel: function() {
                    JSPC.app.gasco.current.config.showLabel = !(JSPC.app.gasco.current.config.showLabel)
                    for (const cc of Object.values(JSPC.app.gasco.current.charts)) {
                        cc.getModel().getSeries().forEach( function(s) {
                            s.option.label.show = JSPC.app.gasco.current.config.showLabel
                        })
                        cc.resize()
                    }
                },
                toggleLegend: function() {
                    JSPC.app.gasco.current.config.showLegend = !(JSPC.app.gasco.current.config.showLegend)
                    for (const cc of Object.values(JSPC.app.gasco.current.charts)) {
                        cc.setOption({ legend: {show: JSPC.app.gasco.current.config.showLegend} })
                        cc.resize()
                    }
                },
                zoomIn: function (){
                    let r = JSPC.app.gasco.current.config.radius
                    if (r < 90) {
                        for (const cc of Object.values(JSPC.app.gasco.current.charts)) {
                            cc.getModel().getSeries().forEach( function(s) {
                                s.option.radius[1] = (r + 5) + '%'
                                cc.resize()
                            })
                        }
                        JSPC.app.gasco.current.config.radius = r + 5
                    }
                },
                zoomOut: function (){
                    let r = JSPC.app.gasco.current.config.radius
                    if (r > 30) {
                        for (const cc of Object.values(JSPC.app.gasco.current.charts)) {
                            cc.getModel().getSeries().forEach( function(s) {
                                s.option.radius[1] = (r - 5) + '%'
                                cc.resize()
                            })
                        }
                        JSPC.app.gasco.current.config.radius = r - 5
                    }
                },
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

                data.data.forEach (function (dd) {
                    JSPC.app.gasco.ui.$charts.append ('<p class="ui header chartHeader">' + dd.title + '</p>');
                    JSPC.app.gasco.ui.$charts.append ('<div class="chartWrapper" id="chartWrapper-' + dd.key + '"></div>');

                    let chartCfg = Object.assign ({}, JSPC.app.gasco.defaultConfig);
                    chartCfg.series[0].data = dd.data;

                    let echart = echarts.init ( $('#chartWrapper-' + dd.key)[0] );
                    echart.setOption (chartCfg);
                    JSPC.app.gasco.current.charts[dd.key] = echart;
                });

                JSPC.app.gasco.ui.$flyout.flyout ('show');
                console.log(JSPC.app.gasco.current.charts);
            })
        });
    </laser:script>

    </g:if>%{-- {subscriptions} --}%

<style>
.chartHeader {
    text-align: center;
}
.chartWrapper {
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