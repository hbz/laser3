package de.laser.reporting.export.local

import de.laser.Identifier
import de.laser.IssueEntitlement
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.GenericHelper
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class IssueEntitlementExport extends BaseDetailsExport {

    static String KEY = 'entitlement'

    static Map<String, Object> CONFIG_X = [

            base : [
                    meta : [
                            class: IssueEntitlement
                    ],
                    fields : [
                            default: [
                                    'globalUID'                           : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '@-entitlement-tippName'              : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippTitleType'         : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'medium'                              : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'status'                              : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    '@-entitlement-tippFirstAuthor'       : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippEditionStatement'  : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippPublisherName'     : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippSeriesName'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippFirstEditor'       : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippOpenAccessX'       : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippDeweyDecimalClassification' : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippLanguage'          : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippSubjectReference'  : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippProvider+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_COMBINATION ],
                                    '@-entitlement-tippPackage'           : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippPlatform'          : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippHostPlatformURL'   : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-tippIdentifier'        : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-entitlement-priceItem'             : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ]
                            ]
                    ]
            ]
    ]

    IssueEntitlementExport(String token, Map<String, Object> fields) {
        init(token, fields)
    }

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()

        IssueEntitlement ie = obj as IssueEntitlement
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {

                if (key == 'globalUID') {
                    content.add( g.createLink( controller: 'issueEntitlement', action: 'show', absolute: true ) + '/' + ie.getProperty(key) + '@' + ie.getProperty(key) )
                }
                else {
                    content.add( getPropertyContent(ie, key, IssueEntitlement.getDeclaredField(key).getType()))
                }
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(ie, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(ie, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

                if (key == '@-entitlement-priceItem') {
                    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

                    List<String> piList = ie.priceItems.collect{ pi ->
                        String list  = pi.listPrice ? (pi.listPrice + (pi.listCurrency ? ' ' + pi.listCurrency: ' ?')) : null
                        String local = pi.localPrice ? (pi.localPrice + (pi.localCurrency ? ' ' + pi.localCurrency: ' ?')) : null
                        String dates = (pi.startDate || pi.endDate) ? ((pi.startDate ? sdf.format(pi.startDate) : '?') + ' - ' + (pi.endDate ? sdf.format(pi.endDate) : '?')) : null
                        [list, local, dates].findAll().join(', ')
                    }
                    content.add( piList ? piList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) : '' )
                }
                else if (key == '@-entitlement-tippName') {
                    content.add( ie.name ?: '' )
                }
                else if (key == '@-entitlement-tippDeweyDecimalClassification') {
                    List<String> ddcList = ie.tipp.ddcs.collect { ddc ->
                        ddc.ddc.value + ': ' + ddc.ddc.getI10n("value")
                    }
                    content.add( ddcList ? ddcList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) : '' )
                }
                else if (key == '@-entitlement-tippEditionStatement') {
                    content.add( ie.tipp.editionStatement ?: '' )
                }
                else if (key == '@-entitlement-tippFirstAuthor') {
                    content.add( ie.tipp.firstAuthor ?: '' )
                }
                else if (key == '@-entitlement-tippFirstEditor') {
                    content.add( ie.tipp.firstEditor ?: '' )
                }
                else if (key == '@-entitlement-tippHostPlatformURL') {
                    content.add( ie.tipp.hostPlatformURL ?: '' )
                }
                else if (key == '@-entitlement-tippIdentifier') {
                    List<Identifier> ids = []

                    if (f.value) {
                        ids = Identifier.executeQuery( "select i from Identifier i where i.value != null and i.value != '' and i.tipp = :tipp and i.ns.id in (:idnsList)",
                                [tipp: ie.tipp, idnsList: f.value] )
                    }
                    content.add( ids.collect{ (it.ns.getI10n('name') ?: GenericHelper.flagUnmatched( it.ns.ns )) + ':' + it.value }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ))
                }
                else if (key == '@-entitlement-tippSubjectReference') {
                    content.add( ie.tipp.subjectReference ?: '' )
                }
                else if (key == '@-entitlement-tippLanguage') {
                    List<String> lList = ie.tipp.languages.collect{ l ->
                        l.language.getI10n("value")
                    }
                    content.add( lList ? lList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) : '' )
                }
                else if (key == '@-entitlement-tippOpenAccessX') {
                    String doa = ie.tipp.delayedOA?.getI10n("value")
                    String hoa = ie.tipp.hybridOA?.getI10n("value")

                    content.add( doa && hoa ? (doa + ' / ' + hoa) : (doa ?: '') + (hoa ?: '')  )
                }
                else if (key == '@-entitlement-tippPackage') {
                    content.add( ie.tipp.pkg?.name ?: '' )
                }
                else if (key == '@-entitlement-tippPlatform') {
                    content.add( ie.tipp.platform?.name ?: '' )
                }
//                else if (key == '@-entitlement-tippProvider') {
//                    List<String> pList = ie.tipp.getPublishers().collect{ p -> // ??? publisher != provider
//                        p.name
//                    }
//                    content.add( pList ? pList.join( CSV_VALUE_SEPARATOR ) : '' )
//                }
                else if (key == '@-entitlement-tippPublisherName') {
                    content.add( ie.tipp.publisherName ?: '' )
                }
                else if (key == '@-entitlement-tippSeriesName') {
                    content.add( ie.tipp.seriesName ?: '' )
                }
                else if (key == '@-entitlement-tippSubjectReference') {
                    content.add( ie.tipp.subjectReference ?: '' )
                }
                else if (key == '@-entitlement-tippTitleType') {
                    content.add( ie.tipp.titleType ?: '' )
                }
            }
            // --> combined properties : TODO
            else if (key in ['@-entitlement-tippProvider+sortname', '@-entitlement-tippProvider+name']) {
                String prop = key.split('\\+')[1]
                List<String> pList = ie.tipp.getPublishers().collect{ p -> // ??? publisher != provider
                    p.getProperty(prop) as String
                }
                content.add( pList ? pList.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) : '' )
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
