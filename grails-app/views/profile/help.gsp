<%@ page import="de.laser.RefdataValue;de.laser.auth.Role;" %>
<laser:htmlStart message="menu.user.help" />

<ui:breadcrumbs>
    <ui:crumb message="menu.institutions.help" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.user.help" type="help"/>
<br />
<br />
<h2 class="ui icon header la-clear-before la-noMargin-top"><i class="icon question circle outline"></i> <g:message code="help.contentHelp"/></h2>

<div class="ui segment">
    <p>
        <strong><g:message code="help.contentHelp.manuel.title"/></strong>
    </p>
    <p>
        <g:message code="help.contentHelp.manuel.content"/>:
        <a href="https://service-wiki.hbz-nrw.de/display/LAS/Startseite" class="header" target="_blank">https://service-wiki.hbz-nrw.de/display/LAS/Startseite</a>
    </p>
</div>

<div class="ui segment">
    <p>
        <strong><g:message code="help.contentHelp.properties.title"/></strong>
    </p>
    <p>
        <g:link class="item" controller="profile" action="properties">${message(code: 'menu.user.properties')}</g:link>
    </p>
</div>

<br />
<br />

<h2 class="ui icon header la-clear-before la-noMargin-top"><i class="icon question circle outline"></i> <g:message code="help.technicalHelp"/></h2>

<div class="ui segment">
    <p>
        <strong><g:message code="help.technicalHelp.spellcheck.title"/></strong>
    </p>
    <div class="ui relaxed list">
        <p>
            <g:message code="help.technicalHelp.spellcheck.content"/>:
        </p>
        <div class="item">
            <i class="large internet explorer middle aligned icon"></i>
            <div class="content">
                <div class="description">Microsoft Edge</div>
                <a href="https://support.microsoft.com/de-de/office/microsoft-editor-pr%C3%BCft-die-grammatik-und-mehr-in-dokumenten-%D0%B5-mails-und-im-internet-91ecbe1b-d021-4e9e-a82e-abc4cd7163d7" target="_blank" class="header">https://support.microsoft.com/de-de/office/microsoft-editor-pr%C3%BCft-die-grammatik-und-mehr-in-dokumenten-%D0%B5-mails-und-im-internet-91ecbe1b-d021-4e9e-a82e-abc4cd7163d7</a>
            </div>
        </div>
        <div class="item">
            <i class="large chrome middle aligned icon"></i>
            <div class="content">
                <div class="description">Google Chrome</div>
                <a href="https://support.google.com/chrome/answer/12027911?hl=de" class="header" target="_blank">https://support.google.com/chrome/answer/12027911?hl=de</a>
            </div>
        </div>
        <div class="item">
            <i class="large firefox middle aligned icon"></i>
            <div class="content">
                <div class="description">Firefox</div>
                <a href="https://support.mozilla.org/de/kb/Rechtschreibpruefung-nutzen" target="_blank" class="header">https://support.mozilla.org/de/kb/Rechtschreibpruefung-nutzen</a>
            </div>
        </div>
    </div>
</div>

<div class="ui segment">
    <p>
        <strong><g:message code="help.technicalHelp.browsercache.title"/></strong>
    </p>
    <div class="ui relaxed list">
        <p>
            <g:message code="help.technicalHelp.browsercache.content"/>:
        </p>
        <div class="item">
            <i class="large internet explorer middle aligned icon"></i>
            <div class="content">
                <div class="description">Internet Explorer</div>
                <a href="https://support.microsoft.com/de-de/help/17438/windows-internet-explorer-view-delete-browsing-history" class="header" target="_blank">https://support.microsoft.com/de-de/help/17438/windows-internet-explorer-view-delete-browsing-history</a>
            </div>
        </div>
        <div class="item">
            <i class="large chrome middle aligned icon"></i>
            <div class="content">
                <div class="description">Google Chrome</div>
                <a href="https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008" class="header" target="_blank">https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008</a>
            </div>
        </div>
        <div class="item">
            <i class="large firefox middle aligned icon"></i>
            <div class="content">
                <div class="description">Firefox</div>
                <a href="https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren" target="_blank" class="header">https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren</a>
            </div>
        </div>
    </div>
</div>


%{--<div class="ui accordion styled fluid la-clear-before">--}%
%{--    <div class="title">--}%
%{--        <i class="dropdown icon"></i>--}%
%{--        <g:message code="help.contentHelp.manuel.title"/>--}%
%{--    </div>--}%
%{--    <div class="content">--}%
%{--        <div class="ui relaxed divided list">--}%
%{--            <p>--}%
%{--                <g:message code="help.contentHelp.manuel.content"/>:--}%
%{--                <a href="https://service-wiki.hbz-nrw.de/display/LAS/Startseite" class="header" target="_blank">https://service-wiki.hbz-nrw.de/display/LAS/Startseite</a>--}%
%{--            </p>--}%
%{--        </div>--}%
%{--    </div>--}%

%{--    <div class="title">--}%
%{--        <i class="dropdown icon"></i>--}%
%{--        <g:message code="help.contentHelp.properties.title"/>--}%
%{--    </div>--}%
%{--    <div class="content">--}%
%{--        <div class="ui relaxed divided list">--}%
%{--            <p>--}%
%{--                <g:link class="item" controller="profile" action="properties">${message(code: 'menu.user.properties')}</g:link>--}%
%{--            </p>--}%
%{--        </div>--}%
%{--    </div>--}%

%{--</div>--}%

%{--<br />--}%

%{--<h3 class="ui icon header la-clear-before la-noMargin-top"><g:message code="help.technicalHelp"/></h3>--}%

%{--    <div class="ui accordion styled fluid la-clear-before">--}%

%{--        <div class="title">--}%
%{--            <i class="dropdown icon"></i>--}%
%{--            <g:message code="help.technicalHelp.browsercache.title"/>--}%
%{--        </div>--}%
%{--        <div class="content">--}%
%{--            <div class="ui relaxed divided list">--}%
%{--                <p><g:message code="help.technicalHelp.browsercache.content"/>:</p>--}%
%{--                <div class="item">--}%
%{--                    <i class="large internet explorer middle aligned icon"></i>--}%
%{--                    <div class="content">--}%
%{--                        <div class="description">Internet Explorer</div>--}%
%{--                        <a href="https://support.microsoft.com/de-de/help/17438/windows-internet-explorer-view-delete-browsing-history" class="header" target="_blank">https://support.microsoft.com/de-de/help/17438/windows-internet-explorer-view-delete-browsing-history</a>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--                <div class="item">--}%
%{--                    <i class="large chrome middle aligned icon"></i>--}%
%{--                    <div class="content">--}%
%{--                        <div class="description">Google Chrome</div>--}%
%{--                        <a href="https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008" class="header" target="_blank">https://support.google.com/chrome/answer/2392709?hl=de&ref_topic=7438008</a>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--                <div class="item">--}%
%{--                    <i class="large firefox middle aligned icon"></i>--}%
%{--                    <div class="content">--}%
%{--                        <div class="description">Firefox</div>--}%
%{--                        <a href="https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren" target="_blank" class="header">https://support.mozilla.org/de/kb/Wie-Sie-den-Cache-leeren-konnen#w_cache-manuell-leeren</a>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--            </div>--}%
%{--        </div>--}%
%{--    </div>--}%

<laser:htmlEnd />
