<%@ page import="de.laser.License; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.UserSetting" %>
<laser:htmlStart text="Frontend for Developers"> %{-- <!doctype html><html><head>--}%

    <style>
    #example .example .column .icon {
        opacity: 1;
        height: 1em;
        display: block;
        margin: 0em auto 0.25em;
        font-size: 2em!important;
        -webkit-transition: color 0.6s ease, transform 0.2s ease;
        -moz-transition: color 0.6s ease, transform 0.2s ease;
        -o-transition: color 0.6s ease, transform 0.2s ease;
        -ms-transition: color 0.6s ease, transform 0.2s ease;
        transition: color 0.6s ease, transform 0.2s ease;
        box-shadow: none;
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

</laser:htmlStart> %{-- </head><body>--}%


%{-- help sidebar --}%
<laser:render template="/templates/help/help_subscription_show"/>
<section id="example">
    %{-- COLORS  --}%
    <laser:render template="frontend_colors" />
    %{-- ICONS  --}%
    <laser:render template="frontend_icons" />
    %{-- DROPDOWNS --}%
    <laser:render template="frontend_dropdowns" />
    %{-- DECKSAVER --}%
    <laser:render template="frontend_decksaver" />
    %{-- TOGGLE BUTTONS  --}%
    <laser:render template="frontend_toggleButtons" />
    %{-- MODALS  --}%
    <laser:render template="frontend_modals" />
    %{-- CONFIRMATION MODAL  --}%
    <laser:render template="frontend_confirmationModals" />
    %{-- RESPONSIBLE TABELLEN  --}%
    <laser:render template="frontend_copyFunction" />
    %{-- Kopierfunktion  --}%
    <laser:render template="frontend_responsibleTable" />
    %{-- LABELS --}%
    <laser:render template="frontend_labels" />
    %{-- FLYOUT --}%
    <laser:render template="frontend_flyout" />
</section>
%{-- OFFENE FRAGEN WCAG  --}%
<laser:render template="frontend_wcag" />


<laser:htmlEnd /> %{-- </body></html> --}%
