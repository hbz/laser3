<laser:htmlStart text="${message(code:'menu.datamanager')} ${message(code:'default.dashboard')}" />

    <ui:breadcrumbs>
      <ui:crumb message="menu.datamanager" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.datamanager" type="datamanager" />

    <br />
    <br />

    <ui:messages data="${flash}" />

<laser:htmlEnd />
