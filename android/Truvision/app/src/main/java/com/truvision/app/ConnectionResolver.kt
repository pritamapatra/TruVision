package com.truvision.app

object ConnectionResolver {
    private const val PI_WIFI_IP = "192.168.4.1"
    private const val PI_USB_IP = "192.168.238.191"  // Updated to current IP
    private const val PORT = "8000"
    
    val WIFI_BASE_URL = "http://$PI_WIFI_IP:$PORT"
    val USB_BASE_URL = "http://$PI_USB_IP:$PORT"
}
