package com.initiativesoft.posprinterlib.interfaces

/**
 * [ConnectionEvent] interface
 */
interface ConnectionEvent {
    fun onConnect(ipAddress: String)
    fun onDisconnect(ipAddress: String)
}