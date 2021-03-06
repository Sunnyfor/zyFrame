package com.sunny.zy.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sunny.zy.R
import com.sunny.zy.base.BaseActivity
import com.sunny.zy.utils.QRCodeUtil
import kotlinx.android.synthetic.main.zy_frag_qr_code.*

/**
 * Desc
 * Author ZhangYe
 * Mail zhangye98@foxmail.com
 * Date 2021/1/4 16:54
 */
class QRCodeActivity : BaseActivity() {

    companion object {
        const val resultKey = "qrCode"

        fun intent(activity: AppCompatActivity, resultCallBack: (result: String) -> Unit) {
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    resultCallBack.invoke(it.data?.getStringExtra(resultKey) ?: "")
                }
            }.launch(Intent(activity, QRCodeActivity::class.java))
        }
    }


    private val qrCodeUtil: QRCodeUtil by lazy {
        QRCodeUtil {
            intent.putExtra(resultKey, it)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun initLayout() = R.layout.zy_frag_qr_code

    override fun initView() {
        setTitleDefault("扫一扫")
        setPermissionsCancelFinish(true)
        setPermissionsNoHintFinish(true)

        requestPermissions(Manifest.permission.CAMERA) {
            qrCodeUtil.init(this, previewView.surfaceProvider)
        }
    }


    override fun onClickEvent(view: View) {

    }

    override fun loadData() {

    }

    override fun onClose() {
        qrCodeUtil.onDestroy()
    }
}