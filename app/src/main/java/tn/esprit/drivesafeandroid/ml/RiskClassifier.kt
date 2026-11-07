package tn.esprit.drivesafeandroid.ml
//import ai.onnxruntime.OnnxTensor
//import ai.onnxruntime.OrtEnvironment
//import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context

class RiskClassifier(context: Context) {

    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        // Load model from assets as byte array
        val modelBytes = context.assets.open("risk_classifier_dt.onnx").readBytes()
        session = env.createSession(modelBytes)
    }

    // Prepare input and run model
    // features = [notification_type, is_interactive, speed, screen_on, is_call]
    fun predictRisk(features: LongArray): Int {
        // ONNX Runtime expects a 2D tensor: [batch_size, feature_count]
        val inputTensor = OnnxTensor.createTensor(env, arrayOf(features))
        val results = session.run(mapOf("input" to inputTensor))
        val outputTensor = results[0].value as Array<LongArray>
        return outputTensor[0][0].toInt()
    }
}
