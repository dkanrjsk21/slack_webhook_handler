import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Solution02 {
    public static void main(String[] args) {
        String prompt = System.getenv("LLM_PROMPT");

//        String prompt = "서양 용, 동양 용의 차이";
        String response = useLLM(prompt);

        int messageStart = response.indexOf("\"message\":{");
        int contentStart = response.indexOf("\"content\":\"", messageStart) + 11;
        int contentEnd = response.indexOf("\"},\"logprobs\"");
        String content = response.substring(contentStart, contentEnd).replace("\\\"", "\"");

        String responseImageResult = useLLMForImage(prompt);

        String result = responseImageResult
                .split("\"url\": \"")[1]
                .split("\",")[0];

        System.out.println("result = " + result);
        SendSlackMessage(prompt, content, result);
    }

    public static void SendSlackMessage(String prompt,String text, String imageUrl){
        String slackUrl = System.getenv("SLACK_WEBHOOK_URL");

        ///  slack webhook attachments

        HttpClient client = HttpClient.newHttpClient(); /// 새로운 클라이언트, like 브라우저, 유저
        String payload = """
                    {"attachments": [{
                        "title": "%s",
                        "text": "%s",
                        "image_url": "%s"
                    }]}
                """.formatted(prompt, text, imageUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(slackUrl))              /// URI 임 url 아님
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();                               /// 요청

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static String useLLM(String prompt){
        String apiUrl = System.getenv("LLM_API_URL");
        String apiKey = System.getenv("LLM_API_KEY");
        String model = System.getenv("LLM_MODEL");

        /// https://api.groq.com/openai/v1/chat/completions
        /// models llama-3.3-70b-versatile

        HttpClient client = HttpClient.newHttpClient(); /// 새로운 클라이언트, like 브라우저, 유저
        String payload = """
                {
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "model": "%s"
                }
                """.formatted(prompt, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))              /// URI 임 url 아님
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();                               /// 요청

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
            System.out.println("response = " + response);
            return response.body();
        }catch (Exception e){
            throw new RuntimeException(e);
        }


    }public static String useLLMForImage(String prompt){
        String apiUrl = System.getenv("LLM2_API_URL");
        String apiKey = System.getenv("LLM2_API_KEY");
        String model = System.getenv("LLM2_MODEL");

        /// https://api.together.xyz/v1/images/generations
        /// model Flux.1 [schnell] (free)

        HttpClient client = HttpClient.newHttpClient(); /// 새로운 클라이언트, like 브라우저, 유저
        String payload = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "width": 1024,
                  "height": 1024,
                  "steps": 1,
                  "n": 1
                }
                """.formatted(model, prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))              /// URI 임 url 아님
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();                               /// 요청

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());
            return response.body();
        }catch (Exception e){
            throw new RuntimeException(e);
        }


    }
}
