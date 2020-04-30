package com.k_int.kbplus

import de.laser.helper.FactoryResult
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient

class Identifier {

    @Transient
    def lastUpdatedService

    static Log static_logger = LogFactory.getLog(Identifier)

    IdentifierNamespace ns
    String value
    String note = ""

    Date dateCreated
    Date lastUpdated
    Date calculatedLastUpdated

    static belongsTo = [
            lic:    License,
            org:    Org,
            pkg:    Package,
            sub:    Subscription,
            ti:     TitleInstance,
            tipp:   TitleInstancePackagePlatform,
            cre:    Creator
    ]

	static constraints = {
		value validator: {val,obj ->
		  if (obj.ns.validationRegex){
			def pattern = ~/${obj.ns.validationRegex}/
			return pattern.matcher(val).matches()
		  }
		}
        note    (nullable: true, blank: true)

	  	lic     (nullable:true)
	  	org     (nullable:true)
	  	pkg     (nullable:true)
	  	sub     (nullable:true)
	  	ti      (nullable:true)
	  	tipp    (nullable:true)
	  	cre     (nullable:true)

		// Nullable is true, because values are already in the database
      	lastUpdated (nullable: true, blank: false)
      	dateCreated (nullable: true, blank: false)
        calculatedLastUpdated (nullable: true, blank: false)
  	}

    static mapping = {
       id   column:'id_id'
    value   column:'id_value', index:'id_value_idx'
       ns   column:'id_ns_fk', index:'id_value_idx'
       note column:'id_note',  type: 'text'

       lic  column:'id_lic_fk'
       org  column:'id_org_fk'
       pkg  column:'id_pkg_fk'
       sub  column:'id_sub_fk'
       ti   column:'id_ti_fk',      index:'id_title_idx'
       tipp column:'id_tipp_fk',    index:'id_tipp_idx'
       cre  column:'id_cre_fk'

        dateCreated column: 'id_date_created'
        lastUpdated column: 'id_last_updated'
        calculatedLastUpdated column: 'id_calc_last_updated'
    }

    static Identifier construct(Map<String, Object> map) {
        return constructWithFactoryResult(map).result
    }

    static FactoryResult constructWithFactoryResult(Map<String, Object> map) {
        FactoryResult factoryResult = new FactoryResult()

        String value        = map.get('value')
        Object reference    = map.get('reference')
        def namespace       = map.get('namespace')
        String nsType       = map.get('nsType')

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
                    ns = new IdentifierNamespace(ns: namespace, isUnique: true, isHidden: false, nsType: nsType)
                } else {
                    ns = new IdentifierNamespace(ns: namespace, isUnique: true, isHidden: false)
                }
				ns.save(flush:true)
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
			if (ns.isUnique && identifierInDB) {
                static_logger.debug("NO IDENTIFIER CREATED: multiple occurrences found for unique namespace ( ${value}, ${ns} )")
                factoryResult.status += FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ
//                factoryResult.result = identifierInDB
                ident = identifierInDB
			} else {
                static_logger.debug("INFO: no match found; creating new identifier for ( ${value}, ${ns}, ${reference.class} )")
				ident = new Identifier(ns: ns, value: value)
				ident.setReference(reference)
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
    }

    void setReference(def owner) {
        lic  = owner instanceof License ? owner : lic
        org  = owner instanceof Org ? owner : org
        pkg  = owner instanceof Package ? owner : pkg
        sub  = owner instanceof Subscription ? owner : sub
        tipp = owner instanceof TitleInstancePackagePlatform ? owner : tipp
        ti   = owner instanceof TitleInstance ? owner : ti
        cre  = owner instanceof Creator ? owner : cre
    }

    Object getReference() {
        int refCount = 0
        def ref

        List<String> fks = ['lic', 'org', 'pkg', 'sub', 'ti', 'tipp', 'cre']
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

    static String getAttributeName(def object) {
        String name

        name = object instanceof License ?  'lic' : name
        name = object instanceof Org ?      'org' : name
        name = object instanceof Package ?  'pkg' : name
        name = object instanceof Subscription ?                 'sub' :  name
        name = object instanceof TitleInstancePackagePlatform ? 'tipp' : name
        name = object instanceof TitleInstance ?                'ti' :   name
        name = object instanceof Creator ?                      'cre' :  name

        name
    }

    String getURL() {
        if (ns.urlPrefix && value) {
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

    def beforeInsert() {
        static_logger.debug("beforeInsert")
    }
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
    }
    def beforeDelete() {
        static_logger.debug("beforeDelete")
    }
    def afterInsert() {
        static_logger.debug("afterInsert")
    }
    def afterUpdate() {
        static_logger.debug("afterUpdate")
        lastUpdatedService.cascadingUpdate(this)
    }
    def afterDelete() {
        static_logger.debug("afterDelete")
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
                  static_logger.log(result.getErrors())
              }
          }
          else {
              static_logger.log("error saving namespace")
              static_logger.log(namespace.getErrors())
          }
      }
  }

  static def refdataFind(params) {
    def result = [];
    def ql = null;
    if ( params.q.contains(':') ) {
      def qp=params.q.split(':');
      // println("Search by namspace identifier: ${qp}");
        IdentifierNamespace namespace = IdentifierNamespace.findByNsIlike(qp[0]);
      if ( namespace && qp.size() == 2) {
        ql = Identifier.findAllByNsAndValueIlike(namespace,"${qp[1]}%")
      }
    }
    else {
      ql = Identifier.findAllByValueIlike("${params.q}%",params)
    }

    if ( ql ) {
      ql.each { id ->
        result.add([id:"${id.class.name}:${id.id}",text:"${id.ns.ns}:${id.value}"])
      }
    }

    result
  }
    // called from AjaxController.lookup2
    static def refdataFind2(params) {
        def result = []
        if (params.q.contains(':')) {
            def qp = params.q.split(':');
            IdentifierNamespace namespace = IdentifierNamespace.findByNsIlike(qp[0]);
            if (namespace && qp.size() == 2) {
                def ql = Identifier.findAllByNsAndValueIlike(namespace,"${qp[1]}%")
                ql.each { id ->
                    result.add([id:"${id.class.name}:${id.id}", text:"${id.value}"])
                }
            }
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

    static def lookupObjectsByIdentifierString(def object, String identifierString) {
        def result = null

        def objType = object.getClass().getSimpleName()
        LogFactory.getLog(this).debug("lookupByIdentifierString(${objType}, ${identifierString})")

        if (objType) {

            def idstrParts = identifierString.split(':');
            switch (idstrParts.size()) {
                case 1:
                    result = executeQuery('select t from ' + objType + ' as t join t.ids as ident where ident.value = ?', [idstrParts[0]])
                    break
                case 2:
                    result = executeQuery('select t from ' + objType + ' as t join t.ids as ident where ident.value = ? and ident.ns.ns = ?', [idstrParts[1], idstrParts[0]])
                    break
                default:
                    break
            }
            LogFactory.getLog(this).debug("components: ${idstrParts} : ${result}");
        }

        result
    }

    @Transient
    def afterInsert = {
        if(this.ns?.ns in IdentifierNamespace.CORE_ORG_NS) {
            if(this.value == IdentifierNamespace.UNKNOWN) {
                this.value = ''
                this.save()
            }
        }

        if(this.ns?.ns == IdentifierNamespace.ISIL)
        {
            if( (this.value != IdentifierNamespace.UNKNOWN) &&
                    ((!(this.value =~ /^DE-/ || this.value =~ /^[A-Z]{2,3}-/) && this.value != '')))
            {
                this.value = 'DE-'+this.value.trim()
            }
        }
    }
}
