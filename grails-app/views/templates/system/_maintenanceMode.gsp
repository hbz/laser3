<%@ page import="de.laser.system.SystemSetting;"%>

<div id="systemMaintenanceMode" class="${SystemSetting.findByName('MaintenanceMode').value != 'true' ? 'hidden' : ''}">
    <div class="ui segment center aligned inverted yellow">
        <h3 class="ui header"><i class="icon cogs"></i> ${message(code:'system.maintenanceMode.header')}</h3>
        ${message(code:'system.maintenanceMode.message')}
    </div>
</div>
