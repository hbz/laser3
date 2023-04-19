<laser:serviceInjection />

<a href="#systemInfoFlyout" id="showSystemInfo" role="dialog" aria-label="System Info" class="ui secondary button icon">
    <i aria-hidden="true" class="exclamation triangle icon"></i>
</a>

<div id="systemInfoFlyout" class="ui eight wide flyout" style="padding:50px 0 10px 0;overflow:scroll">
    <div class="ui header">
        <i class="exclamation triangle icon"></i>
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
