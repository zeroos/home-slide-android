package fr.outadoc.quickhass.feature.onboarding.rest

import fr.outadoc.quickhass.feature.onboarding.model.DiscoveryInfo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DiscoveryApi {

    @GET("{baseUrl}/api/discovery_info")
    suspend fun getDiscoveryInfo(@Path("baseUrl", encoded = true) baseUrl: String): Response<DiscoveryInfo>
}