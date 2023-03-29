package technology.polygon.polygonid_android_sdk

import android.content.Context
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache

const val ENGINE = "PolygonIdEngine"

class PolygonIdSdk {
    private lateinit var flutterEngine: FlutterEngine

    companion object {
        private var ref: PolygonIdSdk? = null

        fun getInstance(): PolygonIdSdk {
            return ref
                ?: throw IllegalStateException("PolygonIdSdk not initialized, please call init() first")
        }

        fun init(context: Context) {
            ref = PolygonIdSdk()
        }
    }

    fun getEngine(context: Context): FlutterEngine {
        return if (FlutterEngineCache.getInstance().contains(ENGINE)) {
            FlutterEngineCache.getInstance().get(ENGINE)!!
        } else {
            val flutterEngine = FlutterEngine(context)
            FlutterEngineCache.getInstance().put(ENGINE, flutterEngine)

            flutterEngine
        }
    }
}