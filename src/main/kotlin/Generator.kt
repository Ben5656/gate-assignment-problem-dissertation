import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.harium.dotenv.Env
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.time.LocalDateTime

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

    generateData("EGCC", "")
}

private fun generateData(airportICAO: String, terminal: String) {
    val toFlightObj: FlightObj = jacksonObjectMapper().readValue(flightAPICall(airportICAO))

    val flightsArray = toFlightObj.response!!.filter {
        if (terminal.isEmpty()) {
            it.status != "cancelled" && it.arr_time_ts!! > (System.currentTimeMillis() / 1000) && it.arr_icao == airportICAO
        } else {
            (it.arr_terminal == terminal) &&
                    it.status != "cancelled" &&
                    it.arr_time_ts!! > (System.currentTimeMillis() / 1000) && it.arr_icao == airportICAO
        }
    }
    flightsArray.forEach {
        it.turnaround = (30..120).random()
        it.turnaround_ts = it.arr_time_ts!! + (it.turnaround!! * 60)
    }
    System.out.printf(
        "| %-10s | %-7s | %-9s | %-8s | %-8s | %-3s | %-3s | %-8s | %-10s |\n",
        "Flight Num", "Airline", "Status", "Terminal", "Aircraft", "Dep", "Arr", "Arr Time", "Turnaround"
    )
    flightsArray.map {
        System.out.printf(
            "| %-10s | %-7s | %-9s | %-8s | %-8s | %-3s | %-3s | %-8s | %-10s |\n",
            it.flight_icao ?: "-",
            it.airline_iata ?: "-",
            it.status ?: "-",
            it.arr_terminal ?: "-",
            it.aircraft_icao ?: "-",
            it.dep_iata ?: "-",
            it.arr_iata ?: "-",
            it.arr_time?.takeLast(5) ?: "-",
            "${it.turnaround} mins"
        )
    }

    //println("Generated pastebin with times: ${pasteBinAPICall(pasteStringGenerator(airportICAO, flightsArray))}")
    println(pasteStringGenerator(airportICAO, flightsArray))
    println("More data available here: https://uk.flightaware.com/live/airport/$airportICAO")
    generateDATFile(airportICAO, flightsArray)
}

private fun flightAPICall(airportICAO: String): String {
    val request = Request.Builder()
        .url("https://airlabs.co/api/v9/schedules?arr_icao=$airportICAO&api_key=${Env.get("FLIGHTS_API_KEY")}")
        .get()
        .build()

    return OkHttpClient().newCall(request).execute().body!!.string()
}

private fun pasteStringGenerator(airportICAO: String, flightsArray: List<Response>): String {
    var dataString = "Airport: $airportICAO, which has ${flightsArray.size} flights arriving.\n\nflights={"
    flightsArray.forEach {
        dataString += "<${it.flight_icao}, ${it.arr_time_ts}, ${it.turnaround_ts}>, "
    }

    return (dataString.dropLast(2) + "}")
}

private fun generateDATFile(airportICAO: String, incomingArray: List<Response>) {
    val flightsArray = incomingArray.shuffled().take(10)
    val file = File("C:\\Users\\benrf\\opl\\DingModel\\DataRevised.dat")


    var arrivalTimes = "\nARRIVAL_TIMES = ["
    flightsArray.forEach {
        arrivalTimes += "${it.arr_time_ts}, "
    }

    var depTimes = "\nDEPART_TIMES = ["
    flightsArray.forEach {
        depTimes += "${(it.turnaround_ts)}, "
    }

    file.writeText("/*********************************************\n" +
            " * Author: Ben\n" +
            " * Generation Date: ${LocalDateTime.now()}\n" +
            " *********************************************/    \n" +
            "\n" +
            "// Representative of: $airportICAO\n" +
            "\n" +
            "NF = ${flightsArray.size};\n" +
            "NG = 5;\n" +
            "NV = 3;\n" +
            "\n" +
            "// Times given in seconds after midnight\n\n// Time the plane will arrive" +
            "${arrivalTimes.dropLast(2)}];\n\n// Time after turnaround the plane needs to leave." +
            "${depTimes.dropLast(2)}];\n\n// Two vehicles - integers are minutes they need to do a task.\n" +
            "VEHICLES = [1, 1, 1];\n" +
            "\n" +
            "// Distance of gates, assuming that they're all in a line for this.\n" +
            "GATE_DISTANCE = [[0, 5, 10, 15, 20],\n" +
            "\t\t\t\t [5, 0, 5, 10, 15],\n" +
            "\t\t\t\t [10, 5, 0, 5, 10],\n" +
            "\t\t\t\t [15, 10, 5, 0, 5],\n" +
            "\t\t\t\t [20, 15, 10, 5, 0]];")
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