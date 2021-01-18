package com.sunny.zy.utils

import java.text.DecimalFormat
import java.util.*

/**
 * 字符串工具类
 * Created by Zy on 2018/6/11.
 */
object StringUtil {
    /**
     * 金额格式化
     * @param money 金额
     * @param len 小数位数
     * @return 格式后的金额
     */

    fun formatMoney(money: String?, len: Int, pattern: String? = null): String {
        if (money == null || money.isEmpty() || money == "0.0" || money == "0" || money == "0.00") {
            return "0.00"
        }
        val num = money.toDouble()

        if (pattern != null) {
            val mFormat = DecimalFormat(pattern)
            return mFormat.format(num)
        }
        val mFormat = if (len == 0) {
            DecimalFormat("#")
        } else {
            val buff = StringBuffer()
            buff.append("#.")
            for (i in 0 until len) {
                buff.append("0")
            }
            DecimalFormat(buff.toString())
        }
        return mFormat.format(num)
    }


    fun formatMoney(money: String?, pattern: String) = formatMoney(money, 0, pattern)
    fun formatMoney(money: Double): String = formatMoney(money.toString(), 2)
    fun formatMoney(money: Float): String = formatMoney(money.toString(), 2)


    //根据长度生成随机字符串
    fun getRandomChar(length: Int): String {            //生成随机字符串
        val chr = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z'
        )
        val random = Random()
        val buffer = StringBuffer()
        for (i in 0 until length) {
            buffer.append(chr[random.nextInt(36)])
        }
        return buffer.toString()
    }


    //生成指定范围的随机数
    fun getRandomChar(min: Int, max: Int): String {
        return getRandomChar(Random().nextInt(max - min) + min)
    }

    fun getTimeStamp(): String = (System.currentTimeMillis() / 1000).toString()
}