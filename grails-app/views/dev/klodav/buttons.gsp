<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New Icons" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="icons" class="item"><i class="certificate icon red"></i> New Icons</g:link>
%{--    <g:link controller="dev" action="buttons" class="item active"><i class="certificate icon orange"></i> New Buttons</g:link>--}%
</nav>

<div class="ui info message">
    <p class="ui header">
        Usage
        <button class="${Btn.MODERN.SIMPLE_ICON} blue" style="float: right"><i class="${Icon.SYM.UNKOWN}"></i></button>
    </p>
    <pre>&lt;button class=&quot;&dollar;{Btn.MODERN.BASIC_ICON} blue&quot;&gt;&lt;i class=&quot;&dollar;{Icon.SYM.UNKOWN}&quot;&gt;&lt;/i&gt;&lt;/button&gt;</pre>
</div>

<div class="ui segment">

    <p class="ui header">de.laser.ui.Btn</p>

    <p>
        <button class="${Btn.SIMPLE}">BASIC</button>
        <button class="${Btn.PRIMARY}">PRIMARY</button>
        <button class="${Btn.SECONDARY}">SECONDARY</button>
        <button class="${Btn.POSITIVE}">POSITIVE</button>
        <button class="${Btn.NEGATIVE}">NEGATIVE</button>
    </p>
    <p>
        <button class="${Btn.SIMPLE_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Btn.PRIMARY_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Btn.SECONDARY_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Btn.POSITIVE_ICON}"><i class="${Icon.CMD.ADD}"></i></button>
        <button class="${Btn.NEGATIVE_ICON}"><i class="${Icon.CMD.REMOVE}"></i></button>
    </p>
    <p>
        <button class="${Btn.MODERN.SIMPLE}">MODERN.SIMPLE</button>
        <button class="${Btn.MODERN.SIMPLE} blue">MODERN.SIMPLE + blue</button>
        <button class="${Btn.MODERN.POSITIVE}">MODERN.POSITIVE</button>
        <button class="${Btn.MODERN.NEGATIVE}">MODERN.NEGATIVE</button>
    </p>
    <p>
        <button class="${Btn.MODERN.SIMPLE_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Btn.MODERN.SIMPLE_ICON} blue"><i class="${Icon.UNC.SQUARE}"></i> + blue</button>
        <button class="${Btn.MODERN.POSITIVE_ICON}"><i class="${Icon.CMD.ADD}"></i></button>
        <button class="${Btn.MODERN.NEGATIVE_ICON}"><i class="${Icon.CMD.REMOVE}"></i></button>
    </p>
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
