package io.github.duzhaokun123.fuckcainiao

fun Any.dump(): String {
    val sb = StringBuilder()
    this.javaClass.fields.forEach {
        sb.append("${it.name}: ${it.get(this)}\n")
    }
    return sb.toString()
}