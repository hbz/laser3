<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.UserSetting" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : Frontend for Developers</title>

    <style>
    #example .example .column .icon {
        opacity: 1;
        height: 1em;
        color: #333333;
        display: block;
        margin: 0em auto 0.25em;
        font-size: 2em;
        -webkit-transition: color 0.6s ease, transform 0.2s ease;
        -moz-transition: color 0.6s ease, transform 0.2s ease;
        -o-transition: color 0.6s ease, transform 0.2s ease;
        -ms-transition: color 0.6s ease, transform 0.2s ease;
        transition: color 0.6s ease, transform 0.2s ease;
    }

    #example .example.html.segment {
        padding: 3.5em 1em 1em;
    }

    #example .example .grid > .column {
        opacity: 0.8;
        text-align: center;
    }
    #example .example > .html.segment {
        padding: 3.5em 1em 1em;
    }
    code .tag .title {
        color: #858188;
        font-weight: normal;
    }
    code.code .class {
        color: #008C79;
    }
    code.code .class b {
        background-color: rgba(218, 189, 40, 0.15);
        color: #9E6C00;
    }
    code .string, code .tag .value, code .phpdoc, code .dartdoc, code .tex .formula {
        color: #008C79;
    }
    code:hover .tag .title {
        color: #892A6F;
    }
    .example {
        margin-top: 0em;
        padding-top: 2em;
    }
    </style>
</head>

<body>
<div class="ui top attached tabular la-tab-with-js menu">
    <div class="item active">Tab 1</div>
    <div class="item">Tab 2</div>
    <div class="item">Tab 3</div>
</div>

<div class="ui bottom attached tab segment">
    <p></p>
    <p></p>
</div>
%{-- help sidebar --}%
<g:render template="/templates/help/help_subscription_show"/>
<section id="example">
    %{-- ICONS  --}%
    <g:render template="frontend_icons" />
    %{-- DROPDOWNS --}%
    <g:render template="frontend_dropdowns" />
    %{-- DECKSAVER --}%
    <g:render template="frontend_decksaver" />
    %{-- TOGGLE BUTTONS  --}%
    <g:render template="frontend_toggleButtons" />
    %{-- MODALS  --}%
    <g:render template="frontend_modals" />
    %{-- CONFIRMATION MODAL  --}%
    <g:render template="frontend_confirmationModals" />
    %{-- RESPONSIBLE TABELLEN  --}%
    <g:render template="frontend_responsibleTable" />
    %{-- LABELS --}%
    <g:render template="frontend_labels" />
</section>
%{-- OFFENE FRAGEN WCAG  --}%
<g:render template="frontend_wcag" />
</body>
</html>
