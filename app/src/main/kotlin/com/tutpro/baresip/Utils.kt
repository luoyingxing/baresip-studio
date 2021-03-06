package com.tutpro.baresip

import android.app.ActivityManager
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.Log
import android.os.PowerManager
import android.app.KeyguardManager

import java.io.*

object Utils {

    fun getFileContents(file: File): String {
        if (!file.exists()) {
            Log.e("Baresip", "Failed to find file: " + file.path)
            return ""
        } else {
            Log.e("Baresip", "Found file: " + file.path)
            val length = file.length().toInt()
            val bytes = ByteArray(length)
            try {
                val `in` = FileInputStream(file)
                try {
                    `in`.read(bytes)
                } finally {
                    `in`.close()
                }
                return String(bytes)
            } catch (e: java.io.IOException) {
                Log.e("Baresip", "Failed to read file: " + file.path + ": " +
                        e.toString())
                return ""
            }

        }
    }

    fun putFileContents(file: File, contents: String) {
        try {
            val fOut = FileOutputStream(file.absoluteFile, false)
            val fWriter = OutputStreamWriter(fOut)
            try {
                fWriter.write(contents)
                fWriter.close()
                fOut.close()
            } catch (e: java.io.IOException) {
                Log.e("Baresip", "Failed to put contents to file: " + e.toString())
            }

        } catch (e: java.io.FileNotFoundException) {
            Log.e("Baresip", "Failed to find contents file: " + e.toString())
        }

    }

    fun copyAssetToFile(context: Context, asset: String, path: String) {
        try {
            val `is` = context.assets.open(asset)
            val os = FileOutputStream(path)
            val buffer = ByteArray(512)
            var byteRead: Int = `is`.read(buffer)
            while (byteRead  != -1) {
                os.write(buffer, 0, byteRead)
                byteRead = `is`.read(buffer)
            }
            os.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e("Baresip", "Failed to read asset " + asset + ": " +
                    e.toString())
        }

    }

    fun alertView(context: Context, title: String, message: String) {
        // val alertDialog = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert).create()
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    fun uriHostPart(uri: String): String {
        return uri.substringAfter("@")
                .substringBefore(":")
                .substringBefore(";")
                .substringBefore(">")
    }

    fun uriUserPart(uri: String): String {
        return uri.substringAfter(":").substringBefore("@")
    }

    fun checkUserID(id: String): Boolean {
        return Regex("^[a-zA-Z]([._-]|[a-zA-Z0-9]){1,49}\$").matches(id)
    }

    fun checkTelNo(no: String): Boolean {
        return Regex("^[+]?[0-9]{1,16}\$").matches(no)
    }

    fun checkIP(ip: String): Boolean {
        return Regex("^(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))$").matches(ip)
    }

    fun checkUriUser(user: String): Boolean {
        for (c in user)
            if (!(c.isLetterOrDigit() || c in "-_.!~*'()&=+$,;?/")) return false
        return true
    }

    fun checkDomain(domain: String): Boolean {
        val parts = domain.split(".")
        for (p in parts) {
            if (p.length == 0 || p.endsWith("-") || !Regex("^[a-zA-z]([-]|[a-zA-Z0-9])+\$").matches(p))
                return false
        }
        return true
    }

    fun checkPort(p: String) : Boolean {
        val number = p.toIntOrNull()
        if (number == null) return false
        return (number > 0) and (number < 65536)
    }

    fun checkHostPort(hp: String) : Boolean {
        val parts = hp.split(":")
        if (parts.size == 1) return checkIP(parts[0]) || checkDomain(parts[0])
        return checkPort(parts[1]) && (checkIP(parts[0]) || checkDomain(parts[0]))
    }

    fun checkParams(p: String) : Boolean {
        /* todo: proper check */
        return true
    }

    fun checkHostPortParams(hpp: String) : Boolean {
        val restParams = hpp.split(";", limit = 2)
        if (restParams.size == 1)
            return checkHostPort(restParams[0])
        else
            return checkHostPort(restParams[0]) && checkParams(restParams[1])
    }

    fun checkSipUri(uri: String): Boolean {
        if (!uri.startsWith("sip:")) return false
        val userRest = uri.substring(4).split("@")
        if (userRest.size == 1) {
            return checkHostPortParams(userRest[0])
        } else if (userRest.size == 2) {
            return checkUriUser(userRest[0]) && checkHostPortParams(userRest[1])
        } else
            return false
    }

    fun checkAorUri(uri: String): Boolean {
        if (!uri.startsWith("sip:")) return false
        val userDomain = uri.replace("sip:", "").split("@")
        if (userDomain.size != 2) return false
        if (!checkUserID(userDomain[0]) && !checkTelNo(userDomain[0])) return false
        return checkDomain(userDomain[1]) || checkIP(userDomain[1])
    }

    fun checkOutboundUri(uri: String): Boolean {
        if (!uri.startsWith("sip:")) return false
        return checkHostPortParams(uri.substring(4))
    }

    fun checkPrintASCII(s: String): Boolean {
        if (s == "") return true
        val printASCIIRegex = Regex("^[ -~]*\$")
        return printASCIIRegex.matches(s)
    }

    fun checkUint(s: String): Boolean {
        val uintRegex = Regex("^[0-9]+\$")
        return uintRegex.matches(s)
    }

    fun checkName(name: String): Boolean {
        if (name.length < 2) return false
        for (c in name) {
            if (!c.isLetterOrDigit() && !(c in "-.!%*_+`'~ ")) return false
        }
        return true
    }

    fun implode(list: List<String>, sep: String): String {
        var res = ""
        for (s in list) {
            if (res == "")
                res = s
            else
                res = res + sep + s
        }
        return res
    }

    fun isVisible(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    fun isDeviceLocked(context: Context): Boolean {
        val isLocked: Boolean

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode()

        if (inKeyguardRestrictedInputMode) {
            isLocked = true
        } else {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            isLocked = !powerManager.isInteractive
        }

        Log.d("Baresip", "Now device is ${if (isLocked) "locked" else "unlocked"}")
        return isLocked
    }

}
