package com.initiativesoft.posprinterlib.vendor.sam4s

import com.initiativesoft.posprinterlib.helper.NetworkHelper
import com.initiativesoft.posprinterlib.printer.CommandBlock as GenericCommandBlock
import com.initiativesoft.posprinterlib.printer.Printer as GenericPrinter
import com.initiativesoft.posprinterlib.printer.TextFormat as GenericTextFormat
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class Printer : GenericPrinter()
{
    private val discoverMessage: ByteArray
    private val networkInterfaceIpList = NetworkHelper.getInterfacesIpList()

    init {
        this.DISCOVER_PORT = 2918
        this.CONNECTION_PORT = 6001

        discoverMessage = ByteArray(DISCOVER_MESSAGE_LENGTH)
        this.buildDiscoverMessage()
    }

    override fun connect(ipAddress: String) {
        super.connect(ipAddress)
    }


    override fun discover(canDiscoverContinue: AtomicBoolean): ArrayList<String> {
        val printerAddressList = ArrayList<String>()
        udpBroadcastSocket = DatagramSocket(DISCOVER_PORT)
        val datagramPacket = DatagramPacket(discoverMessage, 0, discoverMessage.size,
                GenericPrinter.DEFAULT_BROADCAST_ADDRESS, DISCOVER_PORT)

        udpBroadcastSocket.broadcast = true
        udpBroadcastSocket.soTimeout = 2000

        val dataReceiver = ByteArray(8192)

        udpBroadcastSocket.send(datagramPacket)
        while(canDiscoverContinue.get()) {
            val broadcastPacketReceiver = DatagramPacket(dataReceiver, dataReceiver.size)

            try {
                udpBroadcastSocket.receive(broadcastPacketReceiver)
            }
            catch (e: SocketTimeoutException) {
                //e.printStackTrace()
                continue
            }

            val responseAddress = broadcastPacketReceiver.address
            val ipSplit = responseAddress.toString().split("/")
            val ipAddress = if(ipSplit.size > 1) {
                ipSplit[1]
            }
            else {
                ipSplit[0]
            }

            if(networkInterfaceIpList.contains(ipAddress)) {
                continue
            }
            else {
                printerAddressList.add(ipAddress)
                //break
            }
        }

        udpBroadcastSocket.close()

        canDiscoverContinue.set(false)

        return printerAddressList
    }

    override fun createCommandBlock(): GenericCommandBlock {
        return CommandBlock()
    }

    override fun createTextFormat(): GenericTextFormat {
        return TextFormat()
    }

    private fun buildDiscoverMessage() {
        discoverMessage[0] = -86
        discoverMessage[1] = -85
        discoverMessage[discoverMessage.size - 1] = 85
        discoverMessage[discoverMessage.size - 2] = 85
    }

    companion object {
        const val DISCOVER_MESSAGE_LENGTH = 35
    }
}