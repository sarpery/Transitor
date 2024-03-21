package Transitor.Backend;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;

public class RequestAPI {

    static String loc = "https://computerscience.dacs.unimaas.nl/get_coordinates";

    public static double[] request(String postcode) throws IOException, InterruptedException {
        // Input JSON for API
        String input = "{\"postcode\": \"" + postcode + "\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(loc))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(input))
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(req, BodyHandlers.ofString());




        // Parsing latitude and longitude from the JSON response
        double latitude = 0.0;
        double longitude = 0.0;
        String responseBody = response.body();

        String[] parts = responseBody.split(":");

        // specifying an error latitude = 1 then 404 not found, latitude = 2 then 500 Internal Server Error, latitude= 3 then 429 Too Many Requests
        if (parts[0].contains("error")) {

            if (parts[1].contains("Postcode not found")){
                return new double[]{1.0,0.0};
            }
            else if(parts[1].contains("Failed to fetch coordinates")){
                return new double[]{2.0,0.0};
            }
            else if (parts[1].contains( "Rate limit exceeded. Try again later.")){
                return new double[]{3.0,0.0};
            }
        }

        // after we check for errors we split the string and extract the longitude and latitude
            String[] parts2 = responseBody.split(",");

                if (parts2[0].contains("latitude")) {
                    String parti  = parts2[0].split(":")[1].trim();

                    latitude = Double.parseDouble(parti.substring(1,parti.length()-1));

                }
                if (parts2[1].contains("longitude")) {
                    String partj  = parts2[1].split(":")[1].trim();

                    latitude = Double.parseDouble(partj.substring(1,partj.length()-1));
                }



        return new double[]{latitude, longitude};
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String code = "6226NB";
        double[] coordinates = request(code);
        double latitude = coordinates[0];
        double longitude = coordinates[1];

        // Outputting latitude and longitude
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);
    }
}



