package technology.polygon.polygonid_android_sdk

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        callFlutterMethod(this)
    }

    fun callFlutterMethod(context: Context): CompletableFuture<Void> {
        val flutterEngine: FlutterEngine
        val completableFuture = CompletableFuture<Void>()

        if (FlutterEngineCache.getInstance().contains("engineFlex")) {
            flutterEngine = FlutterEngineCache.getInstance().get("engineFlex")!!
        } else {
            flutterEngine = FlutterEngine(applicationContext)
            FlutterEngineCache.getInstance().put("engineFlex", flutterEngine)
        }

        val dartExecutor = flutterEngine.dartExecutor
        dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint(
                "main.dart",
                "init"
            ),
        )

//        FlutterEngine(context).dartExecutor.executeDartEntrypoint(
//            DartExecutor.DartEntrypoint(
//                "main.dart",
//                "init"
//            ),
//        )

        val methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "technology.polygon.polygonid_flutter_wrapper")
        methodChannel.invokeMethod("initSDK", null, object : MethodChannel.Result {
            override fun success(result: Any?) {
                completableFuture.complete(null)
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                completableFuture.completeExceptionally(Throwable(errorMessage))
            }

            override fun notImplemented() {

            }
        })

//        val methodChannel2 = MethodChannel(FlutterEngine(context).dartExecutor.binaryMessenger, "technology.polygon.polygonid_flutter_wrapper")
//        methodChannel2.invokeMethod("hello", null, object : MethodChannel.Result {
//            override fun success(result: Any?) {
//                Log.d("TAG", "success: ${result as String}")
//            }
//
//            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
//                completableFuture.completeExceptionally(Throwable(errorMessage))
//            }
//
//            override fun notImplemented() {
//
//            }
//        })

        return completableFuture
    }
}