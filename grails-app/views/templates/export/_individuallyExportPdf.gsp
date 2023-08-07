<%@ page import="de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.RefdataValue;de.laser.Links" %>
<laser:serviceInjection />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>${message(code:'laser')} : ${message(code:'license.current')}</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
        body {
            font-size: 12px;
            font-family: sans-serif;
        }
        h1 > span {
            font-size: 80%;
            color: rgba(0,0,0, 0.35);
        }
        table {
            border-spacing: 0;
            border-collapse: collapse;
            border-width: 0;
            width: 100%;
        }

        table thead tr {
            text-align: left;
            color: #FFFFFF;
            background-color: #2471a3;
            border: 1px solid black;
            border-collapse: collapse;
        }

        table thead tr th {
            padding: 0.6em 0.6em;
            border-color: #2471a3;
            border: 1px solid black;
            border-collapse: collapse;
        }

        table tbody tr.even {
            background-color: #F6F7F7;
        }

        table tbody tr td {
            padding: 0.35em 0.6em;
            border: 1px solid black;
            border-collapse: collapse;
        }
        /* this CSS class enables the usage of multiple pages */
        .new-page {
            page-break-before: always;
        }
        </style>
    </head>
    <body>
        <g:each in="${pages}" var="page" status="p">
            <article>
                <table>
                    <thead>
                    <tr>
                        <th><g:message code="sidewide.number"/></th>
                        <g:each in="${page.titleRow}" var="titleColumn">
                            <th>${titleColumn}</th>
                        </g:each>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${page.columnData}" var="cd" status="jj">
                        <tr <% if((jj+1) % 2 == 0) { print 'class="even"' } else { print 'class="odd"' } %>>
                            <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                            <g:each in="${cd}" var="content">
                                <td>${content}</td>
                            </g:each>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </article>
            <g:if test="${p < pages.size()-1}">
                <div class="new-page"></div>
            </g:if>
        </g:each>
    </body>
</html>