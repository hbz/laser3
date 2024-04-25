<laser:serviceInjection/>

<ui:modal modalSize="${modalSize}" id="${modalID}" text="${modalText}" refreshModal="true" hideSubmitButton="true">

    <g:form action="${exportAction}" controller="${exportController}" params="${exportParams}">
        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields,
                                                                                    filterFields: filterFields,
                                                                                    exportFileName: exportFileName,
                                                                                    contactSwitch: contactSwitch,
                                                                                    csvFieldSeparator: csvFieldSeparator,
                                                                                    orgSwitch: orgSwitch,
                                                                                    accessPointNotice: accessPointNotice,
                                                                                    currentTabNotice: currentTabNotice,
                                                                                    overrideFormat: overrideFormat,
                                                                                    showClickMeConfigSave: showClickMeConfigSave]}"/>

    </g:form>

</ui:modal>