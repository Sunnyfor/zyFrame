package com.sunny.zy.bluetooth

import android.bluetooth.*
import com.sunny.zy.ZyFrameStore
import com.sunny.zy.bluetooth.bean.BluetoothBean
import com.sunny.zy.utils.LogUtil
import com.sunny.zy.utils.ToastUtil
import java.util.*

/**
 * Desc
 * Author ZhangYe
 * Mail zhangye98@foxmail.com
 * Date 2021/3/22 16:28
 */
object BluetoothConnector {

    const val STATE_CONNECTED = 0 //连接
    const val STATE_DIS_CONNECT = 1 //断开连接
    const val STATE_GATT_WRITE = 2 //可写入
    const val STATE_MESSAGE = 3 //可写入

    fun connect(device: BluetoothDevice, serviceId: String, notifyId: String): BluetoothBean? {

        val bluetoothBean = BluetoothBean(device)

        device.connectGatt(ZyFrameStore.getContext(), false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt?, status: Int, newState: Int
            ) {
                super.onConnectionStateChange(gatt, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        LogUtil.i("连接蓝牙成功:${device.address}  $status  $newState")
                        bluetoothBean.isConnect = true
                        bluetoothBean.gatt = gatt
                        gatt?.discoverServices()
                        bluetoothBean.receive(STATE_CONNECTED,"连接蓝牙成功")
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        bluetoothBean.isConnect = false
                        bluetoothBean.gatt = null
                        LogUtil.i("蓝牙设备断开:${device.address}  $status")
                        bluetoothBean.receive(STATE_DIS_CONNECT,"蓝牙设备断开")
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                LogUtil.i("发现服务:${status}")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val gattService = gatt?.getService(UUID.fromString(serviceId))
                    val characteristic = gattService?.getCharacteristic(UUID.fromString(notifyId))
                    val isOk = gatt?.setCharacteristicNotification(characteristic, true)
                    if (isOk == true) {
                        LogUtil.i("打开蓝牙通知!")
                        characteristic?.descriptors?.forEach {
                            val result =
                                it.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            if (result) {
                                LogUtil.i("设置蓝牙写入!")
                                gatt.writeDescriptor(it)
                                bluetoothBean.receive(STATE_GATT_WRITE,"蓝牙设备蓝牙写入")
                            }
                        }
                    }

                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                LogUtil.i("蓝牙数据onCharacteristicRead：${status}")
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                LogUtil.i("蓝牙数据onCharacteristicWrite：${status}")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                characteristic?.value?.let {
                    val result = String(it)
                    LogUtil.i("蓝牙数据：${result}")
                    bluetoothBean.receive(STATE_MESSAGE,result)
                }
            }
        })

        return bluetoothBean
    }

    /**
     * 发送消息
     */
    fun sendMsg(bluetoothBean: BluetoothBean, byteArray: ByteArray): Boolean {

        if (!bluetoothBean.isConnect) {
            ToastUtil.show("设备${bluetoothBean.device.address}未连接！")
            return false
        }
        val gattService = bluetoothBean.gatt?.getService(UUID.fromString(bluetoothBean.serviceId))
        val characteristic = gattService?.getCharacteristic(UUID.fromString(bluetoothBean.writeId))
        characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        characteristic?.value = byteArray
        LogUtil.i(
            "发送数据！${String(byteArray)}"
        )
        return bluetoothBean.gatt?.writeCharacteristic(characteristic) ?: false

    }
}