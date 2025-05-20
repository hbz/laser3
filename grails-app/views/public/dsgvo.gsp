<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:htmlStart message="privacyNotice" />

<sec:ifLoggedIn>
    <ui:breadcrumbs>
        <ui:crumb text="${message(code:'menu.user.help')}" class="active" />
    </ui:breadcrumbs>
</sec:ifLoggedIn>

<ui:h1HeaderWithIcon message="privacyNotice" type="help"/>

<br />
<br />

<ui:greySegment>

<a href="https://www.hbz-nrw.de/datenschutz" class="${Btn.SIMPLE}" target="_blank" onclick="$('#modalDsgvo').modal('hide')">
    <i class="${Icon.LNK.EXTERNAL}"></i> ${message(code:'dse')}
</a>

<br />
<br />

<a href="${resource(dir: 'files', file: 'Verzeichnis_Verarbeitungstaetigkeiten_LASeR_V1.10.pdf')}" class="${Btn.SIMPLE}" target="_blank"  onclick="$('#modalDsgvo').modal('hide')">
    <icon:pdf /> ${message(code:'vdv')}
</a>

</ui:greySegment>

<laser:htmlEnd />

