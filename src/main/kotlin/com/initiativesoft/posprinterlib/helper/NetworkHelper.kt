package com.initiativesoft.posprinterlib.helper

import java.net.InetAddress
import java.net.NetworkInterface

object NetworkHelper {

    /**
     * Get all [NetworkInterface], and the ip address that belongs to them
     * @returns a [ArrayList] of ip addresses belonging to the [NetworkInterface]
     */
    fun getInterfacesIpList(): ArrayList<String> {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()

        val list = ArrayList<String>()
        for(_interface in networkInterfaces) {
            if(_interface.isLoopback) {
                continue
            }
            for(inetAddr in _interface.inetAddresses) {
                list.add(inetAddr.hostAddress)
            }

        }

        return list
    }
}