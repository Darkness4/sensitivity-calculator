package fr.marc_nguyen.sensitivy.core.mappers

/**
 * Interface to map an object to an entity
 */
interface EntityMappable<out R> {
    fun asEntity(): R
}
