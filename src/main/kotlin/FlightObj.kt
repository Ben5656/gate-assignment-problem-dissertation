data class FlightObj(
    val request: Request?,
    val response: List<Response>?,
    val terms: String?
)

data class Request(
    val client: Client?,
    val currency: String?,
    val host: String?,
    val id: String?,
    val key: Key?,
    val lang: String?,
    val method: String?,
    val params: Params?,
    val pid: Int?,
    val server: String?,
    val time: Int?,
    val version: Int?
)

data class Response(
    val aircraft_icao: String?,
    val airline_iata: String?,
    val airline_icao: String?,
    val arr_actual: String?,
    val arr_actual_ts: Int?,
    val arr_actual_utc: String?,
    val arr_baggage: String?,
    val arr_delayed: Int?,
    val arr_estimated: String?,
    val arr_estimated_ts: Int?,
    val arr_estimated_utc: String?,
    val arr_gate: String?,
    val arr_iata: String?,
    val arr_icao: String?,
    val arr_terminal: String?,
    val arr_time: String?,
    val arr_time_ts: Int?,
    val arr_time_utc: String?,
    val cs_airline_iata: String?,
    val cs_flight_iata: String?,
    val cs_flight_number: String?,
    val delayed: Int?,
    val dep_actual: String?,
    val dep_actual_ts: Int?,
    val dep_actual_utc: String?,
    val dep_delayed: Int?,
    val dep_estimated: String?,
    val dep_estimated_ts: Int?,
    val dep_estimated_utc: String?,
    val dep_gate: String?,
    val dep_iata: String?,
    val dep_icao: String?,
    val dep_terminal: String?,
    val dep_time: String?,
    val dep_time_ts: Int?,
    val dep_time_utc: String?,
    val duration: Int?,
    val flight_iata: String?,
    val flight_icao: String?,
    val flight_number: String?,
    val status: String?,
    var turnaround: Int?,
    var turnaround_ts: Int?
)

data class Client(
    val agent: Agent?,
    val connection: Connection?,
    val device: Device?,
    val geo: Geo?,
    val ip: String?,
    val karma: Karma?
)

data class Key(
    val api_key: String?,
    val expired: String?,
    val id: Int?,
    val limits_by_hour: Int?,
    val limits_by_minute: Int?,
    val limits_by_month: Int?,
    val limits_total: Int?,
    val registered: String?,
    val type: String?
)

data class Params(
    val dep_icao: String?,
    val arr_icao: String?,
    val lang: String?
)

class Agent

data class Connection(
    val isp_code: Int?,
    val isp_name: String?,
    val type: String?
)

class Device

data class Geo(
    val continent: String?,
    val country: String?,
    val country_code: String?,
    val city: String?,
    val lat: Double?,
    val lng: Double?,
    val timezone: String?
)

data class Karma(
    val is_blocked: Boolean?,
    val is_bot: Boolean?,
    val is_crawler: Boolean?,
    val is_friend: Boolean?,
    val is_regular: Boolean?
)