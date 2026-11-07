package tn.esprit.drivesafeandroid.ml

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
class AlertPredictor (context: Context){

        private val interpreter: Interpreter by lazy {
            val model = FileUtil.loadMappedFile(context, "decision_tree.tflite")
            Interpreter(model)
        }

        /**
         * Predict alert level based on current sensor readings.
         * @param vehicleMoving - 1 if moving, 0 if stopped
         * @param phoneUse - 1 if user interacting with phone
         * @param behavior - 1 if risky behavior detected (texting, scrolling, call)
         * @param speed - current speed in km/h
         * @return alert level: 0 (none), 1 (low), 2 (high)
         */
        fun predict(vehicleMoving: Float, phoneUse: Float, behavior: Float, speed: Float): Int {
            val input = arrayOf(floatArrayOf(vehicleMoving, phoneUse, behavior, speed))
            val output = FloatArray(1)
            interpreter.run(input, output)
            return output[0].toInt()
        }



}