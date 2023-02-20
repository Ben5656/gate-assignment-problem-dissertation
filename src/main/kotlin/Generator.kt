import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.harium.dotenv.Env
fun main(args: Array<String>) {
    generateData("EGLL", "5")
}

private fun generateData(airportICAO: String, terminal: String) {
    val toFlightObj: FlightObj = jacksonObjectMapper().readValue(apiRequest(airportICAO).body())

    val flightsArray = toFlightObj.response!!.filter {
        (it.arr_terminal == terminal || it.dep_terminal == terminal) && it.status != "cancelled"
    }

    println("\nTerminal $terminal has ${flightsArray.size} scheduled arriving/departing flights:\n")
    System.out.printf("%-10s | %-7s | %-9s | %-8s | %-3s | %-3s | %-8s | %-8s | %-16s | %-16s\n", "Flight Num", "Airline", "Status", "Aircraft", "Dep", "Arr", "Dep Time", "Arr Time", "Dep Est", "Arr Est")
    flightsArray.map {
        System.out.printf("%-10s | %-7s | %-9s | %-8s | %-3s | %-3s | %-8s | %-8s | %-16s | %-16s\n", it.flight_icao, it.airline_iata, it.status, it.aircraft_icao, it.dep_iata, it.arr_iata, it.dep_time!!.takeLast(5), it.arr_time!!.takeLast(5), it.dep_estimated, it.arr_estimated)
    }
}

private fun apiRequest(airportICAO: String) : HttpResponse<String> {
    val url = "https://airlabs.co/api/v9/schedules?dep_icao=$airportICAO&api_key=${Env.get("API_KEY")}"
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()
    return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
}