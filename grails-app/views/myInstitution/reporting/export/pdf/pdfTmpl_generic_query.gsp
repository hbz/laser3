<%@ page import="de.laser.utils.DateUtils; de.laser.reporting.report.GenericHelper; de.laser.reporting.export.base.BaseDetailsExport;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <g:render template="/templates/reporting/export/pdfStyle" />
    <style type="text/css">
    .queryChart {
        margin: 4em 0 0 0;
        width: 100%;
        height: auto;
    }
    </style>
</head>
<body>

    <h1>LAS:eR Report <span>- ${DateUtils.getLocalizedSDF_noTime().format( new Date() )}</span></h1>

    <div class="infoWrapper">
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
                            <g:if test="${GenericHelper.isCollection(label.value.value)}">
                                ${label.value.value.join(', ')}
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
    </div>

    <g:if test="${contentType == 'image'}">
        <g:if test="${imageData}">
            <img class="queryChart" src="${imageData}" alt="[ Platzhalter ]" />
        </g:if>
        <g:else>
            [ Platzhalter ]
        </g:else>
    </g:if>
    <g:elseif test="${contentType == 'table'}">

        <table>
            <thead>
                <tr>
                    <th class="th_0"></th>
                    <g:each in="${header}" var="cell" status="i">
                        <th class="th_${i+1}">${cell}</th>
                    </g:each>
                </tr>
            </thead>
            <tbody>
                <g:each in="${content}" var="row" status="i">
                    <tr <% if(i%2==0) { print 'class="odd tr_' + i + '"' } else { print 'class="even tr_' + i + '"' }%>>
                        <td class="td_0">${i+1}.</td>
                        <g:each in="${row}" var="cell" status="j">
                            <td <% if(j%2==0) { print 'class="odd td_' + (j+1) + '"' } else { print 'class="even td_' + (j+1) + '"' }%>>
                                ${cell}
                            </td>
                        </g:each>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </g:elseif>
</body>
</html>

