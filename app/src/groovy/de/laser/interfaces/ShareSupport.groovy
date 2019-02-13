package de.laser.interfaces

import de.laser.traits.ShareableTrait

interface ShareSupport {

    boolean showShareButton()

    def updateShare(ShareableTrait sharedObject)

    def syncAllShares(List<ShareSupport> targets)
}
