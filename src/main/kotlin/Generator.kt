import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.harium.dotenv.Env
import okhttp3.FormBody
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


fun main(args: Array<String>) {
    /**
     * Gatwick = EGKK
     * Heathrow = EGLL
     * Manchester = EGCC
     * East Mid = EGNX
     * Farnborough = EGLF
     * Guernsey = EGJB
     */

    // Terminal = "" for all
    generateData("EGNX", "")
}

private fun generateData(airportICAO: String, terminal: String) {
    val toFlightObj: FlightObj = jacksonObjectMapper().readValue(apiRequest(airportICAO).body())

    val flightsArray = toFlightObj.response!!.filter {
        if(terminal.isEmpty()) {
            it.status != "cancelled" &&
                    it.dep_time_ts!! > (System.currentTimeMillis() / 1000)
        } else {
            (it.arr_terminal == terminal || it.dep_terminal == terminal) &&
                    it.status != "cancelled" &&
                    it.dep_time_ts!! > (System.currentTimeMillis() / 1000)
        }
    }
    System.out.printf("| %-10s | %-7s | %-9s | %-8s | %-3s | %-3s | %-8s | %-8s | %-7s | %-7s |\n", "Flight Num", "Airline", "Status", "Aircraft", "Dep", "Arr", "Dep Time", "Arr Time", "Dep Est", "Arr Est")
    flightsArray.map {
        System.out.printf("| %-10s | %-7s | %-9s | %-8s | %-3s | %-3s | %-8s | %-8s | %-7s | %-7s |\n",
            it.flight_icao,
            it.airline_iata,
            it.status,
            it.aircraft_icao ?: "-",
            it.dep_iata,
            it.arr_iata,
            it.dep_time!!.takeLast(5),
            it.arr_time!!.takeLast(5),
            it.dep_estimated?.takeLast(5) ?: "-",
            it.arr_estimated?.takeLast(5) ?: "-")
    }

    if(terminal.isEmpty()) println("\nThe airport $airportICAO has ${flightsArray.size} scheduled arriving/departing flights:\n")
    else println("\nTerminal $terminal has ${flightsArray.size} scheduled arriving/departing flights\n")

    val stringBuilder = StringBuilder("Airport: $airportICAO, which has ${flightsArray.size} flights arriving/departing.\n\n")
    stringBuilder.append("flights={")
    for(flight in flightsArray) {
        stringBuilder.append("<${flight.flight_icao}, ${flight.arr_time_ts}, ${flight.dep_time_ts}>, ")
    }
    stringBuilder.append("}")

    println("Generated pastebin with times: ${pasteBin(stringBuilder.toString())}")
    println("More data available here: https://uk.flightaware.com/live/airport/$airportICAO")

}

private fun apiRequest(airportICAO: String) : HttpResponse<String> {
    val url = "https://airlabs.co/api/v9/schedules?dep_icao=$airportICAO&api_key=${Env.get("FLIGHTS_API_KEY")}"
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
}

private fun pasteBin (textToPaste: String ) : String {
    val url = "https://pastebin.com/api/api_post.php"

    val client = OkHttpClient()

    val requestBody = FormBody.Builder()
        .add("api_dev_key", Env.get("PASTEBIN_API_KEY"))
        .add("api_option", "paste")
        .add("api_paste_code", textToPaste)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()
    val response =  client.newCall(request).execute()
    return response.body!!.string()
}