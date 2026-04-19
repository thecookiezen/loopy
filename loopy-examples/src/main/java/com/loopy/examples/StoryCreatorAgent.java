package com.loopy.examples;

import com.loopy.core.action.ActionResult;
import com.loopy.core.agent.LifecycleListener;
import com.loopy.core.mailbox.ImmutableMailbox;
import com.loopy.core.planning.DefaultBeliefDeriver;
import com.loopy.core.planning.GoapPlanner;
import com.loopy.core.tool.ToolRegistry;
import com.loopy.dsl.AgentBuilder;
import com.loopy.core.llm.LlmRequest;
import com.loopy.llm.openai.OpenAiConfig;
import com.loopy.llm.openai.OpenAiLlmClient;
import com.loopy.runtime.AgentExecution;
import com.loopy.runtime.AgentRunner;
import com.loopy.runtime.RunOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Story Creator Agent - drafts a short story and reviews it for quality.
 *
 * This example demonstrates:
 * - GOAP planning with chained actions
 * - LLM-powered actions
 * - Virtual thread execution
 * - Actor model lifecycle logging
 * - Cost tracking
 *
 * Usage: Set OPENAI_API_KEY environment variable and run.
 */
public final class StoryCreatorAgent {

    private static final Logger LOG = LoggerFactory.getLogger(StoryCreatorAgent.class);

    public record StoryPrompt(String genre, String theme) {
    }

    public record StoryDraft(String genre, String theme, String draft) {
    }

    public record ReviewedStory(String genre, String theme, String story, String reviewNotes) {
    }

    public static void main(String[] args) {
        var llmClient = new OpenAiLlmClient(OpenAiConfig.fromEnvironment());

        var agent = AgentBuilder.named("story-creator")
                .describedAs("Creates a short story and reviews it for quality")
                .action("draft-story")
                    .describedAs("Writes a first draft of a short story from a prompt")
                    .input(StoryPrompt.class)
                    .output(StoryDraft.class)
                    .cost(0.5)
                    .executor(ctx -> {
                        var prompt = ctx.input(StoryPrompt.class);

                        var request = LlmRequest.simple("gpt-4o-mini",
                                "Write a short story (3-4 paragraphs) in the " + prompt.genre()
                                        + " genre exploring the theme of \"" + prompt.theme()
                                        + "\". Focus on vivid imagery and compelling characters.");
                        var response = llmClient.chat(request);
                        var draft = new StoryDraft(prompt.genre(), prompt.theme(), response.message().content());
                        return ActionResult.Success.of(draft, response.usage());
                    })
                .add()
                .action("review-story")
                    .describedAs("Reviews and polishes a story draft")
                    .input(StoryDraft.class)
                    .output(ReviewedStory.class)
                    .cost(0.3)
                    .executor(ctx -> {
                        var draft = ctx.input(StoryDraft.class);

                        var request = LlmRequest.simple("gpt-4o-mini",
                                "You are a story editor. Review this " + draft.genre()
                                        + " short story and provide a polished version along with your review notes.\n\n"
                                        + "Theme: " + draft.theme() + "\n\nDraft:\n" + draft.draft()
                                        + "\n\nRespond with the improved story followed by a section called 'Review Notes' "
                                        + "explaining your changes.");
                        var response = llmClient.chat(request);
                        var content = response.message().content();
                        String story = content;
                        String reviewNotes = "";
                        int marker = content.indexOf("Review Notes");
                        if (marker >= 0) {
                            story = content.substring(0, marker).trim();
                            reviewNotes = content.substring(marker).trim();
                        }
                        var reviewed = new ReviewedStory(draft.genre(), draft.theme(), story, reviewNotes);
                        return ActionResult.Success.of(reviewed, response.usage());
                    })
                .add()
                .goal("story-reviewed")
                    .describedAs("A reviewed and polished story has been produced")
                    .satisfiedBy(ReviewedStory.class)
                .add()
                .build();

        var goal = agent.goals().iterator().next();
        var initialState = ImmutableMailbox.empty()
                .post(new StoryPrompt("science fiction", "first contact with an alien civilization"));

        var options = new RunOptions(
                10, 5, java.time.Duration.ofMinutes(2),
                List.of(),
                agent.defaultSupervision(),
                List.of(LifecycleListener.logging()));

        LOG.info("Starting Story Creator Agent...");
        var runner = new AgentRunner(List.of(LifecycleListener.logging()));
        var execution = runner.run(
                agent, goal, initialState, llmClient,
                ToolRegistry.empty(), new GoapPlanner(), new DefaultBeliefDeriver(), options);

        switch (execution) {
            case AgentExecution.Completed completed -> {
                LOG.info("Agent completed!");
                if (completed.result() instanceof ReviewedStory result) {
                    System.out.println("\n=== " + result.genre().toUpperCase() + " STORY ===");
                    System.out.println("Theme: " + result.theme());
                    System.out.println("\n" + result.story());
                    System.out.println("\n--- Review Notes ---");
                    System.out.println(result.reviewNotes());
                }
                System.out.println(completed.trace().costSummary());
            }
            case AgentExecution.Stuck stuck ->
                LOG.warn("Agent got stuck: {}", stuck.reason());
            case AgentExecution.Failed failed ->
                LOG.error("Agent failed", failed.cause());
        }
    }
}
