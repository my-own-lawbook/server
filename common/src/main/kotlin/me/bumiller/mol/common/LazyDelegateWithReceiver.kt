package me.bumiller.mol.common

import java.util.*
import kotlin.reflect.KProperty

fun <This, Return> lazyWithReceiver(initializer: This.() -> Return): LazyWithReceiver<This, Return> = LazyWithReceiver(initializer)

/**
 * Custom implementation of a [Lazy] that allows referencing the receiver in the case of an extension property
 */
class LazyWithReceiver<This, out Return>(val initializer: This.() -> Return) {
    private val values = WeakHashMap<This, Return>()

    operator fun getValue(thisRef: This, property: KProperty<*>): Return = synchronized(values)
    {
        return values.getOrPut(thisRef) { thisRef.initializer() }
    }
}