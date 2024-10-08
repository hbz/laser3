<%@ page import="de.laser.ui.Btn" %>
<laser:serviceInjection />

<a href="#systemInfoFlyout" id="showSystemInfo" role="button" aria-label="System Info" class="${Btn.ICON.SECONDARY} la-debugInfos">
    <i aria-hidden="true" class="tools icon"></i>
</a>

<div id="systemInfoFlyout" class="ui eight wide flyout">
    <div class="ui header">
        <i class="tools icon"></i>
        <div class="content">SYSTEM INFORMATION</div>
    </div>
    <div class="content">
        <div class="ui list">
            <g:each in="${systemService.serviceCheck()}" var="systemCheck">
                <div class="item">
                    <strong>${systemCheck.key}</strong>: ${systemCheck.value}
                </div>
            </g:each>
        </div>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#showSystemInfo').on('click', function(e) {
        e.preventDefault();
        $('#systemInfoFlyout').flyout('toggle');
    });
</laser:script>
