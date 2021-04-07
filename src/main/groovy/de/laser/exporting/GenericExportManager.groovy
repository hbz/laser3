package de.laser.exporting

class GenericExportManager {

    void doExport(GenericExportConfig config, List<Long> idList) {

        List<String> rows = []
        Map<String, Object> fields = config.getCurrentConfig()

        idList.each { Long id ->
            List<String> row = []

            if (config.KEY == 'license') {
                row = config.exportLicense( id, fields)
            }
            else if (config.KEY == 'organisation') {
                row = config.exportOrganisation( id, fields)
            }
            else if (config.KEY == 'subscription') {
                row = config.exportSubscription( id, fields)
            }

            if (row) {
                rows.add( buildRow( row ) )
            }
        }

        rows.each { row ->
            println row
        }
    }

    String buildRow(List<String> content) {
        content.join(', ')
    }
}
