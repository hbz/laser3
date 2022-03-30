package de.laser

import com.k_int.kbplus.PendingChangeService
import de.laser.helper.BeanStore
import de.laser.titles.TitleInstance
import de.laser.helper.FactoryResult
import de.laser.interfaces.CalculatedLastUpdated
import grails.converters.JSON
import grails.plugins.orm.auditable.Auditable
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.web.json.JSONElement

/**
 * A class to retain identifiers for objects. Identifiers may be for example ISBNs for books ISSNs for journals, ISILs for packages, VIAF or other normed tokens for organisations.
 * An identifier belongs to a namespace; the namespace is typised. See {@link IdentifierNamespace} resp. {@link BootStrapService#setIdentifierNamespace()}. Moreover, an identifier may be unique or not; this depends upon the
 * namespace configuration. An identifier may also be inherited from parent subscriptions or licenses to member objects. The inheritance is - like with {@link de.laser.base.AbstractPropertyWithCalculatedLastUpdated}
 * implementations - only possible with {@link Subscription}s or {@link License}s as all other objects are global (i.e. not bound to a certain institution ({@link Org})
 * @see IdentifierNamespace
 * @see Subscription#ids
 * @see License#ids
 * @see Org#ids
 * @see TitleInstancePackagePlatform#ids
 * @see Package#ids
 */
class Identifier implements CalculatedLastUpdated, Comparable, Auditable {

    static Log static_logger = LogFactory.getLog(Identifier)

    IdentifierNamespace ns
    Identifier instanceOf
    String value
    String note = ""

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static transients = ['URL'] // mark read-only accessor methods

    static belongsTo = [
            lic:    License,
            org:    Org,
            pkg:    Package,
            sub:    Subscription,
            tipp:   TitleInstancePackagePlatform
    ]

	static constraints = {
		value validator: {val,obj ->
		  if (obj.ns.validationRegex){
			def pattern = ~/${obj.ns.validationRegex}/
			return pattern.matcher(val).matches()
		  }
		}
        note        (nullable: true, blank: true)

	  	lic         (nullable:true)
	  	org         (nullable:true)
	  	pkg         (nullable:true)
	  	sub         (nullable:true)
	  	tipp        (nullable:true)
        instanceOf  (nullable: true)

		// Nullable is true, because values are already in the database
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
  	}

    static mapping = {
        id    column:'id_id'
        value column:'id_value', index:'id_value_idx'
        ns    column:'id_ns_fk', index:'id_value_idx'
        note  column:'id_note',  type: 'text'

        lic   column:'id_lic_fk', index: 'id_lic_idx'
        org   column:'id_org_fk', index: 'id_org_idx'
        pkg   column:'id_pkg_fk', index: 'id_pkg_idx'
        sub   column:'id_sub_fk', index: 'id_sub_idx'
        tipp  column:'id_tipp_fk', index: 'id_tipp_idx'
        instanceOf column: 'id_instance_of_fk'

        dateCreated column: 'id_date_created'
        lastUpdated column: 'id_last_updated'
        lastUpdatedCascading column: 'id_last_updated_cascading'
    }

    @Override
    Collection<String> getLogIncluded() {
        [ 'value', 'note' ]
    }
    @Override
    Collection<String> getLogExcluded() {
        [ 'version', 'lastUpdated', 'lastUpdatedCascading' ]
    }

    /**
     * The comparator implementation for identifiers - compares this identifier to the given other instance
     * It will be compared against:
     * <ol>
     *     <li>German namespace name</li>
     *     <li>namespace key</li>
     *     <li>namespace type (class name of namespce type)</li>
     *     <li>identifier value (lexicographically)</li>
     * </ol>
     * @param o the identifier to compare with
     * @return the comparison result (-1, 0 or 1)
     */
    @Override
    int compareTo(Object o) {
        String aVal = ns.name_de ?: ns.ns
        String bVal = o.ns.name_de ?: o.ns.ns
        int result = aVal.compareToIgnoreCase(bVal)
        if(result == 0) {
            if(ns.nsType) {
                result = ns.nsType.compareTo(o.ns.nsType)
            }
            if(result == 0 || !ns.nsType) {
                result = value.compareTo(o.value)
            }
        }
        result
    }

    /**
     * Constructor call, hands the configuration map to {@link #constructWithFactoryResult(java.util.Map)}
     * @param map the parameter map of the identifier
     * @return the new or updated identifier object
     */
    static Identifier construct(Map<String, Object> map) {
        return constructWithFactoryResult(map).result
    }

    /**
     * Factory method to construct new identifier instances. The method constructs not only the identifier itself but checks
     * whether the identifier is already given for the namespace and prevents constructing multiple instances iff the namespace is configured to unique. A warning is being raised if the identifier
     * (historically) exists already or even multiple instances exist at a place where only one identifier is supposed to exist.
     * Moreover, the namespace is created, too, if it does not exist
     * @param map the parameter map of the identifier
     * @return the new or updated identifier object
     * @see IdentifierNamespace
     */
    static FactoryResult constructWithFactoryResult(Map<String, Object> map) {

        withTransaction {

        FactoryResult factoryResult = new FactoryResult()

        String value     = map.get('value')
        Object reference = map.get('reference')
        def namespace    = map.get('namespace')
        String name_de   = map.get('name_de')
        String nsType    = map.get('nsType')
        String note      = map.get('note')
        Identifier parent = map.get('parent')
        boolean isUnique = true
        if(map.containsKey('isUnique') && map.get('isUnique') == false)
            isUnique = false

        IdentifierNamespace ns
		if (namespace instanceof IdentifierNamespace) {
			ns = namespace
		}
        else {
            if (nsType){
                ns = IdentifierNamespace.findByNsIlikeAndNsType(namespace?.trim(), nsType)
            } else {
			    ns = IdentifierNamespace.findByNsIlike(namespace?.trim())
            }

			if(! ns) {
                if (nsType){
                    ns = new IdentifierNamespace(ns: namespace, isUnique: isUnique, isHidden: false, nsType: nsType, name_de: name_de)
                } else {
                    ns = new IdentifierNamespace(ns: namespace, isUnique: isUnique, isHidden: false, name_de: name_de)
                }
                ns.save()
            }
            else {
                if(ns.name_de != name_de && name_de != null) {
                    ns.name_de = name_de
                    ns.save()
                }
            }
        }

        String attr = Identifier.getAttributeName(reference)

        def ident = Identifier.executeQuery(
                'select ident from Identifier ident where ident.value = :val and ident.ns = :ns and ident.' + attr + ' = :ref order by ident.id',
                [val: value, ns: ns, ref: reference]

        )
        if (ident){
            factoryResult.status += FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ
        }
        if (! ident.isEmpty()) {
            if (ident.size() > 1) {
                factoryResult.status += FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ
                static_logger.debug("WARNING: multiple matches found for ( ${value}, ${ns}, ${reference} )")
            }
            ident = ident.first()
        }

        if (! ident) {
            Identifier identifierInDB = Identifier.findByNsAndValue(ns, value)
			if (ns.isUnique && identifierInDB && value != "Unknown") {
                static_logger.debug("NO IDENTIFIER CREATED: multiple occurrences found for unique namespace ( ${value}, ${ns} )")
                factoryResult.status += FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ
//                factoryResult.result = identifierInDB
                ident = identifierInDB
			} else {
                static_logger.debug("INFO: no match found; creating new identifier for ( ${value}, ${ns}, ${reference.class} )")
				ident = new Identifier(ns: ns, value: value)
                if(parent)
                    ident.instanceOf = parent
				ident.setReference(reference)
                ident.note = note
				boolean success = ident.save()
                if (success){
                    factoryResult.status += FactoryResult.STATUS_OK
                } else {
                    factoryResult.status += FactoryResult.STATUS_ERR
                }
			}
        }

        factoryResult.result = ident
        factoryResult
        } // withTransaction
    }

    /**
     * Links the identifier to its owner object
     * @param owner the object whose identifier should be linked. May be one of:
     * <ul>
     *     <li>{@link License}</li>
     *     <li>{@link Org}</li>
     *     <li>{@link Package}</li>
     *     <li>{@link Subscription}</li>
     *     <li>{@link TitleInstancePackagePlatform}</li>
     * </ul>
     */
    void setReference(def owner) {
        lic  = owner instanceof License ? owner : lic
        org  = owner instanceof Org ? owner : org
        pkg  = owner instanceof Package ? owner : pkg
        sub  = owner instanceof Subscription ? owner : sub
        tipp = owner instanceof TitleInstancePackagePlatform ? owner : tipp
        //ti   = owner instanceof TitleInstance ? owner : ti
    }

    /**
     * Gets the owner of this identifier; if multiple references exist, a warning is being raised
     * @return the reference object iff exactly one reference is set, null otherwise
     */
    Object getReference() {
        int refCount = 0
        def ref

        List<String> fks = ['lic', 'org', 'pkg', 'sub', 'ti', 'tipp']
        fks.each { fk ->
            if (this."${fk}") {
                refCount++
                ref = this."${fk}"
            }
        }
        if (refCount == 1) {
            return ref
        }

        static_logger.debug("WARNING: identifier #${this.id}, refCount: ${refCount}")
        return null
    }

    /**
     * Gets the field name of the reference object
     * @param object the owner object whose field name should be retrieved
     * @return the field name where the reference is set
     */
    static String getAttributeName(def object) {
        String name

        name = object instanceof License ?  'lic' : name
        name = object instanceof Org ?      'org' : name
        name = object instanceof Package ?  'pkg' : name
        name = object instanceof Subscription ?                 'sub' :  name
        name = object instanceof TitleInstancePackagePlatform ? 'tipp' : name
        name = object instanceof TitleInstance ?                'ti' :   name

        name
    }

    /**
     * Builds for the given identifier a valid URL iff its namespace has an URL prefix defined
     * @return the value as an URL, prefixed by {@link IdentifierNamespace#urlPrefix}
     */
    String getURL() {
        if (ns.urlPrefix && value && value != IdentifierNamespace.UNKNOWN) {
            if (ns.urlPrefix.endsWith('=')) {
                return "${ns.urlPrefix}${value}"
            }
            else if (ns.urlPrefix.endsWith('/')) {
                return "${ns.urlPrefix}${value}"
            }
            else {
                return "${ns.urlPrefix}/${value}"
            }
        }
        null
    }

    /**
     * Triggers after the insertion of the identifier; prefixes a shortened ISIL by DE if it does start by a number
     */
    def afterInsert() {
        static_logger.debug("afterInsert")

        // -- moved from def afterInsert = { .. }
        if (this.ns?.ns in IdentifierNamespace.CORE_ORG_NS) {
            if (this.value == IdentifierNamespace.UNKNOWN) {
                this.value = ''
                if(!this.save()) {
                    log.error(this.getErrors().getAllErrors().toListString())
                }
            }
        }

        if (this.ns?.ns == IdentifierNamespace.ISIL) {
            if( (this.value != IdentifierNamespace.UNKNOWN) &&
                    ((!(this.value =~ /^DE-/ || this.value =~ /^[A-Z]{2,3}-/) && this.value != '')))
            {
                this.value = 'DE-'+this.value.trim()
            }
        }
        // -- moved from def afterInsert = { .. }

        BeanStore.getCascadingUpdateService().update(this, dateCreated)
    }
    def afterUpdate() {
        static_logger.debug("afterUpdate")
        BeanStore.getCascadingUpdateService().update(this, lastUpdated)
    }
    def afterDelete() {
        static_logger.debug("afterDelete")
        BeanStore.getCascadingUpdateService().update(this, new Date())
    }

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    /**
     * Triggers before the database update of the identifier; prefixes WIB IDs and ISILs if they start by a number
     * @return
     */
    def beforeUpdate() {
        static_logger.debug("beforeUpdate")
        value = value?.trim()
      // TODO [ticket=1789]
      //boolean forOrg = IdentifierOccurrence.findByIdentifier(this)
      //if(forOrg) {
      if (org != null) {
          if(this.ns?.ns == 'wibid') {
              if(!(this.value =~ /^WIB/) && this.value != '') {
                  this.value = 'WIB'+this.value.trim()
              }
          }
          if(this.ns?.ns == 'ISIL') {
              if(!(this.value =~ /^DE-/ || this.value =~ /^[A-Z]{2}-/) && this.value != '') {
                  this.value = 'DE-'+this.value.trim()
              }
          }
      }
        Map<String, Object> changes = [
                oldMap: [:],
                newMap: [:]
        ]
        this.getDirtyPropertyNames().each { prop ->
            changes.oldMap.put( prop, this.getPersistentValue(prop) )
            changes.newMap.put( prop, this.getProperty(prop) )
        }
        BeanStore.getAuditService().beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }

    /**
     * Triggered by generic method; triggers itself update of all inheriting objects
     * @param changeDocument the map of changes to be passed onto inheriting identifiers; processed by {@link PendingChange} object
     */
    void notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")
        if (changeDocument.event.equalsIgnoreCase('Identifier.updated')) {

            // legacy ++

            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.subscription."+changeDocument.prop, locale.toString())
            String description = BeanStore.getMessageSource().getMessage('default.accept.placeholder',null, locale)
            if (contentItemDesc) {
                description = contentItemDesc.content
            }
            else {
                ContentItem defaultMsg = ContentItem.findByKeyAndLocale("kbplus.change.subscription.default", locale.toString())
                if( defaultMsg)
                    description = defaultMsg.content
            }

            // legacy ++

            List<PendingChange> slavedPendingChanges = []

            List<Identifier> depedingProps = Identifier.findAllByInstanceOf( this )
            depedingProps.each{ Identifier childId ->

                String definedType = 'text'

                // overwrite specials ..
                if (changeDocument.prop == 'note') {
                    definedType = 'text'
                    description = '(NOTE)'
                }

                def msgParams = [
                        definedType,
                        "${childId.ns.class.name}:${childId.ns.id}",
                        (changeDocument.prop in ['note'] ? "${changeDocument.oldLabel}" : "${changeDocument.old}"),
                        (changeDocument.prop in ['note'] ? "${changeDocument.newLabel}" : "${changeDocument.new}"),
                        "${description}"
                ]

                if(childId.sub) {
                    PendingChange newPendingChange = BeanStore.getChangeNotificationService().registerPendingChange(
                            PendingChange.PROP_SUBSCRIPTION,
                            childId.sub,
                            childId.sub.getSubscriber(),
                            [
                                    changeTarget:"${Subscription.class.name}:${childId.sub.id}",
                                    changeType: PendingChangeService.EVENT_PROPERTY_CHANGE,
                                    changeDoc:changeDocument
                            ],
                            PendingChange.MSG_SU02,
                            msgParams,
                            "Der Identifikator <strong>${childId.ns.getI10n("name")}</strong> hat sich von <strong>\"${changeDocument.oldLabel?:changeDocument.old}\"</strong> zu <strong>\"${changeDocument.newLabel?:changeDocument.new}\"</strong> von der Lizenzvorlage geändert. " + description
                    )
                    if (newPendingChange && childId.sub.isSlaved) {
                        slavedPendingChanges << newPendingChange
                    }
                }
                else if(childId.lic) {
                    PendingChange newPendingChange = BeanStore.getChangeNotificationService().registerPendingChange(
                            PendingChange.PROP_LICENSE,
                            childId.lic,
                            childId.lic.getLicensee(),
                            [
                                    changeTarget:"${License.class.name}:${childId.lic.id}",
                                    changeType: PendingChangeService.EVENT_PROPERTY_CHANGE,
                                    changeDoc:changeDocument
                            ],
                            PendingChange.MSG_LI02,
                            msgParams,
                            "Der Identifikator <strong>${childId.ns.getI10n("name")}</strong> hat sich von <strong>\"${changeDocument.oldLabel?:changeDocument.old}\"</strong> zu <strong>\"${changeDocument.newLabel?:changeDocument.new}\"</strong> von der Lizenzvorlage geändert. " + description
                    )
                    if (newPendingChange && childId.lic.isSlaved) {
                        slavedPendingChanges << newPendingChange
                    }
                }
            }

            slavedPendingChanges.each { spc ->
                log.debug('autoAccept! performing: ' + spc)
                BeanStore.getPendingChangeService().performAccept(spc)
            }
        }
        else if (changeDocument.event.equalsIgnoreCase('Identifier.deleted')) {

            List<PendingChange> openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null and pc.payload is not null and pc.oid = :objectID",
                    [objectID: "${this.class.name}:${this.id}"] )
            openPD.each { pc ->
                if (pc.payload) {
                    JSONElement payload = JSON.parse(pc.payload)
                    if (payload.changeDoc) {
                        def scp = genericOIDService.resolveOID(payload.changeDoc.OID)
                        if (scp?.id == id) {
                            pc.delete()
                        }
                    }
                }
            }
        }
    }

    @Deprecated
  static Identifier lookupOrCreateCanonicalIdentifier(ns, value) {
        static_logger.log("loc canonical identifier")

      value = value?.trim()
      ns = ns?.trim()
      // println ("lookupOrCreateCanonicalIdentifier(${ns},${value})");
      IdentifierNamespace namespace
      Identifier result
      if(IdentifierNamespace.findByNsIlike(ns)) {
          namespace = IdentifierNamespace.findByNsIlike(ns)
          if(Identifier.findByNsAndValue(namespace,value)) {
              Identifier.findByNsAndValue(namespace,value)
          }
          else {
              result = new Identifier(ns:namespace, value:value)
              if(result.save())
                  result
          }
      }
      else {
          namespace = new IdentifierNamespace(ns:ns, isUnique: false, isHidden: false)
          if(namespace.save()) {
              result = new Identifier(ns:namespace, value:value)
              if(result.save())
                  result
              else {
                  static_logger.log("error saving identifier")
                  static_logger.log(result.errors.toString())
              }
          }
          else {
              static_logger.log("error saving namespace")
              static_logger.log(namespace.errors.toString())
          }
      }
  }

    /**
     * Retrieves a list of identifiers for a dropdown menu
     * @param params the search request params for filtering among the values
     * @return a {@link List} of {@link Map}s for dropdown display
     */
    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []
        List<Identifier> ql = []
        if (params.q.contains(':')) {
            String[] qp = params.q.split(':')
            IdentifierNamespace namespace = IdentifierNamespace.findByNsIlike(qp[0])

            if (namespace && qp.size() == 2) {
                ql = Identifier.findAllByNsAndValueIlike(namespace, "${qp[1]}%")
            }
        }
        else {
            ql = Identifier.findAllByValueIlike("${params.q}%", params)
        }

        ql.each { id ->
            result.add([id: "${id.class.name}:${id.id}", text: "${id.ns.ns}:${id.value}"])
        }
        result
    }

    // called from AjaxController.resolveOID2()
    @Deprecated
    // front end creation broken
    static def refdataCreate(value) {
        // value is String[] arising from  value.split(':');
        if ( ( value.length == 4 ) && ( value[2] != '' ) && ( value[3] != '' ) )
            return lookupOrCreateCanonicalIdentifier(value[2],value[3]); // ns, value

        return null;
    }

    /**
     * Gets a list of objects matching to the given identifier value
     * @param object an empty object instance to determine the identifier owner's class
     * @param identifierString the value to search for
     * @return a {@link List} of objects matching to the given identifier value
     */
    static List lookupObjectsByIdentifierString(def object, String identifierString) {
        List result = []

        String objType = object.getClass().getSimpleName()
        LogFactory.getLog(this).debug("lookupObjectsByIdentifierString(${objType}, ${identifierString})")

        if (objType) {

            String[] idstrParts = identifierString.split(':');
            switch (idstrParts.size()) {
                case 1:
                    result = executeQuery('select t from ' + objType + ' as t join t.ids as ident where ident.value = :val', [val: idstrParts[0]])
                    break
                case 2:
                    result = executeQuery('select t from ' + objType + ' as t join t.ids as ident where ident.value = :val and ident.ns.ns = :ns', [val: idstrParts[1], ns: idstrParts[0]])
                    break
                default:
                    break
            }
            LogFactory.getLog(this).debug("components: ${idstrParts} : ${result}")
        }

        result
    }

    /**
     * Gets the Leitweg-ID parts (an identifier mandatory for North-Rhine Westphalia billing system) for an org. A Leitweg-ID is composed by:
     * <ol>
     *     <li>coarse addressing (numeric, 2-12 digits)</li>
     *     <li>fine addressing (alphanumeric, up to 30 digits)</li>
     *     <li>check digit (numeric, 2 digits)</li>
     * </ol>
     * @return the {@link Map} reflecting the three parts of the Leitweg-ID
     */
    Map getLeitID(){
        String leitID1
        String leitID2
        String leitID3
        if(this.value){
            List leitID = this.value.split('-')

            if(leitID.size() == 2){
                leitID1 = leitID[0]
                leitID3 = leitID[1]
            }

            if(leitID.size() == 3){
                leitID1 = leitID[0]
                leitID2 = leitID[1]
                leitID3 = leitID[2]
            }

        }

        return [leitID1: leitID1, leitID2: leitID2, leitID3: leitID3]
    }
}
