<%@page import="de.laser.reporting.export.myInstitution.GlobalExportHelper; de.laser.helper.DateUtils; de.laser.reporting.report.myInstitution.base.BaseConfig;de.laser.ReportingGlobalService;de.laser.Org;de.laser.Subscription;de.laser.reporting.report.ReportingCache;de.laser.properties.PropertyDefinition" %>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title><g:message code="laser"/> : <g:message code="myinst.reporting"/></title>
        <laser:javascript src="echarts.js"/>%{-- dont move --}%
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}"/>
            <semui:crumb text="${message(code:'myinst.reporting')}" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            <g:message code="myinst.reporting"/>
        </h1>

        <div class="ui message info">
            <p>
                <strong>${message(code:'reporting.macro.step1')}</strong> <br />
                ${message(code:'reporting.macro.info1')}
            </p>
            <p>
                <strong>${message(code:'reporting.macro.step2')}</strong> <br />
                ${message(code:'reporting.macro.info2')}
            </p>
            <p>
                <strong>${message(code:'reporting.macro.step3')}</strong> <br />
                ${message(code:'reporting.macro.info3')}
            </p>
        </div>

        <g:if test="${filterHistory}">
            <div id="history-chooser" class="ui icon button right floated">
                <i class="icon history"></i>
            </div>
            <div id="history-chooser-content" class="ui segment hidden">
                <div class="ui form">
                    <div class="field">
                        <div class="ui relaxed divided list">
                            <g:each in="${filterHistory}" var="fh">
                                <g:set var="fhRCache" value="${new ReportingCache(ReportingCache.CTX_GLOBAL, fh.split('/').last() as String)}" />
                                <g:set var="meta" value="${fhRCache.readMeta()}" />
                                <g:set var="filterCache" value="${fhRCache.readFilterCache()}" />
                                <div class="item">
                                    <div class="image middle aligned"><i class="icon grey history large"></i></div>
                                    <div class="content" style="line-height: 1.5em;">
                                        <div class="header">
                                            <g:link controller="myInstitution" action="reporting" params="${[filter: meta.filter /*, token: fhRCache.token*/ ] + filterCache.map}">
                                                ${DateUtils.getSDF_OnlyTime().format(meta.timestamp)} - ${BaseConfig.getMessage('base.filter.' + meta.filter)}
                                            </g:link>
                                        </div>
                                        <div class="description">
                                            <g:render template="/myInstitution/reporting/query/generic_filterLabels" model="${[filterLabels: GlobalExportHelper.getCachedFilterLabels(fhRCache.token), simple: true]}" />

                                            <%= filterCache.result %>
                                        </div>
                                    </div>
                                </div>
                            </g:each>
                        </div>
                    </div>
                    <div class="field">
                        <g:link action="reporting" class="ui button" params="${[cmd: 'deleteHistory']}">${message(code:'reporting.filter.history.delete')}</g:link>
                    </div>
                </div>
            </div>
        </g:if>

        <h3 class="ui header">${message(code:'reporting.macro.step1')}</h3>

        <g:if test="${!filter}">
            <div class="ui segment form">
                <div class="fields two">
                    <div class="field">
                        <label for="filter-chooser">${message(code:'reporting.filter.base')}</label>
                        <g:select name="filter-chooser"
                                  from="${cfgFilterList}"
                                  optionKey="${{it}}"
                                  optionValue="${{BaseConfig.getMessage('base.filter.' + it)}}"
                                  class="ui selection dropdown la-not-clearable"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}" />
                    </div>
                </div>
            </div>
        </g:if>

        <g:each in="${BaseConfig.FILTER}" var="filterItem">

            <g:if test="${!filter || filter == filterItem}">
                <div id="filter-${filterItem}" class="filter-form-wrapper ${filter ? '' : 'hidden'}">
                    <g:form action="reporting" method="POST" class="ui form">
                        <g:render template="/myInstitution/reporting/filter/${filterItem}" />

                        <div class="field">
                            %{-- <g:link action="reporting" class="ui button">${message(code:'reporting.filter.save')}</g:link> --}%
                            <g:link action="reporting" class="ui button primary">${message(code:'default.button.reset.label')}</g:link>
                            <input type="submit" class="ui button secondary" value="${message(code:'default.button.search.label')}" />
                            <input type="hidden" name="filter" value="${filterItem}" />
                            <input type="hidden" name="token" value="${token}" />
                        </div>
                    </g:form>
                </div>
            </g:if>
        </g:each>

        <g:render template="/templates/reporting/helper" />

        <g:if test="${filterResult}">

            %{-- <sec:ifAnyGranted roles="ROLE_YODA">
                <g:link controller="yoda" action="cacheInfo" params="${[key: ReportingCache.CTX_GLOBAL + token]}" target="_blank" class="ui button small"><i class="icon bug"></i> YODA only CACHE</g:link>
            </sec:ifAnyGranted> --}%

            <h3 class="ui header">${message(code:'reporting.macro.step2')}</h3>

            <g:render template="/myInstitution/reporting/query/${filter}" />

            <div id="chart-wrapper"></div>
            <div id="chart-details"></div>
        </g:if>

        <style>
            #history-chooser-content { margin-top: 4.5em; }
            #chart-wrapper { height: 400px; width: 98%; margin: 2em auto 1em; }
            h3.ui.header { margin-top: 3em !important; }
            .ui.form .fields .field { margin-bottom: 0 !important; }
        </style>

        <laser:script file="${this.getGroovyPageFileName()}">

            $('#history-chooser').on( 'click', function() {
                $('#history-chooser-content').toggleClass('hidden');
            })

            $('#filter-chooser').on( 'change', function(e) {
                $('.filter-form-wrapper').addClass('hidden')
                $('#filter-' + $(e.target).dropdown('get value')).removeClass('hidden');
            })

            $('*[id^=query-chooser').on( 'change', function(e) {
                var value = $(e.target).dropdown('get value');
                if (value) {
                    $('*[id^=query-chooser').not($('#' + e.target.id)).dropdown('clear');
                    JSPC.app.reporting.current.request.query = value;
                    JSPC.app.reporting.requestChartJsonData();
                }
            })

            $('#chart-chooser').on( 'change', function(e) {
                JSPC.app.reporting.current.request.chart = $(e.target).dropdown('get value');
                JSPC.app.reporting.requestChartJsonData();
            })

            JSPC.app.reporting.requestChartJsonData = function() {
                if ( JSPC.app.reporting.current.request.query && JSPC.app.reporting.current.request.chart ) {
                    JSPC.app.reporting.current.chart = {};

                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="chart" />",
                        dataType: 'script',
                        method: 'post',
                        data: JSPC.app.reporting.current.request,
                        beforeSend: function (xhr) {
                            $('#loadingIndicator').show();
                            $('#query-export-button, #query-help-button').attr('disabled', 'disabled');
                        }
                    })
                    .done( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );

                        if (! JSPC.app.reporting.current.chart.option) {
                            $("#reporting-modal-nodata").modal('show');
                        }
                        else {
                            if (JSPC.app.reporting.current.request.chart == 'bar') {
                                $('#chart-wrapper').css('height', 150 + (19 * JSPC.app.reporting.current.chart.option.dataset.source.length) + 'px');
                            }
                            else if (JSPC.app.reporting.current.request.chart == 'pie') {
                                $('#chart-wrapper').css('height', 350 + (12 * JSPC.app.reporting.current.chart.option.dataset.source.length) + 'px');
                            }
                            else if (JSPC.app.reporting.current.request.chart == 'radar') {
                                $('#chart-wrapper').css('height', 400 + (8 * JSPC.app.reporting.current.chart.option.dataset.source.length) + 'px');
                            }

                            var echart = echarts.init($('#chart-wrapper')[0]);
                            echart.setOption( JSPC.app.reporting.current.chart.option );
                            echart.on( 'click', function (params) {
                                var clone = {}
                                Object.assign(clone, JSPC.app.reporting.current.request);
                                clone.id = params.data[0];
                                clone.label = params.data[1];
                                clone.context = '${BaseConfig.KEY_MYINST}';
                                JSPC.app.reporting.requestChartHtmlDetails(clone);
                            });
                            echart.on( 'legendselectchanged', function (params) { /* console.log(params); */ });

                            JSPC.app.reporting.current.chart.echart = echart;

                            $('#query-export-button').removeAttr('disabled');
                            if (JSPC.app.reporting.current.request.query.indexOf('-x-') >=0) {
                                $('#query-help-button').removeAttr('disabled');
                            }
                        }
                    })
                    .fail( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );
                    })
                    .always(function() {
                        $('#loadingIndicator').hide();
                    });
                }
            }

            $('#chart-chooser').dropdown('set selected', 'bar');

            JSPC.app.reporting.countryRegionUpdate = function( selectorPart ) {
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
                        $region.dropdown('change values', data.map( function(e){
                            return { value: e.id, name: e.value_de, text: e.value_de }
                        }))
                        $region.dropdown('set selected', $regionValue.attr('value'));
                    }
                });
            }

            $("select[name=filter\\:member_country]").on( 'change', function() {
                JSPC.app.reporting.countryRegionUpdate( 'filter\\:member' );
            }).trigger( 'change' );

            $("select[name=filter\\:org_country]").on( 'change', function() {
                JSPC.app.reporting.countryRegionUpdate( 'filter\\:org' );
            }).trigger( 'change' );

            $("select[name=filter\\:member_region_virtualFF]").dropdown(
                'setting', 'onChange', function(value, text, $choice) {
                    $("input[name=filter\\:member_region]").attr('value', value);
            });

            $("select[name=filter\\:org_region_virtualFF]").dropdown(
                'setting', 'onChange', function(value, text, $choice) {
                    $("input[name=filter\\:org_region]").attr('value', value);
            });

            $("*[name$='_propertyKey']").on('change', function(){
                var defaults = {}
                <%
                    params.findAll{ it.key.startsWith('filter:') && (it.key.endsWith('_propertyKey') || it.key.endsWith('_propertyValue')) }.each{ it ->
                        println "defaults['${it.key}'] = '${it.value}';"
                    }
                %>
                var $key = $(this);
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
                        },
                        async: false
                    });
                } else {
                    $value.dropdown('restore defaults');
                }
            });
            $("*[name$='_propertyKey']").trigger('change');
        </laser:script>

        <semui:modal id="reporting-modal-error" text="REPORTING" hideSubmitButton="true">
            <p>${message(code:'reporting.modal.error')}</p>
        </semui:modal>
        <semui:modal id="reporting-modal-nodata" text="REPORTING" hideSubmitButton="true">
            <p>${message(code:'reporting.modal.nodata')}</p>
        </semui:modal>

    </body>
</html>
