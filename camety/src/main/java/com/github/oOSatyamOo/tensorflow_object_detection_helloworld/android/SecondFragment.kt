package com.github.oOSatyamOo.tensorflow_object_detection_helloworld.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Bundle
import android.os.FileUtils
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.oOSatyamOo.tensorflow_object_detection_helloworld.android.databinding.FragmentSecondBinding
import com.github.oOSatyamOo.tensorflow_object_detection_helloworld.android.ml.AutoModel1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class   SecondFragment : Fragment() {

     var _binding: FragmentSecondBinding?=null
    lateinit var myHandler : Handler
    lateinit var cameraDevice: CameraDevice
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var cameraManager :CameraManager
    lateinit var surface: Surface
    val handlerThred:HandlerThread = HandlerThread("videoThread")
    lateinit var autoModel:AutoModel1
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
//    lateinit var imageView: ImageView
    lateinit var labels:List<String>
    var colors = listOf<Int> (
        Color.BLUE,Color.GREEN,Color.RED,Color.CYAN,Color.GRAY,Color.BLACK,Color.DKGRAY,Color.MAGENTA,Color.YELLOW,Color.YELLOW
    )
    val paint = Paint()



    override fun onCreate(savedInstanceState: Bundle?) {
        handlerThred.start()
        myHandler = Handler(handlerThred.looper)
        autoModel = AutoModel1.newInstance(requireContext())
        imageProcessor= ImageProcessor.Builder().add(ResizeOp(300,300,ResizeOp.ResizeMethod.BILINEAR)).build()
        labels= FileUtil.loadLabels(requireContext(),"labels.txt")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

       _binding!!.textureView.surfaceTextureListener =  object: TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface0: SurfaceTexture) {
                bitmap =  _binding!!.textureView.bitmap!!

// Creates inputs for reference.
                var image = TensorImage.fromBitmap(bitmap)

// Runs model inference and gets result.
                val outputs = autoModel.process(image)
                val locations = outputs.locationsAsTensorBuffer.floatArray
                val classes = outputs.classesAsTensorBuffer.floatArray
                val scores = outputs.scoresAsTensorBuffer.floatArray
                val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

                image = imageProcessor.process(image)
//
                var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)

                val h = mutable.height
                val w = mutable.width
                paint.textSize = h/15f
                paint.strokeWidth = h/85f
                var x = 0
                scores.forEachIndexed { index, fl ->
                    x = index
                    x *= 4
                    if(fl > 0.5){
                        paint.setColor(colors.get(index))
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(RectF(locations.get(x+1)*w, locations.get(x)*h, locations.get(x+3)*w, locations.get(x+2)*h), paint)
                        paint.style = Paint.Style.FILL
                        canvas.drawText(labels.get(classes.get(index).toInt())+" "+fl.toString(), locations.get(x+1)*w, locations.get(x)*h, paint)
                    }
                }

              _binding!!.imageView.setImageBitmap(mutable)

            }

        }

        cameraManager   =    requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
         return binding.root

    }
    @SuppressLint("MissingPermission")
    fun openCamera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0],object:CameraDevice.StateCallback(){
            override fun onOpened(camera0: CameraDevice) {
                cameraDevice=camera0
//                cameraManager.setTorchMode(cameraDevice.id,true)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    cameraManager.getTorchStrengthLevel(cameraDevice.id)
//                }
                surface= Surface(_binding!!.textureView.surfaceTexture)
                val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

//                cameraDevice.createSession(listOf(surface),object :CameraCaptureSession.StateCallback(){
//                    override fun onConfigured(session0: CameraCaptureSession) {
//                        session0.setRepeatingRequest(captureRequest.build(),null,null)
//                    }
//
//                    override fun onConfigureFailed(session: CameraCaptureSession) {
//                        TODO("Not yet implemented")
//                    }
//                },myHandler)
                val stateCallback = object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        // The camera is already closed
//                        if (cameraDevice == null) {
//                            return
//                        }
                        // When the session is ready, we start displaying the preview.
                        try {
                            session.setRepeatingRequest(captureRequest.build(), null, null)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        // Handle configuration failure
                    }
                }
                val executor = Executors.newSingleThreadExecutor()

                val sessionConfiguration = SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listOf(OutputConfiguration(surface)),
                    executor,
                    stateCallback
                )
                cameraDevice.createCaptureSession(sessionConfiguration)
            }

            override fun onDisconnected(camera: CameraDevice) {
                TODO("Not yet implemented")
                if (cameraDevice == null) {
                            return
                        }
            }

            override fun onError(camera: CameraDevice, error: Int) {
                TODO("Not yet implemented")
            }
        },myHandler)
    }
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        _binding!!.fab.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        surface.release()

        _binding=null
        cameraDevice.close()
        surface.release()
        handlerThred.quit()
        bitmap.recycle()
        autoModel.close()
//        imageView

    }

    override fun onDestroy() {
        handlerThred.quit()
        super.onDestroy()
    }
}