<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: 'privacyNotice')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="privacyNotice" class="active"/>
</semui:breadcrumbs>

<semui:headerWithIcon message="privacyNotice" />

<br />
<br />

<semui:form>

<a href="https://www.hbz-nrw.de/datenschutz"  class="ui button" target="_blank" onclick="$('#modalDsgvo').modal('hide')">
    <i class="share square icon"></i>
    ${message(code:'dse')}
</a>

<br />
<br />

<a href="${resource(dir: 'files', file: 'Verzeichnis_Verarbeitungstaetigkeiten_LASeR_V1.3.pdf')}" class="ui button" target="_blank"  onclick="$('#modalDsgvo').modal('hide')">
    <i class="file pdf icon"></i>
    ${message(code:'vdv')}
</a>

</semui:form>


</body>
</html>

