<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart message="privacyNotice" />

<ui:breadcrumbs>
    <ui:crumb message="privacyNotice" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="privacyNotice" type="help"/>

<br />
<br />

<ui:greySegment>

<a href="https://www.hbz-nrw.de/datenschutz" class="${Btn.SIMPLE}" target="_blank" onclick="$('#modalDsgvo').modal('hide')">
    <i class="${Icon.LNK.EXTERNAL}"></i> ${message(code:'dse')}
</a>

<br />
<br />

<a href="${resource(dir: 'files', file: 'Verzeichnis_Verarbeitungstaetigkeiten_LASeR_V1.8.pdf')}" class="${Btn.SIMPLE}" target="_blank"  onclick="$('#modalDsgvo').modal('hide')">
    <i class="file pdf icon"></i> ${message(code:'vdv')}
</a>

</ui:greySegment>

<laser:htmlEnd />

