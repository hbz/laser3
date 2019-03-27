package com.k_int.kbplus

import grails.util.Holders

public class GokbDiffEngine {

    def static diff(ctx, oldpkg, newpkg, newTippClosure, updatedTippClosure, deletedTippClosure, pkgPropChangeClosure, tippUnchangedClosure, auto_accept) {

        if ((oldpkg == null) || (newpkg == null)) {
            println("Error - null package passed to diff");
            return
        }

        if (oldpkg.packageName != newpkg.packageName && oldpkg.packageName != null) {
            // println("packageName updated from ${oldpkg.packageName} to ${newpkg.packageName}");
            pkgPropChangeClosure(ctx, 'title', newpkg.packageName, true);
        } else {
            // println("packageName consistent");
        }

        if (oldpkg.packageId != newpkg.packageId) {
            // println("packageId updated from ${oldpkg.packageId} to ${newpkg.packageId}");
        } else {
            // println("packageId consistent");
        }

        def primaryUrl = (oldpkg?.nominalPlatformPrimaryUrl == newpkg?.nominalPlatformPrimaryUrl) ? oldpkg?.nominalPlatformPrimaryUrl : newpkg?.nominalPlatformPrimaryUrl

        //TODO: Umstellung auf UUID vielleicht
        oldpkg.tipps.sort { it.tippId }
        newpkg.tipps.sort { it.tippId }

        def ai = oldpkg.tipps.iterator();
        def bi = newpkg.tipps.iterator();

        def tippa = ai.hasNext() ? ai.next() : null
        def tippb = bi.hasNext() ? bi.next() : null

        while (tippa != null || tippb != null) {

            replaceImpIDwithGokbID(ctx, tippb, primaryUrl)

            if (tippa != null && tippb != null &&
                    (tippa.tippId == tippb.tippId ||
                            (tippa.tippUuid && tippb.tippUuid && tippa.tippUuid == tippb.tippUuid)
                    )
            ) {

                def tipp_diff = getTippDiff(tippa, tippb)

                if (tippb.status != 'Current' && tipp_diff.size() == 0) {
                    deletedTippClosure(ctx, tippa, auto_accept)
                    System.out.println("Title " + tippa + " Was removed from the package");

                    tippa = ai.hasNext() ? ai.next() : null;
                    tippb = bi.hasNext() ? bi.next() : null
                } else if (tipp_diff.size() == 0) {
                    tippUnchangedClosure(ctx, tippa);

                    tippa = ai.hasNext() ? ai.next() : null
                    tippb = bi.hasNext() ? bi.next() : null
                } else {
                    // See if any of the actual properties are null
                    println("Got tipp diffs: ${tipp_diff}");
                    updatedTippClosure(ctx, tippb, tippa, tipp_diff, auto_accept)

                    tippa = ai.hasNext() ? ai.next() : null
                    tippb = bi.hasNext() ? bi.next() : null
                }
            } else if ((tippb != null) && (tippa == null)) {
                System.out.println("TIPP " + tippb + " Was added to the package");
                newTippClosure(ctx, tippb, auto_accept)
                tippb = bi.hasNext() ? bi.next() : null;
            } else {
                deletedTippClosure(ctx, tippa, auto_accept)
                System.out.println("TIPP " + tippa + " Was removed from the package");
                tippa = ai.hasNext() ? ai.next() : null;
            }
        }

    }

    def static getTippDiff(tippa, tippb) {
        def result = []

        if ((tippa.url ?: '').toString().compareTo((tippb.url ?: '').toString()) == 0) {
        } else {
            result.add([field: 'hostPlatformURL', newValue: tippb.url, oldValue: tippa.url])
        }

        if ((tippa.coverage ?: '').toString().compareTo((tippb.coverage ?: '').toString()) == 0) {
        } else {
            result.add([field: 'coverage', newValue: tippb.coverage, oldValue: tippa.coverage])
        }

        if ((tippa.accessStart ?: '').toString().compareTo((tippb.accessStart ?: '').toString()) == 0) {
        } else {
            result.add([field: 'accessStart', newValue: tippb.accessStart, oldValue: tippa.accessStart])
        }

        if ((tippa.accessEnd ?: '').toString().compareTo((tippb.accessEnd ?: '').toString()) == 0) {
        } else {
            result.add([field: 'accessEnd', newValue: tippb.accessEnd, oldValue: tippa.accessEnd])
        }

        if ((tippa?.title?.name ?: '').toString().compareTo((tippb?.title?.name ?: '').toString()) == 0) {
        } else {
            result.add([field: 'titleName', newValue: tippb?.title?.name, oldValue: tippa?.title?.name])
        }

        result;
    }

    def static replaceImpIDwithGokbID(pkg, newTipp, primaryUrl) {

        //Replace ImpID with GokbID
        if (newTipp) {
            def db_tipp = null

            if (newTipp?.tippUuid) {
                db_tipp = pkg.tipps.find { it.gokbId == newTipp?.tippUuid }
            }
            if (!db_tipp) {
                db_tipp = pkg.tipps.find { it.impId == newTipp?.tippUuid }
            }
            if (db_tipp) {
                if (Holders.config.globalDataSync.replaceLocalImpIds.TIPP && newTipp.tippUuid && db_tipp.gokbId != newTipp.tippUuid) {
                    db_tipp.impId = (db_tipp.impId == newTipp.tippUuid) ? db_tipp.impId : newTipp.tippUuid
                    db_tipp.gokbId = newTipp.tippUuid
                    db_tipp.save(flush: true, failOnError: true)
                }
            }

            def plat_instance = Platform.lookupOrCreatePlatform([name: newTipp.platform, gokbId: newTipp.platformUuid, primaryUrl: primaryUrl]);
            if(plat_instance) {
                plat_instance.primaryUrl = (plat_instance?.primaryUrl == primaryUrl) ? plat_instance?.primaryUrl : primaryUrl
                plat_instance.save(flush: true)
            }

            def title_of_tipp_to_update = TitleInstance.findByGokbId(newTipp.title.gokbId)
            if (!title_of_tipp_to_update) {
                title_of_tipp_to_update = TitleInstance.lookupOrCreate(newTipp.title.identifiers, newTipp.title.name, newTipp.title.titleType, newTipp.title.gokbId)
            }

            if (Holders.config.globalDataSync.replaceLocalImpIds.TitleInstance && title_of_tipp_to_update && newTipp.title.gokbId &&
                    (title_of_tipp_to_update?.gokbId != newTipp.title.gokbId || !title_of_tipp_to_update?.gokbId)) {
                title_of_tipp_to_update.impId = (title_of_tipp_to_update.impId == newTipp.title.gokbId) ? title_of_tipp_to_update.impId : newTipp.title.gokbId
                title_of_tipp_to_update.gokbId = newTipp.title.gokbId
                title_of_tipp_to_update.save(flush: true)
            }

        }

    }

}
