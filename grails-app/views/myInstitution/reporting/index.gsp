<%@page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.config.ConfigMapper; de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.GenericHelper; de.laser.ReportingFilter; de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription;de.laser.reporting.report.ReportingCache;de.laser.properties.PropertyDefinition" %>
<laser:htmlStart message="myinst.reporting">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

        <ui:breadcrumbs>
            <ui:crumb controller="org" action="show" id="${contextService.getOrg().id}" text="${contextService.getOrg().getDesignation()}"/>
            <ui:crumb text="${message(code:'myinst.reporting')}" class="active" />
        </ui:breadcrumbs>

        <ui:h1HeaderWithIcon message="myinst.reporting" type="reporting" />

        <laser:render template="/templates/reporting/helper" />%{--js--}%

        <div style="margin-right:0.5em">
            <div id="bookmark-toggle" class="${Btn.ICON.SIMPLE_TOOLTIP} right floated disabled la-long-tooltip"
                    data-content="${message(code:'reporting.ui.global.bookmarks')}" data-position="top right">
                    <i class="icon bookmark"></i>
            </div>
            <div id="history-toggle" class="${Btn.ICON.SIMPLE_TOOLTIP} right floated disabled la-long-tooltip"
                    data-content="${message(code:'reporting.ui.global.history')}" data-position="top right">
                    <i class="icon history"></i>
            </div>
            <div id="info-toggle" class="${Btn.ICON.SIMPLE} right floated">
                <i class="${Icon.UI.HELP}"></i>
            </div>
        </div>

        <div id="hab-wrapper"></div>

        <div id="info-content" class="hidden">

            <div class="ui segment">
                <span class="ui top attached label" style="text-align:center;">
                    <i class="${Icon.UI.HELP} large"></i>${message(code:'reporting.ui.global.help')}
                </span>
                <div style="margin: 3.5em 2em 0.5em !important">
                    <p>
                        <strong>${message(code:'reporting.ui.global.step1')}</strong> <br />
                        ${message(code:'reporting.ui.global.info1')}
                    </p>
                    <p>
                        <strong>${message(code:'reporting.ui.global.step2')}</strong> <br />
                        ${message(code:'reporting.ui.global.info2')}
                    </p>
                    <p>
                        <strong>${message(code:'reporting.ui.global.step3')}</strong> <br />
                        ${message(code:'reporting.ui.global.info3')}
                    </p>
                    <p>
                        <i class="icon history blue"></i><strong>${message(code:'reporting.ui.global.history')}</strong> <br />
                        ${message(code:'reporting.ui.global.infoHistory')}
                    </p>
                    <p>
                        <i class="icon bookmark teal"></i><strong>${message(code:'reporting.ui.global.bookmarks')}</strong> <br />
                        ${message(code:'reporting.ui.global.infoBookmarks')}
                    </p>
                    <p>
                        <i class="${Icon.UI.HELP} blue"></i><strong>${message(code:'reporting.ui.global.help')}</strong> <br />
                        ${message(code:'reporting.ui.global.infoHelp')}
                    </p>
                    <p>
                        <strong>we:kb</strong> <br />
                        ${message(code:'reporting.ui.global.infoWekb')}
                    </p>
                    <p>
                        <strong>${GenericHelper.flagUnmatched('text')}</strong> <br />
                        ${message(code:'reporting.ui.global.infoUnmatched')}
                    </p>
                    <sec:ifAnyGranted roles="ROLE_YODA">
                        <br />
                        <div class="ui divider"></div>

                        <div class="ui relaxed horizontal list" style="text-align:center; width:100%;">
                            <div class="item">
                            <div class="content middle aligned">
                                <div class="header">We:kb</div>
                                <g:set var="wekbSource" value="${ElasticSearchHelper.getWekbSource()}" />
                                <g:if test="${wekbSource}">
                                    <a href="${wekbSource.baseUrl}" target="_blank">${wekbSource.baseUrl}</a>
                                </g:if>
                                <g:else>--</g:else>
                            </div>
                        </div>
                            <div class="item">
                                <div class="content middle aligned">
                                    <div class="header">Elasticsearch Index</div>
                                    <g:if test="${ConfigMapper.getConfig('reporting.elasticSearch', Map)}">
                                        <a href="${ConfigMapper.getConfig('reporting.elasticSearch.url', String) + '/_cat/indices?v'}" target="_blank">${ConfigMapper.getConfig('reporting.elasticSearch.url', String)}</a>
                                    </g:if>
                                    <g:else>--</g:else>
                                </div>
                            </div>
                            <div class="item">
                                <div class="content middle aligned">
                                    <div class="header">Cache</div>
                                    <g:if test="${token}">
                                        <g:link controller="yoda" action="systemCache" params="${[cmd:'get', type:'reporting', token:token]}" target="_blank">${token}</g:link>
                                    </g:if>
                                    <g:else>--</g:else>
                                </div>
                            </div>
                        </div>
                    </sec:ifAnyGranted>
                </div>
            </div>
        </div>

        <h3 class="ui header">${message(code:'reporting.ui.global.step1')}</h3>

        <g:if test="${! filter}">
            <div class="ui segment form">
                <div class="fields two">
                    <div class="field">
                        <label for="filter-chooser">${message(code:'reporting.ui.global.filter.base')}</label>
%{--                        <g:select name="filter-chooser"--}%
%{--                                  from="${cfgFilterList}"--}%
%{--                                  optionKey="${{it}}"--}%
%{--                                  optionValue="${{BaseConfig.getFilterLabel(it)}}"--}%
%{--                                  class="ui selection dropdown la-not-clearable"--}%
%{--                                  noSelection="${['': message(code: 'default.select.choose.label')]}" />--}%

%{--                        <select id="filter-chooser" name="filter-chooser" class="ui selection dropdown la-not-clearable">--}%
%{--                            <option value="">${message(code: 'default.select.choose.label')}</option>--}%
%{--                            <g:each in="${cfgFilterList}" var="cfg">--}%
%{--                                <option class="item" value="${cfg}">${BaseConfig.getFilterLabel(cfg)}</option>--}%
%{--                            </g:each>--}%
%{--                        </select>--}%

                        <div id="filter-chooser" class="ui selection dropdown la-not-clearable">
                            <input type="hidden" name="filter-chooser">
                            <i class="dropdown icon"></i>
                            <div class="default text">${message(code: 'default.select.choose.label')}</div>
                            <div class="menu">
                                <g:each in="${cfgFilterList}" var="cfg">
                                    <div class="item" data-value="${cfg}">
                                        <i class="${BaseConfig.getIcon(cfg)} grey"></i> ${BaseConfig.getFilterLabel(cfg)}
                                    </div>
                                </g:each>
                            </div>
                        </div>
                    </div>
                    <div class="field">
                        <label></label>
                        <div>${message(code:'reporting.cfg.filter.asterisk')}</div>
                    </div>
                </div>
            </div>
        </g:if>

        <div id="filter-wrapper">
            <g:if test="${filter}">
                <laser:render template="/myInstitution/reporting/filter/filter_form" />
            </g:if>
        </div>

        <g:if test="${filterResult}">
            %{-- <sec:ifAnyGranted roles="ROLE_YODA">
                <g:link controller="yoda" action="systemCache" params="${[key: ReportingCache.CTX_GLOBAL + token]}" target="_blank" class="${Btn.SIMPLE} small right floated"><icon:bug /> YODA only CACHE</g:link>
            </sec:ifAnyGranted> --}%

            <h3 class="ui header">${message(code:'reporting.ui.global.step2')}</h3>

            <laser:render template="/myInstitution/reporting/query/${filter}" />

            <div id="reporting-chart-nodata" class="ui error message">${message(code:'reporting.modal.nodata')}</div>

            <div id="chart-wrapper"></div>
            <div id="chart-details"></div>
        </g:if>

        <style>
            #history-content, #bookmark-content, #info-content { margin-top: 4em; }
            #history-content table .description ,
            #bookmark-content table .description { margin: 0.3em 0; }
            #last-added-bookmark { margin-left: 1em; }
            #reporting-chart-nodata { display: none; }
            #chart-wrapper { height: 400px; width: 98%; margin: 3em auto 2em; }
            h3.ui.header { margin-top: 3em !important; }
            .ui.form .fields .field { margin-bottom: 0 !important; }
        </style>

        <laser:script file="${this.getGroovyPageFileName()}">
            /*-- hab --*/

            JSPC.app.reporting.updateHabMenu = function (current) {
                var base = ['info', 'bookmark', 'history']
                var negative = base.filter( function (c) { return c.indexOf( current ) < 0; } )

                $( negative.map(function (e) { return '#' + e + '-content' }).join(',') ).addClass('hidden');
                $( negative.map(function (e) { return '#' + e + '-toggle' }).join(',') ).removeClass('blue').removeClass('teal');
                $( '#' + current + '-content').toggleClass('hidden');
                if (current == 'bookmark') {
                    $( '#' + current + '-toggle').toggleClass('teal');
                } else {
                    $( '#' + current + '-toggle').toggleClass('blue');
                }
            }
            $('#info-toggle').on ('click', function () {
                JSPC.app.reporting.updateHabMenu('info');
            })
            $('#bookmark-toggle').on ('click', function () {
                JSPC.app.reporting.updateHabMenu('bookmark');
                $('#bookmark-content #last-added-bookmark').fadeOut(250).fadeIn(250).delay(50).fadeOut(250).fadeIn(250).delay(50).fadeOut(250).fadeIn(250).delay(50).fadeOut(250);
            })
            $('#history-toggle').on ('click', function () {
                JSPC.app.reporting.updateHabMenu('history');
            })
            $('#hab-wrapper').load( '<g:createLink controller="ajaxHtml" action="reporting" />', function () {});

            /*-- filter --*/

            $('#filter-chooser').on ('change', function (e) {
                $.ajax({
                    url: '<g:createLink controller="myInstitution" action="reporting" />',
                    data: {
                        init: true,
                        filter: $('input[name=filter-chooser]').val()   /* filter: $(this).val() */
                    },
                    dataType: 'html',
                    beforeSend: function (xhr) { $('#globalLoadingIndicator').show(); }
                })
                .done (function (data) {
                    $('#filter-wrapper').html(data);
                    r2d2.initDynamicUiStuff('#filter-wrapper');
                    r2d2.initDynamicXEditableStuff('#filter-wrapper');

                    JSPC.app.reporting.initFilterEvents();
                    $('#filter-wrapper > div').removeClass('hidden');
                })
                .fail (function () { $("#reporting-modal-error").modal('show'); })
                .always (function () { $('#globalLoadingIndicator').hide(); });
            })

            /*-- charts --*/

            $('*[id^=query-chooser]').on ('change', function (e) {
                var value = $(e.target).val();
                if (value) {
                    $('*[id^=query-chooser]').not( $('#' + this.id)).dropdown('clear');
                    JSPC.app.reporting.current.request.query = value;
                    JSPC.app.reporting.requestChartJsonData();
                }
            })
            $('#chart-chooser').on ('change', function (e) {
                if (JSPC.app.reporting.current.request) {
                    JSPC.app.reporting.current.request.chart = $(e.target).dropdown('get value');
                    JSPC.app.reporting.requestChartJsonData();
                }
            })

            JSPC.app.reporting.requestChartJsonData = function () {
                if ( JSPC.app.reporting.current.request.query && JSPC.app.reporting.current.request.chart ) {
                    JSPC.app.reporting.current.chart = {};

                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="chart" />",
                        dataType: 'script',
                        method: 'post',
                        data: JSPC.app.reporting.current.request,
                        beforeSend: function (xhr) {
                            $('#globalLoadingIndicator').show();
                            $('#query-export-button, #query-help-button').attr('disabled', 'disabled');
                        }
                    })
                    .done (function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );
                        $('#reporting-chart-nodata').hide();

                        if (! JSPC.app.reporting.current.chart.option && ! JSPC.app.reporting.current.chart.statusCode) {
                            $("#reporting-modal-error").modal('show');
                        }
                        else if (JSPC.app.reporting.current.chart.statusCode == 500) {
                            $("#reporting-modal-error").modal('show');
                        }
                        else if (JSPC.app.reporting.current.chart.statusCode == 204) {
                            $('#reporting-chart-nodata').show();
                        }
                        else {
                            var chartHeight = 400;
                            if (JSPC.app.reporting.current.request.chart == 'bar') {
                                chartHeight = 150 + (19 * JSPC.app.reporting.current.chart.option.dataset.source.length);
                            }
                            else if (JSPC.app.reporting.current.request.chart == 'pie') {
                                chartHeight = Math.min(1800, 380 + (12 * JSPC.app.reporting.current.chart.option.dataset.source.length));
                                JSPC.app.reporting.current.myCountsToggle = false;
                            }
                            else if (JSPC.app.reporting.current.request.chart == 'radar') {
                                chartHeight = 400 + (8 * JSPC.app.reporting.current.chart.option.dataset.source.length);
                            }
                            $('#chart-wrapper').css('height', chartHeight + 'px');

                            var echart = echarts.init($('#chart-wrapper')[0]);
                            echart.setOption( JSPC.app.reporting.current.chart.option );
                            echart.on ('click', function (params) {
                                var clone = {}
                                Object.assign(clone, JSPC.app.reporting.current.request);
                                clone.id = params.data[0];
                                clone.label = params.data[1];
                                clone.context = '${BaseConfig.KEY_MYINST}';
                                JSPC.app.reporting.requestChartHtmlDetails(clone);
                            });
                            echart.on ('legendselectchanged', function (params) { /* console.log(params); */ });

                            JSPC.app.reporting.current.chart.echart = echart;

                            let escQuery = JSPC.app.reporting.current.request.query.replaceAll('*', '\\*')
                            let $dhs = $('#queryHelpModal .help-section[data-help-section=' + escQuery + ']');
                            if ($dhs.length) {
                                $('#query-help-button').removeAttr('disabled');
                            }
                            $('#query-export-button').removeAttr('disabled');
                        }
                    })
                    .fail (function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );
                        $('#reporting-chart-nodata').hide();
                        $("#reporting-modal-error").modal('show');
                    })
                    .always (function () { $('#globalLoadingIndicator').hide(); });
                }
            }

            $('#chart-chooser').dropdown('set selected', 'bar');

            /* -- helper -- */

            JSPC.app.reporting.initFilterEvents = function () {
                $("select[name=filter\\:member_country]").on ('change', function () {
                    JSPC.app.reporting.countryRegionUpdate( 'filter\\:member' );
                }).trigger( 'change' );

                $("select[name=filter\\:org_country]").on ('change', function () {
                    JSPC.app.reporting.countryRegionUpdate( 'filter\\:org' );
                }).trigger( 'change' );

                $("select[name=filter\\:member_region_virtualFF]").dropdown(
                    'setting', 'onChange', function (value, text, $choice) {
                        $("input[name=filter\\:member_region]").attr('value', value);
                });

                $("select[name=filter\\:org_region_virtualFF]").dropdown(
                    'setting', 'onChange', function (value, text, $choice) {
                        $("input[name=filter\\:org_region]").attr('value', value);
                });

                $("*[name$='_propertyKey']").on ('change', function () {
                    JSPC.app.reporting.propertyUpdate( this );
                }).trigger( 'change' );
            }

            JSPC.app.reporting.countryRegionUpdate = function (selectorPart) {
                var $country     = $('select[name=' + selectorPart + '_country]');
                var $region      = $('select[name=' + selectorPart + '_region_virtualFF]');
                var $regionValue = $('input[name=' + selectorPart + '_region]');

                var url = '<g:createLink controller="ajaxJson" action="getRegions"/>?simple=true';
                if ($country.val()) {
                    url = url + '&country=' + $country.val()
                }
                $region.dropdown('change values', []);

                $.ajax({
                    url: url,
                    success: function (data) {
                        $region.dropdown('change values', data.map( function (e) {
                            return { value: e.id, name: e.value_de, text: e.value_de }
                        }))
                        $region.dropdown('set selected', $regionValue.attr('value'));
                    }
                });
            }

            JSPC.app.reporting.propertyUpdate = function (elem) {
                var defaults = {}
                <%
                    params.findAll{ it.key.startsWith('filter:') && (it.key.endsWith('_propertyKey') || it.key.endsWith('_propertyValue')) }.each{ it ->
                        println "defaults['${it.key}'] = '${it.value}';"
                    }
                %>
                var $key = $(elem);
                var $value = $("*[name='" + $key.attr('name').replace('_propertyKey', '_propertyValue') + "']");
                $value.empty().attr('disabled', 'disabled').parent().addClass('disabled');

                var kValue  = $key.dropdown('get value');
                if (kValue) {
                    $.ajax({
                        url: '<g:createLink controller="ajaxJson" action="getPropRdValues"/>?oid=${PropertyDefinition.class.name}:' + kValue,
                        success: function (data) {
                            var pdv;
                            if (data.length > 0) {
                                $value.removeAttr('disabled').parent().removeClass('disabled');

                                if (defaults[$key.attr('name')] == kValue && defaults[$value.attr('name')]) {
                                    pdv = defaults[$value.attr('name')];
                                }
                                for (var i=0; i < data.length; i++) {
                                    if (data[i].value == pdv) {
                                        $value.append('<option selected="selected" value="' + data[i].value + '">' + data[i].name + '</option>');
                                    } else {
                                        $value.append('<option value="' + data[i].value + '">' + data[i].name + '</option>');
                                    }
                                }
                            }
                            $value.dropdown().dropdown({ clearable: true, values: data });
                            if (pdv) {
                                $value.dropdown('set selected', defaults[$value.attr('name')]);
                            }
                        }
                    });
                } else {
                    $value.dropdown('restore defaults');
                }
            }

            JSPC.app.reporting.initFilterEvents();
        </laser:script>

        <ui:modal id="reporting-modal-error" text="REPORTING" hideSubmitButton="true">
            <p><i class="${Icon.UI.ERROR} large orange"></i> ${message(code:'reporting.modal.error')}</p>
        </ui:modal>
<laser:htmlEnd />
