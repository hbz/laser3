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

        //def primaryUrl = (oldpkg?.nominalPlatformPrimaryUrl == newpkg?.nominalPlatformPrimaryUrl) ? oldpkg?.nominalPlatformPrimaryUrl : newpkg?.nominalPlatformPrimaryUrl

        compareLocalPkgWithGokbPkg(ctx, oldpkg, newpkg, newTippClosure, updatedTippClosure, tippUnchangedClosure,deletedTippClosure, auto_accept)

        /*//TODO: Umstellung auf UUID vielleicht
        oldpkg.tipps.sort { it.tippUuid }
        newpkg.tipps.sort { it.tippUuid }

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
                    println("Got tipp diffs: ${tipp_diff}")
                    try {
                        updatedTippClosure(ctx, tippb, tippa, tipp_diff, auto_accept)
                    }
                    catch (Exception e) {
                        System.err.println("Error on executing updated TIPP closure! Please verify logs:")
                        e.printStackTrace()
                    }

                    tippa = ai.hasNext() ? ai.next() : null
                    tippb = bi.hasNext() ? bi.next() : null
                }
            } else if ((tippb != null) && (tippa == null)) {
                System.out.println("TIPP " + tippb + " Was added to the package");
                newTippClosure(ctx, tippb, auto_accept)
                tippb = bi.hasNext() ? bi.next() : null;
                tippa = ai.hasNext() ? ai.next() : null;
            } else {
                deletedTippClosure(ctx, tippa, auto_accept)
                System.out.println("TIPP " + tippa + " Was removed from the package");
                tippa = ai.hasNext() ? ai.next() : null;
                tippb = bi.hasNext() ? bi.next() : null;
            }
        }*/

    }

    def static getTippDiff(tippa, tippb) {
        def result = []

        if ((tippa.url ?: '').toString().compareTo((tippb.url ?: '').toString()) != 0) {
            result.add([field: 'hostPlatformURL', newValue: tippb.url, oldValue: tippa.url])
        }

        if ((tippa.coverage ?: '').toString().compareTo((tippb.coverage ?: '').toString()) != 0) {
            result.add([field: 'coverage', newValue: tippb.coverage, oldValue: tippa.coverage])
        }

        if ((tippa.accessStart ?: '').toString().compareTo((tippb.accessStart ?: '').toString()) != 0) {
            result.add([field: 'accessStart', newValue: tippb.accessStart, oldValue: tippa.accessStart])
        }

        if ((tippa.accessEnd ?: '').toString().compareTo((tippb.accessEnd ?: '').toString()) != 0) {
            result.add([field: 'accessEnd', newValue: tippb.accessEnd, oldValue: tippa.accessEnd])
        }

        if ((tippa?.title?.name ?: '').toString().compareTo((tippb?.title?.name ?: '').toString()) != 0) {
            result.add([field: 'titleName', newValue: tippb?.title?.name, oldValue: tippa?.title?.name])
        }

        if ((tippa?.title?.name ?: '').toString().compareTo((tippb?.title?.name ?: '').toString()) != 0) {
            result.add([field: 'titleName', newValue: tippb?.title?.name, oldValue: tippa?.title?.name])
        }

        if ((tippa?.platform?.gokbId ?: '').toString().compareTo((tippb?.platform?.gokbId ?: '').toString()) != 0) {
            result.add([field: 'titleName', newValue: "${tippb?.platform?.name}, url: ${tippb?.platform?.primaryUrl}", oldValue: "${tippa?.platform?.name}, url: ${tippa?.platform?.primaryUrl}"])
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

    def static compareLocalPkgWithGokbPkg(ctx, oldpkg, newpkg, newTippClosure, updatedTippClosure, tippUnchangedClosure,deletedTippClosure, auto_accept)
    {
        def primaryUrl = (oldpkg?.nominalPlatformPrimaryUrl == newpkg?.nominalPlatformPrimaryUrl) ? oldpkg?.nominalPlatformPrimaryUrl : newpkg?.nominalPlatformPrimaryUrl
        def oldpkgTippsTippUuid = oldpkg.tipps.collect{it.tippUuid}
        def newpkgTippsTippUuid = newpkg.tipps.collect{it.tippUuid}

        newpkg.tipps.each{ tippnew ->

            replaceImpIDwithGokbID(ctx, tippnew, primaryUrl)

            if(tippnew?.tippUuid in oldpkgTippsTippUuid)
            {

                    //Temporary
                    def localDuplicateTippEntries = TitleInstancePackagePlatform.executeQuery("from TitleInstancePackagePlatform as tipp where tipp.gokbId = :tippUuid and tipp.status != :status ", [tippUuid: tippnew.tippUuid, status: RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'])])
                    def newAuto_accept = (localDuplicateTippEntries.size() > 1) ? true : false
                    newAuto_accept = auto_accept ?: newAuto_accept
                    if(newAuto_accept && tippnew.status != 'Deleted') {
                        System.out.println("TIPP " + tippnew + " Was added to the package with autoAccept" + newAuto_accept);
                        newTippClosure(ctx, tippnew, newAuto_accept)
                    } else
                    {
                    //Temporary END

                        def tippold = oldpkg.tipps.find{it.tippUuid == tippnew.tippUuid && it.status != 'Deleted'}

                        def db_tipp = ctx.tipps.find {it.gokbId == tippnew.tippUuid && it.status?.value != 'Deleted'}
                        def tipp_diff = getTippDiff(tippold, tippnew)

                        if (tippnew.status != 'Current' && tipp_diff.size() == 0) {
                            if(tippnew.status != db_tipp.status.value) {
                                deletedTippClosure(ctx, tippold, auto_accept, db_tipp)
                                System.out.println("Title " + tippold + " Was removed from the package");
                            }

                        } else if (tipp_diff.size() == 0) {
                            tippUnchangedClosure(ctx, tippold);

                        } else {
                            // See if any of the actual properties are null
                            println("Got tipp diffs: ${tipp_diff}")
                            try {
                                updatedTippClosure(ctx, tippnew, tippold, tipp_diff, auto_accept, db_tipp)
                            }
                            catch (Exception e) {
                                System.err.println("Error on executing updated TIPP closure! Please verify logs:")
                                e.printStackTrace()
                            }
                        }
                    }
            }
            else if (!(tippnew.tippUuid in oldpkgTippsTippUuid) && tippnew.status != 'Deleted') {


                    System.out.println("TIPP " + tippnew + " Was added to the package with autoAccept " + auto_accept);
                    newTippClosure(ctx, tippnew, auto_accept)


            }
        }
        oldpkg.tipps.each { tippold ->
            if(!(tippold?.tippUuid in newpkgTippsTippUuid))
            {
                def db_tipp = ctx.tipps.find {it.gokbId == tippold.tippUuid && it.status?.value != 'Deleted'}
                if(tippold.status.id != db_tipp.status.id) {
                    deletedTippClosure(ctx, tippold, auto_accept)
                    System.out.println("TIPP " + tippold + " Was removed from the package");
                }
            }
        }
    }

}
