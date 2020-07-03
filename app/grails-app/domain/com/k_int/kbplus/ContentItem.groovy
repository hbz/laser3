package com.k_int.kbplus

class ContentItem {

  String key
  String locale
  String content

  Date dateCreated
  Date lastUpdated

  static mapping = {
         id column:'ci_id'
        key column:'ci_key'
     locale column:'ci_locale'
    content column:'ci_content', type:'text'

      dateCreated column: 'ci_date_created'
      lastUpdated column: 'ci_last_updated'
  }

  static constraints = {
        key (blank:false)
     locale (blank:true)
    content (blank:false)

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)
  }

  static ContentItem lookupOrCreate(String key, String locale, String content) {
    ContentItem result = ContentItem.findByKeyAndLocale(key, locale)
    if ( result == null ) {
      result = new ContentItem(key:key, locale:locale, content:content)
      result.locale = locale
      result.save()
    }
    result
  }
}
