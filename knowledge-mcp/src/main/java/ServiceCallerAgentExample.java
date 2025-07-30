package dev.langchain4j.agentic.example;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgentServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.model.anthropic.AnthropicChatModel;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ServiceCallerAgentExample {

    // 1. Define your service interface as an Agent
    public interface WeatherServiceAgent {
        @SystemMessage("You are a weather service that provides current weather information for cities.")
        @UserMessage("Get the current weather for {{city}}. Use the weather tool to fetch real data.")
        @Agent("A weather service agent that fetches current weather data")
        String getWeather(@V("city") String city);
    }

    // 2. Define a service calling agent that orchestrates multiple service calls
    public interface TravelPlannerAgent {
        @SystemMessage("""
            You are a travel planning assistant that helps users plan their trips.
            Use the available weather and hotel services to provide comprehensive travel advice.
            """)
        @UserMessage("""
            Plan a trip to {{destination}} for {{dates}}.
            Check the weather and find available hotels, then provide recommendations.
            """)
        @Agent("A travel planner that uses weather and hotel services")
        String planTrip(@V("destination") String destination, @V("dates") String dates);
    }

    // 3. Define your backend service as a Tool
    static class WeatherService {
        private final Map<String, String> weatherData = Map.of(
            "New York", "Sunny, 22°C",
            "London", "Cloudy, 15°C",
            "Tokyo", "Rainy, 18°C",
            "Paris", "Partly cloudy, 20°C"
        );

        @Tool("Get current weather information for a city")
        public String getCurrentWeather(@P("city name") String city) {
            return weatherData.getOrDefault(city, "Weather data not available for " + city);
        }
    }

    public static void main(String[] args) {
        ChatModel chatModel = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-3-haiku-20240307")
                .logRequests(true)
                .logResponses(true)
                .build();

        // Create service instances
        WeatherService weatherService = new WeatherService();

        // Build individual service agents
        WeatherServiceAgent weatherAgent = AgentServices.agentBuilder(WeatherServiceAgent.class)
                .chatModel(chatModel)
                .tools(weatherService)
                .build();

        // Build a supervisor agent that can call multiple services
        SupervisorAgent travelPlanner = AgentServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(weatherAgent)  // Add the weather service agent
                .build();

        // Example usage - the agent will:
        // 1. Call weather service to get weather info
        // 2. Call hotel service to find accommodations
        // 3. Combine the information to provide travel advice
        String travelPlan = travelPlanner.invoke(
            "Plan a 3-day trip to Tokyo in December. I need weather info and hotel recommendations."
        );

        System.out.println("Travel Plan:");
        System.out.println(travelPlan);

        // Example of direct service agent call
        String weather = weatherAgent.getWeather("Paris");
        System.out.println("\nWeather in Paris: " + weather);
    }
}
