package fr.marc_nguyen.sensitivity.core.mappers

/**
 * Interface to map an object to a model
 */
interface ModelMappable<out R> {
    fun asModel(): R
}
