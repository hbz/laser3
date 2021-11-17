<%@ page import="de.laser.reporting.export.base.BaseDetailsExport;de.laser.reporting.report.GenericHelper;de.laser.helper.DateUtils;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <g:render template="/templates/reporting/export/pdfStyle" />
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

