package ml.crystalize.jarvis.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Serkan Sahin
 */
public class LeagueMMRCmd extends GameCommand {

    private static final String REGIONS = "EUNE|EUW|NA";
    private final List<String> queues = Arrays.asList("ranked", "normal", "ARAM");

    public LeagueMMRCmd() {
        this.name = "leaguemmr";
        this.aliases = new String[]{"lmmr"};
        this.help = "gets League of Legends MMR for a summoner.";
        this.guildOnly = false;
        this.arguments = String.format("<%s> <Summoner>", REGIONS);
    }

    @Override
    protected void execute(CommandEvent event) {
        // Get command arguments from message
        String args = event.getArgs();
        // Keep message channel for reference later on
        MessageChannel channel = event.getChannel();

        // If input command does not match what we expect (using regex)
        if (!args.matches(String.format("(?i)(%s) .+", REGIONS))) {
            // Return error message and proper command usage
            channel.sendMessage("You need to supply a region and a summoner name, like `EUW Name`.").queue();
            return;
        }

        // Split the command to region and summoner name
        String[] params = args.split(" ", 2);
        // Get summoner name from second portion of params
        String summoner = params[1];
        try {
            // Try to encode summoner name into UTF-8 for sending to API
            summoner = URLEncoder.encode(summoner, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Be angry at their input if impossible :)
            channel.sendMessage(String.format("Could not parse %s as a summoner.", summoner)).queue();
            return;
        }

        String url;

        // Prepare API URL
        url = String.format("https://%s.whatismymmr.com/api/v1/summoner?name=%s", params[0].toLowerCase(), summoner);
        JSONObject summonerJson;
        try {
            // Get JSON from API endpoint
            summonerJson = Common.readJsonFromUrl(url);
        } catch (IOException | JSONException e) {
            // Print error details to channel
            channel.sendMessage(String.format("An error occured when retrieving response from API.\n```%s```", e.getLocalizedMessage())).queue();
            return;
        }

        // Prepare MMR Distribution API URL
        url = String.format("https://%s.whatismymmr.com/api/v1/distribution", params[0].toLowerCase());
        JSONObject distributionJson;
        try {
            // Get JSON from API endpoint
            distributionJson = Common.readJsonFromUrl(url);
        } catch (IOException | JSONException e) {
            // Print error details to channel
            channel.sendMessage(String.format("An error occured when retrieving response from API.\n```%s```", e.getLocalizedMessage())).queue();
            return;
        }

        // Build message with given details
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("League of Legends (MMR)");
        builder.setAuthor(params[1]);
        builder.setColor(Color.BLUE);

        // If the returned JSON is null (expected for bad summoner name)
        if (summonerJson == null) {
            // Be angry again :)
            builder.getDescriptionBuilder().append(":x: Summoner not found :x:");
        } else {
            // Keep track if any queue has rankings
            boolean found = false;
            // Loop through the queues
            for (String queue : queues) {
                // If the queue exists and the avg ranked also has a value
                if (summonerJson.has(queue) && !summonerJson.getJSONObject(queue).isNull("avg")) {
                    found = true;
                    JSONObject qobject = summonerJson.getJSONObject(queue);

                    // Get avg and err, and parse those
                    int avg = qobject.getInt("avg");
                    int err = qobject.getInt("err");
                    List<String> list = new ArrayList<>();
                    list.add(String.format("**Average:** %s", avg));
                    list.add(String.format("**Error:** ±%s", err));

                    String field;
                    // If current loop is for the ranked queue, has history and has at least 1 object in it
                    if (queue.equals("ranked") && qobject.has("historical") && qobject.getJSONArray("historical").length() > 0) {
                        // Get previous ranking from history
                        int previous = qobject.getJSONArray("historical").getJSONObject(0).getInt("avg");
                        // Decide if upwards or downwards trend
                        field = String.format("**Trend:** %s", (avg > previous ? ":chart_with_upwards_trend:" : ":chart_with_downwards_trend:"));
                        list.add(field);
                    } // Else if the queue is ranked (but doesn't have history
                    else if (queue.equals("ranked")) {
                        // Show no trend
                        field = "**Trend:** N/A";
                        list.add(field);
                    } // Else keep blank

                    // Calculate top X% from distribution
                    JSONObject queueDistribution = distributionJson.getJSONObject(queue);
                    Map<String, Object> map = queueDistribution.toMap();
                    int barrier = 0;
                    int total = 0;
                    for (Entry<String, Object> entry : map.entrySet()) {
                        int count = (int) entry.getValue();
                        if (Integer.parseInt(entry.getKey()) < avg) {
                            barrier += count;
                        }
                        total += (int) count;
                    }
                    double percentage = (total - barrier) * 100L / (double) total;
                    list.add(String.format("**Top:** %s%%", Math.round(percentage)));
                    //list.add(String.format("%s %s %s", barrier, total, s * 100.0));

                    // Create field with header + body
                    String header = (queue.substring(0, 1).toUpperCase() + queue.substring(1)) + (qobject.getBoolean("warn") ? " :warning:" : "");
                    String fields = String.join("\n", list);
                    builder.addField(header, fields, true);
                }
            }

            // If no queue data was found
            if (!found) {
                // Give 'em sad news :(
                builder.getDescriptionBuilder().append("No data :x:");
            }
        }

        // Send the goodness back
        channel.sendMessage(builder.build()).queue();
        event.getMessage().delete().queue();
    }

}
