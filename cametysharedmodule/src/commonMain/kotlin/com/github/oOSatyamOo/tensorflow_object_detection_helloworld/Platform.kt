package com.github.oOSatyamOo.tensorflow_object_detection_helloworld

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform