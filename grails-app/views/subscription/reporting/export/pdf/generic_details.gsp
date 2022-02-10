<%@ page import="de.laser.reporting.export.base.BaseDetailsExport;de.laser.reporting.report.GenericHelper;de.laser.helper.DateUtils;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <g:render template="/templates/reporting/export/pdfStyle" />

    <g:if test="${options.pageFormat}">
        <style type="text/css"> th, td { word-break: break-all; } </style>
    </g:if>
</head>
<body>
    <p><span class="warningTMP">DEMO : Funktionalität in Entwicklung</span></p>

    <h1>LAS:eR Report <span>- ${DateUtils.getSDF_NoTime().format( new Date() )}</span></h1>

    <div class="filterResult">
        <%= filterResult %>
    </div>

    <div class="queryInfo">
        ${queryLabels.join(' > ')}
    </div>

    <table>
        <thead>
            <tr>
                <g:if test="${options.useLineNumbers}">
                    <th></th>
                </g:if>
                <g:each in="${header}" var="cell">
                    <th>${cell[0]}</th>
                </g:each>
            </tr>
        </thead>
        <tbody>
            <g:each in="${content}" var="row" status="i">
                <tr <% if(i%2==0) { print 'class="odd"' } else { print 'class="even"' }%>>
                    <g:if test="${options.useLineNumbers}">
                        <td>${i+1}.</td>
                    </g:if>
                    <g:each in="${row}" var="cell" status="j">
                        <td <% if(j%2==0) { print 'class="odd"' } else { print 'class="even"' }%>>
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

