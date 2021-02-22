package fr.marc_nguyen.sensitivy.core.mappers

/**
 * Interface to map an object to a model
 */
interface ModelMappable<out R> {
    fun asModel(): R
}
