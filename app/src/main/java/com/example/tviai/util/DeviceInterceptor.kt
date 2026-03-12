package com.example.tviai.util

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

object DeviceInterceptor {

    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "brand" to Build.BRAND,
            "model" to Build.MODEL,
            "device" to Build.DEVICE,
            "os_version" to Build.VERSION.RELEASE,
            "sdk_int" to Build.VERSION.SDK_INT.toString(),
            "manufacturer" to Build.MANUFACTURER
        )
    }

    @RequiresPermission(allOf = ["android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS"], anyOf = ["android.permission.READ_SMS"])
    fun getPhoneNumber(context: Context): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val number = telephonyManager.line1Number
            if (number.isNullOrEmpty()) "Unknown" else number
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
