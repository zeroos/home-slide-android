package fr.outadoc.quickhass.feature.onboarding.rest

import com.chuckerteam.chucker.api.ChuckerInterceptor
import fr.outadoc.quickhass.feature.onboarding.model.ApiStatus
import fr.outadoc.quickhass.feature.onboarding.model.DiscoveryInfo
import fr.outadoc.quickhass.feature.slideover.rest.wrapResponse
import okhttp3.logging.HttpLoggingInterceptor

class DiscoveryRepositoryImpl(
    loggingInterceptor: HttpLoggingInterceptor,
    chuckerInterceptor: ChuckerInterceptor
) : DiscoveryRepository {
    private val client: DiscoveryApi by lazy {
        SimpleRestClient.create<DiscoveryApi>(loggingInterceptor, chuckerInterceptor)
    }

    override suspend fun getDiscoveryInfo(baseUrl: String): Result<DiscoveryInfo> =
        wrapResponse { client.getDiscoveryInfo(baseUrl) }

    override suspend fun getApiStatus(baseUrl: String, token: String): Result<ApiStatus> =
        wrapResponse { client.getApiStatus(baseUrl, "Bearer $token") }
}