package de.laser.interfaces

import de.laser.traits.ShareableTrait

interface ShareSupport {

    def updateShare(ShareableTrait sharedObject)

    def synAllShares(List<ShareSupport> targets)
}
