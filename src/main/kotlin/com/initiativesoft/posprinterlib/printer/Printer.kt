package com.initiativesoft.posprinterlib.printer

import com.initiativesoft.posprinterlib.helper.FailureReason
import com.initiativesoft.posprinterlib.interfaces.ConnectionEvent
import com.initiativesoft.posprinterlib.interfaces.DiscoverEvent
import sun.plugin.dom.exception.InvalidStateException
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

abstract class Printer
{
    /**
     * This is the port which will be used for the [discover] function
     */
    protected var DISCOVER_PORT = 0

    /**
     * This is the port which will be used for the [connect] function
     */
    protected var CONNECTION_PORT = 0

    /**
     * The [DatagramSocket] which will be used for the [discover] function
     */
    protected lateinit var udpBroadcastSocket: DatagramSocket
    /**
     * The [Socket] which will be used for the [discover] function
     */
    protected lateinit var tcpBroadcastSocket: Socket


    /**
     * The [DatagramSocket] which will be used for the [connect] function
     */
    lateinit var udpConnectionSocket: DatagramSocket
    /**
     * The [Socket] which will be used for the [connect] function
     */
    lateinit var tcpConnectionSocket: Socket


    private val discoveryThreadExecutor = Executors.newFixedThreadPool(2)//Discover and timeout
    private val ioThreadExecutor = Executors.newFixedThreadPool(4)

    private var _canDiscoverContinue = AtomicBoolean(false)

    private var _onDiscoverEventBackingField: DiscoverEvent? = null
    private var _onConnectionEventBackingField: ConnectionEvent? = null

    /**
     * The [InputStream] for the connection
     */
    protected lateinit var inputStream: InputStream
    /**
     * The [OutputStream] for the connection
     */
    protected lateinit var outputStream: OutputStream

    private var commandBlockQueue = ConcurrentLinkedDeque<CommandBlock>()

    private var _isCommandQueueRunning = AtomicBoolean(false)

    private var _isConnectionValid = AtomicBoolean(false)

    protected var connectionIpAddress = ""

    init {
        tcpConnectionSocket = Socket()
    }


    /**
     * Connect to the printer at [ipAddress]
     * Executes on the current thread
     */
    open fun connect(ipAddress: String) {
        connectionIpAddress = ipAddress
        val connectionInfo = InetSocketAddress(ipAddress, CONNECTION_PORT)
        tcpConnectionSocket.connect(connectionInfo)
        inputStream = tcpConnectionSocket.getInputStream()
        outputStream = tcpConnectionSocket.getOutputStream()

        _onConnectionEventBackingField?.onConnect(ipAddress)
    }

    /**
     * Connect to the printer at [ipAddress]
     * Executes on a separate thread
     */

    open fun connectAsync(ipAddress: String)
    {
        connectionIpAddress = ipAddress
        val connectionInfo = InetSocketAddress(ipAddress, CONNECTION_PORT)
        ioThreadExecutor.execute {
            tcpConnectionSocket.connect(connectionInfo)
            inputStream = tcpConnectionSocket.getInputStream()
            outputStream = tcpConnectionSocket.getOutputStream()

            _isConnectionValid.set(true)
            _onConnectionEventBackingField?.onConnect(connectionIpAddress)
        }
    }


    /**
     * Sends the [commandBlock]
     */
    fun sendCommandBlock(commandBlock: CommandBlock) {
        outputStream.write(commandBlock.buildByteArray())
        outputStream.flush()
    }

    /**
     * Queue the [commandBlock] which will be de queued run on a separate thread
     */
    fun queueCommandBlock(commandBlock: CommandBlock) {
        commandBlockQueue.add(commandBlock)
        if(!_isCommandQueueRunning.get())
            ioThreadExecutor.execute {
                deQueueAndSend()
            }
    }

    /**
     * Disconnects the [tcpConnectionSocket]
     * Executes on the current thread
     */
    open fun disconnect() {
        if(!tcpConnectionSocket.isClosed && tcpConnectionSocket.isConnected) {
            tcpConnectionSocket.close()
            this._onConnectionEventBackingField?.onDisconnect(connectionIpAddress)
        }
    }

    /**
     * Disconnects the [tcpConnectionSocket]
     * Executes on a separate thread
     */
    open fun disconnectAsync() {
        ioThreadExecutor.execute {
            if(!tcpConnectionSocket.isClosed && tcpConnectionSocket.isConnected) {
                tcpConnectionSocket.close()
                this._onConnectionEventBackingField?.onDisconnect(connectionIpAddress)
            }
        }

    }

    /**
     *
     */

    /**
     * Sets the [DiscoverEvent]
     */
    fun setOnDiscoverEvent(onDiscoverEvent: DiscoverEvent) {
        this._onDiscoverEventBackingField = onDiscoverEvent
    }

    /**
     * Sets the [ConnectionEvent]
     */
    fun setOnConnectionEvent(onConnectionEvent: ConnectionEvent) {
        this._onConnectionEventBackingField = onConnectionEvent
    }

    /**
     * Start printer discovery on a separate.
     * Can be stopped manually with [stopDiscover].
     * Specify a non-zero values for [timeOut], it will call a [DiscoverEvent.onDiscoverFailed] with
     * [FailureReason.TIMEOUT]
     */
    fun startDiscover(timeOut: Long = 0, timeUnit: TimeUnit = TimeUnit.SECONDS) {
        //Can't have 2 discoveries
        if(_canDiscoverContinue.get()) {
            throw InvalidStateException("A discovery is already in progress")
        }

        _canDiscoverContinue.set(true)
        val totalTime = timeUnit.toMillis(timeOut)
        discoveryThreadExecutor.execute {
            val discoverResult = discover(_canDiscoverContinue)
            _onDiscoverEventBackingField?.onDiscover(discoverResult)
        }

        if(timeOut != 0L) {
            val startTime = System.currentTimeMillis() // Thread safe
            discoveryThreadExecutor.execute {
                while(_canDiscoverContinue.get()) {
                    val endTime = System.currentTimeMillis()
                    if (endTime - startTime >= totalTime) {
                        _canDiscoverContinue.set(false)
                        _onDiscoverEventBackingField?.onDiscoverFailed(FailureReason.TIMEOUT)
                    }
                }
            }
        }
    }

    /**
     * Stops the discovery process.
     * If successful [DiscoverEvent.onDiscoverFailed] is called with [FailureReason.QUIT]
     */
    fun stopDiscover() {
        if(_canDiscoverContinue.get()) {
            _canDiscoverContinue.set(false)
            _onDiscoverEventBackingField?.onDiscoverFailed(FailureReason.QUIT)
        }
    }

    /**
     * The function which will be called on [startDiscover].
     * The [canDiscoverContinue] param is passed to inform of a force or timeout exit
     *
     * @return an [ArrayList] of ip addresses
     */
    abstract fun discover(canDiscoverContinue: AtomicBoolean): ArrayList<String>

    /**
     * Create a [TextFormat] instance
     */
    abstract fun createTextFormat(): TextFormat

    /**
     * Create a [CommandBlock] instance
     */

    abstract fun createCommandBlock(): CommandBlock

    /**
     * A function which de queues all queued [CommandBlock] and sends them.
     */
    private fun deQueueAndSend() {
        _isCommandQueueRunning.set(true)

        while(commandBlockQueue.size > 0) {
            if(!_isConnectionValid.get())
                continue

            val queueItem = commandBlockQueue.peek()
            sendCommandBlock(queueItem)
            commandBlockQueue.pop()
        }

        _isCommandQueueRunning.set(false)
    }

    companion object {
        @JvmStatic
        val DEFAULT_BROADCAST_ADDRESS: InetAddress =
                InetAddress.getByName("255.255.255.255")
    }
}
