<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New Icons" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="icons" class="item"><i class="certificate icon yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="buttons" class="item active"><i class="certificate icon red"></i> New Buttons</g:link>
</nav>

<div class="ui info message">
    <p class="ui header">
        Usage
        <button class="${Btn.MODERN.BASIC_ICON} blue" style="float: right"><i class="${Icon.SYM.UNKOWN}"></i></button>
    </p>
    <pre>&lt;button class=&quot;&dollar;{Btn.MODERN.BASIC_ICON} blue&quot;&gt;&lt;i class=&quot;&dollar;{Icon.SYM.UNKOWN}&quot;&gt;&lt;/i&gt;&lt;/button&gt;</pre>
</div>

<div class="ui segment">

    <div class="ui two column grid">
        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn" hideClose="true"> Text (Icon optional) </ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.SIMPLE} orange">BASIC</button> </td>
                    <td> Btn.SIMPLE <br/> orange </td>
                    <td> ${Btn.SIMPLE} (+ orange) </td>
                    <td> Colors by declaration; default: blue </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.PRIMARY}">PRIMARY</button> </td>
                    <td> Btn.PRIMARY </td>
                    <td> ${Btn.PRIMARY} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.SECONDARY}">SECONDARY</button> </td>
                    <td> Btn.SECONDARY </td>
                    <td> ${Btn.SECONDARY} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.POSITIVE}"><i class="${Icon.CMD.LINKIFY}"></i> POSITIVE</button> </td>
                    <td> Btn.POSITIVE </td>
                    <td> ${Btn.POSITIVE} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.NEGATIVE}"><i class="${Icon.CMD.UNLINK}"></i> NEGATIVE</button> </td>
                    <td> Btn.NEGATIVE </td>
                    <td> ${Btn.NEGATIVE} </td>
                    <td> </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn"  hideClose="true"> Icon only, NO Text </ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.ICON.SIMPLE} yellow"><i class="${Icon.UNC.SQUARE}"></i></button> </td>
                    <td> Btn.ICON.SIMPLE <br/> yellow </td>
                    <td> ${Btn.ICON.SIMPLE} (+ yellow) </td>
                    <td> Colors by declaration; default: blue </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.ICON.PRIMARY}"><i class="${Icon.UNC.SQUARE}"></i></button> </td>
                    <td> Btn.ICON.PRIMARY </td>
                    <td> ${Btn.ICON.PRIMARY} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.ICON.SECONDARY}"><i class="${Icon.UNC.SQUARE}"></i></button> </td>
                    <td> Btn.ICON.SECONDARY </td>
                    <td> ${Btn.ICON.SECONDARY} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.ICON.POSITIVE}"><i class="${Icon.CMD.ADD}"></i></button> </td>
                    <td> Btn.ICON.POSITIVE </td>
                    <td> ${Btn.ICON.POSITIVE} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.ICON.NEGATIVE}"><i class="${Icon.CMD.REMOVE}"></i></button> </td>
                    <td> Btn.ICON.NEGATIVE </td>
                    <td> ${Btn.ICON.NEGATIVE} </td>
                    <td> </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="ui one column grid">
        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn"  hideClose="true"> Text (Icon optional) + Javascript </ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.POSITIVE_CONFIRM}" data-confirm-term-how="ok"><i class="${Icon.CMD.ADD}"></i> POSITIVE_CONFIRM</button> </td>
                    <td> Btn.POSITIVE_CONFIRM </td>
                    <td> ${Btn.POSITIVE_CONFIRM} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.NEGATIVE_CONFIRM}" data-confirm-term-how="delete"><i class="${Icon.CMD.DELETE}"></i> NEGATIVE_CONFIRM</button> </td>
                    <td> Btn.NEGATIVE_CONFIRM </td>
                    <td> ${Btn.NEGATIVE_CONFIRM} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.SIMPLE_CLICKCONTROL}">SIMPLE_CLICKCONTROL</button> </td>
                    <td> Btn.SIMPLE_CLICKCONTROL </td>
                    <td> ${Btn.SIMPLE_CLICKCONTROL} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.NEGATIVE_CLICKCONTROL}">NEGATIVE_CLICKCONTROL</button> </td>
                    <td> Btn.NEGATIVE_CLICKCONTROL </td>
                    <td> ${Btn.NEGATIVE_CLICKCONTROL} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.SIMPLE_TOOLTIP}" data-content="Something to know ..">SIMPLE_TOOLTIP</button> </td>
                    <td> Btn.SIMPLE_TOOLTIP </td>
                    <td> ${Btn.SIMPLE_TOOLTIP} </td>
                    <td> Tooltip (data-attributes needed) </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="ui one column grid">
        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn" hideClose="true"> Icon only, NO Text  + Javascript </ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.ICON.NEGATIVE_CONFIRM}" data-confirm-term-how="delete"><i class="${Icon.CMD.DELETE}"></i></button> </td>
                    <td> Btn.ICON.NEGATIVE_CONFIRM </td>
                    <td> ${Btn.ICON.NEGATIVE_CONFIRM} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.ICON.SIMPLE_TOOLTIP}" data-content="Something to know .."><i class="${Icon.CMD.DOWNLOAD}"></i></button> </td>
                    <td> Btn.ICON.SIMPLE_TOOLTIP </td>
                    <td> ${Btn.ICON.SIMPLE_TOOLTIP} </td>
                    <td> Tooltip (data-attributes needed) </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="ui segment">

    <div class="ui one column grid">
        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn.MODERN" hideClose="true"> Icon only, NO Text </ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.MODERN.BASIC_ICON} orange"><i class="${Icon.CMD.REPLACE}"></i></button> </td>
                    <td> Btn.MODERN.BASIC_ICON <br/> orange </td>
                    <td> ${Btn.MODERN.BASIC_ICON} (+ orange) </td>
                    <td> Colors by declaration; default: blue (PROBLEM) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.SIMPLE_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button> </td>
                    <td> Btn.MODERN.SIMPLE_ICON </td>
                    <td> ${Btn.MODERN.SIMPLE_ICON} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.POSITIVE_ICON}"><i class="${Icon.CMD.ADD}"></i></button> </td>
                    <td> Btn.MODERN.POSITIVE_ICON </td>
                    <td> ${Btn.MODERN.POSITIVE_ICON} </td>
                    <td> </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.NEGATIVE_ICON}"><i class="${Icon.CMD.REMOVE}"></i></button> </td>
                    <td> Btn.MODERN.NEGATIVE_ICON </td>
                    <td> ${Btn.MODERN.NEGATIVE_ICON} </td>
                    <td> </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="ui one column grid">
        <div class="column">
            <ui:msg class="info" header="de.laser.ui.Btn.MODERN" hideClose="true"> Icon only, NO Text + Javascript</ui:msg>

            <table class="ui simple table very compact">
                <thead>
                <tr>
                    <th>UI</th>
                    <th>Usage</th>
                    <th>CSS</th>
                    <th>Info</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td> <button class="${Btn.MODERN.POSITIVE_ICON_CONFIRM}" data-confirm-term-how="ok"><i class="${Icon.CMD.ADD}"></i></button> </td>
                    <td> Btn.MODERN.POSITIVE_ICON_CONFIRM </td>
                    <td> ${Btn.MODERN.POSITIVE_ICON_CONFIRM} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.NEGATIVE_ICON_CONFIRM}" data-confirm-term-how="delete"><i class="${Icon.CMD.DELETE}"></i></button> </td>
                    <td> Btn.MODERN.NEGATIVE_ICON_CONFIRM </td>
                    <td> ${Btn.MODERN.NEGATIVE_ICON_CONFIRM} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.BASIC_ICON_TOOLTIP} yellow" data-content="Something to know .."><i class="${Icon.UI.HELP}"></i></button> </td>
                    <td> Btn.MODERN.BASIC_ICON_TOOLTIP <br/> yellow </td>
                    <td> ${Btn.MODERN.BASIC_ICON_TOOLTIP} (+ yellow) </td>
                    <td> Colors by declaration; default: blue (PROBLEM) <br/> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.SIMPLE_ICON_TOOLTIP}" data-content="Something to know .."><i class="${Icon.UI.INFO}"></i></button> </td>
                    <td> Btn.MODERN.SIMPLE_ICON_TOOLTIP </td>
                    <td> ${Btn.MODERN.SIMPLE_ICON_TOOLTIP} </td>
                    <td> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.POSITIVE_ICON_TOOLTIP}" data-content="Something to know .."><i class="${Icon.CMD.ATTACHMENT}"></i></button> </td>
                    <td> Btn.MODERN.POSITIVE_ICON_TOOLTIP </td>
                    <td> ${Btn.MODERN.POSITIVE_ICON_TOOLTIP} </td>
                    <td> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.NEGATIVE_ICON_TOOLTIP}" data-content="Something to know .."><i class="${Icon.CMD.ERASE}"></i></button> </td>
                    <td> Btn.MODERN.NEGATIVE_ICON_TOOLTIP </td>
                    <td> ${Btn.MODERN.NEGATIVE_ICON_TOOLTIP} </td>
                    <td> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.SIMPLE_ICON_CONFIRM_TOOLTIP}" data-confirm-term-how="ok" data-content="Something to know .."><i class="${Icon.CMD.COPY}"></i></button> </td>
                    <td> Btn.MODERN.SIMPLE_ICON_CONFIRM_TOOLTIP </td>
                    <td> ${Btn.MODERN.SIMPLE_ICON_CONFIRM_TOOLTIP} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) <br/> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.POSITIVE_ICON_CONFIRM_TOOLTIP}" data-confirm-term-how="ok" data-content="Something to know .."><i class="${Icon.CMD.ADD}"></i></button> </td>
                    <td> Btn.MODERN.POSITIVE_ICON_CONFIRM_TOOLTIP </td>
                    <td> ${Btn.MODERN.POSITIVE_ICON_CONFIRM_TOOLTIP} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) <br/> Tooltip (data-attributes needed) </td>
                </tr>
                <tr>
                    <td> <button class="${Btn.MODERN.NEGATIVE_ICON_CONFIRM_TOOLTIP}" data-confirm-term-how="unlink" data-content="Something to know .."><i class="${Icon.CMD.UNLINK}"></i></button> </td>
                    <td> Btn.MODERN.NEGATIVE_ICON_CONFIRM_TOOLTIP </td>
                    <td> ${Btn.MODERN.NEGATIVE_ICON_CONFIRM_TOOLTIP} </td>
                    <td> Confirmation Dialog (data-confirm-attributes needed) <br/> Tooltip (data-attributes needed) </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="column">
        </div>
    </div>
</div>


%{--<div class="ui basic segment">--}%
%{--    <p class="ui header">de.laser.ui.Btn</p>--}%
%{--    <div class="ui five cards">--}%
%{--        <g:each in="${Btn.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f" status="i">--}%
%{--            <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">--}%
%{--                <div class="content">--}%
%{--                    <div class="header">--}%
%{--                        <i class="${Button[f.name]} large"></i>--}%
%{--                        ${f.name.split('\\.').last()}--}%
%{--                    </div>--}%
%{--                    <div class="meta">${Button[f.name]}</div>--}%
%{--                </div>--}%
%{--            </div>--}%
%{--        </g:each>--}%
%{--    </div>--}%
%{--</div>--}%

%{--<g:each in="${Btn.getDeclaredClasses().findAll{ true }}" var="btn">--}%
%{--    <div class="ui basic segment">--}%
%{--        <p class="ui header">${btn.name.replace(Btn.name + '$', 'Btn.')}</p>--}%
%{--        <div class="ui five cards">--}%
%{--            <g:each in="${btn.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f">--}%
%{--                <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">--}%
%{--                    <div class="content">--}%
%{--                        <div class="header">--}%
%{--                            <i class="${btn[f.name]} large"></i>--}%
%{--                            ${f.name.split('\\.').last()}--}%
%{--                        </div>--}%
%{--                        <div class="meta">${btn[f.name]}</div>--}%
%{--                    </div>--}%
%{--                </div>--}%
%{--            </g:each>--}%
%{--        </div>--}%
%{--    </div>--}%
%{--</g:each>--}%

<style>
    #mainContent .ui.segment > .ui.header   { padding-bottom: 1em; }
    #mainContent .ui.card .content .header  { color: #3b3b3b; }
    #mainContent .ui.card.hover             { background-color: #f3f3f3; border-color: #cc2711; }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#mainContent .ui.card').hover(
        function() {
            let dc = $(this).attr('data-cat')
            if (dc) {
                $('#mainContent .ui.card[data-cat=' + dc + ']').addClass('hover')
            }
        },
        function() {
            $('#mainContent .ui.card').removeClass('hover')
        }
    );
</laser:script>

<laser:htmlEnd />
