<%@ page import="de.laser.ui.CSS; de.laser.ui.Icon; de.laser.ui.Btn; de.laser.helper.DatabaseInfo; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStore" %>

<laser:htmlStart message="menu.admin.databaseIndices" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.databaseIndices" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.databaseIndices" type="admin"/>

<g:set var="tables_internal1" value="${indices.findAll{ it[2] in ['id', 'version'] }.collect{ it[0] }}" />
<g:set var="tables_internal2" value="${indices.findAll{ it[2] in ['dateCreated', 'lastUpdated'] }.collect{ it[0] }}" />
<g:set var="tables_collections" value="${indices.findAll{ it[3].toString().startsWith('interface ') }.collect{ it[0] }}" />
<g:set var="tables_laser" value="${indices.findAll{ it[3].toString().startsWith('class de.laser.') }.collect{ it[0] }}" />

<g:set var="dformat" value="${new java.text.DecimalFormat("###,###,###")}" />

<br/>

<div id="filter">
    <div class="ui buttons">
        <button class="${Btn.ICON.SIMPLE} small" id="filter_internal1">Id/Version</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_internal2">DateCreated/LastUpdated</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_collections">Collections</button>
    </div>
    <div class="ui buttons">
        <button class="${Btn.ICON.SIMPLE} small" id="filter_type">Typ</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_mapping">Mapping</button>
    </div>

    <g:select class="ui dropdown clearable" from="${['1.000':1000, '5.000':5000, '10.000':10000, '50.000':50000, '100.000':100000, '500.000':500000, '1.000.000':1000000, '5.000.000':5000000, '10.000.000':10000000]}"
              id="threshold_filter_value" name="threshold_filter_value"
              optionValue="${{it.key}}"
              optionKey="${{it.value}}"
              noSelection="${['': 'DB-Einträge (Domainklasse)']}" />

    <button class="${Btn.ICON.SIMPLE} small" id="threshold_filter"><i class="icon filter"></i><icon:sort/></button>

    <div class="ui buttons">
        <button class="${Btn.ICON.SIMPLE} small positive" id="filter_none">Alle</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_index">Alle Indizes</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_trigram">Nur Trigramm-Indizes</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_todo">Ohne Index</button>
    </div>
</div>

<ui:msg class="info" header="Achtung" text="Die angezeigten Daten können unvollständig sein. Indizes werden NUR über das GORM-Mapping oder entsprechende Annotationen erkannt."/>

<style>
    #table {
        background-color: #ffffff;
    }
    #table tr.info td {
        color: #276f86;
        background-color: #f8ffff;
    }
    #table tr.threshold_filter td {
        display: none;
    }
    #table tr.unique td {
        font-style: italic;
    }
    #table tr.trigram td {
        font-weight: bold;
    }

    #table.filter_internal1 tbody tr[data-internal1=true] {
        display: none;
    }
    #table.filter_internal2 tbody tr[data-internal2=true] {
        display: none;
    }
    #table.filter_collections tbody tr[data-collections=true] {
        display: none;
    }
    #table.filter_type thead tr th:nth-child(4),
    #table.filter_type tbody tr td:nth-child(4) {
        display: none;
    }
    #table.filter_mapping thead tr th:nth-child(5),
    #table.filter_mapping tbody tr td:nth-child(5) {
        display: none;
    }
    #table.filter_todo tbody tr:not(.negative) {
        display: none;
    }
    #table.filter_index tbody tr:not(.positive, .info) {
        display: none;
    }
    #table.filter_trigram tbody tr:not(.trigram) {
        display: none;
    }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.idxFilter = { threshold_filter: false }

    JSPC.app.applyFilter = function() {
        $('#table tbody tr').removeClass('threshold_filter')

        let tv = $('#threshold_filter_value').dropdown('get value')
        if (tv) {
            let $th = $('#table tbody tr[data-threshold]').filter(function() {
                if (JSPC.app.idxFilter.threshold_filter ) {
                    return parseInt($(this).attr('data-threshold')) >= parseInt(tv)
                } else {
                    return parseInt($(this).attr('data-threshold')) <= parseInt(tv)
                }
            });
            $th.addClass('threshold_filter')
        }
    }
    $('#threshold_filter').on('click', function() {
        JSPC.app.idxFilter.threshold_filter = !JSPC.app.idxFilter.threshold_filter
        JSPC.app.applyFilter()
    })
    $('#threshold_filter_value').on('change', function() {
        JSPC.app.applyFilter()
    })
    $('#filter_internal1, #filter_internal2, #filter_collections, #filter_type, #filter_mapping').on('click', function() {
        $(this).toggleClass('positive')
        $('#table').toggleClass( $(this).attr('id') )
    })
    $('#filter_none, #filter_index, #filter_trigram, #filter_todo').on('click', function() {
        let $none   = $('#filter_none')
        let $index  = $('#filter_index')
        let $trigram = $('#filter_trigram')
        let $todo   = $('#filter_todo')
        let id      = $(this).attr('id')

        $none.removeClass('positive')
        $index.removeClass('positive')
        $trigram.removeClass('positive')
        $todo.removeClass('positive')

        $('#table').removeClass('filter_index filter_trigram filter_todo')

        if (id == 'filter_none') {
            $none.addClass('positive')
        } else {
                 if (id == 'filter_index')   { $index.addClass('positive') }
            else if (id == 'filter_trigram') { $trigram.addClass('positive') }
            else if (id == 'filter_todo')    { $todo.addClass('positive') }
            $('#table').addClass( id )
        }
    })

    $('table').addClass('filter_internal1 filter_internal2 filter_collections filter_type filter_mapping')
    $('table').removeClass('hidden')
</laser:script>

<table class="${CSS.ADMIN_HOVER_TABLE} hidden" id="table">
        <thead>
            <tr>
                <th class="center aligned">#</th>
                <th>Domainklasse</th>
                <th>Feld</th>
                <th>Typ</th>
                <th>Mapping</th>
                <th class="center aligned">count(*)</th>
                <th class="center aligned">ref.</th>
                <th>Index</th>
                <th>Größe</th>
                <th class="center aligned">Usage</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${indices}" var="row" status="i">
%{--            <g:each in="${indices.sort{ (it[1].split('\\.').last() + it[2]).toString() }}" var="row" status="i">--}%
                <g:set var="isDisabled"     value="${tables_internal1.contains(i) || tables_internal2.contains(i) || tables_collections.contains(i)}" />
                <g:set var="calcCssUnique"  value="${row[5] == 'UNIQUE' ? 'unique' : ''}" />
                <g:set var="calcCssTrigram" value="${row[5]?.contains('_trigram') ? 'trigram' : ''}" />
                <g:set var="calcCssClass"   value="${row[5] ? (tables_laser.contains(i) ? 'info' : 'positive') : tables_laser.contains(i) ? 'negative' : ''}" />
                <g:set var="countFrom"      value="${counts.get(row[1])}" />
                <g:set var="countTo"        value="${counts.get(row[3].toString().replace('class ', ''))}" />
                <g:set var="threshold"      value="${(countFrom && countFrom != '?') ? countFrom : 0}" />

                <tr class="${calcCssClass} ${calcCssUnique} ${calcCssTrigram} ${isDisabled ? 'disabled ' : ''}"
                    data-internal1="${tables_internal1.contains(i)}"
                    data-internal2="${tables_internal2.contains(i)}"
                    data-collections="${tables_collections.contains(i)}"
                    data-threshold="${threshold}"
                >
                    <td class="center aligned">
                        <%
                            switch( calcCssClass ) {
                                case 'info':        println '<i class="' + Icon.SYM.YES + '"></i>'; break
                                case 'positive':    println '<i class="' + Icon.SYM.YES + '"></i>'; break
                                case 'warning':     println '<i class="' + Icon.SYM.NO + '"></i>'; break
                                default:            println (i+1)
                            }
                        %>
                    </td>
                    <td> ${row[1].split('\\.').last()} </td>
                    <td> ${row[2]} </td>
                    <td> ${row[3].toString().replace('class ', '').replace('interface ', '')} </td>
                    <td> ${row[4]} </td>
                    <td class="center aligned"> ${countFrom && countFrom != '?' ? dformat.format(countFrom) : ''} </td>
                    <td class="center aligned"> ${countTo && countTo != '?' ? dformat.format(countTo) : ''} </td>
                    <td>
                        <g:each in="${row[5]?.split(',')}" var="idx">
                            ${idx} <br/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${row[6]}" var="idx">
                            <g:if test="${idx}">
                                ${idx['idx_size']}
                            </g:if>
                            <g:else>
                                <strong class="sc_red">FAILED</strong>
                            </g:else>
                            <br />
                        </g:each>
                    </td>
                    <td class="center aligned">
                        <g:each in="${row[6]}" var="idx">
                            <g:if test="${idx}">
                                ${dformat.format(idx['idx_scan'])}
                            </g:if>
                            <g:else>
                                <strong class="sc_red">FAILED</strong>
                            </g:else>
                            <br />
                        </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
