<%@ page import="de.laser.storage.BeanStore" %>

<div class="ui tablet computer only padded grid ">
    <div class="ui secondary top ${BeanStore.getSpringSecurityService().isLoggedIn() ? '':'fixed'}  fluid menu">
        <div class="ui container" role="none">
            <a class="header item">
                <img class="ui fluid image" alt="${message(code:'reporting.ui.global.help')}" src="${resource(dir: 'media', file: 'gasco/gasco-small.png')}"/>
            </a>
            <nav class="right menu" id="GASCOMenue">
                <g:link controller="gasco" action="about" class="item${actionName == 'about' ? ' active' : ''}">Über uns</g:link>
                <g:link controller="gasco" action="monitor" class="item${actionName in ['monitor', 'monitorDetails'] ? ' active' : ''}">GASCO-Monitor</g:link>
                <g:link controller="gasco" action="members" class="item${actionName == 'members' ? ' active' : ''}">GASCO-Mitglieder</g:link>
            </nav>
        </div>
    </div>
</div>
<div class="ui mobile only padded grid">
    <div class="ui top fixed borderless fluid inverted menu">
        <a class="header item">
            <img class="ui fluid image" alt="${message(code:'reporting.ui.global.help')}" src="${resource(dir: 'media', file: 'gasco/gasco-small.png')}"/>
        </a>
        <div class="right menu">
            <div class="item">
                <button class="ui icon toggle basic inverted button">
                    <i class="content icon"></i>
                </button>
            </div>
        </div>
        <div class="ui vertical borderless inverted fluid menu" >
            <g:link controller="gasco" action="about" class="item${actionName == 'about' ? ' active' : ''}">Über uns</g:link>
            <g:link controller="gasco" action="monitor" class="item${actionName in ['monitor', 'monitorDetails'] ? ' active' : ''}">GASCO-Monitor</g:link>
            <g:link controller="gasco" action="members" class="item${actionName == 'members' ? ' active' : ''}">GASCO-Mitglieder</g:link>
        </div>
    </div>
</div>
<laser:script file="${this.getGroovyPageFileName()}">
    $(document).ready(function() {
        $(".ui.toggle.button").click(function() {
            $(".mobile.only.grid .ui.vertical.menu").toggle(100);
        });
    });
</laser:script>
<style>
.ui.mobile.only.grid .ui.menu .ui.vertical.menu {
    display: none;
}
</style>