package com.sunny.zy.http

import com.sunny.zy.http.bean.BaseHttpResultBean
import com.sunny.zy.http.bean.DownLoadResultBean
import com.sunny.zy.http.bean.HttpResultBean
import com.sunny.zy.http.bean.WebSocketResultBean
import com.sunny.zy.http.request.ZyRequest
import com.sunny.zy.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

/**
 * Desc
 * Author Zy
 * Date 2020/4/28 02:07
 */
@Suppress("UNCHECKED_CAST")
object ZyHttp {

    //请求创建器
    var zyRequest = ZyRequest()

    var clientFactory = OkHttpClientFactory()

    var FilePath = "path"

    /**
     * get请求
     * @param url URL服务器地址
     * @param params 传递的数据map（key,value)
     * @param httpResultBean 包含解析结果的实体bean
     */
    suspend fun <T : BaseHttpResultBean> get(
        url: String,
        params: Map<String, String>? = null,
        httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.getRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }


    suspend fun <T : BaseHttpResultBean> head(
        url: String,
        params: Map<String, String>? = null,
        httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.headRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }

    /**
     * postForm请求
     * @param url URL服务器地址
     * @param params 传递的数据map（key,value)
     * @param httpResultBean 包含解析结果的实体bean
     */
    suspend fun <T : BaseHttpResultBean> post(
        url: String,
        params: Map<String, String>?,
        httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.postFormRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }


    suspend fun <T : BaseHttpResultBean> postJson(url: String, json: String, httpResultBean: T) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.postJsonRequest(url, json)
            execution(request, httpResultBean, this)
        }
    }


    suspend fun <T : BaseHttpResultBean> patch(
        url: String,
        params: Map<String, String>?,
        httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.patchFormRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }

    suspend fun <T : BaseHttpResultBean> patchJson(url: String, json: String, httpResultBean: T) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.patchJsonRequest(url, json)
            execution(request, httpResultBean, this)
        }
    }


    suspend fun <T : BaseHttpResultBean> put(
        url: String, params: Map<String, String>?, httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.putFormRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }

    suspend fun <T : BaseHttpResultBean> putJson(url: String, json: String, httpResultBean: T) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.putJsonRequest(url, json)
            execution(request, httpResultBean, this)
        }
    }


    suspend fun <T : BaseHttpResultBean> delete(
        url: String, params: Map<String, String>?, httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.deleteFormRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }

    suspend fun <T : BaseHttpResultBean> deleteJson(url: String, json: String, httpResultBean: T) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.deleteJsonRequest(url, json)
            execution(request, httpResultBean, this)
        }

    }


    suspend fun <T : BaseHttpResultBean> formUpload(
        url: String,
        params: Map<String, String>,
        httpResultBean: T
    ) {
        return withContext(Dispatchers.IO) {
            //创建okHttp请求
            val request = zyRequest.formUploadRequest(url, params)
            execution(request, httpResultBean, this)
        }
    }


    fun webSocket(
        url: String,
        params: Map<String, String>?,
        webSocketResultBean: WebSocketResultBean
    ) {
        val request = zyRequest.getRequest(url, params)
        try {
            webSocketResultBean.webSocket =
                clientFactory.getOkHttpClient().newWebSocket(request, webSocketResultBean)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 执行网络请求并处理结果
     * @param request OkHttp请求对象
     * @param httpResultBean 包含解析结果的实体bean
     */
    private suspend fun <T : BaseHttpResultBean> execution(
        request: Request,
        httpResultBean: T,
        scope: CoroutineScope
    ) {
        try {
            httpResultBean.scope = scope
            //请求URL赋值
            httpResultBean.url = request.url.toString()
            //执行异步网络请求
            if (httpResultBean is DownLoadResultBean) {
                executeDownload(request, httpResultBean)
            } else if (httpResultBean is HttpResultBean<*>) {
                executeHttp(request, httpResultBean)
            }
        } catch (e: Exception) {
            //出现异常获取异常信息
            httpResultBean.exception = e
            httpResultBean.message = e.message ?: ""
            LogUtil.e("发生异常->:$httpResultBean")
        }

        withContext(Dispatchers.Main) {
            ZyConfig.httpResultCallback?.invoke(httpResultBean)
        }
    }


    private fun executeDownload(request: Request, resultBean: DownLoadResultBean) {
        resultBean.call = clientFactory.createDownloadClient(resultBean).newCall(request)
        resultBean.call?.execute()?.let { response ->
            //获取HTTP状态码
            resultBean.httpCode = response.code
            //获取Response回执信息
            resultBean.message = response.message
            //获取请求URL
            resultBean.url = request.url.toString()
            if (response.isSuccessful) {
                response.body?.let {
                    resultBean.file =
                        ZyConfig.iResponseParser.parserDownloadResponse(it, resultBean)
                }
            }
        }
    }

    private fun <T> executeHttp(request: Request, resultBean: HttpResultBean<T>) {
        resultBean.call = clientFactory.getOkHttpClient().newCall(request)
        resultBean.call?.execute()?.let { response ->
            //获取HTTP状态码
            resultBean.httpCode = response.code
            //获取Response回执信息
            resultBean.message = response.message
            //获取响应URL
            resultBean.resUrl = response.request.url.toString()

            if (response.isSuccessful) {
                response.body?.let {
                    resultBean.bean = ZyConfig.iResponseParser.parserHttpResponse(
                        it, resultBean
                    )
                }
            }
        }

    }
}