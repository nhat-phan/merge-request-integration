package net.ntworld.mergeRequestIntegration.provider.gitlab

interface Transformer<T, R> {
    fun transform(input: T): R
}
