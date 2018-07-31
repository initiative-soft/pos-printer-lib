package com.initiativesoft.posprinterlib.interfaces

import com.initiativesoft.posprinterlib.helper.FailureReason

/**
 * [DiscoverEvent] interface
 */
interface DiscoverEvent {
    fun onDiscover(arrayList: ArrayList<String>)
    fun onDiscoverFailed(failureReason: FailureReason)
}


