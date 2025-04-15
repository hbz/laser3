<%@ page import="de.laser.utils.FileUtils; de.laser.config.ConfigDefaults; de.laser.config.ConfigMapper; de.laser.utils.DateUtils; de.laser.Doc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.simpleShareConfCheck" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.simpleShareConfCheck" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.simpleShareConfCheck" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="admin" action="simpleFilesCheck" class="item">Dateisystem</g:link>
    <g:link controller="admin" action="simpleDocsCheck" class="item">Datenbank</g:link>
    <g:link controller="admin" action="simpleShareConfCheck" class="item active">Sichtbarkeit</g:link>
</nav>

<%
    String cbq = 'select count(*) from DocContext where '
%>

<p class="ui header small">DocContext @ PROVIDER</p>

<pre>
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'provider != null and shareConf is null')}   <<< OK : AccessService (docContext -> doc -> owner)
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])}
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])}
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_ALL])}

    SHARE_CONF & TARGET_ORG         ${DocContext.executeQuery(cbq + 'provider != null and shareConf != null and targetOrg != null')}   <<< 0 = OK

    Keine Sichtbarkeitseinstellung beim Upload                 <<< OK
    Neu angelegte Einträge OHNE targetOrg und OHNE shareConf   <<< OK
</pre>

<p class="ui header small">DocContext @ VENDOR</p>

<pre>
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'vendor != null and shareConf is null')}   <<< OK : AccessService (docContext -> doc -> owner)
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])}
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])}
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_ALL])}

    SHARE_CONF & TARGET_ORG         ${DocContext.executeQuery(cbq + 'vendor != null and shareConf != null and targetOrg != null')}   <<< 0 = OK

    Keine Sichtbarkeitseinstellung beim Upload                 <<< OK
    Neu angelegte Einträge OHNE targetOrg und OHNE shareConf   <<< OK
</pre>

<p class="ui header small">DocContext @ ORG</p>

<pre>
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'org != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null   <<< FEHLER
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Einträge mit TARGET_ORG = docContext -> doc -> owner überflüssig?
    Beim Upload in eigener Einrichtung Option SHARE_CONF_UPLOADER_AND_TARGET entfernt   <<< gefixt
</pre>

<p class="ui header small">DocContext @ LICENSE</p>

<pre>
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'license != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Für TN in Einrichtungsvertrag Sichtbarkeitseinstellung beim Upload (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG)
    Für Konsortialmanager in Einrichtungsvertrag Sichtbarkeitseinstellung beim Upload (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG)
</pre>

<p class="ui header small">DocContext @ SUBSCRIPTION</p>

<pre>
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'subscription != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Für TN keine Sichtbarkeitseinstellung beim Upload
    Für Konsortialmanager in TN bei SHARE_CONF_UPLOADER_AND_TARGET wird TARGET_ORG gesetzt   <<< OK, aber alte Daten müssen migriert werden
</pre>

<laser:htmlEnd />
