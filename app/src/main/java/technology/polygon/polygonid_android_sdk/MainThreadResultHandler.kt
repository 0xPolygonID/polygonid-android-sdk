package technology.polygon.polygonid_android_sdk

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.MethodChannel

class MainThreadResultHandler(result: MethodChannel.Result) : MethodChannel.Result {
    private var result: MethodChannel.Result? = result
    private var handler: Handler = Handler(Looper.getMainLooper())

    override fun success(result: Any?) {
        handler.post {
            this.result?.success(result)
        }
    }

    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
        handler.post {
            this.result?.error(errorCode, errorMessage, errorDetails)
        }
    }

    override fun notImplemented() {
        handler.post {
            this.result?.notImplemented()
        }
    }
}