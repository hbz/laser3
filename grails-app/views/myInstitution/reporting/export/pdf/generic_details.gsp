<%@ page import="de.laser.reporting.report.GenericHelper; de.laser.reporting.export.base.BaseDetailsExport;de.laser.helper.DateUtils;" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title>${title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <g:render template="/templates/reporting/export/pdfStyle" />
    <style type="text/css">
    .filterInfo,
    .filterResult {
        font-size: 90%;
    }
    </style>
</head>
<body>
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
                                <g:if test="${options.useHyperlinks && (cp.startsWith('http://') || cp.startsWith('https://'))}">
                                    <a href="${cp}">${cp}</a><br/>
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

