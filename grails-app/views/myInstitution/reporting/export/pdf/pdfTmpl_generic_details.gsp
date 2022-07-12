<%@ page import="de.laser.utils.DateUtils; de.laser.reporting.report.GenericHelper; de.laser.reporting.export.base.BaseDetailsExport;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <g:render template="/templates/reporting/export/pdfStyle" />

    <g:if test="${options.useSmallFont || options.pageFormat}">
        <style type="text/css">
            <g:if test="${options.useSmallFont}"> body { font-size: 12px !important; } </g:if>
            <g:if test="${options.pageFormat}"> th, td { word-break: break-all; } </g:if>
        </style>
    </g:if>
</head>
<body>
    <p><span class="warningTMP">DEMO : Funktionalität in Entwicklung</span></p>

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

    <table>
        <thead>
            <tr>
                <% int cellIndex = 0 %>
                <g:if test="${options.useLineNumbers}">
                    <th class="th_0"></th>
                    <% cellIndex = 1 %>
                </g:if>
                <g:each in="${header}" var="cell" status="i">
                    <th class="th_${i+cellIndex}">${cell[0]}</th>
                </g:each>
            </tr>
        </thead>
        <tbody>
            <g:each in="${content}" var="row" status="i">
                <tr <% if(i%2==0) { print 'class="odd tr_' + i + '"' } else { print 'class="even tr_' + i + '"' }%>>
                    <g:if test="${options.useLineNumbers}">
                        <td class="td_0">${i+1}.</td>
                    </g:if>
                    <g:each in="${row}" var="cell" status="j">
                        <td <% if(j%2==0) { print 'class="odd td_' + (j+cellIndex) + '"' } else { print 'class="even td_' + (j+cellIndex) + '"' }%>>
                            <g:each in="${cell}" var="cp">
                                <g:if test="${cp.startsWith('http://') || cp.startsWith('https://')}">
                                    <g:if test="${cp.indexOf('@') > 0}">
                                        <g:if test="${options.useHyperlinks}">
                                            <a href="${cp.split('@')[0]}">${cp.split('@')[1]}</a><br/>%{-- masking globalUID and gokbId --}%
                                        </g:if>
                                        <g:else>
                                            ${cp.split('@')[0]}<br/>
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${options.useHyperlinks}">
                                            <a href="${cp}">${cp}</a><br/>
                                        </g:if>
                                        <g:else>
                                            ${cp}<br/>
                                        </g:else>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    ${cp}<br/>
                                </g:else>
                            </g:each>
                        </td>
                    </g:each>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>

