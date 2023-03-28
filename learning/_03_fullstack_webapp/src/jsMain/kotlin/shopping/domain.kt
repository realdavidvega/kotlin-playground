package shopping

@JsModule("uuid")
@JsNonModule
@JsName("v4")
external fun uuidV4(): String

actual fun randomUUID(): String = uuidV4()
