import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class GeoCodeApi(
    private val latitude: Double,
    private val longitude: Double,
    private val context: Context,
    private val listener: GeoCodeListener?
) {

    private var geoAddress: GeoAddress? = null

    suspend fun getAddress() {
        try {
            withContext(Dispatchers.IO) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (addresses != null && addresses.isNotEmpty()) {
                    geoAddress = GeoAddress().apply {
                        address = addresses[0].subLocality + ", " + addresses[0].locality + ", " +
                                addresses[0].adminArea + ", " + addresses[0].postalCode + ", " +
                                addresses[0].countryName
                        pinCode = addresses[0].postalCode
                        city = addresses[0].locality
                    }
                }
            }
            returnAddress()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle IOException here (e.g., show an error message to the user)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle other exceptions here if needed
        }
    }

    private fun returnAddress() {
        if (listener != null) {
            listener.onGeoAddressFound(geoAddress)
        }
    }

    class GeoAddress {
        var address: String? = null
        var city: String? = null
        var pinCode: String? = null

        override fun toString(): String {
            return "GeoAddress{" +
                    "address='" + address + '\'' +
                    '}'
        }
    }

    interface GeoCodeListener {
        fun onGeoAddressFound(geoAddress: GeoAddress?)
    }
}
