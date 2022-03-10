package com.k_int.kbplus

import de.laser.ContentItem
import grails.gorm.transactions.Transactional

@Deprecated
@Transactional
class MessageService {

  String getMessage(String key, String locale) {
    String result = internalGetMessage(key,locale);
    if ( result == null ) {
      result=internalGetMessage(key)
    }

    if ( result == null ) {
      result = internalGetMessage('kbplus.key.text.empty', locale)

      if( result == null ){
        result = "No message set for this key."
      }
    }

    return result;
  }

  String getMessage(String key) {
    result=internalGetMessage(key)
    return result;
  }

  //@Cacheable(value='message', key='#key+#locale')
  String internalGetMessage(String key,String locale) {
    log.debug("getMessage(${key},${locale})");
    def ci=ContentItem.findByKeyAndLocale(key,locale)
    if ( ci != null ) {
      return ci.content
    }

    // If we didn't find a locale specific string, try and return the default no-locale string
    // return getMessage(key)
    return null
  }

  //@Cacheable('message')
  String internalGetMessage(String key) {
    log.debug("getMessage(${key})");
    def ci = ContentItem.findByKeyAndLocale(key,'')
    if ( ci != null )
      return ci.content
     
    return null
  }

  //@CachePut('message')
  String update(String key,String locale) {
    log.debug("getMessage(${key},${locale})");
    def ci=ContentItem.findByKeyAndLocale(key,locale)
    if ( ci != null ) {
      return ci.content
    }

    // If we didn't find a locale specific string, try and return the default no-locale string
    return getMessage(key)
  }
}
