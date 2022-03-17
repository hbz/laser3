package de.laser

import de.laser.helper.AppUtils
import de.laser.titles.TitleInstance
import de.laser.helper.ConfigUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

/**
 * Maybe we should collect bulk operations here??
 * @deprecated they are usually done with DBM changesets
 */
@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class MigrationsController {

    static boolean ftupdate_running = false

    @Deprecated
    @Secured(['ROLE_YODA'])
    @Transactional
    def erms2362() {
        List<String> validNsList = ['acs', 'ebookcentral', 'emerald', 'herdt', 'mitpress', 'oup']

        Closure pre = { tmp ->
            "<pre>" + tmp + "</pre>"
        }
        Closure code = { tmp ->
            '<code>' + tmp.join('<br>') + '</code>'
        }

        String result       = ''
        String ns_str       = params.ns ?: null
        String ns_old_str   = ns_str ? 'inid_' + ns_str : null

        result += '<ul>'
        validNsList.each { vns ->
            result += '<li><a href="' + AppUtils.getConfig('grails.serverURL') + '/migrations/erms2362?ns=' + vns + '">' + vns + '</a></li>'
        }
        result += '</ul>'
        result += '<hl/>'

        if (! validNsList.contains(ns_str)) {
            render text: '<!DOCTYPE html><html lang="en"><head><title>' +
                    AppUtils.getConfig('grails.serverURL') +
                    '</title></head><body><pre>Namespace <strong>' + ns_str + '</strong> is not positive listed .. process canceled</pre></body></html>'

            return
        }

        long ts = System.currentTimeMillis()

        log.debug('erms2362() processing: ' + ns_old_str + ' <-- ' + ns_str)

        IdentifierNamespace oldNs = IdentifierNamespace.findByNs(ns_old_str)
        IdentifierNamespace newNs = IdentifierNamespace.findByNs(ns_str)

        if (oldNs && newNs) {
            Map<String, Object> crs = calcErms2362(oldNs, newNs)

            List matches  = crs.matches
            List orphaned = crs.orphaned

            // frontend

            result += "<h2>NS:${oldNs.id} <strong>${oldNs.ns}</strong> << NS:${newNs.id} <strong>${newNs.ns}</strong></h2>"
            result += pre("${matches.size() + orphaned.size()} identifier/s in old namespace found")
            result += pre("${matches.size()} matched, ${orphaned.size()} orphaned")

            if (params.cmd == 'execute') {
                result += '<p style="color:red">MIGRATION EXECUTED, RELOAD PAGE TO REFRESH</p>'
            }

            if (! matches.isEmpty()) {
                String[] m = []
                matches.each { c ->
                    String tmp = "([ NS:${c.oldNs.id} <strong>${c.oldNs.ns}</strong> / ID:${c.oldId.id} <strong>${c.oldId.value}</strong> ]"
                    tmp += " <strong><<</strong> " + (c.newIds.collect{ it ->
                        "[ NS:${c.newNs.id} <strong>${c.newNs.ns}</strong> / ID:${it.id} <strong>${it.value}</strong> ]"
                    }).join(", ")
                    tmp += ")"

                    if( c.link) {
                        tmp += " =-----> [ <a href='${c.link}' target='_blank'>${c.link.replace(AppUtils.getConfig('grails.serverURL'),'')}</a> ]"
                    }
                    m += tmp
                }
                result += pre('--- Matches:')
                result += code(m)
            }

            if (! orphaned.isEmpty()) {
                String[] o = []
                orphaned.each { c ->
                    String tmp = "[ NS:${c.oldNs.id} <strong>${c.oldNs.ns}</strong> / ID:${c.oldId.id} <strong>${c.oldId.value}</strong> ]"
                    tmp += " =-----> [ ${c.reference.type}:${c.reference.id} - "

                    if (c.reference.gokbId) {
                        tmp += "GOKBID:${c.reference.gokbId} - "
                    }
                    if (c.reference.link) {
                        tmp += "<a href='${c.reference.link}' target='_blank'>${c.reference.link.replace(AppUtils.getConfig('grails.serverURL'),'')}</a> ]"
                    }
                    o += tmp
                }
                result += pre('--- Orphans:')
                result += code(o)
            }
            result += '<br/><br/>'

            log.debug( 'matches: ' + matches )
            log.debug( 'orphaned: ' + orphaned )

            // processing

            if (params.cmd == 'execute') {
                log.debug( 'execute ..')
                if (! matches.isEmpty()) {
                    matches.each { c ->
                        ((Identifier) c.oldId).delete()
                    }
                }
                if (! orphaned.isEmpty()) {
                    orphaned.each { c ->
                        ((Identifier) c.oldId).setNs(c.newNs)
                        ((Identifier) c.oldId).save()
                    }
                }
            }
        }
        else {
            result += oldNs ? '' : pre('<strong>ERROR</strong>: old namespace <strong>' + ns_old_str + '</strong> not found')
            result += newNs ? '' : pre('<strong>ERROR</strong>: new namespace <strong>' + ns_str + '</strong> not found')
        }

        result += code([
                "# ${AppUtils.getConfig('grails.serverURL')}",
                "# " + ConfigUtils.getLaserSystemId(),
                "# Processing time: ${System.currentTimeMillis() - ts} ms"
        ])

        render text: '<!DOCTYPE html><html lang="en"><head><title>' + AppUtils.getConfig('grails.serverURL') + '</title></head><body>' + result + '</body></html>'
    }

    @Deprecated
    private Map<String, Object> calcErms2362(IdentifierNamespace oldNs, IdentifierNamespace newNs) {

        Closure href = { reference ->
            String link = reference instanceof TitleInstance ? '/title/show/' : (
                    reference instanceof License ? '/lic/show/' : (
                            reference instanceof Subscription ? '/subscription/show/' : (
                                    reference instanceof Package ? '/package/show/' : (
                                            reference instanceof Org ? '/org/show/' : (
                                                    null
                                            )))))

            return link ? (AppUtils.getConfig('grails.serverURL') + link + reference.id) : null
        }

        List<Identifier> oldIds = Identifier.findAllByNs(oldNs)
        List matches = []
        List orphaned = []

        oldIds.eachWithIndex { old, i ->
            log.debug('processing (' + (i + 1) + ' from ' + oldIds.size() + '): ' + old)

            List<Identifier> newIds = Identifier.findAllWhere([
                    value: old.value,
                    ns   : newNs,
                    lic  : old.lic,
                    org  : old.org,
                    pkg  : old.pkg,
                    sub  : old.sub,
                    ti   : old.ti,
                    tipp : old.tipp
            ])
            def reference = old.getReference()

            if (!newIds.isEmpty()) {
                matches.add([
                        oldNs : oldNs,
                        oldId : old,
                        newNs : newNs,
                        newIds: newIds,
                        link  : href(reference)
                ])
            } else {
                orphaned.add([
                        oldNs    : oldNs,
                        oldId    : old,
                        newNs    : newNs,
                        reference: [
                                id    : reference.id,
                                gokbId: reference.hasProperty('gokbId') ? reference.gokbId : null,
                                type  : reference.getClass()?.canonicalName,
                                link  : href(reference)
                        ]
                ])
            }
        }

        return [ matches: matches, orphaned: orphaned ]
    }

}
