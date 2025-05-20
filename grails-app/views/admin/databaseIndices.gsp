<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn; de.laser.helper.DatabaseInfo; groovy.sql.GroovyRowResult; de.laser.RefdataValue; de.laser.storage.BeanStore" %>

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
    <button class="${Btn.ICON.SIMPLE} small" id="filter_internal1"><icon:arrow /> Id/Version</button>
    <button class="${Btn.ICON.SIMPLE} small" id="filter_internal2"><icon:arrow /> DateCreated/LastUpdated</button>
    <button class="${Btn.ICON.SIMPLE} small" id="filter_collections"><icon:arrow /> Collections</button>
    <g:select class="ui dropdown clearable" from="${['1.000':1000, '5.000':5000, '10.000':10000, '50.000':50000, '100.000':100000, '500.000':500000, '1.000.000':1000000, '5.000.000':5000000, '10.000.000':10000000]}"
              id="threshold_filter_value" name="threshold_filter_value"
              optionValue="${{it.key}}"
              optionKey="${{it.value}}"
              noSelection="${['': 'DB-Einträge (Domainklasse)']}" />

    <button class="${Btn.ICON.SIMPLE} small" id="threshold_filter"><i class="icon filter"></i><i class="icon sort"></i></button>
    <button class="${Btn.ICON.SIMPLE} small" id="filter_mapping"><i class="arrow down icon"></i> Mapping</button>
    <div class="ui buttons">
        <button class="${Btn.ICON.SIMPLE} small" id="filter_index"><i class="${Icon.SYM.YES}"></i> Index</button>
        <button class="${Btn.ICON.SIMPLE} small" id="filter_todo"><i class="${Icon.SYM.YES}"></i> Todo</button>
    </div>
</div>

<ui:msg class="info" header="Achtung" text="Die angezeigten Daten sind unvollständig und bieten nur eine Orientierungshilfe."/>

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

    #table.filter_internal1 tbody tr[data-internal1=true] {
        display: none;
    }
    #table.filter_internal2 tbody tr[data-internal2=true] {
        display: none;
    }
    #table.filter_collections tbody tr[data-collections=true] {
        display: none;
    }
    #table.filter_mapping thead tr th:nth-child(4),
    #table.filter_mapping tbody tr td:nth-child(4) {
        display: none;
    }
    #table.filter_todo tbody tr:not(.negative) {
        display: none;
    }
    #table.filter_index tbody tr:not(.positive, .info) {
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
    $('#filter_internal1, #filter_internal2, #filter_collections, #filter_mapping').on('click', function() {
        $(this).toggleClass('positive')
        $('#table').toggleClass( $(this).attr('id') )
    })
    $('#filter_todo, #filter_index').on('click', function() {
        let $todo = $('#filter_todo')
        let $index = $('#filter_index')
        let id = $(this).attr('id')

        if (id == 'filter_todo' && $index.hasClass('positive')) {
            $index.toggleClass('positive')
            $('#table').toggleClass('filter_index')
        }
        else if (id == 'filter_index' && $todo.hasClass('positive')) {
            $todo.toggleClass('positive')
            $('#table').toggleClass('filter_todo')
        }

        $(this).toggleClass('positive')
        $('#table').toggleClass( id )
    })

    $('table').addClass('filter_internal1 filter_internal2 filter_collections filter_mapping')
    $('table').removeClass('hidden')
</laser:script>

<table class="ui la-hover-table very compact table hidden" id="table">
        <thead>
            <tr>
                <th class="center aligned">#</th>
                <th>Domainklasse</th>
                <th>Feld</th>
                <th>Mapping</th>
                <th>Typ</th>
                <th class="center aligned">&lArr;</th>
                <th class="center aligned">&rArr;</th>
                <th>Index</th>
                <th>Größe</th>
                <th class="center aligned">*</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${indices}" var="row" status="i">
                <g:if test="${!lastRow || lastRow != row[1]}">
                    <g:set var="lastRow" value="${row[1]}" />
                    <tr class="disabled">
                        <td colspan="9" style="background-color: #f4f8f9"></td>
                    </tr>
                </g:if>
                <g:set var="isDisabled" value="${tables_internal1.contains(i) || tables_internal2.contains(i) || tables_collections.contains(i)}" />
                <g:set var="calcCssUnique" value="${row[5] == 'UNIQUE' ? 'unique' : ''}" />
                <g:set var="calcCssClass" value="${row[5] ? (tables_laser.contains(i) ? 'positive' : 'info') : tables_laser.contains(i) ? 'negative' : ''}" />
                <g:set var="countFrom" value="${counts.get(row[1])}" />
                <g:set var="countTo" value="${counts.get(row[3].toString().replace('class ', ''))}" />
                <g:set var="threshold" value="${(countFrom && countFrom != '?') ? countFrom : 0}" />

                <tr class="${calcCssClass} ${calcCssUnique} ${isDisabled ? 'disabled ' : ''}"
                    data-internal1="${tables_internal1.contains(i)}"
                    data-internal2="${tables_internal2.contains(i)}"
                    data-collections="${tables_collections.contains(i)}"
                    data-threshold="${threshold}"
                >
                    <td class="center aligned">
                        <%
                            switch( calcCssClass ) {
                                case 'positive':    println '<i class="' + Icon.SYM.YES + '"></i>'; break
                                case 'info':        println '<i class="' + Icon.SYM.YES + '"></i>'; break
                                case 'warning':     println '<i class="' + Icon.SYM.NO + '"></i>'; break
                                default:            println (i+1)
                            }
                        %>
                    </td>
                    <td> ${row[1].split('\\.').last()} </td>
                    <td> ${row[2]} </td>
                    <td> ${row[4]} </td>
                    <td> ${row[3].toString().replace('class ', '').replace('interface ', '')} </td>
                    <td class="center aligned"> ${countFrom && countFrom != '?' ? dformat.format(countFrom) : ''} </td>
                    <td class="center aligned"> ${countTo && countTo != '?' ? dformat.format(countTo) : ''} </td>
                    <td>
                        <g:each in="${row[5]?.split(',')}" var="idx"> ${idx} <br/> </g:each>
                    </td>
                    <td>
                        <g:each in="${row[6]}" var="idx"> ${idx ? idx['idx_size'] : ''} <br/> </g:each>
                    </td>
                    <td class="center aligned">
                        <g:each in="${row[6]}" var="idx"> ${idx ? dformat.format(idx['idx_scan']) : ''} <br/> </g:each>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
