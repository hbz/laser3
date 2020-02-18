import com.k_int.kbplus.Platform
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.YodaService
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import grails.util.Holders

YodaService yodaService = Holders.grailsApplication.mainContext.getBean('yodaService')

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-1") {
        grailsChange {
            change {
                //this changeset is HIGHLY EXPLOSIVE, TEST IT EXTENSIVELY BEFORE USE!!!!!!

                //2019-12-06
                //ERMS-1929
                //removing deprecated field impId, move ti_type_rv_fk to ti_medium_rv_fk
                sql.execute("""alter table package drop column pkg_identifier;
                alter table org drop column org_imp_id;
                alter table package drop column pkg_imp_id;
                alter table package rename pkg_type_rv_fk to pkg_content_type_rv_fk;
                alter table platform drop column plat_imp_id;
                alter table subscription drop column sub_imp_id;
                alter table title_instance drop column ti_imp_id;
                alter table title_instance_package_platform drop column tipp_imp_id;
                alter table title_instance_package_platform drop column tipp_sub_fk;
                alter table title_instance rename ti_type_rv_fk to ti_medium_rv_fk;
                update refdata_value set rdv_value = 'Book' where rdv_value = 'EBook';
                update refdata_category set rdc_description = 'title.medium' where rdc_description = 'title.type';""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-2") {
        grailsChange {
            change {
                //2019-12-10
                //ERMS-1901 (ERMS-1500)
                //org.name set not null with default "Name fehlt"
                sql.execute("""update org set org_name = 'Name fehlt!' where org_name is null;
                alter table org alter column org_name set default 'Name fehlt!';
                alter table org alter column org_name set not null;""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-3") {
        grailsChange {
            change {
                //2020-02-14
                //ERMS-1901 (ERMS-1957)
                //manually set platform and package data to correct one, drop tables title_institution_platform and core_assertion
                //www.degruyter.de in De Gruyter Online
                sql.execute("""update org_access_point_link set platform_id = 5 where platform_id = 27;""")
                //seach.ebscohost.com in EBSCOhost
                sql.execute("""update org_access_point_link set platform_id = 12 where platform_id in (30,59);""")
                //CareLit in CareLit Online
                sql.execute("""update org_access_point_link set platform_id = 98 where platform_id = 1;""")
                //Web of Science in Web of Science
                sql.execute("""update org_access_point_link set platform_id = 8 where platform_id = 62;""")
                sql.execute("""drop table core_assertion;""")
                sql.execute("""drop table title_institution_provider;""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-4") {
        grailsChange {
            change {
                try {
                    println("Starting with Packages")
                    Package.withNewSession {
                        Map packageDuplicates = yodaService.listDuplicatePackages()
                        List<Long> toDelete = []
                        toDelete.addAll(packageDuplicates.pkgDupsWithoutTipps.collect { dup -> dup.id })
                        packageDuplicates.pkgDupsWithTipps.each { dup ->
                            List<Subscription> concernedSubs = Subscription.executeQuery('select distinct(ie.subscription) from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg', [pkg: dup])
                            if (!concernedSubs)
                                toDelete << dup.id
                        }
                        yodaService.executePackageCleanup(toDelete)
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-5") {
        grailsChange {
            change {
                try {
                    println("to TitleInstances")
                    TitleInstance.withNewSession {
                        yodaService.executeTiCleanup(yodaService.listDuplicateTitles())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-6") {
        grailsChange {
            change {
                try {
                    println("to TIPPs")
                    TitleInstancePackagePlatform.withNewSession {
                        yodaService.executeTIPPCleanup(yodaService.listDeletedTIPPs())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-7") {
        grailsChange {
            change {
                try {
                    println("to Platforms")
                    Platform.withNewSession {
                        yodaService.executePlatformCleanup(yodaService.listPlatformDuplicates())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1571847838001-8") {
        grailsChange {
            change {
                //2020-01-23
                //ERMS-1901 (ERMS-1948)
                //cleanup - set gokbId as unique and not null, delete erroneous coverage data from ebooks and databases, delete column package_type_rv_fk
                sql.execute("""delete from issue_entitlement_coverage where ic_ie_fk in (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join title_instance ti on tipp_ti_fk = ti_id where class not like '%JournalInstance%');
                ALTER TABLE title_instance ALTER COLUMN ti_gokb_id TYPE character varying(511);
                alter table title_instance alter column ti_gokb_id set not null;
                alter table title_instance add constraint unique_ti_gokb_id unique (ti_gokb_id);
                update title_instance_package_platform set tipp_gokb_id = concat('generic.null.value.',tipp_id) where tipp_gokb_id is null;
                alter table title_instance_package_platform alter column tipp_gokb_id type character varying(511);
                alter table title_instance_package_platform alter column tipp_gokb_id set not null;
                alter table title_instance_package_platform ADD CONSTRAINT unique_tipp_gokb_id UNIQUE (tipp_gokb_id);
                update package set pkg_gokb_id = concat('generic.null.value',pkg_id) where pkg_gokb_id is null;
                alter table package alter column pkg_gokb_id type character varying(511);
                alter table package alter column pkg_gokb_id set not null;
                alter table package ADD CONSTRAINT unique_pkg_gokb_id UNIQUE (pkg_gokb_id);
                alter table platform alter column plat_gokb_id type character varying(511);
                alter table platform alter column plat_gokb_id set not null;
                alter table platform ADD CONSTRAINT unique_plat_gokb_id UNIQUE (plat_gokb_id);""")
            }
        }
    }

}