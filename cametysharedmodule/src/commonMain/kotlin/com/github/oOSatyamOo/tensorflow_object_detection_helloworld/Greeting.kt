package com.github.oOSatyamOo.tensorflow_object_detection_helloworld

class Greeting {
    private val platform: Platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}