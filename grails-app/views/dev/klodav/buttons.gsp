<%@ page import="de.laser.ui.Button; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
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
        <button class="${Button.MODERN.BLUE_ICON}" style="float: right"><i class="${Icon.SYM.UNKOWN}"></i></button>
    </p>
    <pre>&lt;button class=&quot;&dollar;{Button.MODERN.BLUE_ICON}&quot;&gt;&lt;i class=&quot;&dollar;{Icon.SYM.UNKOWN}&quot;&gt;&lt;/i&gt;&lt;/button&gt;</pre>
</div>

<div class="ui segment">

    <p class="ui header">de.laser.ui.Button</p>

    <p>
        <button class="${Button.BASIC}">BASIC</button>
        <button class="${Button.PRIMARY}">PRIMARY</button>
        <button class="${Button.SECONDARY}">SECONDARY</button>
        <button class="${Button.POSITIVE}">POSITIVE</button>
        <button class="${Button.NEGATIVE}">NEGATIVE</button>
    </p>
    <p>
        <button class="${Button.BASIC_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Button.PRIMARY_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Button.SECONDARY_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Button.POSITIVE_ICON}"><i class="${Icon.CMD.ADD}"></i></button>
        <button class="${Button.NEGATIVE_ICON}"><i class="${Icon.CMD.REMOVE}"></i></button>
    </p>
    <p>
        <button class="${Button.MODERN.BLUE}">MODERN.BLUE</button>
        <button class="${Button.MODERN.POSITIVE}">MODERN.POSITIVE</button>
        <button class="${Button.MODERN.NEGATIVE}">MODERN.NEGATIVE</button>
    </p>
    <p>
        <button class="${Button.MODERN.BLUE_ICON}"><i class="${Icon.UNC.SQUARE}"></i></button>
        <button class="${Button.MODERN.POSITIVE_ICON}"><i class="${Icon.CMD.ADD}"></i></button>
        <button class="${Button.MODERN.NEGATIVE_ICON}"><i class="${Icon.CMD.REMOVE}"></i></button>
    </p>
</div>


%{--<div class="ui basic segment">--}%
%{--    <p class="ui header">de.laser.ui.Button</p>--}%
%{--    <div class="ui five cards">--}%
%{--        <g:each in="${Button.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f" status="i">--}%
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

%{--<g:each in="${Button.getDeclaredClasses().findAll{ true }}" var="btn">--}%
%{--    <div class="ui basic segment">--}%
%{--        <p class="ui header">${btn.name.replace(Button.name + '$', 'Button.')}</p>--}%
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
