<%@ page import="de.laser.reporting.export.base.BaseDetailsExport;de.laser.reporting.report.myInstitution.GenericHelper;de.laser.helper.DateUtils;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style type="text/css">
    body {
        font-size: 16px;
    }
    h1 > span {
        font-size: 80%;
        color: rgba(0,0,0, 0.35);
    }
    table {
        margin-top: 3em;
        border-spacing: 0;
        border-collapse: collapse;
        border-width: 0;
    }
    table thead tr {
        text-align: left;
        color: #FFFFFF;
        background-color: #2471a3;
    }
    table thead tr th {
        padding: 1em 0.6em;
        border-color: #2471a3;
        border-bottom: 0.5em solid #FFFFFF;
    }
    table tbody tr.even {
        background-color: #F6F7F7;
    }
    table tbody tr td {
        padding: 0.35em 0.6em;
    }
    .warning {
        position: absolute;
        top: 0;
        right: 0;
        padding: 0.5em 1em;
        border: 1px dashed #bb1600;
        color: #bb1600;
        font-weight: bold;
    }
    .filterInfo,
    .filterResult {
        font-size: 90%;
    }
    .filterInfo,
    .filterResult,
    .queryInfo {
        margin-bottom: 0.5em;
        padding: 0.5em 1em;
        border-left: 5px solid #a9cce3;
    }
    .queryInfo {
        font-weight: bold;
    }
    </style>
</head>
<body>
    <p><span class="warning">DEMO : Funktionalität in Entwicklung</span></p>

    <h1>LAS:eR Report <span>- ${DateUtils.getSDF_NoTime().format( new Date() )}</span></h1>

    <div class="filterInfo">
        <g:each in="${filterLabels}" var="lblGroup">
            ${lblGroup.value.source}

            <g:each in="${lblGroup.value}" var="label">
                <g:if test="${label.key != 'source'}">
                    -
                    <g:if test="${label.key in ['endDate', 'startDate']}">
                        ${label.value.label} ${label.value.value}
                    </g:if>
                    <g:else>
                        ${label.value.label}:
                        <g:if test="${GenericHelper.isFieldMultiple(label.key)}">
                            <g:if test="${label.value.value instanceof String}">
                                ${label.value.value}
                            </g:if>
                            <g:else>
                                ${label.value.value.join(', ')}
                            </g:else>
                        </g:if>
                        <g:else>
                            ${label.value.value}
                        </g:else>
                    </g:else>
                </g:if>
            </g:each>
            <br />
        </g:each>
    </div>

    <div class="filterResult">
        <%= filterResult %>
    </div>

    <div class="queryInfo">
        ${queryLabels.join(' > ')}
    </div>

    <table>
        <thead>
            <tr>
                <th></th>
                <g:each in="${header}" var="cell">
                    <th>${cell[0]}</th>
                </g:each>
            </tr>
        </thead>
        <tbody>
            <g:each in="${content}" var="row" status="i">
                <tr <% if(i%2==0) { print 'class="odd"' } else { print 'class="even"' }%>>
                    <td>${i+1}.</td>
                    <g:each in="${row}" var="cell" status="j">
                        <td <% if(j%2==0) { print 'class="odd"' } else { print 'class="even"' }%>>
                            <g:each in="${cell}" var="cp">
                                ${cp}<br/>
                            </g:each>
                        </td>
                    </g:each>
                </tr>
            </g:each>
        </tbody>
    </table>
</body>
</html>

