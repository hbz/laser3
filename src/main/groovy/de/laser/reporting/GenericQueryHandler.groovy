package de.laser.reporting

import de.laser.Org

class GenericQueryHandler {

    static String NO_DATA_LABEL = '- keine Angabe -'

    static void handleNonMatchingData(String query, String hql, List idList, Map<String, Object> result) {

        List noDataList = Org.executeQuery( hql, [idList: idList] )

        if (noDataList) {
            result.data.add( [null, NO_DATA_LABEL, noDataList.size()] )

            result.dataDetails.add( [
                    query:  query,
                    id:     null,
                    label:  NO_DATA_LABEL,
                    idList: noDataList
            ])
        }
    }
}
