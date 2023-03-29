package technology.polygon.polygonid_android_sdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
const val TAG = "TestTag"

@RunWith(AndroidJUnit4::class)
class SDKInstrumentedTest {
    @Test
    @UiThreadTest
    fun initSDK() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val dartExecutor = FlutterEngine(appContext).dartExecutor
        dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                "main.dart",
                "init"
            ),
        )

        val methodChannel = MethodChannel(FlutterEngine(appContext).dartExecutor.binaryMessenger, "technology.polygon.polygonid_flutter_wrapper")
        methodChannel.invokeMethod("initSDK", null, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Log.d(TAG, "success: ")
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                // completableFuture.completeExceptionally(Throwable(errorMessage))
                Log.d(TAG, "Error")
            }

            override fun notImplemented() {

            }
        })

        assertEquals("technology.polygon.polygonid_android_sdk.test", appContext.packageName)
    }
}