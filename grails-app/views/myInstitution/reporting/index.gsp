<%@page import="de.laser.ReportingService;de.laser.Org;de.laser.Subscription" %>
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
            <g:message code="myinst.reporting"/> <span class="ui label red">DEMO</span>
        </h1>

        <div class="ui message info">
            <p>
                <strong>1. Suchanfrage</strong>
                <br />
                Erzeugen Sie eine Ergebnismenge anhand kombinierbarer Filter.
                Verschiedene Einstiegspunkte ermöglichen eine individuelle Zusammenstellung Ihrer Daten.
            </p>
            <p>
                <strong>2. Ergebnis</strong>
                <br />
                Die in Schritt 1 erzeugte Ergebnismenge kann durch vordefinierte Abfragen
                untersucht und visualisiert werden.
            </p>
            <p>
                <strong>3. Details</strong>
                <br />
                Die in Schritt 2 erzeugte Visualisierung ist interaktiv und bietet Zugriff auf weitere Informationen.
            </p>
        </div>
        <h2 class="ui header">1. Suchanfrage</h2>

        <g:set var="hidden" value="hidden" />

        <g:if test="${!filter}">
            <div class="ui segment form">
                <div class="field">
                    <label for="filter-chooser">Einstiegspunkt</label>
                    <g:select name="filter-chooser"
                              from="${cfgFilterList}"
                              optionKey="key"
                              optionValue="value"
                              class="ui selection dropdown la-not-clearable"
                              noSelection="${['': message(code: 'default.select.choose.label')]}" />
                </div>
            </div>
        </g:if>
        <g:else>
            <g:set var="hidden" value="" />
        </g:else>

        <g:if test="${!filter || filter == 'subscription'}">
            <div id="filter-subscription" class="filter-form-wrapper ${hidden}">
               <g:render template="/myInstitution/reporting/filter/subscription" />
            </div>
        </g:if>

        <g:if test="${!filter || filter == 'organisation'}">
            <div id="filter-organisation" class="filter-form-wrapper ${hidden}">
                <g:render template="/myInstitution/reporting/filter/organisation" />
            </div>
        </g:if>

        <g:if test="${result}">
            <h2 class="ui header">2. Ergebnis</h2>

            <g:if test="${filter == 'subscription'}">
                <g:render template="/myInstitution/reporting/query/subscription" />
            </g:if>

            <g:if test="${filter == 'organisation'}">
                <g:render template="/myInstitution/reporting/query/organisation" />
            </g:if>

            <div id="chart-wrapper"></div>

            <div id="chart-details"></div>

            <style>
            #chart-wrapper {
                height: 450px;
                width: 98%;
                margin: 1em auto;
            }
            </style>

        </g:if>


        <laser:script file="${this.getGroovyPageFileName()}">
            if (! JSPC.app.reporting) { JSPC.app.reporting = {}; }
            if (! JSPC.app.reporting.current) { JSPC.app.reporting.current = {}; }
            if (! JSPC.app.reporting.current.chart) { JSPC.app.reporting.current.chart = {}; }

            $('#filter-chooser').on( 'change', function(e) {
                $('.filter-form-wrapper').addClass('hidden')
                $('#filter-' + $(e.target).dropdown('get value')).removeClass('hidden');
            })

            $('*[id^=query-chooser').on( 'change', function(e) {
                var value = $(e.target).dropdown('get value');
                if (value) {
                    $('*[id^=query-chooser').not($('#' + e.target.id)).dropdown('clear');
                    JSPC.app.reporting.current.request.query = value;
                    JSPC.app.reporting.requestChart();
                }
            })

            $('#chart-chooser').on( 'change', function(e) {
                JSPC.app.reporting.current.request.chart = $(e.target).dropdown('get value');
                JSPC.app.reporting.requestChart();
            })

            $('#chart-export').on( 'click', function(e) {
                if ( JSPC.app.reporting.current.request.query ) {
                    JSPC.app.reporting.requestExport();
                }
            })

            JSPC.app.reporting.requestExport = function() {
                console.log(JSPC.app.reporting.current.request);
                alert('[c1] - Noch nicht implementiert');
            }

            JSPC.app.reporting.requestChart = function() {
                if ( JSPC.app.reporting.current.request.query && JSPC.app.reporting.current.request.chart ) {
                    JSPC.app.reporting.current.chart = {};

                    $.ajax({
                        url: "<g:createLink controller="ajaxJson" action="chart" />",
                        dataType: 'script',
                        method: 'post',
                        data: JSPC.app.reporting.current.request
                    })
                    .done( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                        $('#chart-details').replaceWith( '<div id="chart-details"></div>' );

                        var chart = echarts.init($('#chart-wrapper')[0]);
                        chart.setOption( JSPC.app.reporting.current.chart.option );
                        chart.on( 'click', function (params) {
                            var valid = false;

                            if (JSPC.app.reporting.current.request.chart == 'pie') {
                                $.each( JSPC.app.reporting.current.chart.details, function(i, v) {
                                    if (params.data.id == v.id) {
                                        valid = true;
                                        JSPC.app.reporting.requestChartDetails(JSPC.app.reporting.current.request, v);
                                    }
                                })
                            }
                            else if (JSPC.app.reporting.current.request.chart == 'bar') {
                                $.each( JSPC.app.reporting.current.chart.details, function(i, v) {
                                    if (params.data[0] == v.id) {
                                        valid = true;
                                        JSPC.app.reporting.requestChartDetails(JSPC.app.reporting.current.request, v);
                                    }
                                })
                            }
                            if (! valid) {
                                alert('[c2] - Keine Details verfügbar');
                            }
                        });
                    })
                    .fail( function (data) {
                        $('#chart-wrapper').replaceWith( '<div id="chart-wrapper"></div>' );
                    })
                }
            }

            JSPC.app.reporting.requestChartDetails = function(request, data) {
                $.ajax({
                    url: "<g:createLink controller="ajaxHtml" action="chartDetails" />",
                    method: 'post',
                    data: data
                })
                .done( function (data) {
                     $('#chart-details').empty();
                     $('#chart-details').html(data);
                })
                .fail( function (data) {
                    alert('Unbekannter Fehler');
                })
            }

        </laser:script>

    </body>
</html>
