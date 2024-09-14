package me.bumiller.mol.common

/**
 * Calls the parameter if this collection is not empty
 *
 * @param call The call
 */
fun <T> List<T>.ifNotEmpty(call: () -> Unit) =
    if(isEmpty()) Unit else call()