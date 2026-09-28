package com.zgautoshop.android

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.util.Locale
class MainActivity : AppCompatActivity(), SerialInputOutputManager.Listener {
    companion object { private const val ACTION_USB_PERMISSION = "com.zgautoshop.android.USB_PERMISSION" }

    private lateinit var usbManager: UsbManager
    private var port: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null
    private lateinit var bikeSpinner: Spinner
    private lateinit var deviceSpinner: Spinner
    private lateinit var status: TextView
    private lateinit var logView: TextView
    private lateinit var responseView: TextView

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) openDevice(device)
                else log("USB permission denied")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbManager = getSystemService(USB_SERVICE) as UsbManager
        registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION), RECEIVER_NOT_EXPORTED)
        buildUi()
        refreshDevices()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20,20,20,20); setBackgroundColor(0xFF0B0E14.toInt()) }
        val title = TextView(this).apply { text = "ZG AUTOSHOP\nHONDA MOTORCYCLE DIAGNOSTICS"; textSize = 21f; setTextColor(0xFF00E5FF.toInt()); setPadding(0,0,0,18) }
        root.addView(title)

        bikeSpinner = Spinner(this)
        bikeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Honda Beat FI V2 — KB1H-N52 IN 01", "Honda Click 125 — K-Line PGM-FI"))
        root.addView(bikeSpinner)

        deviceSpinner = Spinner(this)
        root.addView(deviceSpinner)

        val refresh = Button(this).apply { text = "REFRESH USB DEVICES"; setOnClickListener { refreshDevices() } }
        root.addView(refresh)
        val connect = Button(this).apply { text = "CONNECT / IDENTIFY"; setOnClickListener { requestSelectedDevice() } }
        root.addView(connect)
        val table = Button(this).apply { text = "READ RAW TABLE 0x11"; setOnClickListener { readTable(0x11) } }
        root.addView(table)
        val clear = Button(this).apply { text = "CLEAR LOG"; setOnClickListener { logView.text = ""; responseView.text = "" } }
        root.addView(clear)

        status = TextView(this).apply { text = "DISCONNECTED"; textSize = 16f; setTextColor(0xFFFF2A6D.toInt()); setPadding(0,14,0,8) }
        root.addView(status)
        responseView = TextView(this).apply { text = "Raw ECU response: —"; textSize = 14f; setTextColor(0xFFE0E6ED.toInt()); setPadding(0,8,0,8) }
        root.addView(responseView)
        logView = TextView(this).apply { textSize = 12f; setTextColor(0xFF00FF66.toInt()); setBackgroundColor(0xFF05070A.toInt()); setPadding(10,10,10,10) }
        root.addView(ScrollView(this).apply { addView(logView); layoutParams = LinearLayout.LayoutParams(-1,0,1f) })
        setContentView(root)
    }

    private fun refreshDevices() {
        val names = usbManager.deviceList.values.map { "${it.deviceName} VID=${it.vendorId.toString(16)} PID=${it.productId.toString(16)}" }
        deviceSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, if (names.isEmpty()) listOf("No USB serial device detected") else names)
        log(if (names.isEmpty()) "No USB device detected. Connect FT232 through USB-OTG." else "Found ${names.size} USB device(s).")
    }

    private fun selectedUsbDevice(): UsbDevice? = usbManager.deviceList.values.toList().getOrNull(deviceSpinner.selectedItemPosition)

    private fun requestSelectedDevice() {
        val device = selectedUsbDevice() ?: run { log("No USB device selected"); return }
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun openDevice(device: UsbDevice) {
        try {
            val drivers = UsbSerialProber.getDefaultProber().probeDevice(device)
            if (drivers == null || drivers.ports.isEmpty()) { log("No supported USB serial driver found"); return }
            val connection = usbManager.openDevice(device) ?: run { log("Could not open USB device"); return }
            port = drivers.ports[0]
            port!!.open(connection)
            port!!.setParameters(10400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            status.text = "USB CONNECTED — K-LINE READY"
            status.setTextColor(0xFF00FF66.toInt())
            log("Opened ${device.deviceName} at 10400 8N1")
            log("WARNING: USB serial is not the K-Line electrical interface. Use a proper K-Line transceiver.")
            ioManager = SerialInputOutputManager(port!!, this).also { it.start() }
            sendHondaWakeup()
        } catch (e: Exception) { log("Connect error: ${e.message}") }
    }

    private fun sendHondaWakeup() {
        // Conservative wake/init placeholder. Exact ECU framing must be validated on the physical ECU.
        log("Starting Honda K-Line initialization...")
        try {
            port?.write(byteArrayOf(0xFE.toByte(), 0x04, 0x72, 0x8C.toByte(), 0x00), 500)
            log("Wake/init frame sent; waiting for ECU response...")
        } catch (e: Exception) { log("Initialization error: ${e.message}") }
    }

    private fun readTable(table: Int) {
        val p = port ?: run { log("Connect first"); return }
        val frame = byteArrayOf(0x72, 0x05, 0x71, table.toByte(), 0x00)
        try { p.write(frame, 500); log("TX table 0x${table.toString(16).uppercase(Locale.US)}: ${hex(frame)}") }
        catch (e: Exception) { log("Read error: ${e.message}") }
    }

    override fun onNewData(data: ByteArray) {
        runOnUiThread { responseView.text = "Raw ECU response: ${hex(data)}"; log("RX: ${hex(data)}") }
    }

    override fun onRunError(e: Exception) { runOnUiThread { log("Serial error: ${e.message}"); status.text = "SERIAL ERROR"; status.setTextColor(0xFFFF2A6D.toInt()) } }

    private fun hex(data: ByteArray) = data.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
    private fun log(s: String) { runOnUiThread { logView.append("[${System.currentTimeMillis()/1000}] $s\n") } }

    override fun onDestroy() {
        ioManager?.stop(); ioManager = null
        try { port?.close() } catch (_: Exception) {}
        unregisterReceiver(usbReceiver)
        super.onDestroy()
    }
}
