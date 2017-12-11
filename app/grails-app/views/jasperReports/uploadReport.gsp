<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>${message(code:'laser', default:'LAS:eR')} Upload Jasper Reports</title>
    <meta name="layout" content="semanticUI"/>

</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="jasper.reports.label" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <p>The types of accepted files are .jasper and .jrxml. Any other files selected will be ignored.</p>

    <g:uploadForm action="uploadReport" controller="jasperReports">

        <b>Select Reports</b>:

        <input type="file" name="report_files" multiple="multiple"><br/>

        <b>Upload Selected</b>

        <input type="submit" class="btn-primary" value="Upload Files"/>

    </g:uploadForm>


</body>

</html>