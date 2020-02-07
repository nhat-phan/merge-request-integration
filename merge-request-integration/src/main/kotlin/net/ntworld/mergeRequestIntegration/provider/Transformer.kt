package net.ntworld.mergeRequestIntegration.provider

interface Transformer<T, R> {
    fun transform(input: T): R
}
