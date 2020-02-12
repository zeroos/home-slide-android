package fr.outadoc.homeslide.app.feature.slideover

import fr.outadoc.homeslide.app.persistence.EntityDao
import fr.outadoc.homeslide.hassapi.api.HomeAssistantApi
import fr.outadoc.homeslide.hassapi.factory.EntityFactory
import fr.outadoc.homeslide.hassapi.factory.TileFactory
import fr.outadoc.homeslide.hassapi.model.Action
import fr.outadoc.homeslide.hassapi.model.EntityState
import fr.outadoc.homeslide.hassapi.model.PersistedEntity
import fr.outadoc.homeslide.hassapi.model.Service
import fr.outadoc.homeslide.hassapi.model.Tile
import fr.outadoc.homeslide.hassapi.model.entity.Cover
import fr.outadoc.homeslide.hassapi.model.entity.Entity
import fr.outadoc.homeslide.hassapi.model.entity.Light
import fr.outadoc.homeslide.hassapi.model.entity.Weather
import fr.outadoc.homeslide.hassapi.repository.EntityRepository
import fr.outadoc.homeslide.rest.util.wrapResponse

class EntityRepositoryImpl(
    private val dao: EntityDao,
    private val tileFactory: TileFactory,
    private val client: HomeAssistantApi
) : EntityRepository {

    override suspend fun getEntityTiles(): Result<List<Tile<Entity>>> {
        val persistedEntities = dao.getPersistedEntities()
            .map { it.entityId to it }
            .toMap()

        return wrapResponse { client.getStates() }
            .map { states ->
                states.asSequence()
                    .map { EntityFactory.create(it) }
                    .map { entity ->
                        val persistedEntity = persistedEntities[entity.entityId]
                        tileFactory
                            .create(entity)
                            .copy(
                                isHidden = persistedEntity?.hidden
                                    ?: !entity.isVisible || INITIAL_DOMAIN_BLACKLIST.contains(entity.domain)
                            )
                }
                .sortedWith(
                    compareBy(
                        // If the user has already ordered the item manually, use that order
                        // Otherwise put it at the end of the list initially
                        { tile -> persistedEntities[tile.source.entityId]?.order ?: Int.MAX_VALUE },

                        // Shove hidden tiles to the end of the list initially
                        { tile -> tile.isHidden },

                        // Order by domain priority (put lights and covers first for example)
                        { tile ->
                            getPriorityForDomain(tile.source.domain) ?: Int.MAX_VALUE
                        },

                        // Order by domain so that the items are somewhat sorted
                        { tile -> tile.source.domain },

                        // Order by label within a domain
                        { tile -> tile.source.friendlyName }
                    )
                )
                    .toList()
            }
    }

    override suspend fun saveEntityListState(entities: List<PersistedEntity>) {
        dao.replaceAll(entities)
    }

    private fun getPriorityForDomain(domain: String): Int? {
        return when (domain) {
            Light.DOMAIN -> 0
            Cover.DOMAIN -> 1
            Weather.DOMAIN -> 2
            else -> null
        }
    }

    override suspend fun getServices(): Result<List<Service>> =
        wrapResponse { client.getServices() }

    override suspend fun callService(action: Action): Result<List<EntityState>> =
        wrapResponse {
            client.callService(
                action.domain,
                action.service,
                action.allParams
            )
        }

    companion object {
        val INITIAL_DOMAIN_BLACKLIST = listOf(
            "automation",
            "device_tracker",
            "updater",
            "camera",
            "persistent_notification"
        )
    }
}