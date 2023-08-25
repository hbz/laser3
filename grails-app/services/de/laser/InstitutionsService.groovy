package de.laser

import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional

import java.nio.file.Files
import java.nio.file.Path

/**
 * This service should manage institution-related matters
 */
@Transactional
class InstitutionsService {

    ContextService contextService

    static final CUSTOM_PROPERTIES_COPY_HARD        = 'CUSTOM_PROPERTIES_COPY_HARD'
    static final CUSTOM_PROPERTIES_ONLY_INHERITED   = 'CUSTOM_PROPERTIES_ONLY_INHERITED'

    /**
     * Creates a copy of the given license. Although the method may suggest it, this is not the
     * actual license copying workflow. Here, a license is being copied iff
     * <ul>
     *     <li>a member license is being generated for a consortial license</li>
     *     <li><s>a concrete license instance is created based on a license template</s> (unreachable and deprecated)</li>
     * </ul>
     * @param base the license which should be copied
     * @param params parameter map containing an eventual consortium and the license name
     * @param option how should custom properties being copied?
     * @return the license copy
     */
    License copyLicense(License base, params, Object option) {

        if (! base) {
            return null
        }

        Org org = params.consortium ?: contextService.getOrg()

        String lic_name = params.lic_name ?: "Kopie von ${base.reference}"

        License licenseInstance = new License(
                reference: lic_name,
                status: base.status,
                noticePeriod: base.noticePeriod,
                licenseUrl: base.licenseUrl,
                instanceOf: base,
                openEnded: base.openEnded,
                isSlaved: true //is default as of June 25th with ticket ERMS-2635
        )

        Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceId(License.class.name,base.id)

        inheritedAttributes.each { AuditConfig attr ->
            licenseInstance[attr.referenceField] = base[attr.referenceField]
        }

        if (! licenseInstance.save()) {
            log.error("Problem saving license ${licenseInstance.errors}")
            return licenseInstance
        } else {
            log.debug("Save ok")

            if (option == InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED) {

                LicenseProperty.findAllByOwner(base).each { LicenseProperty lp ->
                    AuditConfig ac = AuditConfig.getConfig(lp)

                    if (ac) {
                        // multi occurrence props; add one additional with backref
                        if (lp.type.multipleOccurrence) {
                            LicenseProperty additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, licenseInstance, lp.type, lp.tenant)
                            additionalProp = lp.copyInto(additionalProp)
                            additionalProp.instanceOf = lp
                            additionalProp.save()
                        }
                        else {
                            // no match found, creating new prop with backref
                            LicenseProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, licenseInstance, lp.type, lp.tenant)
                            newProp = lp.copyInto(newProp)
                            newProp.instanceOf = lp
                            newProp.save()
                        }
                    }
                }

                Identifier.findAllByLic(base).each { Identifier id ->
                    AuditConfig ac = AuditConfig.getConfig(id)

                    if(ac) {
                        Identifier.constructWithFactoryResult([value: id.value, parent: id, reference: licenseInstance, namespace: id.ns])
                    }
                }

                // documents (test if documents is really never null)
                base.documents.each { DocContext dctx ->

                    if (dctx.isShared) {
                        new DocContext(
                                owner: dctx.owner,
                                license: licenseInstance,
                                domain: dctx.domain,
                                status: dctx.status,
                                sharedFrom: dctx
                        ).save()
                    }
                }
            }
            else if (option == InstitutionsService.CUSTOM_PROPERTIES_COPY_HARD) {

                for (prop in base.propertySet) {
                    LicenseProperty copiedProp = new LicenseProperty(type: prop.type, owner: licenseInstance)
                    copiedProp = prop.copyInto(copiedProp)
                    copiedProp.instanceOf = null
                    copiedProp.save()
                    //licenseInstance.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                }

                // clone documents
                base.documents?.each { dctx ->
                    Doc clonedContents = new Doc(
                            type: dctx.getDocType(),
                            confidentiality: dctx.getDocConfid(),
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType,
                            migrated: dctx.owner.migrated,
                            server: dctx.owner.server
                    ).save()

                    String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

                    Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                    Path target = new File("${fPath}/${clonedContents.uuid}").toPath()
                    Files.copy(source, target)

                    new DocContext(
                            owner: clonedContents,
                            license: licenseInstance,
                            domain: dctx.domain,
                            status: dctx.status
                    ).save()
                }
            }

            RefdataValue licensee_role = RDStore.OR_LICENSEE
            RefdataValue lic_cons_role = RDStore.OR_LICENSING_CONSORTIUM

            log.debug("adding org link to new license")

            if (contextService.getOrg().isCustomerType_Consortium()) {
                new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role).save()
            }
            else {
                new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role).save()
            }
            OrgRole.findAllByLicAndRoleTypeAndIsShared(base,RDStore.OR_LICENSOR,true).each { OrgRole licRole ->
                new OrgRole(lic: licenseInstance, org: licRole.org, roleType: RDStore.OR_LICENSOR, isShared: true).save()
            }

            return licenseInstance
        }
    }
}
