package de.laser.interfaces

import de.laser.traits.ShareableTrait

interface ShareSupport {

    def updateAllShares()

    def updateShare(ShareableTrait sharedObject)

}
