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
<style>
    pre.ok      { padding: 1em; border: 1px dashed green; }
    pre.todo    { padding: 1em; border: 1px dashed red; }
</style>
<p class="ui header small">DocContext @ PROVIDER</p>

<pre class="ok">
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'provider != null and shareConf is null')}   <<< OK : AccessService (docContext -> doc -> owner)
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])}     <<< OK : gdc > set SHARE_CONF = null
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])}     <<< OK : gdc > set SHARE_CONF = null
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'provider != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_ALL])}

    SHARE_CONF & TARGET_ORG         ${DocContext.executeQuery(cbq + 'provider != null and shareConf != null and targetOrg != null')}   <<< 0 = OK

    Keine Sichtbarkeitseinstellung beim Upload                 <<< OK
    Neu angelegte Einträge OHNE targetOrg und OHNE shareConf   <<< OK
</pre>

<p class="ui header small">DocContext @ VENDOR</p>

<pre class="ok">
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'vendor != null and shareConf is null')}   <<< OK : AccessService (docContext -> doc -> owner)
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])}    <<< OK : gdc > set SHARE_CONF = null
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])}    <<< OK : gdc > set SHARE_CONF = null
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'vendor != null and shareConf = :sc', [sc: RDStore.SHARE_CONF_ALL])}

    SHARE_CONF & TARGET_ORG         ${DocContext.executeQuery(cbq + 'vendor != null and shareConf != null and targetOrg != null')}   <<< 0 = OK

    Keine Sichtbarkeitseinstellung beim Upload                 <<< OK
    Neu angelegte Einträge OHNE targetOrg und OHNE shareConf   <<< OK
</pre>

<p class="ui header small">DocContext @ ORG</p>

<pre class="todo">
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'org != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null   <<< FEHLER, aber nicht auf PROD
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'org != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Sichtbarkeitseinstellung beim Upload:
        - Eigene Einrichtung (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG)                                 -> TARGET_ORG wird NICHT gesetzt
        - Fremde Einrichtung (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG, SHARE_CONF_UPLOADER_AND_TARGET) -> TARGET_ORG wird gesetzt

    Beim Upload in eigener Einrichtung Option SHARE_CONF_UPLOADER_AND_TARGET entfernt   <<< OK : gefixt

    Einträge mit TARGET_ORG = docContext -> doc -> owner überflüssig?
</pre>

<p class="ui header small">DocContext @ LICENSE</p>

<pre class="todo">
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'license != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null   <<< OK : gdc > set shareConf = SHARE_CONF_ALL
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'license != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Sichtbarkeitseinstellung beim Upload:
        - Einrichtungsvertrag:
            - Konsortialmanager (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG) -> TARGET_ORG wird NICHT gesetzt
            - TN (SHARE_CONF_ALL, SHARE_CONF_UPLOADER_ORG)                -> TARGET_ORG wird NICHT gesetzt
        - KM-Vertrag:
            - Konsortialmanager KEINE - Nur "Dokumente teilen"
</pre>

<p class="ui header small">DocContext @ SUBSCRIPTION</p>

<pre class="todo">
    SHARE_CONF = null               ${DocContext.executeQuery(cbq + 'subscription != null and shareConf is null')}
    SHARE_CONF_UPLOADER_ORG         ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_ORG])} + TARGET_ORG
    SHARE_CONF_UPLOADER_AND_TARGET  ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} TARGET_ORG = null   <<< OK : gdc > set TARGET_ORG = dc.subscription.subscriber
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET])} + TARGET_ORG
    SHARE_CONF_ALL                  ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg is null', [sc: RDStore.SHARE_CONF_ALL])} TARGET_ORG = null
                                    ${DocContext.executeQuery(cbq + 'subscription != null and shareConf = :sc and targetOrg != null', [sc: RDStore.SHARE_CONF_ALL])} + TARGET_ORG

    Sichtbarkeitseinstellung beim Upload:
        - TN-Lizenz:
            - Konsortialmanager (SHARE_CONF_UPLOADER_ORG, SHARE_CONF_UPLOADER_AND_TARGET)
                - SHARE_CONF_UPLOADER_ORG         -> TARGET_ORG wird NICHT gesetzt
                - SHARE_CONF_UPLOADER_AND_TARGET  -> TARGET_ORG wird gesetzt   <<< OK, nach gdc
            - TN KEINE
        - KM-Lizenz:
            - Konsortialmanager KEINE - Nur "Dokumente teilen"
</pre>

<p class="ui header small">TODO</p>

<pre>
    Subscription . SHARE_CONF_UPLOADER_AND_TARGET -> SHARE_CONF_ALL ?

    X . SHARE_CONF_UPLOADER_ORG -> shareConf = null ?

    Org.shareConf > Dokumente   !=   License.shareConf > Geteilte Dokumente    !=   Subscription.shareConf > Dokumente
</pre>

<laser:htmlEnd />
