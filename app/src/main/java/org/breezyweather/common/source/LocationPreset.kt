/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.source

import breezyweather.domain.location.model.Location
import org.breezyweather.BuildConfig
import java.util.Locale

/**
 * When a preset doesn't have a secondary source listed (null values), it will use main source
 * Current recommendations:
 * - Main source: national weather source or Open-Meteo
 * - Air quality: national weather source or Open-Meteo
 * - Pollen: Open-Meteo
 * - Minutely: national weather source or Open-Meteo
 * - Alerts: national weather source or AccuWeather (or WMO if Accu is broken, but should be avoided as it is a very slow source)
 * - Normals: national weather source or AccuWeather
 */
enum class LocationPreset(
    val main: String,
    val airQuality: String? = null,
    val pollen: String? = null,
    val minutely: String? = null,
    val alert: String? = null,
    val normals: String? = null
) {
    DEFAULT("openmeteo", alert = "accu", normals = "accu"),
    DEFAULT_FDROID("openmeteo"),

    // North America
    CANADA("eccc", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo"),
    USA("nws", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo", normals = "accu"),

    // Europe
    //AUSTRIA("openmeteo" /* GeoSphere too lightweight */, airQuality = "geosphereat", minutely = "geosphereat", alert = "geosphereat", normals = "geosphereat"),
    DENMARK("dmi", airQuality = "openmeteo", pollen = "openmeteo", minutely = "metno", normals = "accu"),
    GERMANY("brightsky", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo", normals = "accu"),
    GERMANY_FDROID("brightsky", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo"),
    FINLAND("metno", airQuality = "openmeteo", pollen = "openmeteo", alert = "accu", normals = "accu"),
    FRANCE("mf", airQuality = "openmeteo", pollen = "recosante"),
    FRANCE_FDROID("openmeteo", pollen = "recosante"),
    IRELAND("metie", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo", normals = "accu"),
    NORWAY("metno", pollen = "openmeteo", alert = "accu", normals = "accu"),
    SWEDEN("smhi", airQuality = "openmeteo", pollen = "openmeteo", minutely = "metno", alert = "accu", normals = "accu"),

    // Asia
    // Do NOT set up other sources as only 中国 source is not rate-limited by the Great Firewall
    CHINA("china"),
    HONG_KONG("openmeteo", alert = "wmosevereweather", normals = "accu"),
    ISRAEL("ims", airQuality = "openmeteo", pollen = "openmeteo", minutely = "openmeteo");

    companion object {
        fun getLocationPreset(countryCode: String?): LocationPreset {
            if (countryCode.isNullOrEmpty()) return DEFAULT
            return if (BuildConfig.FLAVOR != "fdroid") {
                when (countryCode.uppercase(Locale.ENGLISH)) {
                    // North America
                    "CA" -> CANADA
                    "US", "PR", "VI", "MP", "GU", "FM", "PW", "AS" -> USA

                    // Europe
                    "DE" -> GERMANY
                    "DK" -> DENMARK
                    "FI" -> FINLAND
                    "FR" -> FRANCE
                    "IE" -> IRELAND
                    "NO" -> NORWAY
                    "SE" -> SWEDEN

                    // Asia
                    "CN" -> CHINA
                    "HK" -> HONG_KONG
                    "IL", "PS" -> ISRAEL

                    else -> DEFAULT
                }
            } else {
                when (countryCode.uppercase(Locale.ENGLISH)) {
                    // Europe
                    "DE" -> GERMANY_FDROID
                    "FR" -> FRANCE_FDROID

                    else -> DEFAULT_FDROID
                }
            }
        }

        fun getLocationWithPresetApplied(location: Location): Location {
            val locationPreset = getLocationPreset(location.countryCode)

            return location.copy(
                weatherSource = locationPreset.main,
                airQualitySource = locationPreset.airQuality,
                pollenSource = locationPreset.pollen,
                minutelySource = locationPreset.minutely,
                alertSource = locationPreset.alert,
                normalsSource = if (location.isCurrentPosition && locationPreset.normals == "accu") {
                    // Special case: if current position, normals are queried at every coordinates
                    // change (instead of once a month), so we want to avoid presetting a minor
                    // feature an user might not be interested in, especially as AccuWeather has not
                    // the best privacy-policy
                    null
                } else locationPreset.normals,
            )
        }
    }
}
