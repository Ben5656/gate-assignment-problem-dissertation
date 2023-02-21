import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.harium.dotenv.Env
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

fun main(args: Array<String>) {
    /**
     * Gatwick = EGKK
     * Heathrow = EGLL
     * Manchester = EGCC
     * East Mid = EGNX
     * Farnborough = EGLF
     * Guernsey = EGJB
     * Los Angeles = KLAX
     *
     * Terminal = "" for all
     */

    generateData("EGJB", "")
}

private fun generateData(airportICAO: String, terminal: String) {
    val toFlightObj: FlightObj = jacksonObjectMapper().readValue(flightAPICall(airportICAO))

    val flightsArray = toFlightObj.response!!.filter {
        if (terminal.isEmpty()) {
            it.status != "cancelled" && it.dep_time_ts!! > (System.currentTimeMillis() / 1000)
        } else {
            (it.arr_terminal == terminal || it.dep_terminal == terminal) &&
                    it.status != "cancelled" &&
                    it.dep_time_ts!! > (System.currentTimeMillis() / 1000)
        }
    }
    System.out.printf(
        "| %-10s | %-7s | %-9s | %-8s | %-8s | %-3s | %-3s | %-8s | %-8s | %-7s | %-7s |\n",
        "Flight Num", "Airline", "Status", "Terminal", "Aircraft", "Dep", "Arr", "Dep Time", "Arr Time", "Dep Est", "Arr Est"
    )
    flightsArray.map {
        System.out.printf(
            "| %-10s | %-7s | %-9s | %-8s | %-8s | %-3s | %-3s | %-8s | %-8s | %-7s | %-7s |\n",
            it.flight_icao ?: "-",
            it.airline_iata ?: "-",
            it.status ?: "-",
            it.dep_terminal ?: "-",
            it.aircraft_icao ?: "-",
            it.dep_iata ?: "-",
            it.arr_iata ?: "-",
            it.dep_time?.takeLast(5) ?: "-",
            it.arr_time?.takeLast(5) ?: "-",
            it.dep_estimated?.takeLast(5) ?: "-",
            it.arr_estimated?.takeLast(5) ?: "-"
        )
    }

    if (terminal.isEmpty()) println("\nThe airport $airportICAO has ${flightsArray.size} scheduled arriving/departing flights:\n")
    else println("\nTerminal $terminal has ${flightsArray.size} scheduled arriving/departing flights\n")

    //println("Generated pastebin with times: ${pasteBinAPICall(pasteStringGenerator(airportICAO, flightsArray))}")
    println("Generated text: ${pasteStringGenerator(airportICAO, flightsArray)}")
    println("More data available here: https://uk.flightaware.com/live/airport/$airportICAO")
}

private fun flightAPICall(airportICAO: String): String {
    val request = Request.Builder()
        .url(
            "https://airlabs.co/api/v9/schedules" +
                    "?dep_icao=$airportICAO" +
                    "&api_key=${Env.get("FLIGHTS_API_KEY")}")
        .get()
        .build()

    return OkHttpClient().newCall(request).execute().body!!.string()
}

private fun pasteStringGenerator(airportICAO: String, flightsArray: List<Response>): String {
    var dataString = "Airport: $airportICAO, which has ${flightsArray.size} flights arriving/departing.\n\nflights={"
    flightsArray.forEach {
        dataString += "<${it.flight_icao}, ${it.arr_time_ts}, ${it.dep_time_ts}>, "
    }
    return dataString.dropLast(2) + "}"
}

private fun pasteBinAPICall(textToPaste: String): String {
    val requestBody = FormBody.Builder()
        .add("api_dev_key", Env.get("PASTEBIN_API_KEY"))
        .add("api_option", "paste")
        .add("api_paste_code", textToPaste)
        .build()

    val request = Request.Builder()
        .url("https://pastebin.com/api/api_post.php")
        .post(requestBody)
        .build()

    return OkHttpClient().newCall(request).execute().body!!.string()
}