package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
//
public class Main {
    public static void main(String[] args) {
        List<String> needAddresses = Arrays.asList(
                "0x4b15a9c28034dc83db40cd810001427d3bd7163d",
                "0xb6a37b5d14d502c3ab0ae6f3a0e058bc9517786e",
                "0x6339e5e072086621540d0362c4e3cea0d643e114"
        );

        for (String needAddress : needAddresses) {
            needAddress = needAddress.toLowerCase();
            Path directoryPath = Paths.get(needAddress);

            try {
                Files.createDirectories(directoryPath);

                ExecutorService executor = Executors.newFixedThreadPool(32);

                BufferedReader reader = new BufferedReader(new FileReader("/Users/hirchytsdaniil/Developer/IdeaProjects/untitled1s/src/main/java/org/example/no_local.txt"));
                String line;
                while ((line = reader.readLine()) != null) {
                    executor.execute(new HandleLineTask(line, needAddress));
                }
                reader.close();

                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.HOURS);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class HandleLineTask implements Runnable {
        private final String line;
        private final String needAddress;

        HandleLineTask(String line, String needAddress) {
            this.line = line;
            this.needAddress = needAddress;
        }

        @Override
        public void run() {
            String[] parts = line.split(":");
            String address = parts[0].toLowerCase();
            String index = parts[1];

            if (!address.equals(needAddress)) {
                return;
            }

            Path filePath = Paths.get(needAddress, index + ".json");
            if (Files.exists(filePath)) {
                return;
            }

            String url = "https://metadata.degods.com/g/" + index + ".json";
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpGet);

                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseText = EntityUtils.toString(response.getEntity());
                    try (FileWriter file = new FileWriter(filePath.toString())) {
                        file.write(responseText);
                    }
                    System.out.println(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public static void main(String[] args) {
//            Set<String> uniqueAddresses = new HashSet<>();
//
//            try (BufferedReader reader = new BufferedReader(new FileReader("missing_traits.txt"))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] parts = line.split(":");
//                    String address = parts[0].toLowerCase(); // Assuming you want to treat different letter cases as the same address
//                    uniqueAddresses.add(address);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            for (String address : uniqueAddresses) {
//                System.out.println(address);
//            }
//        }
    }
}
