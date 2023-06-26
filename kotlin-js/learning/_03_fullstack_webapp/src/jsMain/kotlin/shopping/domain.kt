package shopping

@JsModule("uuid")
@JsNonModule
external object UUID {
    @JsName("v4")
    fun uuidV4(): String
}

actual fun randomUUID(): String = UUID.uuidV4()
