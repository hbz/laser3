package de.laser.reporting.report.myInstitution

import de.laser.Org
import de.laser.Package
import de.laser.RefdataValue
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap

class PackageFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (pkg.id) from Package pkg']
        List<String> whereParts         = [ 'where pkg.id in (:packageIdList)']
        Map<String, Object> queryParams = [ packageIdList: [] ]

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'package' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: BaseConfig.getMessage(BaseConfig.KEY_PACKAGE + '.source.' + filterSource)])

        switch (filterSource) {
            case 'all-pkg':
                queryParams.packageIdList = Package.executeQuery( 'select pkg.id from Package pkg' )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'package_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'pkg.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES) {
                            whereParts.add( 'pkg.' + p + ' is true' )
                        }
                        else if (rdv == RDStore.YN_NO) {
                            whereParts.add( 'pkg.' + p + ' is false' )
                        }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'pkg.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    println ' ------------ not implemented ------------ '
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'PackageFilter.filter() -->'
//
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('packageIdList', Package.executeQuery( query, queryParams ))

//        println 'packages (raw) >> ' + queryParams.packageIdList.size()
//        println 'packages (queried) >> ' + filterResult.data.packageIdList.size()

        filterResult
    }
}
